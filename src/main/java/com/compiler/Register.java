package com.compiler;

public record Register(Type type, String name, int size) implements Location {
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

  public static final Register RAX = new Register(Type.RAX, "rax", 8);
  public static final Register EAX = new Register(Type.RAX, "eax", 4);
  public static final Register AX = new Register(Type.RAX, "ax", 2);
  public static final Register AL = new Register(Type.RAX, "al", 1);

  public static final Register RBX = new Register(Type.RBX, "rbx", 8);
  public static final Register EBX = new Register(Type.RBX, "ebx", 4);
  public static final Register BX = new Register(Type.RBX, "bx", 2);
  public static final Register BL = new Register(Type.RBX, "bl", 1);

  public static final Register RCX = new Register(Type.RCX, "rcx", 8);
  public static final Register ECX = new Register(Type.RCX, "ecx", 4);
  public static final Register CX = new Register(Type.RCX, "cx", 2);
  public static final Register CL = new Register(Type.RCX, "cl", 1);

  public static final Register RDX = new Register(Type.RDX, "rdx", 8);
  public static final Register EDX = new Register(Type.RDX, "edx", 4);
  public static final Register DX = new Register(Type.RDX, "dx", 2);
  public static final Register DL = new Register(Type.RDX, "dl", 1);

  public static final Register RSP = new Register(Type.RSP, "rsp", 8);
  public static final Register ESP = new Register(Type.RSP, "esp", 4);
  public static final Register SP = new Register(Type.RSP, "sp", 2);

  public static final Register RBP = new Register(Type.RBP, "rbp", 8);
  public static final Register EBP = new Register(Type.RBP, "ebp", 4);
  public static final Register BP = new Register(Type.RBP, "bp", 2);

  public static final Register R8 = new Register(Type.R8, "r8", 8);
  public static final Register R8D = new Register(Type.R8, "r8d", 4);
  public static final Register R8W = new Register(Type.R8, "r8w", 2);
  public static final Register R8B = new Register(Type.R8, "r8b", 1);

  public static final Register R9 = new Register(Type.R9, "r9", 8);
  public static final Register R9D = new Register(Type.R9, "r9d", 4);
  public static final Register R9W = new Register(Type.R9, "r9w", 2);
  public static final Register R9B = new Register(Type.R9, "r9b", 1);

  public static Register Get(Type type, int size) {
    return switch (type) {
      case RAX -> GetRegisterOfSize(size, RAX, EAX, AX, AL);
      case RBX -> GetRegisterOfSize(size, RBX, EBX, BX, BL);
      case RCX -> GetRegisterOfSize(size, RCX, ECX, CX, CL);
      case RDX -> GetRegisterOfSize(size, RDX, EDX, DX, DL);
      case RSP -> GetRegisterOfSize(size, RSP, ESP, SP, null);
      case RBP -> GetRegisterOfSize(size, RBP, EBP, BP, null);
      case R8 -> GetRegisterOfSize(size, R8, R8D, R8W, R8B);
      case R9 -> GetRegisterOfSize(size, R9, R9D, R9W, R9B);
    };
  }

  private static Register GetRegisterOfSize(int size, Register rQword, Register rDword, Register rWord, Register rByte) {
    Register value = switch (size) {
      case 8 -> rQword;
      case 4 -> rDword;
      case 2 -> rWord;
      case 1 -> rByte;
      default -> throw new IllegalStateException("Unexpected value: " + size);
    };

    if (value == null) {
      throw new IllegalStateException("Size not supported");
    }

    return value;
  }

  @Override
  public String toString() {
    return name;
  }
}
