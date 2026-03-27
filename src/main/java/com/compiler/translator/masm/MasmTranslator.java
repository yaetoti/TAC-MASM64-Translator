package com.compiler.translator.masm;

import com.compiler.ir.codes.*;
import com.compiler.translator.masm.codegen.IntegerMoveHandlers;
import com.compiler.translator.masm.codegen.MasmMoveUtils;
import com.compiler.ir.symbols.*;
import com.compiler.translator.masm.context.FunctionContext;
import com.compiler.translator.masm.context.FunctionMemoryManager;
import com.compiler.translator.masm.context.RegisterManager;
import com.compiler.translator.masm.context.SymbolLocation;
import com.compiler.translator.masm.memory.MasmStorageClass;
import com.compiler.translator.masm.memory.OffsetMemory;
import com.compiler.translator.masm.memory.Register;
import com.compiler.ir.structure.File;
import com.compiler.ir.structure.Program;
import com.compiler.ir.types.DtInteger;
import com.compiler.ir.types.DtPointer;
import com.compiler.translator.masm.utils.CodeEmitter;
import com.compiler.translator.masm.utils.MasmStringUtils;
import com.compiler.translator.masm.utils.MasmTypeUtils;

import java.util.List;

public final class MasmTranslator {
  public MasmTranslator() {

  }

  public CodeEmitter out;

  public void Translate(Program program) {
    // Bruh, how do we generate code for variable initialization
    // Variables have default values. File values have them right away. Class variables are initialized in constructor or before it

    for (var file : program.physicalStructure.files) {
      TranslateFile(file);
    }
  }

  private void TranslateFile(File file) {
    System.out.println("\n\n\n--- Translating file: " + file.fullPath + " ---\n\n\n");

    // Initialize
    out = new CodeEmitter(false);

    // Generate

    // Public symbols
    // TODO handle modifier

    out.EmitComment("=== PUBLIC ===");
    for (var variable : file.variables) {
      if (variable.isExternal || variable.isStatic) {
        continue;
      }

      out.EmitF("public %s", variable.name);
    }

    out.EmitNL();

    // TODO public/private functions

    // Extern functions
    out.EmitComment("=== EXTERN FUNCTIONS ===");
    for (var function : file.functions) {
      if (!function.isExternal) {
        continue;
      }

      out.EmitF("extern %s : PROC", function.declaration.name());
    }

    out.EmitNL();

    // Extern symbols
    out.EmitComment("=== EXTERN SYMBOLS ===");
    for (var variable : file.variables) {
      if (!variable.isExternal) {
        continue;
      }

      out.EmitF("extern %s : %s", variable.name, MasmStringUtils.GetTypeString(variable.type));
    }

    out.EmitNL();

    // Imported symbols
    out.EmitComment("=== IMPORTED SYMBOLS ===");
    for (var variable : file.importedVariables) {
      out.EmitF("extern %s : %s", variable.name, MasmStringUtils.GetTypeString(variable.type));
    }

    out.EmitNL();

    // === DATA ===
    out.Emit(".data");

    // Symbol definitions
    // TODO data type
    // TODO constant value
    // TODO pointers
    out.EmitComment("=== SYMBOL DEFINITIONS ===");
    for (var variable : file.variables) {
      if (variable.isExternal) {
        continue;
      }

      out.EmitF("%s %s %s", variable.name, MasmStringUtils.GetDeclarationString(variable.type), MasmStringUtils.GetConstantString(variable.constant));
    }

    out.EmitNL();

    // === CODE ===
    out.Emit(".code");

    out.EmitComment("=== FUNCTION DEFINITIONS ===");
    for (var function : file.functions) {
      if (function.isExternal) {
        continue;
      }

      out.EmitF("%s proc", function.declaration.name());
      out.IncreaseIndent();

      var ctxFunc = new FunctionContext();
      ctxFunc.out = out;
      ctxFunc.function = function;
      ctxFunc.memoryManager = new FunctionMemoryManager();
      ctxFunc.registerManager = new RegisterManager(ctxFunc);

      TranslateFunction(ctxFunc);

      out.DecreaseIndent();
      out.EmitF("%s endp", function.declaration.name());
    }

    out.Emit("end");

    System.out.println(out.Collect());
  }

