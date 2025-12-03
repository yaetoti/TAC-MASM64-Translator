package com.compiler;

import com.compiler.symbols.SymbolGlobalFunction;

public class FunctionContext {
  public CodeEmitter out;
  public SymbolGlobalFunction function;
  public RegisterManager registerManager;
  public FunctionMemoryManager memoryManager;
}
