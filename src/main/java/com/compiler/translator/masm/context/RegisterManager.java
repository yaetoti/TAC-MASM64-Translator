package com.compiler.translator.masm.context;

import com.compiler.translator.masm.memory.Register;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.function.Predicate;

// TODO LRU

// The difference between Free and Acquire is order. Free just frees whatever registers it encounters. Acquire first gets free registers and only if it’s not enough of them - spills more

public class RegisterManager {
  public static final Predicate<RegisterInfo> IS_GPR = info -> info.type.GetBank() == Register.Bank.GPR;
  public static final Predicate<RegisterInfo> IS_VEC = info -> info.type.GetBank() == Register.Bank.VEC;
  public static final Predicate<RegisterInfo> IS_FREE = RegisterInfo::IsFree;
  public static final Predicate<RegisterInfo> IS_LOCKED = RegisterInfo::IsLocked;
  public static final Predicate<RegisterInfo> IS_OCCUPIED = RegisterInfo::IsOccupied;
  public static final Predicate<RegisterInfo> IS_SPILLABLE = RegisterInfo::IsSpillable;
  public static final Predicate<RegisterInfo> IS_ACQUIRABLE = info -> info.IsFree() || info.IsSpillable();
  public static Predicate<RegisterInfo> IS_EXACT_SIZE(int size) {
    return info -> info.GetRegister(size) != null;
  }

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

  public RegisterInfo Get(Predicate<RegisterInfo> predicate) {
    return GetAll(predicate, 1).getFirst();
  }

  public ArrayList<RegisterInfo> GetAll() {
    return new ArrayList<>(registers.values());
  }

  public ArrayList<RegisterInfo> GetAll(Register.Type... types) {
    return GetAll(Arrays.stream(types).toList());
  }

  public ArrayList<RegisterInfo> GetAll(Collection<Register.Type> types) {
    var result = new ArrayList<RegisterInfo>();
    for (var type : types) {
      result.add(registers.get(type));
    }

    return result;
  }

  public ArrayList<RegisterInfo> GetAll(Predicate<RegisterInfo> predicate) {
    var result = new ArrayList<RegisterInfo>();
    for (var value : registers.values()) {
      if (predicate.test(value)) {
        result.add(value);
      }
    }

    return result;
  }

  public ArrayList<RegisterInfo> GetAll(Predicate<RegisterInfo> predicate, int limit) {
    var result = new ArrayList<RegisterInfo>();
    for (var value : registers.values()) {
      if (result.size() >= limit) {
        break;
      }

      if (predicate.test(value)) {
        result.add(value);
      }
    }

    return result;
  }



  public RegisterInfo Free(Register.Type type) {
    return FreeAll(type).getFirst();
  }

  public RegisterInfo Free(Predicate<RegisterInfo> predicate) {
    return FreeAll(predicate, 1).getFirst();
  }

  public ArrayList<RegisterInfo> FreeAll() {
    for (var info : registers.values()) {
      if (info.IsLocked()) {
        throw new RuntimeException("Cannot flush locked register");
      }

      if (info.IsFree()) {
        continue;
      }

      ctx.Flush(info.GetSymbol());
    }

    return new ArrayList<>(registers.values());
  }

  public ArrayList<RegisterInfo> FreeAll(Register.Type... types) {
    return FreeAll(Arrays.stream(types).toList());
  }

  public ArrayList<RegisterInfo> FreeAll(Collection<Register.Type> types) {
    var result = new ArrayList<RegisterInfo>();
    for (var type : types) {
      var info = registers.get(type);
      if (info.IsLocked()) {
        throw new RuntimeException("Cannot flush locked register");
      }

      if (info.IsFree()) {
        result.add(info);
        continue;
      }

      ctx.Flush(info.GetSymbol());
      result.add(info);
    }

    return result;
  }

  public ArrayList<RegisterInfo> FreeAll(Predicate<RegisterInfo> predicate) {
    var result = new ArrayList<RegisterInfo>();
    for (var info : registers.values()) {
      if (!predicate.test(info)) {
        continue;
      }

      if (info.IsLocked()) {
        throw new RuntimeException("Cannot flush locked register");
      }

      if (info.IsFree()) {
        continue;
      }

      ctx.Flush(info.GetSymbol());
      result.add(info);
    }

    return result;
  }

  public ArrayList<RegisterInfo> FreeAll(Predicate<RegisterInfo> predicate, int limit) {
    var result = new ArrayList<RegisterInfo>();
    for (var info : registers.values()) {
      if (result.size() >= limit) {
        break;
      }

      if (!predicate.test(info)) {
        continue;
      }

      if (info.IsLocked()) {
        throw new RuntimeException("Cannot flush locked register");
      }

      if (info.IsFree()) {
        continue;
      }

      ctx.Flush(info.GetSymbol());
      result.add(info);
    }

    return result;
  }



  public RegisterInfo Acquire(Register.Type type) {
    return Free(type);
  }

  public RegisterInfo Acquire(Predicate<RegisterInfo> predicate) {
    var register = Get(IS_FREE.and(predicate));
    if (register != null) {
      return register;
    }

    register = Get(IS_SPILLABLE.and(predicate));
    if (register == null) {
      throw new RuntimeException("No free registers");
    }

    return Free(register.GetType());
  }

  public ArrayList<RegisterInfo> AcquireAll() {
    return FreeAll();
  }

  public ArrayList<RegisterInfo> AcquireAll(Register.Type... types) {
    return FreeAll(types);
  }

  public ArrayList<RegisterInfo> AcquireAll(Collection<Register.Type> types) {
    return FreeAll(types);
  }

  public ArrayList<RegisterInfo> AcquireAll(Predicate<RegisterInfo> predicate) {
    var registers = GetAll(IS_ACQUIRABLE.and(predicate));
    var types = registers.stream().map(RegisterInfo::GetType).toList();
    return FreeAll(types);
  }

  public ArrayList<RegisterInfo> AcquireAll(Predicate<RegisterInfo> predicate, int limit) {
    var registers = GetAll(IS_FREE.and(predicate), limit);
    if (registers.size() == limit) {
      return registers;
    }

    var spillable = GetAll(IS_SPILLABLE.and(predicate), limit - registers.size());
    if (spillable.size() < limit - registers.size()) {
      throw new RuntimeException("Not enough free registers");
    }

    var types = spillable.stream().map(RegisterInfo::GetType).toList();
    registers.addAll(FreeAll(types));
    return registers;
  }



  @Deprecated
  public ArrayList<RegisterInfo> FreeAll(int amount, Register.Bank bank) {
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

  @Deprecated
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
    var registers = FreeAll(1, bank);
    return registers.getFirst();
  }
}
