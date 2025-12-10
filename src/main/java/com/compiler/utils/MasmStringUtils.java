package com.compiler.utils;

import com.compiler.symbols.IConstant;
import com.compiler.symbols.IntegerConstant;
import com.compiler.symbols.PointerConstant;
import com.compiler.types.DtInteger;
import com.compiler.types.DtPointer;
import com.compiler.types.IDataType;

// === Utils ===
public final class MasmStringUtils {
  private static final String DB = "db";
  private static final String DW = "dw";
  private static final String DD = "dd";
  private static final String DQ = "dq";

  private static final String BYTE = "byte";
  private static final String WORD = "word";
  private static final String DWORD = "dword";
  private static final String QWORD = "qword";

  public static String GetDeclarationString(IDataType type) {
    switch (type) {
      case DtInteger dtInteger -> {
        return switch (dtInteger.GetSize()) {
          case 1 -> DB;
          case 2 -> DW;
          case 4 -> DD;
          case 8 -> DQ;
          default -> throw new IllegalStateException("Unexpected size: " + dtInteger.GetSize());
        };
      }
      case DtPointer dtPointer -> {
        return DQ;
      }
    }
  }

  public static String GetTypeString(IDataType type) {
    return switch (type) {
      case DtInteger dtInteger -> GetTypeString(dtInteger.GetSize());
      case DtPointer dtPointer -> QWORD;
    };
  }

  public static String GetTypeString(int size) {
    return switch (size) {
      case 1 -> BYTE;
      case 2 -> WORD;
      case 4 -> DWORD;
      case 8 -> QWORD;
      default -> null;
    };
  }

  public static String GetConstantString(IConstant constant) {
    // TODO mov rax, ? ; bruh.. move that to HandleDeclaration
    if (constant == null) {
      return "?";
    }

    // TODO name mangling for symbols
    switch (constant) {
      case IntegerConstant integerConstant -> {
        return integerConstant.value();
      }
      case PointerConstant pointerConstant -> {
        return pointerConstant.symbol().GetName();
      }
      default -> throw new IllegalStateException("Unexpected value: " + constant);
    }
  }
}
