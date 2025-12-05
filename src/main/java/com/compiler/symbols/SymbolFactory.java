package com.compiler.symbols;

import com.compiler.*;
import com.compiler.structure.File;
import com.compiler.structure.Module;
import com.compiler.types.IDataType;

import java.util.ArrayList;
import java.util.HashMap;

public final class SymbolFactory {
  private long nextId;
  private final HashMap<Long, ISymbol> symbols = new HashMap<>();

  public SymbolGlobalVariable CreateSymbolGlobalVariable(File file, Module module, boolean isStatic, boolean isExternal, String name, IDataType dataType, IConstant constant) {
    long id = nextId++;
    var symbol = new SymbolGlobalVariable(id, file, module, isStatic, isExternal, name, dataType, constant);
    symbols.put(id, symbol);
    return symbol;
  }

  public SymbolGlobalFunction CreateSymbolGlobalFunction(File file, Module module, boolean isExternal, FunctionDeclaration declaration, ArrayList<SymbolLocalVariable> locals) {
    long id = nextId++;
    var symbol = new SymbolGlobalFunction(id, file, module, isExternal, declaration, locals);
    symbols.put(id, symbol);
    return symbol;
  }

  public SymbolLocalVariable CreateSymbolLocalVariable(SymbolGlobalFunction function, String name, IDataType type) {
    long id = nextId++;
    var symbol = new SymbolLocalVariable(id, function, name, type);
    symbols.put(id, symbol);
    return symbol;
  }

  public ISymbol GetSymbol(Long id) {
    return symbols.get(id);
  }
}
