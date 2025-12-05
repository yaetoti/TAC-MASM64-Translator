package com.compiler.codes;

import com.compiler.symbols.IVariable;

public record CodeReturn(IVariable[] returnValues) implements ICode {}
