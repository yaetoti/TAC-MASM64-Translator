package com.compiler.symbols;

sealed public interface ISymbol extends IOperand permits IVariable, SymbolGlobalFunction {
  long GetId();
  String GetName();
}
