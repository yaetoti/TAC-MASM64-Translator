package com.yaetoti;

public class Codegen {
  public static String GetPointerString(MASM.Type type) {
    return switch (type) {
      case BYTE -> "byte ptr";
      case WORD -> "word ptr";
      case DWORD -> "dword ptr";
      case QWORD -> "qword ptr";
    };
  }

  public static String GetAddressString(MASM.Type type, MASM.Operand operand) {
    String ptrString = GetPointerString(type);
    switch (operand) {
    case MASM.LabelMemory memory -> {
      if (memory.offset() >= 0) {
        return String.format("%s [%s + %d]", ptrString, memory.label(), memory.offset());
      }
      return String.format("%s [%s - %d]", ptrString, memory.label(), memory.offset());
    }
    case MASM.OffsetMemory memory -> {
      StringBuilder builder = new StringBuilder();
      builder.append(ptrString);
      builder.append(" [");
      // Base
      if (memory.base() != null) {
        // Base
        if (memory.base().type() != MASM.Type.QWORD) {
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
    // TODO can we even take address of register?
    default -> throw new IllegalStateException("Unexpected operand: " + operand);
    }
  }

  // --- Move ---

  public static String MoveToRegister(MASM.Register dst, MASM.Memory src, boolean isSigned) {
    // Depending on the operand
    MASM.Type dstSize = dst.type();
    MASM.Type srcSize = switch (src) {
      case MASM.OffsetMemory memory -> memory.size();
      case MASM.LabelMemory memory -> memory.size();
    };

    boolean isPromotion = dstSize.size > srcSize.size;
    MASM.Type lowerSize = isPromotion ? srcSize : dstSize;

    String srcName = GetAddressString(lowerSize, src);
    String dstName = dst.name();

    // Copy and shrinking
    if (!isPromotion) {
      return String.format("    mov     %s, %s", dstName, srcName);
    }

    // Promotion
    if (isSigned) {
      if (srcSize == MASM.Type.DWORD) {
        return String.format("    movsxd  %s, %s", dstName, srcName);
      }

      return String.format("    movsx   %s, %s", dstName, srcName);
    }

    if (srcSize == MASM.Type.DWORD) {
      return String.format("    mov     %s, %s", MASM.Register.GetRegister(dst.registerType(), MASM.Type.DWORD).name(), srcName);
    }

    return String.format("    movzx   %s, %s", dstName, srcName);
  }

  public static String MoveToRegister(MASM.Register dst, MASM.Register src, boolean isSigned) {
    // Depending on the operand
    MASM.Type dstSize = dst.type();
    MASM.Type srcSize = src.type();

    boolean isPromotion = dstSize.size > srcSize.size;
    MASM.Type lowerSize = isPromotion ? srcSize : dstSize;

    String srcName = src.name();
    String dstName = dst.name();

    // Copy and shrinking
    if (!isPromotion) {
      return String.format("    mov     %s, %s", dstName, MASM.Register.GetRegister(src.registerType(), lowerSize).name());
    }

    // Promotion
    if (isSigned) {
      if (srcSize == MASM.Type.DWORD) {
        return String.format("    movsxd  %s, %s", dstName, srcName);
      }

      return String.format("    movsx   %s, %s", dstName, srcName);
    }

    if (srcSize == MASM.Type.DWORD) {
      return String.format("    mov     %s, %s", MASM.Register.GetRegister(dst.registerType(), lowerSize).name(), srcName);
    }

    return String.format("    movzx   %s, %s", dstName, srcName);
  }

  public static String MoveToRegister(MASM.Register dst, TAC.Constant src) {
    // TODO some size checks probably.
    // TODO Replace with masm immediate
    return String.format("    mov     %s, %s", dst.name(), src.value());
  }

  public static String MoveToRegister(MASM.Register dst, MASM.Immediate src) {
    // TODO some size checks probably.
    // TODO Replace with masm immediate
    return String.format("    mov     %s, %s", dst.name(), src.value());
  }

  public static String MoveToMemory(MASM.Memory dst, MASM.Register src) {
    // Depending on the operand
    MASM.Type dstSize = switch (dst) {
      case MASM.OffsetMemory memory -> memory.size();
      case MASM.LabelMemory memory -> memory.size();
    };
    MASM.Type srcSize = src.type();

    boolean isPromotion = dstSize.size > srcSize.size;
    MASM.Type lowerSize = isPromotion ? srcSize : dstSize;
    if (isPromotion) {
      throw new IllegalStateException("Cannot promote to memory");
    }

    String dstName = GetAddressString(lowerSize, dst);
    String srcName = MASM.Register.GetRegister(src.registerType(), lowerSize).name();

    // Copy and shrinking
    return String.format("    mov     %s, %s", dstName, srcName);
  }
}
