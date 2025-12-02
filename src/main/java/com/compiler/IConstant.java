package com.compiler;

import com.compiler.symbols.ISymbol;

// Constants. Are not symbols
sealed public interface IConstant {}
// TODO type?
record IntegerConstant(String value) implements IConstant {}
record PointerConstant(ISymbol symbol) implements IConstant {}