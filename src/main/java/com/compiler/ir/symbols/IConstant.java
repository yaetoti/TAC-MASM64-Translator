package com.compiler.ir.symbols;

// Constants. Are not symbols
sealed public interface IConstant extends IOperand permits FloatConstant, IntegerConstant, PointerConstant {}

