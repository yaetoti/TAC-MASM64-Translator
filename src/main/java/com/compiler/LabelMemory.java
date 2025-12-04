package com.compiler;

// TODO What masm type, bruh, we use it for everything. nullable type?
public record LabelMemory(MasmType masmType, String label, int offset) implements Memory {
  @Override
  public String toString() {
    if (offset() >= 0) {
      return String.format("%s [%s + %d]", masmType.GetPointerString(), label, offset);
    }
    return String.format("%s [%s - %d]", masmType.GetPointerString(), label, offset);
  }
}
