package com.compiler.symbols;

import com.compiler.File;
import com.compiler.IConstant;
import com.compiler.IDataType;
import com.compiler.Module;

//record SymbolVariable() implements ISymbol {}
// Global: Defined in file, access from anywhere
// Static: Defined in file, access from file
public final class SymbolGlobalVariable implements ISymbol {
  public long id;
  public File file;
  public com.compiler.Module module;
  public boolean isStatic;
  public boolean isExternal;
  public String name;
  public IDataType dataType;
  public IConstant constant;

  SymbolGlobalVariable(long id, File file, Module module, boolean isStatic, boolean isExternal, String name, IDataType dataType, IConstant constant) {
    this.id = id;
    this.file = file;
    this.module = module;
    this.isStatic = isStatic;
    this.isExternal = isExternal;
    this.name = name;
    this.dataType = dataType;
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
}
