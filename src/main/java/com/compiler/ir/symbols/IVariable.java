package com.compiler.ir.symbols;

import com.compiler.ir.types.IDataType;

public sealed interface IVariable extends ISymbol permits SymbolGlobalVariable, SymbolLocalVariable, SymbolParameter {
  IDataType GetDataType();
  default <T> T GetDataTypeAs(Class<T> clazz) {
    return clazz.cast(GetDataType());
  }
}

