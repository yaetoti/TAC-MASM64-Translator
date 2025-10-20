package com.yaetoti;

public class Codegen {
  public static String GetPointerString(TAC.Size size) {
    return switch (size) {
      case BYTE -> "byte ptr";
      case WORD -> "word ptr";
      case DWORD -> "dword ptr";
      case QWORD -> "qword ptr";
    };
  }

  public static String GetAddressString(TAC.Size size, TAC.Operand operand) {
    String ptrString = GetPointerString(size);
    switch (operand) {
    case TAC.LabelMemory memory -> {
      if (memory.offset() >= 0) {
        return String.format("%s [%s + %d]", ptrString, memory.label(), memory.offset());
      }
      return String.format("%s [%s - %d]", ptrString, memory.label(), memory.offset());
    }
    case TAC.OffsetMemory memory -> {
      StringBuilder builder = new StringBuilder();
      builder.append(ptrString);
      builder.append(" [");
      // Base
      if (memory.base() != null) {
        // Base
        if (memory.base().size() != TAC.Size.QWORD) {
          throw new IllegalStateException("Base register must be 8 bytes");
        }
        builder.append(memory.base().name());

        // + or -
        if (memory.index() != null) {
          if (memory.scale() > 0) {
            builder.append(" + ");
          }
          else {
            builder.append(" - ");
          }
        }
      }

      // Index
      if (memory.index() != null) {
        // Check scale
        switch (memory.scale()) {
        case 1, 2, 4, 8 -> {}
        default -> throw new IllegalStateException("Scale must be 1, 2, 4 or 8");
        }

        builder.append(memory.index().name());
        builder.append(" * ");
        builder.append(memory.scale());
      }

      // Offset
      if (memory.base() != null || memory.index() != null) {
        if (memory.offset() >= 0) {
          builder.append(" + ");
          builder.append(memory.offset());
        }
        else {
          builder.append(" - ");
          builder.append(Math.abs(memory.offset()));
        }
      }
      else {
        builder.append(memory.offset());
      }

      builder.append("]");

      return builder.toString();
    }
    default -> throw new IllegalStateException("Unexpected operand: " + operand);
    }
  }

  // --- Move ---

  public static String MoveToRegister(TAC.Register dst, TAC.Memory src, boolean isSigned) {
    // Depending on the operand
    TAC.Size dstSize = dst.size();
    TAC.Size srcSize = switch (src) {
      case TAC.OffsetMemory memory -> memory.size();
      case TAC.LabelMemory memory -> memory.size();
    };

    boolean isPromotion = dstSize.size > srcSize.size;
    TAC.Size lowerSize = isPromotion ? srcSize : dstSize;

    String srcName = GetAddressString(lowerSize, src);
    String dstName = dst.name();

    // Copy and shrinking
    if (!isPromotion) {
      return String.format("    mov     %s, %s", dstName, srcName);
    }

    // Promotion
    if (isSigned) {
      if (srcSize == TAC.Size.DWORD) {
        return String.format("    movsxd  %s, %s", dstName, srcName);
      }

      return String.format("    movsx   %s, %s", dstName, srcName);
    }

    if (srcSize == TAC.Size.DWORD) {
      return String.format("    mov     %s, %s", TAC.Register.GetRegister(dst.type(), TAC.Size.DWORD).name(), srcName);
    }

    return String.format("    movzx   %s, %s", dstName, srcName);
  }

  public static String MoveToRegister(TAC.Register dst, TAC.Register src, boolean isSigned) {
    // Depending on the operand
    TAC.Size dstSize = dst.size();
    TAC.Size srcSize = src.size();

    boolean isPromotion = dstSize.size > srcSize.size;
    TAC.Size lowerSize = isPromotion ? srcSize : dstSize;

    String srcName = src.name();
    String dstName = dst.name();

    // Copy and shrinking
    if (!isPromotion) {
      return String.format("    mov     %s, %s", dstName, TAC.Register.GetRegister(src.type(), lowerSize).name());
    }

    // Promotion
    if (isSigned) {
      if (srcSize == TAC.Size.DWORD) {
        return String.format("    movsxd  %s, %s", dstName, srcName);
      }

      return String.format("    movsx   %s, %s", dstName, srcName);
    }

    if (srcSize == TAC.Size.DWORD) {
      return String.format("    mov     %s, %s", TAC.Register.GetRegister(dst.type(), lowerSize).name(), srcName);
    }

    return String.format("    movzx   %s, %s", dstName, srcName);
  }

  public static String MoveToRegister(TAC.Register dst, TAC.Constant src) {
    // TODO some size checks probably
    return String.format("    mov     %s, %s", dst.name(), src.value());
  }

  public static String MoveToMemory(TAC.Memory dst, TAC.Register src) {
    // Depending on the operand
    TAC.Size dstSize = switch (dst) {
      case TAC.OffsetMemory memory -> memory.size();
      case TAC.LabelMemory memory -> memory.size();
    };
    TAC.Size srcSize = src.size();

    boolean isPromotion = dstSize.size > srcSize.size;
    TAC.Size lowerSize = isPromotion ? srcSize : dstSize;
    if (isPromotion) {
      throw new IllegalStateException("Cannot promote to memory");
    }

    String dstName = GetAddressString(lowerSize, dst);
    String srcName = TAC.Register.GetRegister(src.type(), lowerSize).name();

    // Copy and shrinking
    return String.format("    mov     %s, %s", dstName, srcName);
  }
}
