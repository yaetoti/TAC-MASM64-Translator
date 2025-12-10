package com.compiler.memory;

public sealed interface Memory extends Location permits LabelMemory, OffsetMemory { }
