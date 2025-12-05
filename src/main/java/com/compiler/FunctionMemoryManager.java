package com.compiler;

import com.compiler.symbols.ISymbol;
import com.compiler.symbols.IVariable;

import java.util.HashMap;

public class FunctionMemoryManager {
  public HashMap<IVariable, SymbolLocation> locations = new HashMap<>();

  public SymbolLocation Get(IVariable symbol) {
    return locations.get(symbol);
  }

  public SymbolLocation Set(IVariable symbol, SymbolLocation location) {
    return locations.put(symbol, location);
  }
}
