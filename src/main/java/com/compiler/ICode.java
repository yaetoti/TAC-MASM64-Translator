package com.compiler;

import com.compiler.symbols.IOperand;
import com.compiler.symbols.ISymbol;

// Assignable:
// - Constant
// - Array
// - String
// - Other symbol

sealed public interface ICode {}
record CodeAssign(ISymbol dst, IOperand src) implements ICode {}
