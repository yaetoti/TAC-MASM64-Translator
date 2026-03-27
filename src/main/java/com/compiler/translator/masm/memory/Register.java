package com.compiler.translator.masm.memory;

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

    VEC0(Bank.VEC),
    VEC1(Bank.VEC),
    VEC2(Bank.VEC),
    VEC3(Bank.VEC),
    VEC4(Bank.VEC),
    VEC5(Bank.VEC),
    VEC6(Bank.VEC),
    VEC7(Bank.VEC),
    VEC8(Bank.VEC),
    VEC9(Bank.VEC),
    VEC10(Bank.VEC),
    VEC11(Bank.VEC),
    VEC12(Bank.VEC),
    VEC13(Bank.VEC),
    VEC14(Bank.VEC),
    VEC15(Bank.VEC),
    ;

    private final Bank bank;

    Type(Bank bank) {
      this.bank = bank;
    }

    public Bank GetBank() {
      return bank;
    }
  }

  // GPR
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

  public static final Register R10 = new Register(Type.R10, "r10", 8);
  public static final Register R10D = new Register(Type.R10, "r10d", 4);
  public static final Register R10W = new Register(Type.R10, "r10w", 2);
  public static final Register R10B = new Register(Type.R10, "r10b", 1);

  public static final Register R11 = new Register(Type.R11, "r11", 8);
  public static final Register R11D = new Register(Type.R11, "r11d", 4);
  public static final Register R11W = new Register(Type.R11, "r11w", 2);
  public static final Register R11B = new Register(Type.R11, "r11b", 1);

  public static final Register R12 = new Register(Type.R12, "r12", 8);
  public static final Register R12D = new Register(Type.R12, "r12d", 4);
  public static final Register R12W = new Register(Type.R12, "r12w", 2);
  public static final Register R12B = new Register(Type.R12, "r12b", 1);

  public static final Register R13 = new Register(Type.R13, "r13", 8);
  public static final Register R13D = new Register(Type.R13, "r13d", 4);
  public static final Register R13W = new Register(Type.R13, "r13w", 2);
  public static final Register R13B = new Register(Type.R13, "r13b", 1);

  public static final Register R14 = new Register(Type.R14, "r14", 8);
  public static final Register R14D = new Register(Type.R14, "r14d", 4);
  public static final Register R14W = new Register(Type.R14, "r14w", 2);
  public static final Register R14B = new Register(Type.R14, "r14b", 1);

  public static final Register R15 = new Register(Type.R15, "r15", 8);
  public static final Register R15D = new Register(Type.R15, "r15d", 4);
  public static final Register R15W = new Register(Type.R15, "r15w", 2);
  public static final Register R15B = new Register(Type.R15, "r15b", 1);

  // VEC
  public static final Register XMM0 = new Register(Type.VEC0, "xmm0", 16);
  public static final Register YMM0 = new Register(Type.VEC0, "ymm0", 32);
  public static final Register ZMM0 = new Register(Type.VEC0, "zmm0", 64);

  public static final Register XMM1 = new Register(Type.VEC1, "xmm1", 16);
  public static final Register YMM1 = new Register(Type.VEC1, "ymm1", 32);
  public static final Register ZMM1 = new Register(Type.VEC1, "zmm1", 64);

  public static final Register XMM2 = new Register(Type.VEC2, "xmm2", 16);
  public static final Register YMM2 = new Register(Type.VEC2, "ymm2", 32);
  public static final Register ZMM2 = new Register(Type.VEC2, "zmm2", 64);

  public static final Register XMM3 = new Register(Type.VEC3, "xmm3", 16);
  public static final Register YMM3 = new Register(Type.VEC3, "ymm3", 32);
  public static final Register ZMM3 = new Register(Type.VEC3, "zmm3", 64);

  public static final Register XMM4 = new Register(Type.VEC4, "xmm4", 16);
  public static final Register YMM4 = new Register(Type.VEC4, "ymm4", 32);
  public static final Register ZMM4 = new Register(Type.VEC4, "zmm4", 64);

  public static final Register XMM5 = new Register(Type.VEC5, "xmm5", 16);
  public static final Register YMM5 = new Register(Type.VEC5, "ymm5", 32);
  public static final Register ZMM5 = new Register(Type.VEC5, "zmm5", 64);

  public static final Register XMM6 = new Register(Type.VEC6, "xmm6", 16);
  public static final Register YMM6 = new Register(Type.VEC6, "ymm6", 32);
  public static final Register ZMM6 = new Register(Type.VEC6, "zmm6", 64);

  public static final Register XMM7 = new Register(Type.VEC7, "xmm7", 16);
  public static final Register YMM7 = new Register(Type.VEC7, "ymm7", 32);
  public static final Register ZMM7 = new Register(Type.VEC7, "zmm7", 64);

  public static final Register XMM8 = new Register(Type.VEC8, "xmm8", 16);
  public static final Register YMM8 = new Register(Type.VEC8, "ymm8", 32);
  public static final Register ZMM8 = new Register(Type.VEC8, "zmm8", 64);

  public static final Register XMM9 = new Register(Type.VEC9, "xmm9", 16);
  public static final Register YMM9 = new Register(Type.VEC9, "ymm9", 32);
  public static final Register ZMM9 = new Register(Type.VEC9, "zmm9", 64);

  public static final Register XMM10 = new Register(Type.VEC10, "xmm10", 16);
  public static final Register YMM10 = new Register(Type.VEC10, "ymm10", 32);
  public static final Register ZMM10 = new Register(Type.VEC10, "zmm10", 64);

  public static final Register XMM11 = new Register(Type.VEC11, "xmm11", 16);
  public static final Register YMM11 = new Register(Type.VEC11, "ymm11", 32);
  public static final Register ZMM11 = new Register(Type.VEC11, "zmm11", 64);

  public static final Register XMM12 = new Register(Type.VEC12, "xmm12", 16);
  public static final Register YMM12 = new Register(Type.VEC12, "ymm12", 32);
  public static final Register ZMM12 = new Register(Type.VEC12, "zmm12", 64);

  public static final Register XMM13 = new Register(Type.VEC13, "xmm13", 16);
  public static final Register YMM13 = new Register(Type.VEC13, "ymm13", 32);
  public static final Register ZMM13 = new Register(Type.VEC13, "zmm13", 64);

  public static final Register XMM14 = new Register(Type.VEC14, "xmm14", 16);
  public static final Register YMM14 = new Register(Type.VEC14, "ymm14", 32);
  public static final Register ZMM14 = new Register(Type.VEC14, "zmm14", 64);

  public static final Register XMM15 = new Register(Type.VEC15, "xmm15", 16);
  public static final Register YMM15 = new Register(Type.VEC15, "ymm15", 32);
  public static final Register ZMM15 = new Register(Type.VEC15, "zmm15", 64);


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

      case VEC0 -> GetVecOfSize(size, XMM0, YMM0, ZMM0);
      case VEC1 -> GetVecOfSize(size, XMM1, YMM1, ZMM1);
      case VEC2 -> GetVecOfSize(size, XMM2, YMM2, ZMM2);
      case VEC3 -> GetVecOfSize(size, XMM3, YMM3, ZMM3);
      case VEC4 -> GetVecOfSize(size, XMM4, YMM4, ZMM4);
      case VEC5 -> GetVecOfSize(size, XMM5, YMM5, ZMM5);
      case VEC6 -> GetVecOfSize(size, XMM6, YMM6, ZMM6);
      case VEC7 -> GetVecOfSize(size, XMM7, YMM7, ZMM7);
      case VEC8 -> GetVecOfSize(size, XMM8, YMM8, ZMM8);
      case VEC9 -> GetVecOfSize(size, XMM9, YMM9, ZMM9);
      case VEC10 -> GetVecOfSize(size, XMM10, YMM10, ZMM10);
      case VEC11 -> GetVecOfSize(size, XMM11, YMM11, ZMM11);
      case VEC12 -> GetVecOfSize(size, XMM12, YMM12, ZMM12);
      case VEC13 -> GetVecOfSize(size, XMM13, YMM13, ZMM13);
      case VEC14 -> GetVecOfSize(size, XMM14, YMM14, ZMM14);
      case VEC15 -> GetVecOfSize(size, XMM15, YMM15, ZMM15);
    };
  }

  private static Register GetGprOfSize(int size, Register rQword, Register rDword, Register rWord, Register rByte) {
    return switch (size) {
      case 8 -> rQword;
      case 4 -> rDword;
      case 2 -> rWord;
      case 1 -> rByte;
      default -> null;
    };
  }

  private static Register GetVecOfSize(int size, Register v128, Register v256, Register v512) {
    return switch(size) {
      case 16 -> v128;
      case 32 -> v256;
      case 64 -> v512;
      default -> null;
    };
  }

  @Override
  public String toString() {
    return name;
  }
}
