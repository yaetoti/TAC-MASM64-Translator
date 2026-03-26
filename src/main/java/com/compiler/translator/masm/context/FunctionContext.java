package com.compiler.translator.masm.context;

import com.compiler.translator.masm.memory.MasmStorageClass;
import com.compiler.translator.masm.memory.Register;
import com.compiler.ir.symbols.IVariable;
import com.compiler.ir.symbols.SymbolGlobalFunction;
import com.compiler.translator.masm.utils.CodeEmitter;
import com.compiler.translator.masm.utils.MasmTypeUtils;

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

  // Return an existing register or allocate a new one for writing (without moving from memory to register)
  public Register DefineRegister(IVariable symbol) {
    assert MasmTypeUtils.GetStorageClass(symbol.GetDataType()) != MasmStorageClass.MEMORY : "Cannot define register for memory variable";

    var location = memoryManager.Get(symbol);

    if (location.register == null) {
      var regInfo = registerManager.GetFreeRegister(MasmTypeUtils.GetBank(symbol.GetDataType()));
      regInfo.symbol = symbol;
      location.register = MasmTypeUtils.GetRegister(regInfo.type, symbol.GetDataType());
    }

    return location.register;
  }

  // Return existing register or allocate a new one for reading (with moving data from memory to register)
  public Register EnsureInRegister(IVariable symbol) {
    assert MasmTypeUtils.GetStorageClass(symbol.GetDataType()) != MasmStorageClass.MEMORY : "Cannot define register for memory variable";

    var location = memoryManager.Get(symbol);

    if (location.register == null) {
      var reg = DefineRegister(symbol);
      // TODO replace move
      out.EmitF("mov %s, %s", reg, location.memory);
    }

    return location.register;
  }

  // Move the symbols to a specific register for reading (it must be free)
  public Register EnsureInRegister(IVariable symbol, Register.Type regType) {
    assert MasmTypeUtils.GetStorageClass(symbol.GetDataType()) != MasmStorageClass.MEMORY : "Cannot define register for memory variable";
    assert MasmTypeUtils.GetBank(symbol.GetDataType()) == regType.GetBank() : "Cannot move to a different register bank";

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

    // If the register is occupied - flush
    if (regInfo.symbol != null) {
      Flush(regInfo.symbol);
    }

    // Move to the new register
    var oldReg = location.register;
    var oldLocation = oldReg == null ? location.memory : oldReg;
    var newReg = Register.Get(regInfo.type, symbol.GetDataType().GetSize());

    // Move to the new location
    // TODO replace move
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

    // TODO depends on register
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
