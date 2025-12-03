package com.compiler;

import com.compiler.symbols.ISymbol;

import java.util.HashMap;

public class FunctionMemoryManager {
  public HashMap<ISymbol, SymbolLocation> locations = new HashMap<>();
}
