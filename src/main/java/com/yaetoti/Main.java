package com.yaetoti;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Questions
// Is

class SymbolTable {
  private final Map<String, ITAC.Type> types = new HashMap<>();
  private final Map<String, Integer> offsets = new HashMap<>();
  private int currentOffset = 0;

  public int AddVariable(String name, ITAC.Type type) {
    types.put(name, type);
    // Align stack to the size of the type, minimum 4 bytes
    int allocationSize = Math.max(4, type.size());
    currentOffset += allocationSize;
    offsets.put(name, currentOffset);
    return currentOffset;
  }

  public ITAC.Type GetType(String name) {
    return Objects.requireNonNull(types.get(name), "Variable not defined: " + name);
  }

  public int GetOffset(String name) {
    return Objects.requireNonNull(offsets.get(name), "Variable not defined: " + name);
  }

  public int GetTotalAllocationSize() {
    return currentOffset;
  }
}

class LabelGenerator {
  int nextLabelIndex = 0;
  public String GenLabel() {
    return "L" + (nextLabelIndex++);
  }
}

class Program {
  record FunctionEntry(String name, FunctionFrame frame) {}

  private final List<FunctionFrame> m_frames;
  // TODO extern declarations
  //private final List<FunctionDeclaration> m_declarations;

  public Program(List<FunctionFrame> frames) {
    m_frames = frames;
  }

//  public List<FunctionDeclaration> GetDeclarations() {
//    return m_declarations;
//  }

  public List<FunctionFrame> GetFunctions() {
    return m_frames;
  }
}

class FunctionFrame {
  private final FunctionDeclaration m_declaration;
  private final List<ITAC.Instruction> m_instructions;

  public FunctionFrame(FunctionDeclaration declaration, List<ITAC.Instruction> instructions) {
    m_declaration = declaration;
    m_instructions = instructions;
  }

  public List<ITAC.Instruction> GetInstructions() {
    return m_instructions;
  }

  public FunctionDeclaration GetDeclaration() {
    return m_declaration;
  }
}

class Scope {

}


record Parameter(String name, ITAC.Type type) {}
record FunctionDeclaration(String name, Parameter[] parameters, ITAC.Type[] returnTypes) {}

// Set parameters
// +Get parameters
// +Set return value
// Get return value

// TODO add instruction call
// TODO add instruction multiple assignment
// For that we need to store all elements that are in stack into values. Or leave it as is, but append offsets. It is if we won't use stack for other purposes

// TODO handle function call (stackcall)
// TODO pass parameters
// TODO clean stack after call
// TODO multiple assignment

// We get call("name", "a, b") - name, parameters' name
// We get declaration for name: FuncDecl("hash", "u64 a, u64 b", "u8")
// IF "stackcall" convention
// Push parameters to stack one by one
// Add call "name"

// +Inside function (from the beginning)
// We get our function declaration by name: FuncDecl("hash", "u64 a, u64 b", "u8")
// We get our parameters (If reference - we can modify it. If can modify - need to copy. How to add offset to stack manager?)
// IF "stackcall"
// Add variables with parameter names, copy from these cells one by one

// Assignment:
// We get call() ... how do we assign call result to variables? We need to also save variables into call


// Stack parameters:
// param3
// param2
// param2
// return value
// rbp
// locals

// TODO problem. We can't write eax to stack, because there may be trash. Either clean it either

public class Main {
  // Define type constants for convenience
  public static final ITAC.Type i8 = new ITAC.Type("i16", 1, true);
  public static final ITAC.Type i16 = new ITAC.Type("i16", 2, true);
  public static final ITAC.Type i32 = new ITAC.Type("i32", 4, true);
  public static final ITAC.Type i64 = new ITAC.Type("i64", 8, true);
  public static final ITAC.Type u8 = new ITAC.Type("u8", 1, false);
  public static final ITAC.Type u16 = new ITAC.Type("u16", 2, false);
  public static final ITAC.Type u32 = new ITAC.Type("u32", 4, false);
  public static final ITAC.Type u64 = new ITAC.Type("u64", 8, false);

  public static void main(String[] args) {
    // High-level goal:
    // s32 a = 5;
    // s32 b;
    // if (a > 10) { b = 100; } else { b = 200; }
    // The final value of b should be 200.

    MasmGenerator generator = new MasmGenerator();

    // Create variables
    var a = new ITAC.Variable("a");
    var b = new ITAC.Variable("b");
    var number2 = new ITAC.Variable("number2");

    // Create labels TODO separate labels for different functions
    LabelGenerator mainGen = new LabelGenerator();
    String elseLabel = mainGen.GenLabel();
    String endIfLabel = mainGen.GenLabel();

    // Create frames
    var hashFrame = new FunctionFrame(
      new FunctionDeclaration("hash", new Parameter[] {
        new Parameter("number1", i64),
        new Parameter("number2", i64)
      }, new ITAC.Type[] { i64 }),
      List.of(
        new ITAC.Assignment(a, new ITAC.Constant("5", i32)),
        //new ITAC.Return(new ITAC.Constant("0", i64))
        new ITAC.Return(number2)
      )
    );

    var mainFrame = new FunctionFrame(
      new FunctionDeclaration("main", new Parameter[] {}, new ITAC.Type[] {}),
      List.of(
        new ITAC.Assignment(a, new ITAC.Constant("5", i32)),
        new ITAC.ConditionalJump(a, ITAC.ComparisonOp.LE, new ITAC.Constant("10", i32), elseLabel),
        new ITAC.Assignment(b, new ITAC.Constant("100", i32)),
        new ITAC.Jump(endIfLabel),
        new ITAC.Label(elseLabel),
        new ITAC.Assignment(b, new ITAC.Constant("200", i32)),
        new ITAC.Label(endIfLabel),
        new ITAC.Return(new ITAC.Constant("0", i64), new ITAC.Constant("200", i64), b)
      )
    );

    // Create program
    var program = new Program(List.of(
      hashFrame,
      mainFrame
    ));

    // Generate code
    String masmCode = generator.Generate(program);
    System.out.println(masmCode);


/*
    // Generate unique labels for the control flow
    String elseLabel = generator.GenLabel(); // e.g., "L0"
    String endIfLabel = generator.GenLabel(); // e.g., "L1"

    var program = new ITAC.Program(List.of(
      // s32 a = 5;
      new ITAC.Assignment(a, new ITAC.Constant("5", i32)),

      // This is the IF statement
      // if (a <= 10) goto elseLabel;
      new ITAC.ConditionalJump(a, ITAC.ComparisonOp.LE, new ITAC.Constant("10", i32), elseLabel),

      // THEN block
      new ITAC.Assignment(b, new ITAC.Constant("100", i32)),
      new ITAC.Jump(endIfLabel),

      // ELSE block
      new ITAC.Label(elseLabel),
      new ITAC.Assignment(b, new ITAC.Constant("200", i32)),

      // End of IF
      new ITAC.Label(endIfLabel),

      // Return
      new ITAC.Return(new ITAC.Constant("0", i64), new ITAC.Constant("200", i64), b)
    ));

    String masmCode = generator.generate(program);
    System.out.println(masmCode);
*/
  }
}
