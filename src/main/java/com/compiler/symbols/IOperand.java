package com.compiler.symbols;

sealed public interface IOperand permits IConstant, ISymbol {}
