package com.compiler.translator.masm;

import com.compiler.ir.FunctionContext;
import com.compiler.translator.masm.memory.Register;

import java.util.ArrayList;
import java.util.LinkedHashMap;

// TODO LRU

public class RegisterManager {
  private final FunctionContext ctx;
  private final LinkedHashMap<Register.Type, RegisterInfo> registers = new LinkedHashMap<>();

  public RegisterManager(FunctionContext ctx) {
    this.ctx = ctx;

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

  public RegisterInfo GetFreeRegister(Register.Bank bank) {
    for (var entry : registers.entrySet()) {
      var info = entry.getValue();

      // Filter by bank
      if (bank != null && bank != info.GetType().GetBank()) {
        continue;
      }

      // Return the first free register
      if (!info.IsOccupied()) {
        return info;
      }
    }

    // Flush and return the first free register
    var registers = FlushRegisters(1, bank);
    return registers.getFirst();
  }

  public ArrayList<RegisterInfo> GetFreeRegisters() {
    var freeRegisters = new ArrayList<RegisterInfo>();
    for (var entry : registers.entrySet()) {
      var info = entry.getValue();
      if (!info.IsOccupied()) {
        freeRegisters.add(entry.getValue());
      }
    }

    return freeRegisters;
  }

  public ArrayList<RegisterInfo> GetFreeRegisters(int amount, Register.Bank bank) {
    var freeRegisters = new ArrayList<RegisterInfo>();

    for (var entry : registers.entrySet()) {
      // Already enough
      if (freeRegisters.size() >= amount) {
        break;
      }

      var info = entry.getValue();

      // Occupied or bank mismatch
      if (info.IsOccupied() || (bank != null && bank != info.GetType().GetBank())) {
        continue;
      }

      freeRegisters.add(entry.getValue());
    }

    // Flush if needed
    if (freeRegisters.size() != amount) {
      var registers = FlushRegisters(amount - freeRegisters.size(), bank);
      freeRegisters.addAll(registers);
    }

    return freeRegisters;
  }

  public void FlushRegister(Register.Type type) {
    var regInfo = registers.get(type);
    assert !regInfo.IsOccupied() : "Cannot flush locked register";

    // Already flushed
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

      // Already free
      if (info.symbol == null) {
        continue;
      }

      ctx.Flush(info.GetSymbol());
    }
  }

  public ArrayList<RegisterInfo> FlushRegisters(int amount, Register.Bank bank) {
    var mm = ctx.memoryManager;
    var candidates = new ArrayList<RegisterInfo>();

    // Find candidates
    for (var entry : registers.entrySet()) {
      // Already enough
      if (candidates.size() >= amount) {
        break;
      }

      var info = entry.getValue();

      // Filter by bank
      if (bank != null && bank != info.GetType().GetBank()) {
        continue;
      }

      // If locked or assigned to a symbol that does not have a memory location - continue
      if (info.IsLocked() || mm.GetMemoryLocation(info.GetSymbol()) == null) {
        // TODO potential spill
        continue;
      }

      candidates.add(info);
    }

    // Not enough registers
    if (candidates.size() != amount) {
      throw new RuntimeException("Not enough free registers to flush");
    }

    // Flush
    for (var info : candidates) {
      ctx.Flush(info.GetSymbol());
    }

    return candidates;
  }
}
