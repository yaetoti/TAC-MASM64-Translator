package com.compiler.translator.masm.memory;

import com.compiler.translator.masm.utils.MasmStringUtils;

public record LabelMemory(int size, String label, int offset) implements Memory {
  @Override
  public String toString() {
    var sb = new StringBuilder();

    var typeString = MasmStringUtils.GetTypeString(size);
    if (typeString != null) {
      sb.append(typeString);
      sb.append(" ptr ");
    }

    sb.append(offset() >= 0
      ? String.format("[%s + %d]", label, offset)
      : String.format("[%s - %d]", label, offset)
    );

    return sb.toString();
  }

  @Override
  public int GetSize() {
    return size;
  }

  @Override
  public LabelMemory Offset(int offset) {
    return new LabelMemory(size, label, this.offset + offset);
  }
}
