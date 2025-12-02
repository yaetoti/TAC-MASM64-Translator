package com.compiler;

import com.compiler.symbols.ISymbol;

public class RegisterInfo {
  public final Register.Type type;
  public ISymbol symbol;
  public boolean isLocked;
  public long accessTime;

  public RegisterInfo(Register.Type type) {
    this.type = type;
  }
}
