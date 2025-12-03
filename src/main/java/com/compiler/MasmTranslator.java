package com.compiler;

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

    out.Append('\n');

    // TODO public/private functions

    // Extern functions
    out.EmitComment("=== EXTERN FUNCTIONS ===");
    for (var function : file.functions) {
      if (!function.isExternal) {
        continue;
      }

      out.EmitF("extern %s : PROC", function.declaration.name());
    }

    out.Append('\n');

    // Extern symbols
    out.EmitComment("=== EXTERN SYMBOLS ===");
    for (var variable : file.variables) {
      if (!variable.isExternal) {
        continue;
      }

      out.EmitF("extern %s : %s", variable.name, MasmStringUtils.GetTypeString(variable.type));
    }

    out.Append('\n');

    // Imported symbols
    out.EmitComment("=== IMPORTED SYMBOLS ===");
    for (var variable : file.importedVariables) {
      out.Append("extern ");
      out.Append(variable.name);
      out.Append(" : ");
      out.Append(MasmStringUtils.GetTypeString(variable.type));
      out.Append('\n');
    }

    out.Append('\n');

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

    out.Append('\n');

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
      ctxFunc.registerManager = new RegisterManager();

      TranslateFunction(ctxFunc);

      out.DecreaseIndent();
      out.EmitF("%s endp", function.declaration.name());
    }

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
      var masmType = MasmTypeUtils.GetMasmType(local.type);

      stackSize += localSize;

      memoryManager.locations.put(local, new SymbolLocation(new OffsetMemory(masmType, Register.RBP, null, 0, -stackSize)));
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

  private void TranslateCodeAssign(CodeAssign codeAssign, FunctionContext ctx) {
    var memoryManager = ctx.memoryManager;
    var registerManager = ctx.registerManager;

    // Move cases:
    // Variable <- Constant
    // Variable <- Variable

    // Which is:
    // Register <- Constant
    // Memory <- Constant /* which is essentially */ Memory <- Register <- Constant
    // And:
    // Register <- Register
    // Register <- Memory
    // Memory <- Register
    // Memory <- Memory /* which is essentially */ Memory <- Register <- Memory

    // So:
    // Register <- Constant (mov rax, 11)
    // Register <- Register (mov rax, rbx)
    // Register <- Memory (mov rax, qword ptr [rbp - 8])
    // Memory <- Register (mov qword ptr [rbp - 8], rax)

    // However, if we handle arrays, we must explicitly handle:
    // Memory <- Constant
    // Nevertheless, we still do this, but in a specific order:
    // Memory <- Register <- Constant

    // Finishing the line, I would like to not touch registers and memory at all. All I want is to handle this:
    // Variable <- Constant
    // Variable <- Variable
    // In any manner

    // Proposed methods:
    // Move(IVariable dst, IVariable src)
    // Move(IVariable dst, IConstant)


    // What's wrong at this point?
    // - We do not check if there are no free registers
    // - Yeah, I can move all the moving logic to FunctionContext. But. I need to know that it is used not only in CodeAssign. I need to know that ISymbol <- IConstant is used elsewhere


    var dstLocation = memoryManager.locations.get(codeAssign.dst());
    var dstMasmType = MasmTypeUtils.GetMasmType(codeAssign.dst().GetDataType());

    switch (codeAssign.dst().GetDataType()) {
      case DtInteger dtInteger -> {
        switch (codeAssign.src()) {
          case IntegerConstant iConstant -> {
            if (dstLocation.register != null) {
              out.EmitF("mov %s, %s", dstLocation.register.name(), iConstant.value());
            }

            var reg = ctx.registerManager.GetFreeRegister();
            var dstReg = Register.GetRegister(reg.type, dstMasmType).name();
            reg.symbol = codeAssign.dst();
            dstLocation.register = reg.type;

            out.EmitF("mov %s, %s", dstReg, iConstant.value());
          }
          case IVariable iSymbol -> {
            var srcLocation = memoryManager.locations.get(iSymbol);
            var srcMasmType = MasmTypeUtils.GetMasmType(iSymbol.GetDataType());

            if (dstLocation.register != null && srcLocation.register != null) {
              var dstReg = Register.GetRegister(dstLocation.register, dstMasmType).name();
              out.EmitF("mov %s, %s", dstReg, srcLocation.register.name());
              break;
            }

            if (dstLocation.register != null && srcLocation.register == null) {
              var dstReg = Register.GetRegister(dstLocation.register, dstMasmType).name();
              out.EmitF("mov %s, %s", dstReg, srcLocation.memory);
              break;
            }

            if (dstLocation.register == null && srcLocation.register != null) {
              var srcReg = Register.GetRegister(srcLocation.register, srcMasmType).name();

              var reg = registerManager.GetFreeRegister();
              var dstReg = Register.GetRegister(reg.type, srcMasmType).name();
              reg.symbol = codeAssign.dst();

              out.EmitF("mov %s, %s", dstReg, srcReg);
              break;
            }

            if (dstLocation.register == null && srcLocation.register == null) {
              // Register <- Memory
              // Assign register

              var reg = registerManager.GetFreeRegister();
              var dstReg = Register.GetRegister(reg.type, srcMasmType).name();
              reg.symbol = codeAssign.dst();

              out.EmitF("mov %s, %s", dstReg, srcLocation.memory);
              dstLocation.register = reg.type;
              break;
            }

            throw new IllegalStateException("Impossible case");
          }
          default -> throw new IllegalStateException("Not supported");
        }
      }
      default -> throw new IllegalStateException("Not implemented");
    }
  }
}
