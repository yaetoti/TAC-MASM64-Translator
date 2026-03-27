package com.compiler.translator.masm.memory;

public sealed interface Memory extends Location permits LabelMemory, OffsetMemory {
  Memory Offset(int offset);
}
