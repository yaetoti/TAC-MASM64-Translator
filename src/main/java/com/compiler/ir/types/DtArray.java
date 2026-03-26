package com.compiler.ir.types;

// TODO for now basePointer is just a pointer to data on a stack
public final class DtArray implements IDataType {
  public final IDataType elementType;
  public final int elementsCount;

  public DtArray(IDataType elementType, int elementsCount) {
    this.elementType = elementType;
    this.elementsCount = elementsCount;
  }

  @Override
  public int GetSize() {
    return elementsCount * elementType.GetSize();
  }
}
