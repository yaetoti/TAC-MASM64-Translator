package com.compiler.memory;

public record Register(Type type, String name, int size) implements Location {
  @Override
  public int GetSize() {
    return size;
  }

  public enum Bank {
    GPR,
    VEC
  }

  public enum Type {
    RAX(Bank.GPR),
    RBX(Bank.GPR),
    RCX(Bank.GPR),
    RDX(Bank.GPR),
    RSP(Bank.GPR),
    RBP(Bank.GPR),
    RSI(Bank.GPR),
    RDI(Bank.GPR),
    R8(Bank.GPR),
    R9(Bank.GPR),
    R10(Bank.GPR),
    R11(Bank.GPR),
    R12(Bank.GPR),
    R13(Bank.GPR),
    R14(Bank.GPR),
    R15(Bank.GPR),

    XMM0(Bank.VEC),
    XMM1(Bank.VEC),
    XMM2(Bank.VEC),
    XMM3(Bank.VEC),
    XMM4(Bank.VEC),
    XMM5(Bank.VEC),
    XMM6(Bank.VEC),
    XMM7(Bank.VEC),
    XMM8(Bank.VEC),
    XMM9(Bank.VEC),
    XMM10(Bank.VEC),
    XMM11(Bank.VEC),
    XMM12(Bank.VEC),
    XMM13(Bank.VEC),
    XMM14(Bank.VEC),
    XMM15(Bank.VEC),
    ;

    private final Bank bank;

    Type(Bank bank) {
      this.bank = bank;
    }

    public Bank GetBank() {
      return bank;
    }
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

  public static final Register RSI = new Register(Type.RCX, "rsi", 8);
  public static final Register ESI = new Register(Type.RCX, "esi", 4);
  public static final Register SI = new Register(Type.RCX, "si", 2);

  public static final Register RDI = new Register(Type.RDX, "rdi", 8);
  public static final Register EDI = new Register(Type.RDX, "edi", 4);
  public static final Register DI = new Register(Type.RDX, "di", 2);

  public static final Register R8 = new Register(Type.R8, "r8", 8);
  public static final Register R8D = new Register(Type.R8, "r8d", 4);
  public static final Register R8W = new Register(Type.R8, "r8w", 2);
  public static final Register R8B = new Register(Type.R8, "r8b", 1);

  public static final Register R9 = new Register(Type.R9, "r9", 8);
  public static final Register R9D = new Register(Type.R9, "r9d", 4);
  public static final Register R9W = new Register(Type.R9, "r9w", 2);
  public static final Register R9B = new Register(Type.R9, "r9b", 1);

  public static final Register R10 = new Register(Type.R9, "r10", 8);
  public static final Register R10D = new Register(Type.R9, "r10d", 4);
  public static final Register R10W = new Register(Type.R9, "r10w", 2);
  public static final Register R10B = new Register(Type.R9, "r10b", 1);

  public static final Register R11 = new Register(Type.R9, "r11", 8);
  public static final Register R11D = new Register(Type.R9, "r11d", 4);
  public static final Register R11W = new Register(Type.R9, "r11w", 2);
  public static final Register R11B = new Register(Type.R9, "r11b", 1);

  public static final Register R12 = new Register(Type.R9, "r12", 8);
  public static final Register R12D = new Register(Type.R9, "r12d", 4);
  public static final Register R12W = new Register(Type.R9, "r12w", 2);
  public static final Register R12B = new Register(Type.R9, "r12b", 1);

  public static final Register R13 = new Register(Type.R9, "r13", 8);
  public static final Register R13D = new Register(Type.R9, "r13d", 4);
  public static final Register R13W = new Register(Type.R9, "r13w", 2);
  public static final Register R13B = new Register(Type.R9, "r13b", 1);

  public static final Register R14 = new Register(Type.R9, "r14", 8);
  public static final Register R14D = new Register(Type.R9, "r14d", 4);
  public static final Register R14W = new Register(Type.R9, "r14w", 2);
  public static final Register R14B = new Register(Type.R9, "r14b", 1);

  public static final Register R15 = new Register(Type.R9, "r15", 8);
  public static final Register R15D = new Register(Type.R9, "r15d", 4);
  public static final Register R15W = new Register(Type.R9, "r15w", 2);
  public static final Register R15B = new Register(Type.R9, "r15b", 1);

