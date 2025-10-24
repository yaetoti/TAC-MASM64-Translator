package com.yaetoti;

public class MasmGenerator {
  public GlobalSymbolTable globalTable;
  public MasmSymbolTable masmTable;
  public TranslationUnit currentUnit;
  public Function currentFunction;
  public Program currentProgram;

  public final StringBuilder code = new StringBuilder();
  public int labelCounter = 0;

  public void Append(String format, Object... args) {
    code.append(String.format(format, args)).append("\n");
  }

  // Helpers

  // TODO move to codegen probably
  private String MoveToRegister(MASM.Register dst, TAC.Operand src) {
    return switch (src) {
      case TAC.Constant constant -> Codegen.MoveToRegister(dst, constant);
      case TAC.Symbol symbol -> {
        var entry = globalTable.GetSymbol(symbol.symbolId());
        var location = masmTable.GetSymbolLocation(symbol.symbolId());

        yield switch (location) {
        case MASM.Memory memory -> Codegen.MoveToRegister(dst, memory, entry.type().type() == DataType.Type.SIGNED);
        case MASM.Register register -> Codegen.MoveToRegister(dst, register, entry.type().type() == DataType.Type.SIGNED);
        default -> throw new IllegalStateException("Immediate values are broken at the moment");
        };
      }
    };
  }

  // TODO new function
  /// isSigned is used for promotion if one is supposed to happen
  private void MoveToLocation(MASM.Location dst, MASM.Operand src, boolean isSigned) {
    switch (src) {
      // From immediate
      case MASM.Immediate immediate -> {
        switch (dst) {
          case MASM.Memory memory -> {
            // The problem is, we don't know what registers we can use at the moment. rax may be filled with something
            // The planning should be beforehand, but tac is not enough for that
            // TODO Compound: to register and to memory, because "mov r/m64, imm32"
            // TODO For such kind of shenanigans we need to operate with register allocator
            throw new IllegalStateException("Immediate to memory move not implemented yet");
          }
          case MASM.Register register -> Append(Codegen.MoveToRegister(register, immediate));
        }
      }
      // From register
      case MASM.Register register -> {
        switch (dst) {
          case MASM.Memory memory -> Append(Codegen.MoveToMemory(memory, register));
          case MASM.Register dstRegister -> Append(Codegen.MoveToRegister(dstRegister, register, isSigned));
        }
      }
      // From memory
      case MASM.Memory memory -> {
        switch (dst) {
          case MASM.Memory dstMemory -> {
            // TODO m -> r -> m
            // TODO For such kind of shenanigans we need to operate with register allocator
            throw new IllegalStateException("Memory to memory move not implemented yet");
          }
          case MASM.Register register -> Append(Codegen.MoveToRegister(register, memory, isSigned));
        }
      }
    }
  }


  // Not helpers (problems creaters)


  // For now it'll just tell how much we need
  class AllocationManager {
    private int reservedSpace;

    public AllocationManager() {

    }

    public void ReserveStack(int size) {
      reservedSpace += size;
    }

    public int GetReservedSpace() {
      return reservedSpace;
    }
  }

  public String Generate(Program program, GlobalSymbolTable symbolTable) {
    currentProgram = program;
    globalTable = symbolTable;
    masmTable = new MasmSymbolTable(program);

    // TODO refactor pretty printing

    // TODO Build MASM symbol table

    // TODO Generate code for every output file. Generate different files
    for (var unit : program.GetTranslationUnits()) {
      currentUnit = unit;

      // Generate data
      Append(".data");

      // TODO generate imports, globals, statics
      // TODO extern functions

      // Generate code
      Append(".code");

      for (var function : unit.GetFunctions()) {
        currentFunction = function;

        // TODO function may be private
        // Declaration
        Append(function.GetDeclaration().name() + " proc");

        // Allocate memory for locals
        AllocationManager allocManager = new AllocationManager();
        for (var symbol : function.GetLocalSymbols()) {
          // TODO that's a bit weird that we map it like that
          var masmType = MASM.Type.FromSize(symbol.type().size());
          allocManager.ReserveStack(masmType.size);
        }

        // Prologue (convention dependant)
        switch (function.GetDeclaration().convention()) {
        case STACKCALL -> GeneratePrologueStackCall(function, allocManager);
        default -> throw new IllegalStateException("Unexpected value: " + function.GetDeclaration().convention());
        }

        // Generate code
        for (var instruction : function.GetInstructions()) {
          translateInstruction(instruction);
        }

        // End
        Append(function.GetDeclaration().name() + " endp");
      }

      // Generate end
      Append("end");
    }

    return code.toString();
  }

  public void GeneratePrologueStackCall(Function function, AllocationManager allocManager) {
    // TODO this will be useful for stdcall
//    for (var symbol : function.GetDeclaration().parameters()) {
//    }

    Append("    push    rbp");
    Append("    mov     rbp, rsp");
    Append("    sub     rsp, " + allocManager.GetReservedSpace());
  }

