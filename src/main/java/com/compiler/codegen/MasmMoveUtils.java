package com.compiler.codegen;

import com.compiler.FunctionContext;
import com.compiler.memory.Register;

public final class MasmMoveUtils {
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
      ctx.out.EmitF("mov %s, %s", dst, src);
      return;
    }

    ctx.out.EmitF("movzx %s, %s", dst, src);
  }
}
