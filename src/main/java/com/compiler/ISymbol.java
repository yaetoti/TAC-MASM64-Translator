package com.compiler;

import java.util.ArrayList;
import java.util.List;

sealed public interface ISymbol {
  String GetName();
}

//record SymbolVariable() implements ISymbol {}
// Global: Defined in file, access from anywhere
// Static: Defined in file, access from file
record SymbolGlobalVariable(File file, Module module, boolean isStatic, boolean isExternal, String name, IDataType dataType, IConstant constant) implements ISymbol {
  @Override
  public String GetName() {
    return name;
  }
}

// TODO add code
// TODO add variables
record SymbolGlobalFunction(File file, Module module, boolean isExternal, FunctionDeclaration declaration, ArrayList<SymbolLocalVariable> locals) implements ISymbol {
  @Override
  public String GetName() {
    return declaration.name();
  }
}

// TODO all functions, not just global
// TODO static variables
// Local variables
record SymbolLocalVariable(SymbolGlobalFunction function, String name, IDataType type) implements ISymbol {
  @Override
  public String GetName() {
    return name;
  }
}
