package com.yaetoti;

import java.util.HashMap;
import java.util.List;

// High level
class LabelGenerator {
  int nextLabelIndex = 0;
  public String GenLabel() {
    return "L" + (nextLabelIndex++);
  }
}

//class Program {
//  private final List<FunctionFrame> m_frames;
//  private final HashMap<String, FunctionFrame> m_framesMapping = new HashMap<>();
//
//  public Program(List<FunctionFrame> frames) {
//    m_frames = frames;
//
//    // Build mapping
//    for (var frame : frames) {
//      m_framesMapping.put(frame.GetDeclaration().name(), frame);
//    }
//  }
//
//  public List<FunctionFrame> GetFunctions() {
//    return m_frames;
//  }
//
//  public FunctionFrame GetFunction(String name) {
//    return m_framesMapping.get(name);
//  }
//}

//class FunctionFrame {
//  private final FunctionDeclaration m_declaration;
//  private final List<TAC.Instruction> m_instructions;
//
//  public FunctionFrame(FunctionDeclaration declaration, List<TAC.Instruction> instructions) {
//    m_declaration = declaration;
//    m_instructions = instructions;
//  }
//
//  public FunctionDeclaration GetDeclaration() {
//    return m_declaration;
//  }
//
//  public List<TAC.Instruction> GetInstructions() {
//    return m_instructions;
//  }
//}

//record FunctionDeclaration(CallingConvention convention, String name, TAC.Symbol[] parameters, DataType[] returnTypes) {}

// TODO Symbol table should be built before TAC
// We should know variable name, type;
// We should know variable lifetime (at least: static, global, function (local, parameter, different scopes));
// Yeah, and there is name shadowing int a = 5; { int a = 10; print(a); /* prints 10 */ }
// So, we have scopes, look up for names in current scope, then the upper one

public class Main {
  public static void main(String[] args) {
    /*
    i64, i64 hash(i64 number1, i64 number2) {
      i32 a = 5;
      return 0, number2;
    }

    i64, i64, i64 main() {
      i32 a = 5;
      i32 b = a <= 10 ? 100 : 200;
      var (_, resultHash) = hash(420, 69);
      return 0, 200, resultHash;
    }
    */

    // TODO create symbol table
    // TODO create program structure

    // Create a symbol table

    GlobalSymbolTable gst = new GlobalSymbolTable();
    // hash() symbols
    var sHashNumber1 = gst.AddSymbol("number1", DataType.i64);
    var sHashNumber2 = gst.AddSymbol("number2", DataType.i64);
    var sHashA = gst.AddSymbol("a", DataType.i32);
    // main() symbols
    var sMainA = gst.AddSymbol("a", DataType.i32);
    var sMainB = gst.AddSymbol("b", DataType.i32);
    var sMainResultHash = gst.AddSymbol("resultHash", DataType.i64);

    // Create labels
    // TODO separate labels for different functions
    LabelGenerator mainGen = new LabelGenerator();
    String ifLabel = mainGen.GenLabel();
    String endIfLabel = mainGen.GenLabel();

    // Create program
    Program program = new Program();
    TranslationUnit unit = new TranslationUnit();
    unit.SetParent(program);
    program.AddTranslationUnit(unit);

    // Declarations
    FunctionDeclaration hashDecl = new FunctionDeclaration(
      "hash",
      CallingConvention.STACKCALL,
      new GlobalSymbolTable.Symbol[] { sHashNumber1, sHashNumber2 },
      new DataType[] { DataType.i64, DataType.i64 }
    );

    FunctionDeclaration mainDecl = new FunctionDeclaration(
      "main",
      CallingConvention.STACKCALL,
      new GlobalSymbolTable.Symbol[] {},
      new DataType[] { DataType.i64, DataType.i64, DataType.i64 }
    );

    // Add functions
    Function hashFunction = new Function();
    hashFunction.SetParent(unit);
    hashFunction.SetDeclaration(hashDecl);
    hashFunction.AddLocalSymbols(List.of(sHashA));
    hashFunction.AddInstructions(
      List.of(
        // i32 a = 5;
        new TAC.Assignment(new TAC.Symbol(sHashA.id()), new TAC.Constant("5", DataType.i32)),
        // return 0, number2;
        new TAC.Return(new TAC.Constant("0", DataType.i64), new TAC.Symbol(sHashNumber2.id()))
      )
    );

    Function mainFunction = new Function();
    mainFunction.SetParent(unit);
    mainFunction.SetDeclaration(mainDecl);
    mainFunction.AddLocalSymbols(List.of(sMainA, sMainB, sMainResultHash));
    mainFunction.AddInstructions(
      List.of(
        // i32 a = 5;
        new TAC.Assignment(new TAC.Symbol(sMainA.id()), new TAC.Constant("5", DataType.i32)),

        // i32 b = a <= 10 ? 100 : 200;
        new TAC.ConditionalJump(new TAC.Symbol(sMainA.id()), TAC.ComparisonOp.LE, new TAC.Constant("10", DataType.i32), ifLabel),
        new TAC.Assignment(new TAC.Symbol(sMainB.id()), new TAC.Constant("100", DataType.i32)),
        new TAC.Jump(endIfLabel),
        new TAC.Label(ifLabel),
        new TAC.Assignment(new TAC.Symbol(sMainB.id()), new TAC.Constant("200", DataType.i32)),
        new TAC.Label(endIfLabel),

        // var (_, resultHash) = hash(420, 69);
        new TAC.Call("hash", new TAC.Operand[] {
          new TAC.Constant("420", DataType.i64),
          new TAC.Constant("69", DataType.i64)
        },
          new TAC.Symbol[] { null, new TAC.Symbol(sMainResultHash.id()) }
        ),

        // return 0, 200, resultHash;
        new TAC.Return(new TAC.Constant("0", DataType.i64), new TAC.Constant("200", DataType.i64), new TAC.Symbol(sMainResultHash.id()))
      )
    );

    // Add functions
    unit.AddFunctions(List.of(hashFunction, mainFunction));

    // Compile
    MasmGenerator generator = new MasmGenerator();
    String masmCode = generator.Generate(program, gst);
    System.out.println(masmCode);
  }
}
