package com.compiler;

import com.compiler.symbols.ISymbol;
import com.compiler.types.IDataType;

public record FunctionDeclaration(String name, CallingConvention convention, ISymbol[] parameters, IDataType[] returnTypes) {}
