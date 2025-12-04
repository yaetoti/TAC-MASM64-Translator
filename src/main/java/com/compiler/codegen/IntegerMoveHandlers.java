package com.compiler.codegen;

import com.compiler.DtInteger;
import com.compiler.FunctionContext;
import com.compiler.Register;
import com.compiler.symbols.IVariable;
import com.compiler.symbols.IntegerConstant;

public final class IntegerMoveHandlers {
  public static void MoveIntegerConstant(IVariable dst, DtInteger dstDataType, IntegerConstant src, FunctionContext ctx) {
    var out = ctx.out;

    var dstReg = ctx.DefineRegister(dst);
    ctx.MarkDirty(dst);
    out.EmitF("mov %s, %s", dstReg, src.value());
  }

  public static void MoveIntegerSymbol(IVariable dst, DtInteger dstDataType, IVariable src, DtInteger srcDataType, FunctionContext ctx) {
    var out = ctx.out;

    var srcReg = ctx.EnsureInRegister(src);
    var dstReg = ctx.DefineRegister(dst);
    ctx.MarkDirty(dst);

    // Same size
    if (srcReg.size() == dstReg.size()) {
      out.EmitF("mov %s, %s", dstReg, srcReg);
      return;
    }

    // Truncation
    if (srcReg.size() >= dstReg.size()) {
      var srcRegTrunk = Register.Get(srcReg.type(), dstReg.size());
      out.EmitF("mov %s, %s", dstReg, srcRegTrunk);
      return;
    }

    // Promotion
    String op = srcDataType.GetSign() == DtInteger.Sign.SIGNED ? "movsx" : "movzx";
    out.EmitF("%s %s, %s", op, dstReg, srcReg);
  }
}
