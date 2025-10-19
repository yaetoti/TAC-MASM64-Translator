package com.yaetoti;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Questions
// Is

class Program {
  private final List<FunctionFrame> m_frames;

  public Program(List<FunctionFrame> frames) {
    m_frames = frames;
  }
}

class FunctionFrame {
  private final List<ITAC.Instruction> m_instructions;
  private int nextLabelIndex = 0;

  public FunctionFrame(List<ITAC.Instruction> instructions) {
    m_instructions = instructions;
  }

  public String GenLabelIndex() {
    return "L" + nextLabelIndex++;
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

    // Generate unique labels for the control flow
    String elseLabel = generator.newLabel(); // e.g., "L0"
    String endIfLabel = generator.newLabel(); // e.g., "L1"

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
  }
}
