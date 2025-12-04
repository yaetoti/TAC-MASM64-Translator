package com.compiler.codegen;

import com.compiler.DtInteger;
import com.compiler.FunctionContext;
import com.compiler.symbols.IVariable;
import com.compiler.symbols.IntegerConstant;

public final class IntegerMoveHandlers {
  public static void MoveIntegerConstant(IVariable dst, DtInteger dstDataType, IntegerConstant src, FunctionContext ctx) {
    var dstReg = ctx.DefineRegister(dst);
    ctx.MarkDirty(dst);

    ctx.out.EmitF("mov %s, %s", dstReg, src.value());
  }

  public static void MoveIntegerSymbol(IVariable dst, DtInteger dstDataType, IVariable src, DtInteger srcDataType, FunctionContext ctx) {
    var srcReg = ctx.EnsureInRegister(src);
    var dstReg = ctx.DefineRegister(dst);
    ctx.MarkDirty(dst);

    MasmMoveUtils.MoveToRegister(ctx, dstReg, srcReg, srcDataType.GetSign() == DtInteger.Sign.SIGNED);
  }
}
