package com.yaetoti;

import java.util.ArrayList;

public class GlobalSymbolTable {
  // TODO usage
  // TODO add used symbols to a function
  public enum SymbolUsage {
    LOCAL,
    PARAMETER,
    STATIC,
    GLOBAL
  }
  public record Symbol(int id, String name, DataType type) {}

  private final ArrayList<Symbol> m_symbols = new ArrayList<>();

  public GlobalSymbolTable() {
  }

  public Symbol AddSymbol(String name, DataType type) {
    var symbol = new Symbol(m_symbols.size(), name, type);
    m_symbols.add(symbol);
    return symbol;
  }

  public Symbol GetSymbol(int id) {
    return m_symbols.get(id);
  }
}
