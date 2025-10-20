package com.yaetoti;
import java.util.List;

/**
 * A simple set of records to define a Three-Address Code (TAC) Intermediate Representation.
 */
public interface TAC {
  enum Size {
    BYTE(1),
    WORD(2),
    DWORD(4),
    QWORD(8);

    public final int size;

    Size(int size) {
      this.size = size;
    }
  }

  record Type(Size size, boolean isSigned) {
    public static final Type i8 = new Type(Size.BYTE, true);
    public static final Type i16 = new Type(Size.WORD, true);
    public static final Type i32 = new Type(Size.DWORD, true);
    public static final Type i64 = new Type(Size.QWORD, true);
    public static final Type u8 = new Type(Size.BYTE, false);
    public static final Type u16 = new Type(Size.WORD, false);
    public static final Type u32 = new Type(Size.DWORD, false);
    public static final Type u64 = new Type(Size.QWORD, false);
  }

  // Represents an operand, which can be a variable name or a constant value
  sealed interface Operand {}
  record Constant(String value, Type type) implements Operand {}
  record Register(Type type, String name, Size size) implements Operand {
    public enum Type {
      RAX,
      RBX,
      RCX,
      RDX,
      RSP,
      RBP,
    }

    public static final Register RAX = new Register(Type.RAX, "rax", Size.QWORD);
    public static final Register EAX = new Register(Type.RAX, "eax", Size.DWORD);
    public static final Register AX = new Register(Type.RAX, "ax", Size.WORD);
    public static final Register AL = new Register(Type.RAX, "al", Size.BYTE);

    public static final Register RBX = new Register(Type.RBX, "rbx", Size.QWORD);
    public static final Register EBX = new Register(Type.RBX, "ebx", Size.DWORD);
    public static final Register BX = new Register(Type.RBX, "bx", Size.WORD);
    public static final Register BL = new Register(Type.RBX, "bl", Size.BYTE);

    public static final Register RCX = new Register(Type.RCX, "rcx", Size.QWORD);
    public static final Register ECX = new Register(Type.RCX, "ecx", Size.DWORD);
    public static final Register CX = new Register(Type.RCX, "cx", Size.WORD);
    public static final Register CL = new Register(Type.RCX, "cl", Size.BYTE);

    public static final Register RDX = new Register(Type.RDX, "rdx", Size.QWORD);
    public static final Register EDX = new Register(Type.RDX, "edx", Size.DWORD);
    public static final Register DX = new Register(Type.RDX, "dx", Size.WORD);
    public static final Register DL = new Register(Type.RDX, "dl", Size.BYTE);

    public static final Register RSP = new Register(Type.RSP, "rsp", Size.QWORD);
    public static final Register ESP = new Register(Type.RSP, "esp", Size.DWORD);
    public static final Register SP = new Register(Type.RSP, "sp", Size.WORD);

    public static final Register RBP = new Register(Type.RBP, "rbp", Size.QWORD);
    public static final Register EBP = new Register(Type.RBP, "ebp", Size.DWORD);
    public static final Register BP = new Register(Type.RBP, "bp", Size.WORD);

    public static Register GetRegister(Type type, Size size) {
      return switch (type) {
        case RAX -> GetRegisterFromSize(size, RAX, EAX, AX, AL);
        case RBX -> GetRegisterFromSize(size, RBX, EBX, BX, BL);
        case RCX -> GetRegisterFromSize(size, RCX, ECX, CX, CL);
        case RDX -> GetRegisterFromSize(size, RDX, EDX, DX, DL);
        case RSP -> GetRegisterFromSize(size, RSP, ESP, SP, null);
        case RBP -> GetRegisterFromSize(size, RBP, EBP, BP, null);
      };
    }

    private static Register GetRegisterFromSize(Size size, Register rQword, Register rDword, Register rWord, Register rByte) {
      Register value = switch (size) { case QWORD -> rQword; case DWORD -> rDword; case WORD -> rWord; case BYTE -> rByte; };
      if (value == null) {
        throw new IllegalStateException("Size not supported");
      }

      return value;
    }
  }
  sealed interface Memory extends Operand {}
  record LabelMemory(Size size, String label, int offset) implements Memory {}
  record OffsetMemory(Size size, Register base, Register index, int scale, int offset) implements Memory {}


  // TODO ???
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
