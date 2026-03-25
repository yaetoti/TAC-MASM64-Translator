package com.compiler.translator.masm.codegen;

import com.compiler.ir.types.DtInteger;
import com.compiler.ir.FunctionContext;
import com.compiler.ir.symbols.IVariable;
import com.compiler.ir.symbols.IntegerConstant;

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
