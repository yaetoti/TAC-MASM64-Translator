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

  // Return existing register or allocate a new one for writing (without moving from memory to register)
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

  // Return existing register or allocate a new one for reading (with moving data from memory to register)
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

  // Move the symbols to a specific register for reading (it must be free)
  // TODO here we must spill occupied register
  public Register EnsureInRegister(IVariable symbol, Register.Type regType) {
    if (MasmTypeUtils.GetStorageClass(symbol.GetDataType()) == MasmStorageClass.MEMORY) {
      throw new RuntimeException("Cannot allocate a register for memory variable");
    }

    var location = memoryManager.Get(symbol);
    var regInfo = registerManager.Get(regType);

    // Already in the register
    if (location.register != null && location.register.type() == regType) {
      return location.register;
    }

    // Cannot allocate locked register
    if (regInfo.isLocked) {
      throw new RuntimeException("Cannot allocate a locked register");
    }

    // If the register is occupied - spill
    if (regInfo.symbol != null) {
      Flush(regInfo.symbol);
    }

    // Move to the new register
    var oldReg = location.register;
    var oldLocation = oldReg == null ? location.memory : oldReg;
    var newReg = Register.Get(regInfo.type, symbol.GetDataType().GetSize());

    // Move to the new location
    out.EmitF("mov %s, %s", newReg, oldLocation);
    location.register = newReg;
    regInfo.symbol = symbol;

    // If the symbol is already in a register - make if free
    if (oldReg != null) {
      var oldRegInfo = registerManager.Get(oldReg.type());
      oldRegInfo.symbol = null;
    }

    return location.register;
  }

  public void Flush(IVariable symbol) {
    var location = memoryManager.Get(symbol);
    // Already spilled
    if (location.register == null) {
      return;
    }

    var regInfo = registerManager.Get(location.register.type());
    if (regInfo.isLocked) {
      throw new RuntimeException("Cannot spill locked register");
    }

    if (location.memory == null) {
      throw new RuntimeException("Cannot spill register without memory location");
    }

    // Spill
    out.EmitF("mov %s, %s", location.memory, location.register);
    location.register = null;
    location.isDirty = false;
    regInfo.symbol = null;
  }

  public void MarkDirty(IVariable symbol) {
    var location = memoryManager.Get(symbol);
    if (location.register == null) {
      throw new RuntimeException("There is no register to mark");
    }

    location.isDirty = true;
  }
}
