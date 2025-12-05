package com.compiler.types;

public record DtPointer(IDataType underlyingType) implements IDataType {
  @Override
  public int GetSize() {
    return 8;
  }
}
