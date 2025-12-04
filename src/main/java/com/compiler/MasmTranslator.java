package com.compiler;

import com.compiler.codegen.IntegerMoveHandlers;
import com.compiler.symbols.*;

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
    // TODO extract
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
    var memoryManager = ctx.memoryManager;
    var function = ctx.function;

    // Code translation
    int stackSize = 0;

    // Calculate stack size
    for (var local : function.locals) {
      int localSize = local.type.GetSize();

      stackSize += localSize;
      memoryManager.locations.put(local, new SymbolLocation(new OffsetMemory(localSize, Register.RBP, null, 0, -stackSize)));
    }

    // Prologue
    out.EmitComment("-- Prologue --");
    out.Emit("push rbp");
    out.Emit("mov rbp, rsp");

    // todo add parameters size (depending on convention)
    if (stackSize != 0) {
      out.EmitF("sub rsp, %s", stackSize);
    }

    // Code
    for (var code : function.codes) {
      out.EmitCommentF("-- %s --", code);
      switch (code) {
        case CodeAssign codeAssign -> TranslateCodeAssign(codeAssign, ctx);
        default -> throw new IllegalStateException("Unexpected code: " + code);
      }
    }

    // TODO ensure all global variables are in memory at this point. Ahhh, volatile, yeah

    // Epilogue
    out.EmitComment("-- Epilogue --");
    out.Emit("pop rbp");
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



  private void TranslateCodeAssign(CodeAssign codeAssign, FunctionContext ctx) {
    EmitMove(codeAssign.dst(), codeAssign.src(), ctx);
  }
}
