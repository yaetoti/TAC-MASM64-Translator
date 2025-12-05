package com.compiler.symbols;

import com.compiler.structure.File;
import com.compiler.types.IDataType;
import com.compiler.structure.Module;

//record SymbolVariable() implements ISymbol {}
// Global: Defined in file, access from anywhere
// Static: Defined in file, access from file
public final class SymbolGlobalVariable implements IVariable {
  public long id;
  public File file;
  public Module module;
  public boolean isStatic;
  public boolean isExternal;
  public String name;
  public IDataType type;
  public IConstant constant;

  SymbolGlobalVariable(long id, File file, Module module, boolean isStatic, boolean isExternal, String name, IDataType type, IConstant constant) {
    this.id = id;
    this.file = file;
    this.module = module;
    this.isStatic = isStatic;
    this.isExternal = isExternal;
    this.name = name;
    this.type = type;
    this.constant = constant;
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
  public int hashCode() {
    return Long.hashCode(id);
  }

  @Override
  public IDataType GetDataType() {
    return type;
  }
}
