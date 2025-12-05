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
}
