package com.compiler;

public record DtPointer(IDataType underlyingType) implements IDataType {
  @Override
  public int GetSize() {
    return 8;
  }
}
