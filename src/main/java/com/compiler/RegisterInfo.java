package com.compiler;

import com.compiler.memory.Register;
import com.compiler.symbols.ISymbol;
import com.compiler.symbols.IVariable;

public class RegisterInfo {
  public final Register.Type type;
  public IVariable symbol;
  public boolean isLocked;
  //public long accessTime;

  public RegisterInfo(Register.Type type) {
    this.type = type;
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
    assert !isLocked : "Register was already locked";
    isLocked = true;
  }

  public void Unlock() {
    assert isLocked : "Register was already unlocked";
    isLocked = false;
  }

  public boolean IsLocked() {
    return isLocked;
  }

  public boolean IsOccupied() {
    return isLocked || symbol != null;
  }
}
