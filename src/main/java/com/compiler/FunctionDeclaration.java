package com.compiler;

public record FunctionDeclaration(String name, CallingConvention convention, ISymbol[] parameters, IDataType[] returnTypes) {}
