package com.compiler.translator.masm.codegen;

import com.compiler.ir.FunctionContext;
import com.compiler.translator.masm.memory.Memory;
import com.compiler.translator.masm.memory.Register;

public final class MasmMoveUtils {
  // TODO a lot is missed here

  // GPR <- GPR (copy? promote?)
  // XMM <- XMM (32? 64? 128?)
  // MEM <- MEM (via gpr or per-byte copy)

  // GPR <- XMM (convert or copy)
  // XMM <- GPR (convert or copy)

  // MEM <- XMM
  // MEM <- GPR
  // MEM -> GPR
  // MEM -> XMM


  public static void MoveToGPR(FunctionContext ctx, Register dst, Register src, boolean isSigned) {
    assert dst.type().GetBank() == Register.Bank.GPR;

    switch (src.type().GetBank()) {
      case GPR -> {
        ctx.out.EmitF("mov %s, %s", dst, src);
      }
      case VEC -> {

      }
    }
    if (src.type().GetBank() == Register.Bank.GPR) {
      ctx.out.EmitF("mov %s, %s", dst, src);
      return;
    }
    if (src.type().GetBank() == Register.Bank.VEC) {
      ctx.out.EmitF("movaps %s, %s", src, dst);
    }
  }

  public static void MoveToRegister(FunctionContext ctx, Register dst, Register src, boolean isSigned) {
    // Same size
    if (dst.size() == src.size()) {
      ctx.out.EmitF("mov %s, %s", dst, src);
      return;
    }

    // Truncation
    if (dst.size() < src.size()) {
      var srcRegTrunk = Register.Get(src.type(), dst.size());
      ctx.out.EmitF("mov %s, %s", dst, srcRegTrunk);
      return;
    }

    // Promotion
    // Signed
    if (isSigned) {
      if (src.size() == 4) {
        ctx.out.EmitF("movsxd %s, %s", dst, src);
        return;
      }

      ctx.out.EmitF("movsx %s, %s", dst, src);
      return;
    }

    // Unsigned
    if (src.size() == 4) {
      ctx.out.EmitF("mov %s, %s", Register.Get(dst.type(), 4), src);
      return;
    }

    ctx.out.EmitF("movzx %s, %s", dst, src);
  }

  public static void MoveToRegister(FunctionContext ctx, Register dst, Memory src) {
    // Same size
    if (dst.size() == src.GetSize()) {
      ctx.out.EmitF("mov %s, %s", dst, src);
      return;
    }

    throw new RuntimeException("Not implemented");
  }

  public static void MoveToMemory(FunctionContext ctx, Memory dst, Register src) {
    // Same size
    if (dst.GetSize() == src.size()) {
      ctx.out.EmitF("mov %s, %s", dst, src);
      return;
    }

    throw new RuntimeException("Not implemented");
  }

  public static void MoveToMemory(FunctionContext ctx, Memory dst, Memory src) {
    var regInfo = ctx.registerManager.GetFreeRegister(Register.Bank.GPR);
    var reg = Register.Get(regInfo.GetType(), dst.GetSize());
    regInfo.Lock();
    MoveToRegister(ctx, reg, src);
    MoveToMemory(ctx, dst, reg);
    regInfo.Unlock();
  }

  public static void MoveToMemory(FunctionContext ctx, Memory dst, Memory src, Register tempReg) {
    MoveToRegister(ctx, tempReg, src);
    MoveToMemory(ctx, dst, tempReg);
  }
}
