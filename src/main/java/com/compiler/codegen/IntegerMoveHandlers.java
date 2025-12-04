package com.compiler.codegen;

import com.compiler.DtInteger;
import com.compiler.FunctionContext;
import com.compiler.MasmTypeUtils;
import com.compiler.Register;
import com.compiler.symbols.IVariable;
import com.compiler.symbols.IntegerConstant;

public final class IntegerMoveHandlers {
  public static void MoveIntegerConstant(IVariable dst, DtInteger dstDataType, IntegerConstant src, FunctionContext ctx) {
    var memoryManager = ctx.memoryManager;
    var registerManager = ctx.registerManager;
    var out = ctx.out;

    var dstLocation = memoryManager.locations.get(dst);
    var dstMasmType = MasmTypeUtils.GetMasmType(dstDataType);

    // Do we support changing constant size? 4 != 8
    // Moving straight to the memory? naaaah. Better be spilling used registers

    if (dstLocation.register != null) {
      // Move straight to register
      out.EmitF("mov %s, %s", dstLocation.register.name(), src.value());
      return;
    }

    // Allocate another register
    // TODO add possibility to move to memory

    var reg = ctx.registerManager.GetFreeRegister();
    var dstReg = Register.GetRegister(reg.type, dstMasmType).name();
    reg.symbol = dst;
    dstLocation.register = reg.type;

    out.EmitF("mov %s, %s", dstReg, src.value());
  }

  public static void MoveIntegerSymbol(IVariable dst, DtInteger dstDataType, IVariable src, DtInteger srcDataType, FunctionContext ctx) {
    var memoryManager = ctx.memoryManager;
    var registerManager = ctx.registerManager;
    var out = ctx.out;

    var dstLocation = memoryManager.locations.get(dst);
    var dstMasmType = MasmTypeUtils.GetMasmType(dstDataType);

    // Here would be switch type
    // For integers there would be promotions and shrinks
    // for floats there would be data type casts

    var srcLocation = memoryManager.locations.get(src);
    var srcMasmType = MasmTypeUtils.GetMasmType(srcDataType);

    if (dstLocation.register != null && srcLocation.register != null) {
      var dstReg = Register.GetRegister(dstLocation.register, dstMasmType).name();
      out.EmitF("mov %s, %s", dstReg, srcLocation.register.name());
      return;
    }

    if (dstLocation.register != null && srcLocation.register == null) {
      var dstReg = Register.GetRegister(dstLocation.register, dstMasmType).name();
      out.EmitF("mov %s, %s", dstReg, srcLocation.memory);
      return;
    }

    if (dstLocation.register == null && srcLocation.register != null) {
      var srcReg = Register.GetRegister(srcLocation.register, srcMasmType).name();

      var reg = registerManager.GetFreeRegister();
      var dstReg = Register.GetRegister(reg.type, srcMasmType).name();
      reg.symbol = dst;
      dstLocation.register = reg.type;
      dstLocation.isDirty = true;

      out.EmitF("mov %s, %s", dstReg, srcReg);
      return;
    }

    if (dstLocation.register == null && srcLocation.register == null) {
      // Register <- Memory
      // Assign register

      var reg = registerManager.GetFreeRegister();
      var dstReg = Register.GetRegister(reg.type, srcMasmType).name();
      reg.symbol = dst;

      out.EmitF("mov %s, %s", dstReg, srcLocation.memory);
      dstLocation.register = reg.type;
      return;
    }

    throw new IllegalStateException("Impossible case");
  }
}
