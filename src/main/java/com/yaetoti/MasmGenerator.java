package com.yaetoti;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MasmGenerator {
  // Maps variable names to their types and stack offsets
  public static class SymbolTable {
    private final Map<String, TAC.Type> types = new HashMap<>();
    private final Map<String, Integer> offsets = new HashMap<>();
    private int currentOffset = 0;

    // Allocate space on the stack for a new variable
    public int addVariable(String name, TAC.Type type) {
      types.put(name, type);
      // Align stack to the size of the type, minimum 4 bytes
      int allocationSize = Math.max(8, type.size());
      currentOffset += allocationSize;
      offsets.put(name, currentOffset);
      return currentOffset;
    }

    public void AddSeparateVariable(String name, TAC.Type type, int offset) {
      types.put(name, type);
      offsets.put(name, offset);
    }

    public TAC.Type getType(String name) {
      return Objects.requireNonNull(types.get(name), "Variable not defined: " + name);
    }

    public int getOffset(String name) {
      return Objects.requireNonNull(offsets.get(name), "Variable not defined: " + name);
    }

    public int getTotalAllocationSize() {
      return currentOffset;
    }
  }

  public SymbolTable symbolTable;
  public final StringBuilder code = new StringBuilder();
  public int labelCounter = 0;

  public String GenLabel() {
    return "L" + (labelCounter++);
  }

  public void Append(String format, Object... args) {
    code.append(String.format(format, args)).append("\n");
  }

  private String GetRegister(Register register, int size) {
    return switch (register) {
      case Register.RAX -> switch (size) { case 8 -> "rax"; case 4 -> "eax"; case 2 -> "ax"; case 1 -> "al"; default -> throw new IllegalArgumentException("Invalid register size: " + size); };
      case Register.RCX -> switch (size) { case 8 -> "rcx"; case 4 -> "ecx"; case 2 -> "cx"; case 1 -> "cl"; default -> throw new IllegalArgumentException("Invalid register size: " + size); };
      case Register.RDX -> switch (size) { case 8 -> "rdx"; case 4 -> "edx"; case 2 -> "dx"; case 1 -> "dl"; default -> throw new IllegalArgumentException("Invalid register size: " + size); };
    };
  }

  private String GetDereferenceCode(String variableName) {
    TAC.Type type = symbolTable.getType(variableName);
    String sizeDirective = switch (type.size()) {
      case 8 -> "qword ptr";
      case 4 -> "dword ptr";
      case 2 -> "word ptr";
      case 1 -> "byte ptr";
      default -> "";
    };

    int offset = symbolTable.getOffset(variableName);
    if (offset < 0) {
      return String.format("%s [rbp + %d]", sizeDirective, -offset);
    }

    return String.format("%s [rbp - %d]", sizeDirective, offset);
  }

  private String GetDereferenceCode(String variableName, int size) {
    String sizeDirective = switch (size) {
      case 8 -> "qword ptr";
      case 4 -> "dword ptr";
      case 2 -> "word ptr";
      case 1 -> "byte ptr";
      default -> "";
    };

    int offset = symbolTable.getOffset(variableName);
    if (offset < 0) {
      return String.format("%s [rbp + %d]", sizeDirective, -offset);
    }

    return String.format("%s [rbp - %d]", sizeDirective, offset);
  }

  public String Generate(Program program) {
    // Generate sections
    Append(".data");
    Append(".code");

    // - Generate epilogue

    for (var function : program.GetFunctions()) {
      // - Init values
      symbolTable = new SymbolTable();
      labelCounter = 0;

      // - Generate PROC
      Append(function.GetDeclaration().name() + " proc");

      // - Generate frame
      Append("    push    rbp");
      Append("    mov     rbp, rsp");

      // - Spilt register parameters into stack, if appropriate. Add variables to the symbol table
      int paramId = 0;
      for (var parameter : function.GetDeclaration().parameters()) {
        // -8 return
        // -16 param1
        symbolTable.AddSeparateVariable(parameter.name(), parameter.type(), -16 - paramId * 8);
        ++paramId;
      }

      // - Allocate stack memory
      buildSymbolTable(function.GetInstructions());
      Append("    sub     rsp, %d", symbolTable.getTotalAllocationSize());

      // - Generate code

      for (TAC.Instruction instruction : function.GetInstructions()) {
        translateInstruction(instruction);
      }

      // - Generate ENDP
      Append(function.GetDeclaration().name() + " endp");
    }

    // Generate end
    Append("end");

    return code.toString();
  }



  public String generate(TAC.Program program) {
    // First pass: build the symbol table to know all variables and required stack space
    buildSymbolTable(program.instructions());

    // Boilerplate MASM setup
    Append(".data");
    Append(".code");
    Append("main PROC");

    // Function Prolog: Set up stack frame
    Append("    push    rbp");
    Append("    mov     rbp, rsp");
    Append("    sub     rsp, %d", symbolTable.getTotalAllocationSize());

    // Second pass: generate code for each instruction
    for (TAC.Instruction instruction : program.instructions()) {
      translateInstruction(instruction);
    }

    // Function Epilog: Restore stack and return
//    append("    add     rsp, %d", symbolTable.getTotalAllocationSize());
//    append("    pop     rbp");
//    append("    mov     rax, 0  ; Return 0");
//    append("    ret");
    Append("main ENDP");
    Append("END");

    return code.toString();
  }

  private void buildSymbolTable(List<TAC.Instruction> instructions) {
    for (TAC.Instruction instruction : instructions) {
      if (instruction instanceof TAC.Assignment(var result, _)) {
        TAC.Type type = determineType(result, instruction);
        symbolTable.addVariable(result.name(), type);
      } else if (instruction instanceof TAC.BinaryOperation(var result, _, _, _)) {
        TAC.Type type = determineType(result, instruction);
        symbolTable.addVariable(result.name(), type);
      }
    }
  }

  private void translateInstruction(TAC.Instruction instruction) {
// Add a small helper to not print "TACI$..." for cleaner comments
    String instructionString = instruction.toString()
      .replaceAll("TACI\\$[A-Za-z]+", "")
      .replaceAll("records\\.", "");

    Append("\n    ; TAC: %s", instructionString);

    // UPDATED with new cases
    switch (instruction) {
    case TAC.Assignment a -> translateAssignment(a);
    case TAC.BinaryOperation b -> translateBinaryOperation(b);
    case TAC.Label(String name) -> Append("%s:", name);
    case TAC.Jump(String targetLabel) -> Append("    jmp     %s", targetLabel);
    case TAC.ConditionalJump cj -> translateConditionalJump(cj);
    case TAC.Return ret -> translateReturn(ret);
    case TAC.Call call -> translateCall(call);
    }
  }

  private void translateCall(TAC.Call code) {
    // TODO Depending on the convention

    // Push parameters
    for (var param : code.params()) {
      switch (param) {
      case TAC.Constant (String value, TAC.Type type) -> {
        loadOperandIntoRegister(Register.RAX, param, type.isSigned() ? TAC.i64 : TAC.u64);

      }
      case TAC.Variable (String name) -> {

      }
      case TAC.Register register -> {
      }
      }
    }

    // Call
    Append("    call     " + code.name());

    // Move parameters

    // Pop parameters
  }

  private void translateReturn(TAC.Return code) {
    // TODO Depending on the convention

    // Restore stack
    Append("    mov     rsp, rbp");
    Append("    pop     rdx");
    // Pop return value
    Append("    pop     rcx");
    // TODO push parameters

    for (var operand : code.operands()) {
      if (operand instanceof TAC.Constant(String value, TAC.Type type)) {
        loadOperandIntoRegister(Register.RAX, operand, type);
        Append("    push    rax");
      }
      else if (operand instanceof TAC.Variable(String name)) {
        TAC.Type resultType = symbolTable.getType(name);
        var type = new TAC.Type(resultType.name(), 8, resultType.isSigned());
        // Load variable into memory. minimum size == 16
        loadOperandIntoRegister(Register.RAX, operand, type);

        Append("    push    rax");
      }
    }

    Append("    mov     rbp, rdx");
    Append("    jmp     rcx");


    // mov rsp, rbp
    // pop rbp

    // pop return address into rax
    // push parameters
    // jmp rax
  }

  private void translateConditionalJump(TAC.ConditionalJump cj) {
    // Assume comparison is between same-sized types for simplicity
    TAC.Type opType = determineOperandType(cj.arg1());

    // 1. Load operands into registers
    loadOperandIntoRegister(Register.RAX, cj.arg1(), opType);
    loadOperandIntoRegister(Register.RCX, cj.arg2(), opType);

    String regA = GetRegister(Register.RAX, opType.size());
    String regC = GetRegister(Register.RCX, opType.size());

    // 2. Compare the two registers
    Append("    cmp     %s, %s", regA, regC);

    // 3. Select the correct jump instruction based on the operator
    String jumpInstruction = switch (cj.op()) {
      case EQ -> "je";  // Jump if Equal
      case NE -> "jne"; // Jump if Not Equal
      // For signed vs unsigned, JL/JG vs JB/JA would be needed.
      // We'll assume signed for this example (J L/G/LE/GE).
      case LT -> "jl";  // Jump if Less
      case LE -> "jle"; // Jump if Less or Equal
      case GT -> "jg";  // Jump if Greater
      case GE -> "jge"; // Jump if Greater or Equal
    };

    // 4. Emit the jump
    Append("    %s     %s", jumpInstruction, cj.targetLabel());
  }

  // Helper to find an operand's type, needed for ConditionalJump
  private TAC.Type determineOperandType(TAC.Operand operand) {
    if (operand instanceof TAC.Constant c) {
      return c.type();
    } else if (operand instanceof TAC.Variable v) {
      return symbolTable.getType(v.name());
    }
    throw new IllegalArgumentException("Unknown operand type");
  }

  private void translateAssignment(TAC.Assignment assignment) {
    TAC.Type resultType = symbolTable.getType(assignment.result().name());

    // From anywhere to RAX
    loadOperandIntoRegister(Register.RAX, assignment.source(), resultType);

    // From RAX to memory
    String sourceAddr = GetRegister(Register.RAX, resultType.size());
    String resultAddr = GetDereferenceCode(assignment.result().name());
    Append("    mov     %s, %s", resultAddr, sourceAddr);
  }

  private void translateBinaryOperation(TAC.BinaryOperation op) {
    TAC.Type resultType = symbolTable.getType(op.result().name());
    String resultAddr = GetDereferenceCode(op.result().name());

    // 1. Load arg1 into RAX
    loadOperandIntoRegister(Register.RAX, op.arg1(), resultType);

    // 2. Load arg2 into RCX
    loadOperandIntoRegister(Register.RCX, op.arg2(), resultType);

    String regA = GetRegister(Register.RAX, resultType.size());
    String regC = GetRegister(Register.RCX, resultType.size());

    // 3. Perform the operation
    switch (op.op()) {
    case ADD -> Append("    add     %s, %s", regA, regC);
    case SUB -> Append("    sub     %s, %s", regA, regC);
    case MUL -> {
      if (resultType.isSigned()) {
        Append("    imul    %s, %s", regA, regC);
      } else {
        Append("    mul     %s", regC); // result in RDX:RAX
      }
    }
    case DIV, MOD -> {
      // Dividend is already in RAX. Prepare RDX.
      if (resultType.isSigned()) {
        switch (resultType.size()) { // Sign-extend RAX into RDX
        case 8 -> Append("    cqo");
        case 4 -> Append("    cdq");
        case 2 -> Append("    cwd");
        case 1 -> Append("    cbw"); // Extends AL into AX, not quite RDX
        }
      } else {
        Append("    xor     rdx, rdx  ; Clear RDX for unsigned division");
      }

      if (resultType.isSigned()) {
        Append("    idiv    %s", regC);
      } else {
        Append("    div     %s", regC);
      }

      if (op.op() == TAC.Op.MOD) {
        // Remainder is in RDX, move it to RAX for storing
        String regD = GetRegister(Register.RDX, resultType.size());
        Append("    mov     %s, %s", regA, regD);
      }
    }
    }

    // 4. Store the result from RAX back to the variable's stack location
    Append("    mov     %s, %s", resultAddr, regA);
  }

  // Helper to load any operand (variable or constant) into a register, handling type promotion
  private void loadOperandIntoRegister(Register reg, TAC.Operand operand, TAC.Type targetType) {
    if (operand instanceof TAC.Constant c) {
      // Load constant
      Append("    mov     %s, %s", GetRegister(reg, targetType.size()), c.value());
    } else if (operand instanceof TAC.Variable v) {
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
  }

  // Helper Methods
  private TAC.Type determineType(TAC.Variable var, TAC.Instruction ctx) {
    // A real compiler would have a more robust type inference system.
    // Here, we infer the type from the context of the operation.
    if (ctx instanceof TAC.Assignment(_, var source)) {
      if (source instanceof TAC.Constant c) return c.type();
      if (source instanceof TAC.Variable v) return symbolTable.getType(v.name());
    } else if (ctx instanceof TAC.BinaryOperation(_, var arg1, _, var arg2)) {
      TAC.Type t1 = (arg1 instanceof TAC.Constant c) ? c.type() : symbolTable.getType(((TAC.Variable)arg1).name());
      TAC.Type t2 = (arg2 instanceof TAC.Constant c) ? c.type() : symbolTable.getType(((TAC.Variable)arg2).name());
      // Promote to the larger type
      return t1.size() >= t2.size() ? t1 : t2;
    }
    throw new IllegalStateException("Cannot determine type for " + var.name());
  }
}
