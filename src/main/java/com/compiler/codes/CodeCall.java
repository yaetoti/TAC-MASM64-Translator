package com.compiler.codes;

import com.compiler.symbols.IFunction;
import com.compiler.symbols.IVariable;

public record CodeCall(IFunction function, IVariable[] params, IVariable[] returnValues) implements ICode {}
