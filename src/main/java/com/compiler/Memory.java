package com.compiler;

// TODO what about addressing arrays?
public sealed interface Memory extends Location permits LabelMemory, OffsetMemory {
  int GetSize();
}
