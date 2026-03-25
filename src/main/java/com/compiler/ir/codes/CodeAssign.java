package com.compiler.ir.codes;

import com.compiler.ir.symbols.IOperand;
import com.compiler.ir.symbols.IVariable;

public record CodeAssign(IVariable dst, IOperand src) implements ICode {}
