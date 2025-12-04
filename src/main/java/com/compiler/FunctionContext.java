package com.compiler;

import com.compiler.symbols.IVariable;
import com.compiler.symbols.SymbolGlobalFunction;

public class FunctionContext {
  public CodeEmitter out;
  public SymbolGlobalFunction function;
  public RegisterManager registerManager;
  public FunctionMemoryManager memoryManager;

  public Register DefineRegister(IVariable symbol) {
    if (MasmTypeUtils.GetStorageClass(symbol.GetDataType()) == MasmStorageClass.MEMORY) {
      throw new RuntimeException("Cannot define register for memory variable");
    }

    var location = memoryManager.Get(symbol);

    if (location.register == null) {
      var regInfo = registerManager.GetFreeRegister();
      regInfo.symbol = symbol;
      location.register = Register.Get(regInfo.type, symbol.GetDataType().GetSize());
    }

    return location.register;
  }

  public Register EnsureInRegister(IVariable symbol) {
    if (MasmTypeUtils.GetStorageClass(symbol.GetDataType()) == MasmStorageClass.MEMORY) {
      throw new RuntimeException("Cannot define register for memory variable");
    }

    var location = memoryManager.Get(symbol);

    if (location.register == null) {
      var regInfo = registerManager.GetFreeRegister();
      regInfo.symbol = symbol;
      location.register = Register.Get(regInfo.type, symbol.GetDataType().GetSize());

      // Load into register
      out.EmitF("mov %s, %s", location.register, location.memory);
    }

    return location.register;
  }

  public void MarkDirty(IVariable symbol) {
    var location = memoryManager.Get(symbol);
    if (location.register == null) {
      throw new RuntimeException("There is no register to mark");
    }

    location.isDirty = true;
  }
}
