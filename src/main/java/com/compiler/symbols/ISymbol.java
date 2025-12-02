package com.compiler.symbols;

sealed public interface ISymbol permits SymbolGlobalFunction, SymbolGlobalVariable, SymbolLocalVariable {
  long GetId();
  String GetName();
}
