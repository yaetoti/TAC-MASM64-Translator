package com.compiler;

import com.compiler.symbols.IOperand;
import com.compiler.symbols.IVariable;

// Assignable:
// - Constant
// - Array
// - String
// - Other symbol

sealed public interface ICode {}
record CodeAssign(IVariable dst, IOperand src) implements ICode {}
