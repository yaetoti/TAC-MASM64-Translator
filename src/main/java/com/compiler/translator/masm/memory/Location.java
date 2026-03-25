package com.compiler.translator.masm.memory;

public sealed interface Location permits Memory, Register {
  int GetSize();
}