  private void TranslateFunction(FunctionContext ctx) {
    switch (ctx.function.declaration.convention()) {
      case STACK_CALL -> throw new IllegalStateException("Not implemented");
      case MS_ABI -> TranslateFunctionMsAbi(ctx);
    }
  }

  private void TranslateFunctionMsAbi(FunctionContext ctx) {
    var memoryManager = ctx.memoryManager;
    var function = ctx.function;

    // Initial stack size = 8 non-volatile registers
    ctx.IncreaseStackSize(56);

    // Calculate stack size + append locals
    int localsSize = 0;
    for (var local : function.locals) {
      int localSize = local.type.GetSize();

      localsSize += localSize;
      ctx.IncreaseStackSize(localSize);
      memoryManager.locations.put(local, new SymbolLocation(new OffsetMemory(localSize, Register.RBP, null, 0, -ctx.GetStackSize())));
    }

    // Prologue
    out.EmitComment("-- Prologue --");
    out.Emit("push rbp");
    out.Emit("mov rbp, rsp");
    out.EmitNL();

    // Save non-volatile registers
    out.EmitComment("-- Save non-volatile registers --");
    out.Emit("push rbx");
    out.Emit("push rsi");
    out.Emit("push rdi");
    out.Emit("push r12");
    out.Emit("push r13");
    out.Emit("push r14");
    out.Emit("push r15");
    out.EmitNL();

    // Allocation (locals + alignment)
    {
      // Calculate padding
      int padding = ((ctx.GetStackSize() + 15) / 16 * 16) - ctx.GetStackSize();
      int allocationSize = localsSize + padding;
      if (padding != 0) {
        ctx.IncreaseStackSize(padding);
      }

      // Allocate
      if (allocationSize != 0) {
        out.EmitCommentF("-- Allocate space for locals (%s bytes) + alignment (%s bytes) --", localsSize, padding);
        out.EmitF("sub rsp, %s", allocationSize);
      }
    }


    // TODO is parameter just a variable? wtf?

    // Append params location. Move parameters 0-3 to shadow space
    out.EmitComment("-- Move parameters to shadow space --");
    final var gprRegs = List.of(Register.Type.RCX, Register.Type.RDX, Register.Type.R8, Register.Type.R9);
    var params = ctx.function.declaration.parameters();

    for (int paramId = 0; paramId < params.length; ++paramId) {
      var param = params[paramId];
      var paramSize = param.GetDataType().GetSize();
      var storageClass = MasmTypeUtils.GetStorageClass(param.GetDataType());
      Register reg = null;

      // Choose register depending on the storage class (GPR/XMM/memory=null)
      if (paramId < 4) {
        if (storageClass == MasmStorageClass.GPR) {
          var type = gprRegs.get(paramId);
          reg = Register.Get(type, paramSize);
        }
      }

      // Set location
      var location = new SymbolLocation(new OffsetMemory(paramSize, Register.RBP, null, 0, 16 + paramId * 8), reg, reg != null);
      ctx.memoryManager.Set(param, location);

      // Set register + move to shadow space
      if (reg != null) {
        ctx.registerManager.Get(reg.type()).SetSymbol(param);

        // TODO no.
        // Move to shadow space
        //ctx.out.EmitF("mov %s, %s", location.memory, reg);
      }
    }

    // Code
    for (var code : function.codes) {
      out.EmitCommentF("-- %s --", code);
      switch (code) {
        case CodeAssign specificCode -> TranslateCodeAssign(specificCode, ctx);
        case CodeCall specificCode -> TranslateCodeCall(specificCode, ctx);
        case CodeReturn specificCode -> TranslateCodeReturn(specificCode, ctx);
        case CodeAssignArrayElement specificCode -> TranslateCodeAssignArrayElement(specificCode, ctx);
        case CodeLoadAddress specificCode -> TranslateCodeLoadAddress(specificCode, ctx);
      }
      out.EmitNL();
    }

    // TODO ensure all global variables are in memory at this point. Ahhh, volatile, yeah

    // Pop non-volatile registers
//    out.EmitComment("-- Restore non-volatile registers --");
//    out.Emit("pop r15");
//    out.Emit("pop r14");
//    out.Emit("pop r13");
//    out.Emit("pop r12");
//    out.Emit("pop rdi");
//    out.Emit("pop rsi");
//    out.Emit("pop rbx");
//    out.EmitNL();
//
//    // Epilogue
//    out.EmitComment("-- Epilogue --");
//    out.Emit("pop rbp");
  }

