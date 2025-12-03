package com.compiler;

public enum MasmType {
  BYTE(1),
  WORD(2),
  DWORD(4),
  QWORD(8);

  public final int size;

  MasmType(int size) {
    this.size = size;
  }

  public static MasmType FromSize(int size) {
    return switch (size) {
      case 1 -> BYTE;
      case 2 -> WORD;
      case 4 -> DWORD;
      case 8 -> QWORD;
      default -> throw new IllegalArgumentException("Invalid size");
    };
  }

  public String GetPointerString() {
    return switch (this) {
      case BYTE -> "byte ptr";
      case WORD -> "word ptr";
      case DWORD -> "dword ptr";
      case QWORD -> "qword ptr";
    };
  }
}
