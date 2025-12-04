package com.compiler;

public record Register(Type registerType, String name, MasmType type) implements Location {
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

  public static final Register RAX = new Register(Type.RAX, "rax", MasmType.QWORD);
  public static final Register EAX = new Register(Type.RAX, "eax", MasmType.DWORD);
  public static final Register AX = new Register(Type.RAX, "ax", MasmType.WORD);
  public static final Register AL = new Register(Type.RAX, "al", MasmType.BYTE);

  public static final Register RBX = new Register(Type.RBX, "rbx", MasmType.QWORD);
  public static final Register EBX = new Register(Type.RBX, "ebx", MasmType.DWORD);
  public static final Register BX = new Register(Type.RBX, "bx", MasmType.WORD);
  public static final Register BL = new Register(Type.RBX, "bl", MasmType.BYTE);

  public static final Register RCX = new Register(Type.RCX, "rcx", MasmType.QWORD);
  public static final Register ECX = new Register(Type.RCX, "ecx", MasmType.DWORD);
  public static final Register CX = new Register(Type.RCX, "cx", MasmType.WORD);
  public static final Register CL = new Register(Type.RCX, "cl", MasmType.BYTE);

  public static final Register RDX = new Register(Type.RDX, "rdx", MasmType.QWORD);
  public static final Register EDX = new Register(Type.RDX, "edx", MasmType.DWORD);
  public static final Register DX = new Register(Type.RDX, "dx", MasmType.WORD);
  public static final Register DL = new Register(Type.RDX, "dl", MasmType.BYTE);

  public static final Register RSP = new Register(Type.RSP, "rsp", MasmType.QWORD);
  public static final Register ESP = new Register(Type.RSP, "esp", MasmType.DWORD);
  public static final Register SP = new Register(Type.RSP, "sp", MasmType.WORD);

  public static final Register RBP = new Register(Type.RBP, "rbp", MasmType.QWORD);
  public static final Register EBP = new Register(Type.RBP, "ebp", MasmType.DWORD);
  public static final Register BP = new Register(Type.RBP, "bp", MasmType.WORD);

  public static final Register R8 = new Register(Type.R8, "r8", MasmType.QWORD);
  public static final Register R8D = new Register(Type.R8, "r8d", MasmType.DWORD);
  public static final Register R8W = new Register(Type.R8, "r8w", MasmType.WORD);
  public static final Register R8B = new Register(Type.R8, "r8b", MasmType.BYTE);

  public static final Register R9 = new Register(Type.R9, "r9", MasmType.QWORD);
  public static final Register R9D = new Register(Type.R9, "r9d", MasmType.DWORD);
  public static final Register R9W = new Register(Type.R9, "r9w", MasmType.WORD);
  public static final Register R9B = new Register(Type.R9, "r9b", MasmType.BYTE);

  public static Register GetRegister(Type type, MasmType size) {
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

  private static Register GetRegisterOfSize(MasmType size, Register rQword, Register rDword, Register rWord, Register rByte) {
    Register value = switch (size) { case QWORD -> rQword; case DWORD -> rDword; case WORD -> rWord; case BYTE -> rByte; };
    if (value == null) {
      throw new IllegalStateException("Size not supported");
    }

    return value;
  }
}
