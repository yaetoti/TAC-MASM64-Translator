package com.yaetoti;

import java.lang.classfile.instruction.LoadInstruction;
import java.util.*;

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
      int allocationSize = Math.max(8, type.size().size);
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

    public TAC.OffsetMemory GetMemoryOperand(String name) {
      var type = types.get(name);
      var offset = offsets.get(name);
      return new TAC.OffsetMemory(type.size(), TAC.Register.RBP, null, 0, -offset);
    }
  }

  public FunctionFrame currentFrame;
  public Program program;
  public SymbolTable symbolTable;
  public final StringBuilder code = new StringBuilder();
  public int labelCounter = 0;

  public String GenLabel() {
    return "L" + (labelCounter++);
  }

  public void Append(String format, Object... args) {
    code.append(String.format(format, args)).append("\n");
  }

  // Helpers

  private String MoveToRegister(TAC.Register dst, TAC.Operand src) {
    return switch (src) {
      case TAC.Constant constant -> Codegen.MoveToRegister(dst, constant);
      case TAC.Variable variable -> {
        var type = symbolTable.getType(variable.name());
        var memory = symbolTable.GetMemoryOperand(variable.name());
        yield Codegen.MoveToRegister(dst, memory, type.isSigned());
      }
      default -> throw new IllegalStateException("Unsupported operand: " + src);
    };
  }

  // Not helpers

  public String Generate(Program program) {
    this.program = program;

    // Generate sections
    Append(".data");
    Append(".code");

    // - Generate epilogue

    for (var function : program.GetFunctions()) {
      // - Init values
      currentFrame = function;
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
    // return n [arr]
    // nah no way bro, how we gonna know how much we need to take? Theres a way, reverse parameters, get n, but wtf even is that

    // TODO Depending on the convention
    // Let's stop on i64 u64 for now.

    // TODO one of the problems: on the top level we do not work with registers. Registers are allocated. We work with either constants either variables

    // Calculate sizes
    int returnSize = 0;
    int paramsSize = 0;
    int[] paramSizes = new int[code.params().length];
    int[] returnSizes = new int[code.returnVariables().length];

    // Calculate params size
    for (int i = 0; i < paramSizes.length; ++i) {
      var param = code.params()[i];

      switch (param) {
        case TAC.Constant (String value, TAC.Type type) -> {
          paramsSize += type.size().size;
          paramSizes[i] = type.size().size;
        }
        case TAC.Variable (String name) -> {
          paramsSize += symbolTable.getType(name).size().size;
          paramSizes[i] = symbolTable.getType(name).size().size;
        }
        case TAC.Register register -> {
          paramsSize += register.size().size;
          paramSizes[i] = register.size().size;
        }
      default -> throw new IllegalStateException("Unexpected value: " + param);
      }
    }

    // Calculate return values sizes
    for (int i = 0; i < code.returnVariables().length; ++i) {
      var retVal = code.returnVariables()[i];
      // TODO if it is 0, we need to find function declaration, get type and use it as size

      if (retVal == null) {
        for (var function : program.GetFunctions()) {
          if (function.GetDeclaration().name().equals(code.name())) {
            returnSize += function.GetDeclaration().returnTypes()[i].size().size;
            returnSizes[i] = function.GetDeclaration().returnTypes()[i].size().size;
          }
        }

        continue;
      }

      returnSize += symbolTable.getType(retVal.name()).size().size;
      returnSizes[i] = symbolTable.getType(retVal.name()).size().size;
    }

    // Reserve space for return values and parameters
    Append("    ; Reserving space and moving parameters");
    if (returnSize + paramsSize != 0) {
      Append("    sub rsp, " + (returnSize + paramsSize));
    }

    // Move parameters
    int offset = 0;
    for (int i = 0; i < code.params().length; ++i) {
      var param = code.params()[i];

      switch (param) {
      case TAC.Constant constant -> {
        var reg = TAC.Register.GetRegister(TAC.Register.Type.RAX, constant.type().size());
        Append(Codegen.MoveToRegister(reg, constant));
        Append(Codegen.MoveToMemory(new TAC.OffsetMemory(constant.type().size(), TAC.Register.RSP, null, 0, offset), reg));
      }
      case TAC.Variable (String name) -> {
        var reg = TAC.Register.GetRegister(TAC.Register.Type.RAX, symbolTable.getType(name).size());
        Append(Codegen.MoveToRegister(reg, symbolTable.GetMemoryOperand(name), false));
        Append(Codegen.MoveToMemory(new TAC.OffsetMemory(symbolTable.getType(name).size(), TAC.Register.RSP, null, 0, offset), reg));
      }
      case TAC.Register register -> {
        var reg = TAC.Register.GetRegister(TAC.Register.Type.RAX, register.size());
        Append(Codegen.MoveToRegister(reg, register, false));
        Append(Codegen.MoveToMemory(new TAC.OffsetMemory(register.size(), TAC.Register.RSP, null, 0, offset), reg));
      }
      default -> throw new IllegalStateException("Unexpected value: " + param);
      }

      offset += paramSizes[i];
    }

    // Call
    Append("    ; Calling function");
    Append("    call     " + code.name());

    // Comment
    Append("    ; Unloading return values");

    // Move retvals to variables
    offset = paramsSize;
    for (int i = 0; i < code.returnVariables().length; ++i) {
      var retVal = code.returnVariables()[i];
      if (retVal == null) {
        // ignore
        offset += returnSizes[i];
        continue;
      }

      var reg = TAC.Register.GetRegister(TAC.Register.Type.RAX, symbolTable.getType(retVal.name()).size());

      // Move from memory to rax
      Append(Codegen.MoveToRegister(reg, new TAC.OffsetMemory(symbolTable.getType(retVal.name()).size(), TAC.Register.RSP, null, 0, offset), false));

      // Move from rax to var memory
      Append(Codegen.MoveToMemory(symbolTable.GetMemoryOperand(retVal.name()), reg));

      offset += returnSizes[i];
    }

    // Deallocate return+param space
    Append("    ; Deallocating return+param space");
    if (returnSize + paramsSize != 0) {
      Append("    add rsp, " + (returnSize + paramsSize));
    }
  }

  private static int GetFunctionParametersSize(FunctionDeclaration func) {
    int size = 0;
    for (var param : func.parameters()) {
      size += param.type().size().size;
    }

    return size;
  }

  private void translateReturn(TAC.Return code) {
    // TODO Depending on the convention

    // TODO
    // Move return values to the reserved space
    // Restore frame
    // Call ret
    // TODO only variables or immediate. Symbol table must know low level location (imm, register, memory)

    // So, bruh, we need to get current function frame,
    // And we can precalculate that. But... it depends on conventions

    int paramsSize = GetFunctionParametersSize(currentFrame.GetDeclaration());
    int offset = 16 + paramsSize;

    Append("    ; Moving return values");
    for (int i = 0; i < code.operands().length; ++i) {
      var operand = code.operands()[i];
      int operandSize = switch (operand) {
        case TAC.Constant (String value, TAC.Type type) -> operandSize = type.size().size;
        case TAC.Variable (String name) -> operandSize = symbolTable.getType(name).size().size;
        default -> throw new IllegalStateException("Unexpected value: " + operand);
      };

      // From where? Depending on the return operand. For now, it can be either constant either variable
      // Move to the corresponding memory location

      // Load return value into rax
      switch (operand) {
      case TAC.Constant _, TAC.Variable _ -> {}
      default -> throw new IllegalStateException("Unexpected value: " + operand);
      }

      // TODO need a function to move into a register of corresponding size. Need to return the register
      var reg = TAC.Register.GetRegister(TAC.Register.Type.RAX, TAC.Size.GetSize(operandSize));
      Append(MoveToRegister(reg, operand));

      // Move rax to parameter memory location
      Append(Codegen.MoveToMemory(new TAC.OffsetMemory(TAC.Size.GetSize(operandSize), TAC.Register.RBP, null, 0, offset), reg));

      offset += operandSize;
    }

    // Restore frame, call return
    Append("    ; Restoring frame");
    Append("    mov     rsp, rbp");
    Append("    pop     rbp");
    Append("    ret");
  }

  private void translateConditionalJump(TAC.ConditionalJump cj) {
    // Assume comparison is between same-sized types for simplicity
    // TODO yeah, yeah, different sign, different size. We must have some uncomparable units. u64 and i64. Need upcast if signs differ
    TAC.Type opType = determineOperandType(cj.arg1());

    // 1. Load operands into registers
    var rax = TAC.Register.GetRegister(TAC.Register.Type.RAX, opType.size());
    var rcx = TAC.Register.GetRegister(TAC.Register.Type.RCX, opType.size());

    Append(MoveToRegister(rax, cj.arg1()));
    Append(MoveToRegister(rcx, cj.arg2()));

    // 2. Compare the two registers
    Append("    cmp     %s, %s", rax.name(), rcx.name());

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
    Append(MoveToRegister(TAC.Register.GetRegister(TAC.Register.Type.RAX, resultType.size()), assignment.source()));

    // From RAX to memory
    Append(Codegen.MoveToMemory(symbolTable.GetMemoryOperand(assignment.result().name()), TAC.Register.GetRegister(TAC.Register.Type.RAX, resultType.size())));
  }

  private void translateBinaryOperation(TAC.BinaryOperation op) {
    TAC.Type resultType = symbolTable.getType(op.result().name());

    // 1. Load arg1 into RAX
    Append(MoveToRegister(TAC.Register.GetRegister(TAC.Register.Type.RAX, resultType.size()), op.arg1()));

    // 2. Load arg2 into RCX
    Append(MoveToRegister(TAC.Register.GetRegister(TAC.Register.Type.RCX, resultType.size()), op.arg2()));

    String regA = TAC.Register.GetRegister(TAC.Register.Type.RAX, resultType.size()).name();
    String regC = TAC.Register.GetRegister(TAC.Register.Type.RCX, resultType.size()).name();

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
        case QWORD -> Append("    cqo");
        case DWORD -> Append("    cdq");
        case WORD -> Append("    cwd");
        case BYTE -> Append("    cbw"); // Extends AL into AX, not quite RDX
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
        String regD = TAC.Register.GetRegister(TAC.Register.Type.RDX, resultType.size()).name();
        Append("    mov     %s, %s", regA, regD);
      }
    }
    }

    // 4. Store the result from RAX back to the variable's stack location
    Append(Codegen.MoveToMemory(symbolTable.GetMemoryOperand(op.result().name()), TAC.Register.GetRegister(TAC.Register.Type.RAX, resultType.size())));
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
      return t1.size().size >= t2.size().size ? t1 : t2;
    }
    throw new IllegalStateException("Cannot determine type for " + var.name());
  }
}
