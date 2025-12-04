package com.compiler;

public record OffsetMemory(int size, Register base, Register index, int scale, int offset) implements Memory {
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(MasmStringUtils.GetTypeString(size));
    sb.append(" ptr [");
    // Base
    if (base != null) {
      // Base
      if (base.size() != 8) {
        throw new IllegalStateException("Base register must be 8 bytes");
      }
      sb.append(base().name());

      // + or -
      if (index() != null) {
        if (scale() > 0) {
          sb.append(" + ");
        }
        else {
          sb.append(" - ");
        }
      }
    }

    // Index
    if (index() != null) {
      // Check scale
      switch (scale()) {
        case 1, 2, 4, 8 -> {}
        default -> throw new IllegalStateException("Scale must be 1, 2, 4 or 8");
      }

      sb.append(index().name());
      sb.append(" * ");
      sb.append(scale());
    }

    // Offset
    if (base() != null || index() != null) {
      if (offset() >= 0) {
        sb.append(" + ");
        sb.append(offset());
      }
      else {
        sb.append(" - ");
        sb.append(Math.abs(offset()));
      }
    }
    else {
      sb.append(offset());
    }

    sb.append("]");

    return sb.toString();
  }

  @Override
  public int GetSize() {
    return size;
  }
}
