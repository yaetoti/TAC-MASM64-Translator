package com.compiler.ir.codes;

import com.compiler.ir.symbols.IVariable;

public record CodeReturn(IVariable[] returnValues) implements ICode {}
