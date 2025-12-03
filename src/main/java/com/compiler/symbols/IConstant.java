package com.compiler.symbols;

// Constants. Are not symbols
sealed public interface IConstant extends IOperand permits IntegerConstant, PointerConstant {}

