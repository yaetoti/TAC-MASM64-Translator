package com.yaetoti;

// TODO Add float, struct, array, VLA
public final class DataType {
  public enum Type {
    SIGNED,
    UNSIGNED
  }

  private final int size;
  private final Type type;

  private DataType(int size, Type type) {
    this.size = size;
    this.type = type;
  }

  public int size() {
    return size;
  }

  public Type type() {
    return type;
  }

  public static final DataType i8 = new DataType(1, Type.SIGNED);
  public static final DataType i16 = new DataType(2, Type.SIGNED);
  public static final DataType i32 = new DataType(4, Type.SIGNED);
  public static final DataType i64 = new DataType(8, Type.SIGNED);
  public static final DataType u8 = new DataType(1, Type.UNSIGNED);
  public static final DataType u16 = new DataType(2, Type.UNSIGNED);
  public static final DataType u32 = new DataType(4, Type.UNSIGNED);
  public static final DataType u64 = new DataType(8, Type.UNSIGNED);
}
