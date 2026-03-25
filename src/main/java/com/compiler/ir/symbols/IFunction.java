package com.compiler.ir.symbols;

import com.compiler.ir.FunctionDeclaration;

public sealed interface IFunction extends ISymbol permits SymbolGlobalFunction {
  FunctionDeclaration GetDeclaration();
}
