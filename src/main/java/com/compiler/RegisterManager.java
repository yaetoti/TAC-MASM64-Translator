package com.compiler;

import com.compiler.memory.Register;
import com.compiler.symbols.IVariable;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class RegisterManager {
  // TODO LRU
  // TODO add all registers
  public FunctionContext ctx;
  public LinkedHashMap<Register.Type, RegisterInfo> registers = new LinkedHashMap<>();

  public RegisterManager(FunctionContext ctx) {
    this.ctx = ctx;
    Clear();
  }

  public void Clear() {
    registers.clear();
    for (var type : Register.Type.values()) {
      if (type == Register.Type.RBP || type == Register.Type.RSP) {
        continue;
      }

      registers.put(type, new RegisterInfo(type));
    }
  }

  public void PutVariable(IVariable symbol, Register.Type type) {
    registers.get(type).symbol = symbol;
  }

  public ArrayList<RegisterInfo> GetRegisters() {
    return new ArrayList<>(registers.values());
  }

  public ArrayList<RegisterInfo> GetFreeRegisters() {
    var freeRegisters = new ArrayList<RegisterInfo>();
    for (var entry : registers.entrySet()) {
      if (!entry.getValue().isLocked && entry.getValue().symbol == null) {
        freeRegisters.add(entry.getValue());
      }
    }

    return freeRegisters;
  }

  public RegisterInfo GetFreeRegister() {
    for (var entry : registers.entrySet()) {
      if (!entry.getValue().isLocked && entry.getValue().symbol == null) {
        return entry.getValue();
      }
    }

    // Spill
    var registers = SpillRegisters(1);
    return registers.getFirst();
  }

  public ArrayList<RegisterInfo> GetFreeRegisters(int amount) {
    var freeRegisters = new ArrayList<RegisterInfo>();

    for (var entry : registers.entrySet()) {
      if (freeRegisters.size() >= amount) {
        break;
      }

      if (!entry.getValue().isLocked && entry.getValue().symbol == null) {
        freeRegisters.add(entry.getValue());
      }
    }

    // Spill
    if (freeRegisters.size() != amount) {
      var registers = SpillRegisters(amount - freeRegisters.size());
      freeRegisters.addAll(registers);
    }

    return freeRegisters;
  }

  public ArrayList<RegisterInfo> SpillRegisters(int amount) {
    var memoryManager = ctx.memoryManager;
    var candidates = new ArrayList<RegisterInfo>();

    // Find candidates
    for (var entry : registers.entrySet()) {
      if (candidates.size() >= amount) {
        break;
      }

      var type = entry.getKey();
      var info = entry.getValue();

      // If locked or assigned to a symbol that does not have a memory location - continue
      // TODO lazy allocation
      if (info.isLocked || (info.symbol != null && memoryManager.locations.get(info.symbol).memory == null)) {
        continue;
      }

      candidates.add(info);
    }

    if (candidates.size() != amount) {
      throw new IllegalStateException("Not enough free registers to spill");
    }

    // Spill
    for (var info : candidates) {
      var location = memoryManager.locations.get(info.symbol);

      // TODO different for float
      ctx.out.EmitF("mov %s, %s", location.memory, location.register);

      info.symbol = null;
      location.register = null;
      location.isDirty = false;
    }

    return candidates;
  }
}
