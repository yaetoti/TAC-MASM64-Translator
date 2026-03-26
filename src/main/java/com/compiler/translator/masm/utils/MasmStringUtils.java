package com.compiler.translator.masm.utils;

import com.compiler.ir.symbols.FloatConstant;
import com.compiler.ir.symbols.IConstant;
import com.compiler.ir.symbols.IntegerConstant;
import com.compiler.ir.symbols.PointerConstant;
import com.compiler.ir.types.DtFloat;
import com.compiler.ir.types.DtInteger;
import com.compiler.ir.types.DtPointer;
import com.compiler.ir.types.IDataType;

public final class MasmStringUtils {
  private static final String DB = "db";
  private static final String DW = "dw";
  private static final String DD = "dd";
  private static final String DQ = "dq";
  private static final String DREAL4 = "real4";
  private static final String DREAL8 = "real8";

  private static final String BYTE = "byte";
  private static final String WORD = "word";
  private static final String DWORD = "dword";
  private static final String QWORD = "qword";

  public static String GetDeclarationString(IDataType type) {
    // TODO But how do you declare float? String? Move that to declaration handler

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
      case DtFloat dtFloat -> {
        return switch(dtFloat.GetSize()) {
          case 4 -> DREAL4;
          case 8 -> DREAL8;
          default -> throw new IllegalStateException("Unexpected size: " + dtFloat.GetSize());
        };
      }
    }
  }

  public static String GetTypeString(IDataType type) {
    // TODO should we even do this on float?

    return switch (type) {
      case DtInteger dtInteger -> GetTypeString(dtInteger.GetSize());
      case DtPointer dtPointer -> QWORD;
      case DtFloat dtFloat -> GetTypeString(dtFloat.GetSize());
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
      case IntegerConstant c -> {
        return c.value();
      }
      case PointerConstant c -> {
        return c.symbol().GetName();
      }
      case FloatConstant c -> {
        return c.value();
      }
      default -> throw new IllegalStateException("Unexpected value: " + constant);
    }
  }
}
