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

      // TODO extract
      var memoryManager = new FunctionMemoryManager();

      // Code translation
      int stackSize = 0;

      // Calculate stack size
      for (var local : function.locals) {
        int localSize = local.type.GetSize();
        var masmType = MasmTypeUtils.GetMasmType(local.type);

        stackSize += localSize;

        memoryManager.locations.put(local, new SymbolLocation(new OffsetMemory(masmType, Register.RBP, null, 0, -stackSize)));

        // Add memory address

        // Can we move in directly into a register?
        // IDataType -> MasmType / null

        // IDataType -> MasmSymbolInfo
        // or switch IDataType
      }

      // Prologue
      out.EmitComment("-- Prologue --");
      out.Emit("push rbp");
      out.Emit("mov rbp, rsp");

      // todo allocate stack
      if (stackSize != 0) {
        out.EmitF("sub rsp, %s", stackSize);
      }

      // Code
      for (var code : function.codes) {
        out.EmitCommentF("-- %s --", code);
        switch (code) {
          case CodeAssign codeAssign -> {
            switch (codeAssign.dst()) {
              case IVariable variable -> {
                var location = memoryManager.locations.get(variable);
                switch (variable.GetDataType()) {
                  case DtInteger dtInteger -> {
                    switch (codeAssign.src()) {
                      case IntegerConstant iConstant -> {
                        if (location.register != null) {
                          out.EmitF("mov %s, %s", location.register.name(), iConstant.value());
                        }

                        out.EmitF("mov %s, %s", location.memory, iConstant.value());
                      }
                      case IVariable iSymbol -> {

                      }
                      default -> throw new IllegalStateException("Not supported");
                    }
                  }
                  case DtPointer dtPointer -> {
                    throw new IllegalStateException("Not implemented");
                  }
                }
              }
              default -> throw new IllegalStateException("Unexpected value: " + codeAssign.dst());
            }
          }
          default -> throw new IllegalStateException("Unexpected code: " + code);
        }
      }

      // Epilogue
      out.EmitComment("-- Epilogue --");
      out.Emit("pop rbp");

      out.DecreaseIndent();
      out.EmitF("%s endp", function.declaration.name());
    }

    System.out.println(out.Collect());
  }
}
