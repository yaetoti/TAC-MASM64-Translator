package com.compiler.ir.types;

public final class DtInteger implements IDataType {
  public enum Sign {
    SIGNED,
    UNSIGNED
  }

  public static final DtInteger i8 = new DtInteger(1, Sign.SIGNED);
  public static final DtInteger i16 = new DtInteger(2, Sign.SIGNED);
  public static final DtInteger i32 = new DtInteger(4, Sign.SIGNED);
  public static final DtInteger i64 = new DtInteger(8, Sign.SIGNED);
  public static final DtInteger u8 = new DtInteger(1, Sign.UNSIGNED);
  public static final DtInteger u16 = new DtInteger(2, Sign.UNSIGNED);
  public static final DtInteger u32 = new DtInteger(4, Sign.UNSIGNED);
  public static final DtInteger u64 = new DtInteger(8, Sign.UNSIGNED);

  private final int m_size;
  private final Sign m_sign;

  private DtInteger(int size, Sign sign) {
    m_size = size;
    m_sign = sign;
  }

  @Override
  public int GetSize() {
    return m_size;
  }

  public Sign GetSign() {
    return m_sign;
  }
}
