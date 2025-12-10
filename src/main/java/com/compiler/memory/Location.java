package com.compiler.memory;

public sealed interface Location permits Memory, Register {
  int GetSize();
}
