package com.compiler.ir.symbols;

sealed public interface IOperand permits IConstant, ISymbol {}
