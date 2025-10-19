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

  private final List<FunctionEntry> m_frames;

  public Program(List<FunctionEntry> frames) {
    m_frames = frames;
  }

  public List<FunctionEntry> GetFunctions() {
    return m_frames;
  }
}

class FunctionFrame {
  private final List<ITAC.Instruction> m_instructions;

  public FunctionFrame(List<ITAC.Instruction> instructions) {
    m_instructions = instructions;
  }

  public List<ITAC.Instruction> GetInstructions() {
    return m_instructions;
  }
}

class Scope {

}

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

    var a = new ITAC.Variable("a");
    var b = new ITAC.Variable("b");

    LabelGenerator mainGen = new LabelGenerator();
    String elseLabel = mainGen.GenLabel();
    String endIfLabel = mainGen.GenLabel();

    var hashFrame = new FunctionFrame(List.of(
      new ITAC.Return(new ITAC.Constant("0", i64))
    ));

    var mainFrame = new FunctionFrame(List.of(
      new ITAC.Assignment(a, new ITAC.Constant("5", i32)),
      new ITAC.ConditionalJump(a, ITAC.ComparisonOp.LE, new ITAC.Constant("10", i32), elseLabel),
      new ITAC.Assignment(b, new ITAC.Constant("100", i32)),
      new ITAC.Jump(endIfLabel),
      new ITAC.Label(elseLabel),
      new ITAC.Assignment(b, new ITAC.Constant("200", i32)),
      new ITAC.Label(endIfLabel),
      new ITAC.Return(new ITAC.Constant("0", i64), new ITAC.Constant("200", i64), b)
    ));

    var program = new Program(List.of(
      new Program.FunctionEntry("hash", hashFrame),
      new Program.FunctionEntry("main", mainFrame)
    ));

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
