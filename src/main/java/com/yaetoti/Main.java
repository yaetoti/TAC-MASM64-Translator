package com.yaetoti;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Questions
// Is

class FunctionFrame {
  private final List<ITAC.Instruction> instructions;
  private final Map<String, ITAC.Type> types = new HashMap<>();
  private final Map<String, Integer> offsets = new HashMap<>();
  private int currentOffset = 0;

  public FunctionFrame(List<ITAC.Instruction> instructions) {
    this.instructions = instructions;
  }

  // Allocate space on the stack for a new variable
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

  public int getTotalAllocationSize() {
    return currentOffset;
  }
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
      new ITAC.Label(endIfLabel)
    ));

    String masmCode = generator.generate(program);
    System.out.println(masmCode);
  }
}
