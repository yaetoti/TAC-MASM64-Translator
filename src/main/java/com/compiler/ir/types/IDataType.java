package com.compiler.ir.types;

sealed public interface IDataType permits DtFloat, DtInteger, DtPointer {
  int GetSize();
}

