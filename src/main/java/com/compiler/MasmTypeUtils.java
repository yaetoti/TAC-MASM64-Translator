package com.compiler;

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
