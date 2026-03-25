package com.compiler.ir.types;

// TODO implement
public final class DtFloat implements IDataType {
  public static final DtFloat f32 = new DtFloat(4);
  public static final DtFloat f64 = new DtFloat(8);

  private final int m_size;

  private DtFloat(int size) {
    this.m_size = size;
  }

  @Override
  public int GetSize() {
    return m_size;
  }
}
