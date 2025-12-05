package com.compiler.memory;

public sealed interface Location permits Memory, Register {}

// rip-relative mapping. only 32 or 8 bit offset

