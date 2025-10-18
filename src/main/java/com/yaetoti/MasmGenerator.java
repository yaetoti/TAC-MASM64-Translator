package com.yaetoti;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MasmGenerator {

  // Maps variable names to their types and stack offsets
  private static class SymbolTable {
    private final Map<String, TACI.Type> types = new HashMap<>();
    private final Map<String, Integer> offsets = new HashMap<>();
    private int currentOffset = 0;

    // Allocate space on the stack for a new variable
    public int addVariable(String name, TACI.Type type) {
      types.put(name, type);
      // Align stack to the size of the type, minimum 4 bytes
      int allocationSize = Math.max(4, type.size());
      currentOffset += allocationSize;
      offsets.put(name, currentOffset);
      return currentOffset;
    }

    public TACI.Type getType(String name) {
      return Objects.requireNonNull(types.get(name), "Variable not defined: " + name);
    }

    public int getOffset(String name) {
      return Objects.requireNonNull(offsets.get(name), "Variable not defined: " + name);
    }

    public int getTotalAllocationSize() {
      return currentOffset;
    }
  }

  private final SymbolTable symbolTable = new SymbolTable();
  private final StringBuilder code = new StringBuilder();

  public String generate(TACI.Program program) {
    // First pass: build the symbol table to know all variables and required stack space
    buildSymbolTable(program);

    // Boilerplate MASM setup
    appendHeader();
    append(".code");
    append("main PROC");

    // Function Prolog: Set up stack frame
    append("    push    rbp");
    append("    mov     rbp, rsp");
    append("    sub     rsp, %d", symbolTable.getTotalAllocationSize());

    // Second pass: generate code for each instruction
    for (TACI.Instruction instruction : program.instructions()) {
      translateInstruction(instruction);
    }

    // Function Epilog: Restore stack and return
    append("    add     rsp, %d", symbolTable.getTotalAllocationSize());
    append("    pop     rbp");
    append("    mov     rax, 0  ; Return 0");
    append("    ret");
    append("main ENDP");
    append("END");

    return code.toString();
  }

  private void buildSymbolTable(TACI.Program program) {
    for (TACI.Instruction instruction : program.instructions()) {
      if (instruction instanceof TACI.Assignment(var result, _)) {
        TACI.Type type = determineType(result, instruction);
        symbolTable.addVariable(result.name(), type);
      } else if (instruction instanceof TACI.BinaryOperation(var result, _, _, _)) {
        TACI.Type type = determineType(result, instruction);
        symbolTable.addVariable(result.name(), type);
      }
    }
  }

  private void translateInstruction(TACI.Instruction instruction) {
    append("\n    ; TAC: %s", instruction.toString().replaceAll("TACI\\$[A-Za-z]+", ""));
    if (instruction instanceof TACI.Assignment a) {
      translateAssignment(a);
    } else if (instruction instanceof TACI.BinaryOperation b) {
      translateBinaryOperation(b);
    }
  }

  private void translateAssignment(TACI.Assignment assignment) {
    TACI.Type resultType = symbolTable.getType(assignment.result().name());
    String resultAddr = getMemoryAddress(assignment.result().name());

    loadOperandIntoRegister("rax", assignment.source(), resultType);

    String resultReg = getRegister("rax", resultType.size());
    append("    mov     %s, %s", resultAddr, resultReg);
  }

  private void translateBinaryOperation(TACI.BinaryOperation op) {
    TACI.Type resultType = symbolTable.getType(op.result().name());
    String resultAddr = getMemoryAddress(op.result().name());

    // 1. Load arg1 into RAX
    loadOperandIntoRegister("rax", op.arg1(), resultType);

    // 2. Load arg2 into RCX
    loadOperandIntoRegister("rcx", op.arg2(), resultType);

    String regA = getRegister("rax", resultType.size());
    String regC = getRegister("rcx", resultType.size());

    // 3. Perform the operation
    switch (op.op()) {
    case ADD -> append("    add     %s, %s", regA, regC);
    case SUB -> append("    sub     %s, %s", regA, regC);
    case MUL -> {
      if (resultType.isSigned()) {
        append("    imul    %s, %s", regA, regC);
      } else {
        append("    mul     %s", regC); // result in RDX:RAX
      }
    }
    case DIV, MOD -> {
      // Dividend is already in RAX. Prepare RDX.
      if (resultType.isSigned()) {
        switch (resultType.size()) { // Sign-extend RAX into RDX
        case 8 -> append("    cqo");
        case 4 -> append("    cdq");
        case 2 -> append("    cwd");
        case 1 -> append("    cbw"); // Extends AL into AX, not quite RDX
        }
      } else {
        append("    xor     rdx, rdx  ; Clear RDX for unsigned division");
      }

      if (resultType.isSigned()) {
        append("    idiv    %s", regC);
      } else {
        append("    div     %s", regC);
      }

      if (op.op() == TACI.Op.MOD) {
        // Remainder is in RDX, move it to RAX for storing
        String regD = getRegister("rdx", resultType.size());
        append("    mov     %s, %s", regA, regD);
      }
    }
    }

    // 4. Store the result from RAX back to the variable's stack location
    append("    mov     %s, %s", resultAddr, regA);
  }

  // Helper to load any operand (variable or constant) into a register, handling type promotion
  private void loadOperandIntoRegister(String reg, TACI.Operand operand, TACI.Type targetType) {
    if (operand instanceof TACI.Constant c) {
      append("    mov     %s, %s", getRegister(reg, targetType.size()), c.value());
    } else if (operand instanceof TACI.Variable v) {
      TACI.Type sourceType = symbolTable.getType(v.name());
      String sourceAddr = getMemoryAddress(v.name());

      // Handle type promotion (e.g., s32 to s64)
      if (targetType.size() > sourceType.size()) {
        if (sourceType.isSigned()) {
          if (sourceType.size() == 4) {
            append("    movsxd  %s, %s", reg, sourceAddr);
          } else {
            append("    movsx   %s, %s", reg, sourceAddr);
          }
        } else {
          if (sourceType.size() == 4) {
            append("    movzxd  %s, %s", reg, sourceAddr);
          } else {
            append("    movzx   %s, %s", reg, sourceAddr);
          }
        }
      } else {
        append("    mov     %s, %s", getRegister(reg, targetType.size()), sourceAddr);
      }
    }
  }

  // Helper Methods
  private TACI.Type determineType(TACI.Variable var, TACI.Instruction ctx) {
    // A real compiler would have a more robust type inference system.
    // Here, we infer the type from the context of the operation.
    if (ctx instanceof TACI.Assignment(_, var source)) {
      if (source instanceof TACI.Constant c) return c.type();
      if (source instanceof TACI.Variable v) return symbolTable.getType(v.name());
    } else if (ctx instanceof TACI.BinaryOperation(_, var arg1, _, var arg2)) {
      TACI.Type t1 = (arg1 instanceof TACI.Constant c) ? c.type() : symbolTable.getType(((TACI.Variable)arg1).name());
      TACI.Type t2 = (arg2 instanceof TACI.Constant c) ? c.type() : symbolTable.getType(((TACI.Variable)arg2).name());
      // Promote to the larger type
      return t1.size() >= t2.size() ? t1 : t2;
    }
    throw new IllegalStateException("Cannot determine type for " + var.name());
  }

  private String getMemoryAddress(String varName) {
    TACI.Type type = symbolTable.getType(varName);
    String sizeDirective = switch (type.size()) {
      case 8 -> "qword ptr";
      case 4 -> "dword ptr";
      case 2 -> "word ptr";
      case 1 -> "byte ptr";
      default -> "";
    };
    return String.format("%s [rbp - %d]", sizeDirective, symbolTable.getOffset(varName));
  }

  private String getRegister(String baseReg, int size) {
    return switch (baseReg.toLowerCase()) {
      case "rax" -> switch (size) { case 8 -> "rax"; case 4 -> "eax"; case 2 -> "ax"; case 1 -> "al"; default -> "rax"; };
      case "rcx" -> switch (size) { case 8 -> "rcx"; case 4 -> "ecx"; case 2 -> "cx"; case 1 -> "cl"; default -> "rcx"; };
      case "rdx" -> switch (size) { case 8 -> "rdx"; case 4 -> "edx"; case 2 -> "dx"; case 1 -> "dl"; default -> "rdx"; };
      default -> baseReg;
    };
  }

  private void append(String format, Object... args) {
    code.append(String.format(format, args)).append("\n");
  }

  private void appendHeader() {
    append("EXTERN ExitProcess : PROC");
    append(".data");
  }
}
