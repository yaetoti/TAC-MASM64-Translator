package com.compiler;

import com.compiler.memory.MasmStorageClass;
import com.compiler.memory.Register;
import com.compiler.symbols.IVariable;
import com.compiler.symbols.SymbolGlobalFunction;
import com.compiler.utils.CodeEmitter;
import com.compiler.utils.MasmTypeUtils;

public class FunctionContext {
  public CodeEmitter out;
  public SymbolGlobalFunction function;
  public RegisterManager registerManager;
  public FunctionMemoryManager memoryManager;
  private int stackSize;

  public void IncreaseStackSize(int amount) {
    if (amount <= 0) {
      throw new RuntimeException("Stack size cannot be negative");
    }

    stackSize += amount;
  }

  public void DecreaseStackSize(int amount) {
    if (amount <= 0) {
      throw new RuntimeException("Stack size cannot be negative");
    }

    stackSize -= amount;
    if (stackSize < 0) {
      throw new RuntimeException("Stack size cannot be negative");
    }
  }

  public int GetStackSize() {
    return stackSize;
  }

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
