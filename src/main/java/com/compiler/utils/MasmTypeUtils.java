package com.compiler.utils;

import com.compiler.memory.MasmStorageClass;
import com.compiler.memory.Register;
import com.compiler.types.DtInteger;
import com.compiler.types.DtPointer;
import com.compiler.types.IDataType;

public final class MasmTypeUtils {
  public static MasmStorageClass GetStorageClass(IDataType type) {
    return switch (type) {
      case DtInteger dtInteger -> MasmStorageClass.GPR;
      case DtPointer dtPointer -> MasmStorageClass.GPR;
    };
  }

  public static Register.Bank GetBank(IDataType type) {
    return switch (type) {
      case DtInteger dtInteger -> Register.Bank.GPR;
      case DtPointer dtPointer -> Register.Bank.GPR;
    };
  }

  public static Register GetRegister(Register.Type regType, IDataType dataType) {
    return Register.Get(regType, dataType.GetSize());
  }
}
