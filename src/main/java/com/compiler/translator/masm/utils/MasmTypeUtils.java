package com.compiler.translator.masm.utils;

import com.compiler.translator.masm.memory.MasmStorageClass;
import com.compiler.translator.masm.memory.Register;
import com.compiler.ir.types.DtFloat;
import com.compiler.ir.types.DtInteger;
import com.compiler.ir.types.DtPointer;
import com.compiler.ir.types.IDataType;

public final class MasmTypeUtils {
  public static MasmStorageClass GetStorageClass(IDataType type) {
    return switch (type) {
      case DtInteger dtInteger -> MasmStorageClass.GPR;
      case DtPointer dtPointer -> MasmStorageClass.GPR;
      case DtFloat dtFloat -> MasmStorageClass.VEC;
    };
  }

  public static Register.Bank GetBank(IDataType type) {
    return switch (type) {
      case DtInteger dtInteger -> Register.Bank.GPR;
      case DtPointer dtPointer -> Register.Bank.GPR;
      case DtFloat dtFloat -> Register.Bank.VEC;
    };
  }

  public static Register GetRegister(Register.Type regType, IDataType dataType) {
    // TODO SUS. Should not work for every data type. Should only allow to get some registers
    // TODO Float32 will go in 128 bit register
    return Register.Get(regType, dataType.GetSize());
  }
}
