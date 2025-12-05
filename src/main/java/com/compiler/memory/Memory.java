package com.compiler.memory;

// TODO what about addressing arrays?
public sealed interface Memory extends Location permits LabelMemory, OffsetMemory {
  int GetSize();
}
