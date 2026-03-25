package com.compiler.ir.types;

// TODO implement
public record DtPointer(IDataType underlyingType) implements IDataType {
  @Override
  public int GetSize() {
    return 8;
  }
}
