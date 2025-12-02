package com.compiler.symbols;

import com.compiler.File;
import com.compiler.FunctionDeclaration;
import com.compiler.Module;

import java.util.ArrayList;
import java.util.Objects;

// TODO add code
// TODO add variables
public final class SymbolGlobalFunction implements ISymbol {
  public long id;
  public File file;
  public com.compiler.Module module;
  public boolean isExternal;
  public FunctionDeclaration declaration;
  public ArrayList<SymbolLocalVariable> locals;

  SymbolGlobalFunction(long id, File file, Module module, boolean isExternal, FunctionDeclaration declaration, ArrayList<SymbolLocalVariable> locals) {
    this.id = id;
    this.file = file;
    this.module = module;
    this.isExternal = isExternal;
    this.declaration = declaration;
    this.locals = locals;
  }

  @Override
  public long GetId() {
    return id;
  }

  @Override
  public String GetName() {
    return declaration.name();
  }

  @Override
  public int hashCode() {
    return Long.hashCode(id);
  }
}
