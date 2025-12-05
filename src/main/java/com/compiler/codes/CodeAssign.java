package com.compiler.codes;

import com.compiler.symbols.IOperand;
import com.compiler.symbols.IVariable;

public record CodeAssign(IVariable dst, IOperand src) implements ICode {}