  // TODO move to a separate file. Allow emitting it anywhere
  // TODO also add move constant to registers, to memory

  private void EmitMove(IVariable dst, IOperand src, FunctionContext ctx) {
    switch (src) {
      case IConstant srcType -> EmitMoveConstant(dst, srcType, ctx);
      case ISymbol srcType -> EmitMoveSymbol(dst, srcType, ctx);
      default -> throw new IllegalStateException("Not supported");
    }
  }

  private void EmitMoveConstant(IVariable dst, IConstant src, FunctionContext ctx) {
    // Destination type
    switch (dst.GetDataType()) {
      case DtInteger dstDataType -> {
        // Source class
        switch (src) {
          case IntegerConstant srcType -> IntegerMoveHandlers.MoveIntegerConstant(dst, dstDataType, srcType, ctx);
          default -> throw new IllegalStateException("Not supported");
        }
      }
      case DtPointer dstDataType -> {
        // Source class
        switch (src) {
          case PointerConstant srcType -> throw new IllegalStateException("Not implemented");
          default -> throw new IllegalStateException("Not supported");
        }
      }
      default -> throw new IllegalStateException("Unexpected value: " + dst.GetDataType());
    }
  }

  private void EmitMoveSymbol(IVariable dst, ISymbol src, FunctionContext ctx) {
    // Destination type
    switch (dst.GetDataType()) {
      case DtInteger dstDataType -> {
        // Source class
        switch (src) {
          case IVariable srcType -> {
            // Source variable data type
            switch (srcType.GetDataType()) {
              case DtInteger srcDataType -> IntegerMoveHandlers.MoveIntegerSymbol(dst, dstDataType, srcType, srcDataType, ctx);
              default -> throw new IllegalStateException("Not supported");
            }
          }
          case SymbolGlobalFunction srcType -> throw new IllegalStateException("Not implemented");
        }
      }
      case DtPointer dstDataType -> throw new IllegalStateException("Not implemented");
      default -> throw new IllegalStateException("Unexpected value: " + dst.GetDataType());
    }
  }



  private void TranslateCodeAssign(CodeAssign code, FunctionContext ctx) {
    EmitMove(code.dst(), code.src(), ctx);
  }

  private void TranslateCodeLoadAddress(CodeLoadAddress code, FunctionContext ctx) {
    var reg = ctx.DefineRegister(code.dst());
    var memory = ctx.memoryManager.GetMemoryLocation(code.src());

    ctx.out.EmitComment("-- Load Address --");
    ctx.out.EmitF("lea %s, %s", reg, memory);
    ctx.out.EmitNL();
  }

  private void TranslateCodeAssignArrayElement(CodeAssignArrayElement code, FunctionContext ctx) {
    // Array is an address, that is stored either in register either in memory. Should be in register
    // A compiler can calculate index manually if we know the type
    // Conversion is not our concern, it is a separate code
    // TODO Only primitive types can be performed with mov. Moving arrays or structs is a non-trivial operation that will be implemented later

    // mov [rbx + rcx * 8], rcx
    // mov [rbx + rcx * 8], [rcx]
    // mov [rbx + rcx * 8], 12

    var arrayMemory = ctx.memoryManager.GetMemoryLocation(code.array());

    if (!(code.index() instanceof IntegerConstant index)) {
      throw new IllegalStateException("Not implemented");
    }

    switch (code.value()) {
      case IConstant iConstant -> {
        switch (iConstant) {
          case FloatConstant floatConstant -> {
            throw new IllegalStateException("Not implemented");
          }
          case IntegerConstant integerConstant -> {
            var elementSize = integerConstant.type().GetSize();
            var regInfo = ctx.registerManager.Acquire(RegisterManager.IS_GPR.and(RegisterManager.IS_EXACT_SIZE(elementSize)));
            var reg = regInfo.GetRegister(elementSize);

            ctx.out.EmitF("mov %s, %s", reg, integerConstant.value());
            ctx.out.EmitF("mov %s, %s", arrayMemory.Offset(elementSize * Integer.parseInt(index.value())), reg);
          }
          case PointerConstant pointerConstant -> {
            throw new IllegalStateException("Not implemented");
          }
        }
      }
      case ISymbol iSymbol -> {
        throw new IllegalStateException("Not implemented");
      }
    }
  }

