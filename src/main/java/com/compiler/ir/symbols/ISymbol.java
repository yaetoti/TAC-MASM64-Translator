package com.compiler.ir.symbols;

sealed public interface ISymbol extends IOperand permits IFunction, IVariable {
  long GetId();
  String GetName();
}
