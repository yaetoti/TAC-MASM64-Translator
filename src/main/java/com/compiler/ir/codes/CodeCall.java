package com.compiler.ir.codes;

import com.compiler.ir.symbols.IFunction;
import com.compiler.ir.symbols.IVariable;

public record CodeCall(IFunction function, IVariable[] params, IVariable[] returnValues) implements ICode {}
