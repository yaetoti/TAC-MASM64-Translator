package com.compiler.symbols;

import com.compiler.types.IDataType;

public final class SymbolParameter implements IVariable {
  public long id;
  public String name;
  public IDataType type;

  SymbolParameter(long id, String name, IDataType type) {
    this.id = id;
    this.name = name;
    this.type = type;
  }

  @Override
  public IDataType GetDataType() {
    return type;
  }

  @Override
  public long GetId() {
    return id;
  }

  @Override
  public String GetName() {
    return name;
  }
}