  private void TranslateCodeReturn(CodeReturn code, FunctionContext ctx) {
    switch (ctx.function.declaration.convention()) {
      case MS_ABI -> TranslateCodeReturnMsAbi(code, ctx);
      case STACK_CALL -> throw new RuntimeException("Not implemented");
    }
  }

  private void TranslateCodeReturnMsAbi(CodeReturn code, FunctionContext ctx) {
    // Free all registers
    out.EmitComment("-- Flush all registers --");
    ctx.registerManager.FreeAll();

    // Move the result to rax
    if (code.returnValues().length != 0) {
      out.EmitComment("-- Move the result to rax --");
      var returnSymbol = code.returnValues()[0];
      ctx.EnsureInRegister(returnSymbol, Register.Type.RAX);
      out.EmitNL();
    }

    // TODO may be useful to clean that information
    // Pop non-volatile registers
    out.EmitComment("-- Restore non-volatile registers --");
    out.Emit("lea rsp, [rbp - 56]");
    out.Emit("pop r15");
    out.Emit("pop r14");
    out.Emit("pop r13");
    out.Emit("pop r12");
    out.Emit("pop rdi");
    out.Emit("pop rsi");
    out.Emit("pop rbx");
    out.EmitNL();

    // Epilogue
    out.EmitComment("-- Epilogue --");
    out.Emit("pop rbp");
    out.Emit("ret");
  }

  private void TranslateCodeCall(CodeCall code, FunctionContext ctx) {
    switch (code.function().GetDeclaration().convention()) {
      case STACK_CALL -> throw new RuntimeException("Not implemented");
      case MS_ABI -> TranslateCodeCallMsAbi(code, ctx);
    }
  }

  private void TranslateCodeCallMsAbi(CodeCall code, FunctionContext ctx) {
    // Save volatile registers
    ctx.registerManager.FreeAll();

    // Allocate space for (padding + params + shadow space)
    int allocSize = Math.max(4, code.params().length) * 8;
    int padding = ((allocSize + 15) / 16 * 16) - allocSize;
    int totalAllocSize = allocSize + padding;

    ctx.out.EmitF("sub rsp, %s", totalAllocSize);

    // Move first 4 parameters to registers
    int registerParamsNumber = Math.min(4, code.params().length);
    final var gpr = List.of(Register.Type.RCX, Register.Type.RDX, Register.Type.R8, Register.Type.R9);

    for (int i = 0; i < registerParamsNumber; ++i) {
      var param = code.params()[i];
      if (MasmTypeUtils.GetStorageClass(param.GetDataType()) == MasmStorageClass.GPR) {
        ctx.EnsureInRegister(code.params()[i], gpr.get(i));
        continue;
      }

      throw new RuntimeException("Not implemented");
    }

    // Move the remaining parameters to memory

    var tempRegInfo = ctx.registerManager.GetFreeRegister(Register.Bank.GPR);
    tempRegInfo.Lock();

    // TODO per-byte copy for non-register types
    for (int i = 4; i < code.params().length; ++i) {
      // Move param to memory location
      var param = code.params()[i];
      var paramSize = param.GetDataType().GetSize();
      var dstMemory = new OffsetMemory(paramSize, Register.RSP, null, 0, i * 8);
      var srcMemory = ctx.memoryManager.GetMemoryLocation(param);

      var reg = Register.Get(tempRegInfo.type, paramSize);
      MasmMoveUtils.MoveToMemory(ctx, dstMemory, srcMemory, reg);
    }

    tempRegInfo.Unlock();

    // Call function
    // TODO mangling
    ctx.out.EmitF("call %s", code.function().GetName());

    // Store return value
    var raxRegInfo = ctx.registerManager.Get(Register.Type.RAX);
    if (code.returnValues().length != 0) {
      var returnSymbol = code.returnValues()[0];
      var returnLocation = ctx.memoryManager.Get(returnSymbol);
      raxRegInfo.symbol = returnSymbol;
      returnLocation.register = Register.Get(Register.Type.RAX, returnSymbol.GetDataType().GetSize());
    }

    // Free stack memory
    ctx.out.EmitF("add rsp, %s", totalAllocSize);
  }
}
