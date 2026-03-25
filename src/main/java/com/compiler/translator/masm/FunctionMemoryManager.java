package com.compiler.translator.masm;

import com.compiler.translator.masm.memory.Memory;
import com.compiler.ir.symbols.IVariable;

import java.util.HashMap;

public class FunctionMemoryManager {
  public HashMap<IVariable, SymbolLocation> locations = new HashMap<>();

  public SymbolLocation Get(IVariable symbol) {
    return locations.get(symbol);
  }

  public SymbolLocation Set(IVariable symbol, SymbolLocation location) {
    return locations.put(symbol, location);
  }

  public Memory GetMemoryLocation(IVariable symbol) {
    var location = Get(symbol);
    if (location == null) {
      return null;
    }

    return location.memory;
  }
}
