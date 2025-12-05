package com.compiler.types;

sealed public interface IDataType permits DtInteger, DtPointer {
  int GetSize();
}

