package com.compiler.translator.masm.context;

import com.compiler.translator.masm.memory.Register;
import com.compiler.ir.symbols.IVariable;

public class RegisterInfo {
  public final Register.Type type;
  public IVariable symbol;
  public boolean isLocked;
  //public long accessTime;

  public RegisterInfo(Register.Type type) {
    this.type = type;
  }

  public Register GetRegister(int size) {
    return Register.Get(type, size);
  }

  public Register.Type GetType() {
    return type;
  }

  public IVariable GetSymbol() {
    return symbol;
  }

  public void SetSymbol(IVariable symbol) {
    this.symbol = symbol;
  }

  public void Lock() {
    if (isLocked) {
      throw new RuntimeException("Register is already locked");
    }

    isLocked = true;
  }

  public void Unlock() {
    if (!isLocked) {
      throw new RuntimeException("Register is already unlocked");
    }

    isLocked = false;
  }

  public boolean IsLocked() {
    return isLocked;
  }

  public boolean IsOccupied() {
    return isLocked || symbol != null;
  }

  public boolean IsFree() {
    return !isLocked && symbol == null;
  }

  public boolean IsSpillable() {
    return !isLocked && symbol != null;
  }
}