  public static final Register XMM0 = new Register(Type.XMM0, "xmm0", 16);
  public static final Register XMM1 = new Register(Type.XMM0, "xmm1", 16);
  public static final Register XMM2 = new Register(Type.XMM0, "xmm2", 16);
  public static final Register XMM3 = new Register(Type.XMM0, "xmm3", 16);
  public static final Register XMM4 = new Register(Type.XMM0, "xmm4", 16);
  public static final Register XMM5 = new Register(Type.XMM0, "xmm5", 16);
  public static final Register XMM6 = new Register(Type.XMM0, "xmm6", 16);
  public static final Register XMM7 = new Register(Type.XMM0, "xmm7", 16);
  public static final Register XMM8 = new Register(Type.XMM0, "xmm8", 16);
  public static final Register XMM9 = new Register(Type.XMM0, "xmm9", 16);
  public static final Register XMM10 = new Register(Type.XMM0, "xmm10", 16);
  public static final Register XMM11 = new Register(Type.XMM0, "xmm11", 16);
  public static final Register XMM12 = new Register(Type.XMM0, "xmm12", 16);
  public static final Register XMM13 = new Register(Type.XMM0, "xmm13", 16);
  public static final Register XMM14 = new Register(Type.XMM0, "xmm14", 16);
  public static final Register XMM15 = new Register(Type.XMM0, "xmm15", 16);

  public static Register Get(Type type, int size) {
    return switch (type) {
      case RAX -> GetGprOfSize(size, RAX, EAX, AX, AL);
      case RBX -> GetGprOfSize(size, RBX, EBX, BX, BL);
      case RCX -> GetGprOfSize(size, RCX, ECX, CX, CL);
      case RDX -> GetGprOfSize(size, RDX, EDX, DX, DL);
      case RSP -> GetGprOfSize(size, RSP, ESP, SP, null);
      case RBP -> GetGprOfSize(size, RBP, EBP, BP, null);
      case RSI -> GetGprOfSize(size, RSI, ESI, SI, null);
      case RDI -> GetGprOfSize(size, RDI, EDI, DI, null);
      case R8 -> GetGprOfSize(size, R8, R8D, R8W, R8B);
      case R9 -> GetGprOfSize(size, R9, R9D, R9W, R9B);
      case R10 -> GetGprOfSize(size, R10, R10D, R10W, R10B);
      case R11 -> GetGprOfSize(size, R11, R11D, R11W, R11B);
      case R12 -> GetGprOfSize(size, R12, R12D, R12W, R12B);
      case R13 -> GetGprOfSize(size, R13, R13D, R13W, R13B);
      case R14 -> GetGprOfSize(size, R14, R14D, R14W, R14B);
      case R15 -> GetGprOfSize(size, R15, R15D, R15W, R15B);

      case XMM0 -> GetVecOfSize(size, XMM0);
      case XMM1 -> GetVecOfSize(size, XMM1);
      case XMM2 -> GetVecOfSize(size, XMM2);
      case XMM3 -> GetVecOfSize(size, XMM3);
      case XMM4 -> GetVecOfSize(size, XMM4);
      case XMM5 -> GetVecOfSize(size, XMM5);
      case XMM6 -> GetVecOfSize(size, XMM6);
      case XMM7 -> GetVecOfSize(size, XMM7);
      case XMM8 -> GetVecOfSize(size, XMM8);
      case XMM9 -> GetVecOfSize(size, XMM9);
      case XMM10 -> GetVecOfSize(size, XMM10);
      case XMM11 -> GetVecOfSize(size, XMM11);
      case XMM12 -> GetVecOfSize(size, XMM12);
      case XMM13 -> GetVecOfSize(size, XMM13);
      case XMM14 -> GetVecOfSize(size, XMM14);
      case XMM15 -> GetVecOfSize(size, XMM15);
    };
  }

  private static Register GetGprOfSize(int size, Register rQword, Register rDword, Register rWord, Register rByte) {
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

  private static Register GetVecOfSize(int size, Register v128) {
    if (size != 16) {
      throw new RuntimeException("No implemented");
    }

    return v128;
  }

  @Override
  public String toString() {
    return name;
  }
}
