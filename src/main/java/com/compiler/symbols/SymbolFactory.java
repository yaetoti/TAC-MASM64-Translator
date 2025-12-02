package com.compiler.symbols;

import com.compiler.*;
import com.compiler.Module;

import java.util.ArrayList;

public final class SymbolFactory {
  private long symbolId;

  public SymbolGlobalVariable CreateSymbolGlobalVariable(File file, Module module, boolean isStatic, boolean isExternal, String name, IDataType dataType, IConstant constant) {
    long id = symbolId++;
    return new SymbolGlobalVariable(id, file, module, isStatic, isExternal, name, dataType, constant);
  }

  public SymbolGlobalFunction CreateSymbolGlobalFunction(File file, Module module, boolean isExternal, FunctionDeclaration declaration, ArrayList<SymbolLocalVariable> locals) {
    long id = symbolId++;
    return new SymbolGlobalFunction(id, file, module, isExternal, declaration, locals);
  }

  public SymbolLocalVariable CreateSymbolLocalVariable(SymbolGlobalFunction function, String name, IDataType type) {
    long id = symbolId++;
    return new SymbolLocalVariable(id, function, name, type);
  }
}
