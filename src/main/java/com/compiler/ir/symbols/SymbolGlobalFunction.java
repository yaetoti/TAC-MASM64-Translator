package com.compiler.ir.symbols;

import com.compiler.ir.structure.File;
import com.compiler.ir.FunctionDeclaration;
import com.compiler.ir.codes.ICode;
import com.compiler.ir.structure.Module;

import java.util.ArrayList;

// TODO add variables
public final class SymbolGlobalFunction implements IFunction {
  public long id;
  public File file;
  public Module module;
  public boolean isExternal;
  public FunctionDeclaration declaration;
  public ArrayList<SymbolLocalVariable> locals;
  public ArrayList<ICode> codes = new ArrayList<>();

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

  @Override
  public FunctionDeclaration GetDeclaration() {
    return declaration;
  }
}
