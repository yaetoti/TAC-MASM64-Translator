package com.compiler;

sealed public interface IDataType permits DtInteger, DtPointer {
  int GetSize();
}

