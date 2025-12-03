package com.compiler.symbols;

import com.compiler.IDataType;

// TODO all functions, not just global
// TODO static variables
// Local variables
public final class SymbolLocalVariable implements IVariable {
  public long id;
  public SymbolGlobalFunction function;
  public String name;
  public IDataType type;

  SymbolLocalVariable(long id, SymbolGlobalFunction function, String name, IDataType type) {
    this.id = id;
    this.function = function;
    this.name = name;
    this.type = type;
  }

  @Override
  public long GetId() {
    return id;
  }

  @Override
  public String GetName() {
    return name;
  }

  @Override
  public IDataType GetDataType() {
    return type;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(id);
  }
}
