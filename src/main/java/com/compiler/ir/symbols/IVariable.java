package com.compiler.ir.symbols;

import com.compiler.ir.types.IDataType;

public sealed interface IVariable extends ISymbol permits SymbolGlobalVariable, SymbolLocalVariable, SymbolParameter {
  IDataType GetDataType();
}

