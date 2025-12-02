package com.yaetoti;

public interface MASM {
  enum Type {
    BYTE(1),
    WORD(2),
    DWORD(4),
    QWORD(8);

    public final int size;

    Type(int size) {
      this.size = size;
    }

    public static Type FromSize(int size) {
      return switch (size) {
        case 1 -> BYTE;
        case 2 -> WORD;
        case 4 -> DWORD;
        case 8 -> QWORD;
        default -> throw new IllegalArgumentException("Invalid size");
      };
    }
  }

  sealed interface Operand { }
  record Immediate(String value, Type type) implements Operand { }

  sealed interface Location extends Operand {}
  record Register(Type registerType, String name, MASM.Type type) implements Location {
    public enum Type {
      RAX,
      RBX,
      RCX,
      RDX,
      RSP,
      RBP,
      R8,
      R9
    }

    public static final Register RAX = new Register(Type.RAX, "rax", MASM.Type.QWORD);
    public static final Register EAX = new Register(Type.RAX, "eax", MASM.Type.DWORD);
    public static final Register AX = new Register(Type.RAX, "ax", MASM.Type.WORD);
    public static final Register AL = new Register(Type.RAX, "al", MASM.Type.BYTE);

    public static final Register RBX = new Register(Type.RBX, "rbx", MASM.Type.QWORD);
    public static final Register EBX = new Register(Type.RBX, "ebx", MASM.Type.DWORD);
    public static final Register BX = new Register(Type.RBX, "bx", MASM.Type.WORD);
    public static final Register BL = new Register(Type.RBX, "bl", MASM.Type.BYTE);

    public static final Register RCX = new Register(Type.RCX, "rcx", MASM.Type.QWORD);
    public static final Register ECX = new Register(Type.RCX, "ecx", MASM.Type.DWORD);
    public static final Register CX = new Register(Type.RCX, "cx", MASM.Type.WORD);
    public static final Register CL = new Register(Type.RCX, "cl", MASM.Type.BYTE);

    public static final Register RDX = new Register(Type.RDX, "rdx", MASM.Type.QWORD);
    public static final Register EDX = new Register(Type.RDX, "edx", MASM.Type.DWORD);
    public static final Register DX = new Register(Type.RDX, "dx", MASM.Type.WORD);
    public static final Register DL = new Register(Type.RDX, "dl", MASM.Type.BYTE);

    public static final Register RSP = new Register(Type.RSP, "rsp", MASM.Type.QWORD);
    public static final Register ESP = new Register(Type.RSP, "esp", MASM.Type.DWORD);
    public static final Register SP = new Register(Type.RSP, "sp", MASM.Type.WORD);

    public static final Register RBP = new Register(Type.RBP, "rbp", MASM.Type.QWORD);
    public static final Register EBP = new Register(Type.RBP, "ebp", MASM.Type.DWORD);
    public static final Register BP = new Register(Type.RBP, "bp", MASM.Type.WORD);

    public static final Register R8 = new Register(Type.R8, "r8", MASM.Type.QWORD);
    public static final Register R8D = new Register(Type.R8, "r8d", MASM.Type.DWORD);
    public static final Register R8W = new Register(Type.R8, "r8w", MASM.Type.WORD);
    public static final Register R8B = new Register(Type.R8, "r8b", MASM.Type.BYTE);

    public static final Register R9 = new Register(Type.R9, "r9", MASM.Type.QWORD);
    public static final Register R9D = new Register(Type.R9, "r9d", MASM.Type.DWORD);
    public static final Register R9W = new Register(Type.R9, "r9w", MASM.Type.WORD);
    public static final Register R9B = new Register(Type.R9, "r9b", MASM.Type.BYTE);

    public static Register GetRegister(Type type, MASM.Type size) {
      return switch (type) {
        case RAX -> GetRegisterFromSize(size, RAX, EAX, AX, AL);
        case RBX -> GetRegisterFromSize(size, RBX, EBX, BX, BL);
        case RCX -> GetRegisterFromSize(size, RCX, ECX, CX, CL);
        case RDX -> GetRegisterFromSize(size, RDX, EDX, DX, DL);
        case RSP -> GetRegisterFromSize(size, RSP, ESP, SP, null);
        case RBP -> GetRegisterFromSize(size, RBP, EBP, BP, null);
        case R8 -> GetRegisterFromSize(size, R8, R8D, R8W, R8B);
        case R9 -> GetRegisterFromSize(size, R9, R9D, R9W, R9B);
      };
    }

    private static Register GetRegisterFromSize(MASM.Type size, Register rQword, Register rDword, Register rWord, Register rByte) {
      Register value = switch (size) { case QWORD -> rQword; case DWORD -> rDword; case WORD -> rWord; case BYTE -> rByte; };
      if (value == null) {
        throw new IllegalStateException("Size not supported");
      }

      return value;
    }
  }

  // TODO what about addressing arrays?
  sealed interface Memory extends Location {}
  // rip-relative mapping. only 32 or 8 bit offset
  record LabelMemory(Type size, String label, int offset) implements Memory {}
  record OffsetMemory(Type size, Register base, Register index, int scale, int offset) implements Memory {}
}
