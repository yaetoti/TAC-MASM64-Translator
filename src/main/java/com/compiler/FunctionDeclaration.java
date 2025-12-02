package com.compiler;

import com.compiler.symbols.ISymbol;

public record FunctionDeclaration(String name, CallingConvention convention, ISymbol[] parameters, IDataType[] returnTypes) {}
