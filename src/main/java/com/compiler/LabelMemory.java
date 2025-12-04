package com.compiler;

public record LabelMemory(int size, String label, int offset) implements Memory {
  @Override
  public String toString() {
    return offset() >= 0
      ? String.format("%s ptr [%s + %d]", MasmStringUtils.GetTypeString(size), label, offset)
      : String.format("%s ptr [%s - %d]", MasmStringUtils.GetTypeString(size), label, offset);
  }

  @Override
  public int GetSize() {
    return size;
  }
}
