package com.compiler.ir.codes;

import com.compiler.ir.symbols.IVariable;

public record CodeLoadAddress(IVariable dst, IVariable src) implements ICode {}
