package com.compiler;

import com.compiler.codegen.IntegerMoveHandlers;
import com.compiler.codes.CodeAssign;
import com.compiler.codes.CodeCall;
import com.compiler.codes.CodeReturn;
import com.compiler.memory.MasmStorageClass;
import com.compiler.memory.OffsetMemory;
import com.compiler.memory.Register;
import com.compiler.structure.File;
import com.compiler.structure.Program;
import com.compiler.symbols.*;
import com.compiler.types.DtInteger;
import com.compiler.types.DtPointer;
import com.compiler.utils.CodeEmitter;
import com.compiler.utils.MasmStringUtils;
import com.compiler.utils.MasmTypeUtils;

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
    out = new CodeEmitter(true);

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

    // TODO External functions

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
        ctx.registerManager.PutVariable(param, reg.type());

        // TODO no.
        // Move to shadow space
        //ctx.out.EmitF("mov %s, %s", location.memory, reg);
      }
    }

    // TODO should I do it here or before function calls?

    // Code
    for (var code : function.codes) {
      out.EmitCommentF("-- %s --", code);
      switch (code) {
        case CodeAssign codeAssign -> TranslateCodeAssign(codeAssign, ctx);
        case CodeCall codeCall -> throw new RuntimeException("Not implemented");
        case CodeReturn codeReturn -> TranslateCodeReturn(codeReturn, ctx);
      }
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
    }
  }



  private void TranslateCodeAssign(CodeAssign code, FunctionContext ctx) {
    EmitMove(code.dst(), code.src(), ctx);
  }

  private void TranslateCodeReturn(CodeReturn code, FunctionContext ctx) {
    switch (ctx.function.declaration.convention()) {
      case MS_ABI -> TranslateCodeReturnMsAbi(code, ctx);
      default -> throw new RuntimeException("Not implemented");
    }
  }

  private void TranslateCodeReturnMsAbi(CodeReturn code, FunctionContext ctx) {
    var returnSymbol = code.returnValues()[0];

    // Free all registers
    out.EmitComment("-- Spill all registers --");
    ctx.registerManager.FlushRegisters();

    // Move the result to rax
    out.EmitComment("-- Move the result to rax --");
    ctx.EnsureInRegister(returnSymbol, Register.Type.RAX);
    out.EmitNL();

    // TODO may be useful to clean that information
    // TODO here we must spill everything to memory
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
}
