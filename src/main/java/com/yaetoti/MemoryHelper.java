package com.yaetoti;

public class MemoryHelper {
  private final MasmGenerator gen;

  public MemoryHelper(MasmGenerator generator) {
    gen = generator;
  }

  public String GetRegister(Register register, int size) {
    return switch (register) {
      case Register.RAX -> switch (size) { case 8 -> "rax"; case 4 -> "eax"; case 2 -> "ax"; case 1 -> "al"; default -> throw new IllegalArgumentException("Invalid register size: " + size); };
      case Register.RCX -> switch (size) { case 8 -> "rcx"; case 4 -> "ecx"; case 2 -> "cx"; case 1 -> "cl"; default -> throw new IllegalArgumentException("Invalid register size: " + size); };
      case Register.RDX -> switch (size) { case 8 -> "rdx"; case 4 -> "edx"; case 2 -> "dx"; case 1 -> "dl"; default -> throw new IllegalArgumentException("Invalid register size: " + size); };
    };
  }

  // How we want to use that function?
  // - Do we want to create function to load to data to the corresponding register?
  // - Load from memory u64 to register i32. Should that be possible? Definitely, if we know what are we doing

  // Directions
  // - To register, to stack, to memory address
  // - From immediate, from another register, from stack, from memory (lea)

  // Types of loading
  // - Strict type to strict register. u and i does not matter here, we just move memory
  // - Promotion. Lawful modification, sign-dependant
  // - Shrinking. "Just fit it in there, I don't care about the rest" mode

  // Load any operand into register handling promotion, loading, and sign extension
  // Load from: constant, stack, another register, data label (lea)
  // TODO extend with loading from registers
  public void LoadConstantToRegister(TAC.Register register, TAC.Constant constant) {
    // Value check? Vibe check? Maybe. How? Integers, Floats
    gen.Append(String.format("    mov     %s, %s", register.name(), constant.value()));
  }

  private void loadOperandIntoRegister(Register reg, TAC.Variable v, TAC.Type targetType) {
    // Promotion
    TAC.Type sourceType = symbolTable.getType(v.name());
    String sourceAddr = GetDereferenceCode(v.name());

    // Handle type promotion (e.g., s32 to s64)
    if (targetType.size() > sourceType.size()) {
      if (sourceType.isSigned()) {
        if (sourceType.size() == 4) {
          Append("    movsxd  %s, %s", reg, sourceAddr);
        } else {
          Append("    movsx   %s, %s", reg, sourceAddr);
        }
      } else {
        if (sourceType.size() == 4) {
          Append("    movzxd  %s, %s", reg, sourceAddr);
        } else {
          Append("    movzx   %s, %s", reg, sourceAddr);
        }
      }
    } else {
      Append("    mov     %s, %s", GetRegister(reg, targetType.size()), sourceAddr);
    }
  }

  public String GetStackAddressString(int offset) {
    return offset > 0
      ? String.format("[rbp + %d]", offset)
      : offset < 0 ? String.format("[rbp - %d]", offset) : "[rbp]";
  }

  public void LoadVariableToRegister(TAC.Register register, String name) {
    // Address string
    // Size

    TAC.Type type = gen.symbolTable.getType(name);
    int offset = gen.symbolTable.getOffset(name);

    if (register.size() == type.size() || register.size() < type.size()) {
      gen.Append(String.format("    mov     %s, %s", register.name(), GetStackAddressString(offset)));
      return;
    }


  }

  public String LoadOperandIntoRegister(Register register, int registerSize, TAC.Operand operand) {
    StringBuilder builder = new StringBuilder();

    switch (operand) {
    case TAC.Constant (String value, TAC.Type type) -> {
      builder.append(String.format("    mov     %s, %s", GetRegister(register, registerSize), value));
    }
    case TAC.Variable (String name) -> {

    }
    }
  }
}
