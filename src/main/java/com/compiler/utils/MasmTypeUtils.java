package com.compiler.utils;

import com.compiler.memory.MasmStorageClass;
import com.compiler.MasmType;
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

  public static MasmType GetMasmType(IDataType type) {
    return switch (type) {
      case DtInteger dtInteger -> MasmType.FromSize(dtInteger.GetSize());
      case DtPointer dtPointer -> MasmType.QWORD;
      default -> null;
    };
  }
}
