package com.compiler.ir.codes;

import com.compiler.ir.symbols.IOperand;
import com.compiler.ir.symbols.IVariable;

public record CodeAssignArrayElement(IVariable basePointer, IOperand index, IOperand value) implements ICode {}
