package com.compiler;

public record OffsetMemory(MasmType masmType, Register base, Register index, int scale, int offset) implements Memory {
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(masmType.GetPointerString());
    builder.append(" [");
    // Base
    if (base != null) {
      // Base
      if (base.type() != MasmType.QWORD) {
        throw new IllegalStateException("Base register must be 8 bytes");
      }
      builder.append(base().name());

      // + or -
      if (index() != null) {
        if (scale() > 0) {
          builder.append(" + ");
        }
        else {
          builder.append(" - ");
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

      builder.append(index().name());
      builder.append(" * ");
      builder.append(scale());
    }

    // Offset
    if (base() != null || index() != null) {
      if (offset() >= 0) {
        builder.append(" + ");
        builder.append(offset());
      }
      else {
        builder.append(" - ");
        builder.append(Math.abs(offset()));
      }
    }
    else {
      builder.append(offset());
    }

    builder.append("]");

    return builder.toString();
  }
}
