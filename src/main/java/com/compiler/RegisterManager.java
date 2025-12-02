package com.compiler;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class RegisterManager {
  // TODO LRU
  // TODO add all registers
  public LinkedHashMap<Register.Type, RegisterInfo> registers = new LinkedHashMap<>();

  public RegisterManager() {
    Clear();
  }

  public void Clear() {
    registers.clear();
    for (var type : Register.Type.values()) {
      registers.put(type, new RegisterInfo(type));
    }
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

    return null;
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

    if (freeRegisters.size() != amount) {
      return null;
    }

    return freeRegisters;
  }
}
