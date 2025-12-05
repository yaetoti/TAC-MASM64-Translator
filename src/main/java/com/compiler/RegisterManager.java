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

  public RegisterInfo Get(Register.Type type) {
    return registers.get(type);
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
    var registers = FlushRegisters(1);
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
      var registers = FlushRegisters(amount - freeRegisters.size());
      freeRegisters.addAll(registers);
    }

    return freeRegisters;
  }

  public void FlushRegister(Register.Type type) {
    var regInfo = registers.get(type);
    if (regInfo.isLocked) {
      throw new RuntimeException("Cannot spill locked register");
    }

    // Already spilled
    if (regInfo.symbol == null) {
      return;
    }

    ctx.Flush(regInfo.symbol);
  }

  public void FlushRegisters() {
    var memoryManager = ctx.memoryManager;

    // Find candidates
    for (var entry : registers.entrySet()) {
      var info = entry.getValue();
      // Locked registers must be unlocked manually
      if (info.isLocked) {
        throw new RuntimeException("Cannot spill locked register");
      }

      // Already free
      if (info.symbol == null) {
        continue;
      }

      // TODO. Only if we call it FreeAllRegisters. But what if we need that data? We need to allocate a new memory and we do not do that
      var location = memoryManager.locations.get(info.symbol);
      if (location.memory == null) {
        throw new RuntimeException("Cannot spill register that does not have a memory location");
      }

      // Spill
      ctx.out.EmitF("mov %s, %s", location.memory, location.register);
      location.register = null;
      location.isDirty = false;
    }
  }

  public ArrayList<RegisterInfo> FlushRegisters(int amount) {
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
