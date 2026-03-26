package com.compiler.ir.types;

sealed public interface IDataType permits DtArray, DtFloat, DtInteger, DtPointer {
  int GetSize();
}

