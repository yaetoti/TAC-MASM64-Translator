package com.compiler;

public class SymbolLocation {
  public Memory memory;
  public Register.Type register;
  public boolean isDirty; // If both locations present, but data in register is newer

  public SymbolLocation(Memory memory) {
    this.memory = memory;
  }

  public SymbolLocation(Register.Type register) {
    this.register = register;
  }

  public SymbolLocation(Memory memory, Register.Type register) {
    this.memory = memory;
    this.register = register;
  }

  public SymbolLocation(Memory memory, Register.Type register, boolean isDirty) {
    this.memory = memory;
    this.register = register;
    this.isDirty = isDirty;
  }
}