  private void translateInstruction(TAC.Instruction instruction) {
    // TODO Add a small helper to not print "TACI$..." for cleaner comments
    String instructionString = instruction.toString()
      .replaceAll("TACI\\$[A-Za-z]+", "")
      .replaceAll("records\\.", "");

    Append("\n    ; TAC: %s", instructionString);

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
    // TODO nah no way bro, how we gonna know how much we need to take? Theres a way, reverse parameters, get n, but wtf even is that

    // TODO ??
    // TODO check stack size. We were pushing 64 every time. Smart or dumb
    // Let's stop on i64 u64 for now.

    // TODO one of the problems: on the top level we do not work with registers. Registers are allocated. We work with either constants either variables

    // Calculate sizes
    int returnSize = 0;
    int paramsSize = 0;
    int[] paramSizes = new int[code.params().length];
    int[] returnSizes = new int[code.returnVariables().length];

    // TODO Can I somehow extract this code?
    // TODO conventions
    // Calculate params size
    for (int i = 0; i < code.params().length; ++i) {
      var param = code.params()[i];
      int paramSize = GetOperandMasmType(param).size;

      paramsSize += paramSize;
      paramSizes[i] = paramSize;
    }

    // Calculate return values size
    for (int i = 0; i < code.returnVariables().length; ++i) {
      var retVal = code.returnVariables()[i];

      // If it is '_', we need to find function declaration, get type and use it as size
      if (retVal == null) {
        var declaration = currentUnit.GetFunctionDeclaration(code.name());
        for (var type : declaration.returnTypes()) {
          returnSize += type.size();
          returnSizes[i] = type.size();
        }

        continue;
      }

      var masmType = GetOperandMasmType(retVal);
      returnSize += masmType.size;
      returnSizes[i] = masmType.size;
    }

    // Reserve space for return values and parameters
    Append("    ; Reserving space and moving parameters");
    if (returnSize + paramsSize != 0) {
      Append("    sub      rsp, " + (returnSize + paramsSize));
    }

    // Move parameters
    int offset = 0;
    for (int i = 0; i < code.params().length; ++i) {
      var param = code.params()[i];

      switch (param) {
      case TAC.Constant constant -> {
        var masmType = MASM.Type.FromSize(constant.type().size());
        var reg = MASM.Register.GetRegister(MASM.Register.Type.RAX, masmType);
        Append(Codegen.MoveToRegister(reg, constant));
        Append(Codegen.MoveToMemory(new MASM.OffsetMemory(masmType, MASM.Register.RSP, null, 0, offset), reg));
      }
      case TAC.Symbol symbol -> {
        var masmType = GetOperandMasmType(symbol);
        var reg = MASM.Register.GetRegister(MASM.Register.Type.RAX, masmType);
        masmTable.GetSymbolLocation(symbol.symbolId());
        // TODO just use corresponding size, stfu
        Append(MoveToRegister(reg, symbol));
        Append(Codegen.MoveToMemory(new MASM.OffsetMemory(masmType, MASM.Register.RSP, null, 0, offset), reg));
      }
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
      // Skip '_' variables
      if (retVal == null) {
        offset += returnSizes[i];
        continue;
      }

      var dataType = globalTable.GetSymbol(retVal.symbolId()).type();
      var masmType = GetOperandMasmType(retVal);
      var masmLocation = masmTable.GetSymbolLocation(retVal.symbolId());
      var reg = MASM.Register.GetRegister(MASM.Register.Type.RAX, masmType);

      // Move from memory to rax
      Append(Codegen.MoveToRegister(reg, new MASM.OffsetMemory(masmType, MASM.Register.RSP, null, 0, offset), false));

      // Move from rax to var memory
      switch (masmLocation) {
      case MASM.Memory memory -> Append(Codegen.MoveToMemory(memory, reg));
      case MASM.Register register -> Append(Codegen.MoveToRegister(register, reg, dataType.type() == DataType.Type.SIGNED));
      // TODO if it fucking can't you need to fucking create another interface
      default -> throw new IllegalStateException("Location can't be immediate");
      }

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
      size += param.type().size();
    }

    return size;
  }

  private void translateReturn(TAC.Return code) {
    // TODO ???
    // So, bruh, we need to get current function frame,
    // And we can precalculate that. But... it depends on conventions

    // Calculate offsets
    int paramsSize = GetFunctionParametersSize(currentFunction.GetDeclaration());
    int offset = 16 + paramsSize;

    // TODO conventions
    // Move return values to the appropriate places
    Append("    ; Moving return values");
    for (int i = 0; i < code.operands().length; ++i) {
      var operand = code.operands()[i];
      int operandSize = GetOperandMasmType(operand).size;

      // TODO need a function to move into a register of corresponding size. Need to return the register
      // Load return value into rax
      var reg = MASM.Register.GetRegister(MASM.Register.Type.RAX, MASM.Type.FromSize(operandSize));
      Append(MoveToRegister(reg, operand));

      // Move rax to parameter memory location
      Append(Codegen.MoveToMemory(new MASM.OffsetMemory(MASM.Type.FromSize(operandSize), MASM.Register.RBP, null, 0, offset), reg));

      offset += operandSize;
    }

    // Restore frame, call return
    Append("    ; Restoring frame");
    Append("    mov     rsp, rbp");
    Append("    pop     rbp");
    Append("    ret");
  }

  // TODO this is fucked. Every high level operand must have a type.. Ahhh, symbols. Alright, alrght
  // Helper to find an operand's type, needed for ConditionalJump
  private DataType GetOperandType(TAC.Operand operand) {
    return switch (operand) {
      case TAC.Constant constant -> constant.type();
      case TAC.Symbol symbol -> globalTable.GetSymbol(symbol.symbolId()).type();
    };
  }

  private MASM.Type GetOperandMasmType(TAC.Operand operand) {
    return switch (operand) {
      case TAC.Constant constant -> MASM.Type.FromSize(constant.type().size());
      case TAC.Symbol symbol -> MASM.Type.FromSize(GetOperandType(symbol).size());
    };
  }

  private void translateConditionalJump(TAC.ConditionalJump cj) {
    // Assume comparison is between same-sized types for simplicity
    // TODO yeah, yeah, different sign, different size. We must have some uncomparable units. u64 and i64. Need upcast if signs differ
    var opType = GetOperandMasmType(cj.arg1());

    // 1. Load operands into registers
    var rax = MASM.Register.GetRegister(MASM.Register.Type.RAX, opType);
    var rcx = MASM.Register.GetRegister(MASM.Register.Type.RCX, opType);

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

  private void translateAssignment(TAC.Assignment assignment) {
    var dataType = globalTable.GetSymbol(assignment.result().symbolId()).type();
    var masmType = GetOperandMasmType(assignment.result());
    var resultLocation = masmTable.GetSymbolLocation(assignment.result().symbolId());

    // TODO replace appends with function calls
    // From anywhere to RAX
    Append(MoveToRegister(MASM.Register.GetRegister(MASM.Register.Type.RAX, masmType), assignment.source()));

    // From RAX to memory
    // TODO aha, now we don't know where to move it
    MoveToLocation(resultLocation, MASM.Register.GetRegister(MASM.Register.Type.RAX, masmType), dataType.type() == DataType.Type.SIGNED);
  }

  private void translateBinaryOperation(TAC.BinaryOperation op) {
    DataType resultType = globalTable.GetSymbol(op.result().symbolId()).type();
    var masmLocation = masmTable.GetSymbolLocation(op.result().symbolId());
    var masmType = MASM.Type.FromSize(resultType.size());

    // TODO for now we assume that types are of correct size

    // 1. Load arg1 into RAX
    Append(MoveToRegister(MASM.Register.GetRegister(MASM.Register.Type.RAX, masmType), op.arg1()));

    // 2. Load arg2 into RCX
    Append(MoveToRegister(MASM.Register.GetRegister(MASM.Register.Type.RCX, masmType), op.arg2()));

    var regA = MASM.Register.GetRegister(MASM.Register.Type.RAX, masmType);
    var regC = MASM.Register.GetRegister(MASM.Register.Type.RCX, masmType);

    // 3. Perform the operation
    switch (op.op()) {
    case ADD -> Append("    add     %s, %s", regA.name(), regC.name());
    case SUB -> Append("    sub     %s, %s", regA.name(), regC.name());
    case MUL -> {
      if (resultType.type() == DataType.Type.SIGNED) {
        Append("    imul    %s, %s", regA.name(), regC.name());
      } else {
        Append("    mul     %s", regC.name()); // result in RDX:RAX
      }
    }
    case DIV, MOD -> {
      // TODO this is fucked. Location can be anything, but we try to extend register. Maybe not, but look at this
      // Dividend is already in RAX. Prepare RDX.
      if (resultType.type() == DataType.Type.SIGNED) {
        switch (masmType) { // Sign-extend RAX into RDX
        case QWORD -> Append("    cqo");
        case DWORD -> Append("    cdq");
        case WORD -> Append("    cwd");
        case BYTE -> Append("    cbw"); // Extends AL into AX, not quite RDX
        }
      } else {
        Append("    xor     rdx, rdx  ; Clear RDX for unsigned division");
      }

      if (resultType.type() == DataType.Type.SIGNED) {
        Append("    idiv    %s", regC.name());
      } else {
        Append("    div     %s", regC.name());
      }

      if (op.op() == TAC.Op.MOD) {
        // Remainder is in RDX, move it to RAX for storing
        String regD = MASM.Register.GetRegister(MASM.Register.Type.RDX, masmType).name();
        Append("    mov     %s, %s", regA, regD);
      }
    }
    }

    // TODO yeah, memory. It needs to be in memory. OR from anywhere to memory
    // 4. Store the result from RAX back to the variable's stack location
    MoveToLocation(masmLocation, regA, resultType.type() == DataType.Type.SIGNED);
  }
}
