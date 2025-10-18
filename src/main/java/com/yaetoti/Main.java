package com.yaetoti;

import java.util.List;

public class Main {

  // Define type constants for convenience
  public static final TACI.Type s32 = new TACI.Type("s32", 4, true);
  public static final TACI.Type s64 = new TACI.Type("s64", 8, true);
  public static final TACI.Type u32 = new TACI.Type("u32", 4, false);

  public static void main(String[] args) {
    // Let's model the TAC for an expression like:
    // s32 a = -100;
    // s32 b = a * 2;
    // s64 c = (s64)b + 500; // Type promotion
    // s64 d = c / a;
    // s64 r = c % a;

    var a = new TACI.Variable("a");
    var b = new TACI.Variable("b");
    var c = new TACI.Variable("c");
    var d = new TACI.Variable("d");
    var r = new TACI.Variable("r");

    var program = new TACI.Program(List.of(
      new TACI.Assignment(a, new TACI.Constant("-100", s32)),
      new TACI.BinaryOperation(b, a, TACI.Op.MUL, new TACI.Constant("2", s32)),
      new TACI.BinaryOperation(c, b, TACI.Op.ADD, new TACI.Constant("500", s64)), // Promotes b to s64
      new TACI.BinaryOperation(d, c, TACI.Op.DIV, a), // c is s64, a is s32
      new TACI.BinaryOperation(r, c, TACI.Op.MOD, a) // c is s64, a is s32
    ));

    MasmGenerator generator = new MasmGenerator();
    String masmCode = generator.generate(program);

    System.out.println(masmCode);
  }
}
