package com.compiler.symbols;

import com.compiler.types.IDataType;

public sealed interface IVariable extends ISymbol permits SymbolGlobalVariable, SymbolLocalVariable {
  IDataType GetDataType();
}
