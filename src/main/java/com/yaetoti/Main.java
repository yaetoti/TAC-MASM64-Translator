package com.yaetoti;

import java.util.List;

// High level
class LabelGenerator {
  int nextLabelIndex = 0;
  public String GenLabel() {
    return "L" + (nextLabelIndex++);
  }
}

public class Main {
  public static void main(String[] args) {
    /*
    extern void ms_abi ExitProcess(u32 uExitCode);

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

    // Create a symbol table

    GlobalSymbolTable gst = new GlobalSymbolTable();
    // external symbols
    var sExternalExitCode = gst.AddSymbol("uExitCode", DataType.u32);
    var sExternalStdHandle = gst.AddSymbol("nStdHandle", DataType.i32);
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

    // Add external functions
    // TODO why are parameters symbols? Because they are used inside function
    unit.AddFunctionImports(List.of(
      new FunctionDeclaration(
        "ExitProcess",
        CallingConvention.MS_ABI,
        new GlobalSymbolTable.Symbol[] { sExternalExitCode },
        new DataType[] {}
      )
    ));

    unit.AddFunctionImports(List.of(
      new FunctionDeclaration(
        "GetStdHandle",
        CallingConvention.MS_ABI,
        new GlobalSymbolTable.Symbol[] { sExternalStdHandle },
        new DataType[] { DataType.u64 }
      )
    ));

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
        new TAC.Call(
          "GetStdHandle",
          new TAC.Operand[] { new TAC.Constant("-11", DataType.i32) },
          new TAC.Symbol[] { null }
        ),

        new TAC.Call(
          "ExitProcess",
          new TAC.Operand[] { new TAC.Constant("69", DataType.u32) },
          new TAC.Symbol[] { null }
        ),

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
        new TAC.Call(
          "hash",
          new TAC.Operand[] {
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
