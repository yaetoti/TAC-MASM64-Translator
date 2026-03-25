package com.compiler.translator.masm;

import com.compiler.translator.masm.memory.Memory;
import com.compiler.translator.masm.memory.Register;

public class SymbolLocation {
  public Memory memory;
  public Register register;
  public boolean isDirty; // If both locations present, but data in register is newer

  public SymbolLocation(Memory memory) {
    this.memory = memory;
  }

  public SymbolLocation(Register register) {
    this.register = register;
  }

  public SymbolLocation(Memory memory, Register register) {
    this.memory = memory;
    this.register = register;
  }

  public SymbolLocation(Memory memory, Register register, boolean isDirty) {
    this.memory = memory;
    this.register = register;
    this.isDirty = isDirty;
  }
}
