package com.compiler.ir;

import com.compiler.ir.symbols.IVariable;
import com.compiler.ir.types.IDataType;

public record FunctionDeclaration(String name, CallingConvention convention, IVariable[] parameters, IDataType[] returnTypes) {}
