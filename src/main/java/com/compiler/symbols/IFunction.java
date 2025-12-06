package com.compiler.symbols;

import com.compiler.FunctionDeclaration;

public sealed interface IFunction extends ISymbol permits SymbolGlobalFunction {
  FunctionDeclaration GetDeclaration();
}
