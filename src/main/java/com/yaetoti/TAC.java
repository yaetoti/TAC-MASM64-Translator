package com.yaetoti;
import java.util.List;

/**
 * A simple set of records to define a Three-Address Code (TAC) Intermediate Representation.
 */
public interface TAC {
  TAC.Type i8 = new TAC.Type("i16", 1, true);
  TAC.Type i16 = new TAC.Type("i16", 2, true);
  TAC.Type i32 = new TAC.Type("i32", 4, true);
  TAC.Type i64 = new TAC.Type("i64", 8, true);
  TAC.Type u8 = new TAC.Type("u8", 1, false);
  TAC.Type u16 = new TAC.Type("u16", 2, false);
  TAC.Type u32 = new TAC.Type("u32", 4, false);
  TAC.Type u64 = new TAC.Type("u64", 8, false);

  // Represents a data type (e.g., s32, u64)
  record Type(String name, int size, boolean isSigned) {}

  // Represents an operand, which can be a variable name or a constant value
  sealed interface Operand {}
  record Constant(String value, Type type) implements Operand {}
  record Register(Type type, String name, int size) implements Operand {
    public enum Type {
      RAX,
      RCX,
      RDX
    }

    public static final Register RAX = new Register(Type.RAX, "rax", 8);
    public static final Register EAX = new Register(Type.RAX, "eax", 4);
    public static final Register AX = new Register(Type.RAX, "ax", 2);
    public static final Register AL = new Register(Type.RAX, "al", 1);

    public static final Register RCX = new Register(Type.RCX, "rcx", 8);
    public static final Register ECX = new Register(Type.RCX, "ecx", 4);
    public static final Register CX = new Register(Type.RCX, "cx", 2);
    public static final Register CL = new Register(Type.RCX, "cl", 1);

    public static final Register RDX = new Register(Type.RDX, "rdx", 8);
    public static final Register EDX = new Register(Type.RDX, "edx", 4);
    public static final Register DX = new Register(Type.RDX, "dx", 2);
    public static final Register DL = new Register(Type.RDX, "dl", 1);

    public Register GetRegister(Type type, int size) {
      return switch (type) {
      case RAX -> switch (size) { case 8 -> RAX; case 4 -> EAX; case 2 -> AX; case 1 -> AL; default -> throw new IllegalArgumentException("Invalid register size: " + size); };
      case RCX -> switch (size) { case 8 -> RCX; case 4 -> ECX; case 2 -> CX; case 1 -> CL; default -> throw new IllegalArgumentException("Invalid register size: " + size); };
      case RDX -> switch (size) { case 8 -> RDX; case 4 -> EDX; case 2 -> DX; case 1 -> DL; default -> throw new IllegalArgumentException("Invalid register size: " + size); };
      };
    }
  }
  record Variable(String name) implements Operand {}

  // Represents a single TAC instruction
  sealed interface Instruction {}
  // Represents 'result = source'
  record Assignment(Variable result, Operand source) implements Instruction {}
  // Represents 'result = arg1 op arg2'
  record BinaryOperation(Variable result, Operand arg1, Op op, Operand arg2) implements Instruction {}
  // NEW INSTRUCTIONS FOR CONTROL FLOW
  record Label(String name) implements Instruction {}
  record Jump(String targetLabel) implements Instruction {}
  record ConditionalJump(Operand arg1, ComparisonOp op, Operand arg2, String targetLabel) implements Instruction {}
  // Even worse, my instructions o:
  record Return(Operand... operands) implements Instruction {}
  /// returnVariables may contain nulls for '_'
  record Call(String name, Operand[] params, Variable[] returnVariables) implements Instruction {}


  // Enum for the arithmetic operations
  enum Op {
    ADD("+"), SUB("-"), MUL("*"), DIV("/"), MOD("%");
    public final String symbol;
    Op(String symbol) { this.symbol = symbol; }
  }

  // NEW ENUM FOR COMPARISON OPERATIONS
  enum ComparisonOp {
    EQ,  // Equal (JE)
    NE,  // Not Equal (JNE)
    LT,  // Less Than (JL)
    LE,  // Less or Equal (JLE)
    GT,  // Greater Than (JG)
    GE;  // Greater or Equal (JGE)
  }

  // A simple container for a list of instructions
  record Program(List<Instruction> instructions) {}
}
