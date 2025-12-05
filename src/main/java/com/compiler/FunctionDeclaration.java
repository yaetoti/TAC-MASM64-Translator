package com.compiler;

import com.compiler.symbols.ISymbol;
import com.compiler.symbols.IVariable;
import com.compiler.types.IDataType;

public record FunctionDeclaration(String name, CallingConvention convention, IVariable[] parameters, IDataType[] returnTypes) {}
