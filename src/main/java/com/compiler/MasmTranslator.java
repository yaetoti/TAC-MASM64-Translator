package com.compiler;

import com.compiler.symbols.*;

public final class MasmTranslator {
  public MasmTranslator() {

  }

  public StringBuilder sbFile;

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
    sbFile = new StringBuilder();

    // Generate

    // Public symbols
    // TODO handle modifier
    sbFile.append("; === PUBLIC ===\n");
    for (var variable : file.variables) {
      if (variable.isExternal || variable.isStatic) {
        continue;
      }

      sbFile.append("public ");
      sbFile.append(variable.name);
      sbFile.append('\n');
    }

    sbFile.append('\n');

    // TODO public/private functions

    // Extern functions
    sbFile.append("; === EXTERN FUNCTIONS ===\n");
    for (var function : file.functions) {
      if (!function.isExternal) {
        continue;
      }

      sbFile.append("extern ");
      sbFile.append(function.declaration.name());
      sbFile.append(" : PROC");
      sbFile.append('\n');
    }

    sbFile.append('\n');

    // Extern symbols
    sbFile.append("; === EXTERN SYMBOLS ===\n");
    for (var variable : file.variables) {
      if (!variable.isExternal) {
        continue;
      }

      sbFile.append("extern ");
      sbFile.append(variable.name);
      sbFile.append(" : ");
      sbFile.append(MasmStringUtils.GetTypeString(variable.type));
      sbFile.append('\n');
    }

    sbFile.append('\n');

    // Imported symbols
    sbFile.append("; === IMPORTED SYMBOLS ===\n");
    for (var variable : file.importedVariables) {
      sbFile.append("extern ");
      sbFile.append(variable.name);
      sbFile.append(" : ");
      sbFile.append(MasmStringUtils.GetTypeString(variable.type));
      sbFile.append('\n');
    }

    sbFile.append('\n');

    // TODO External functions

    // === DATA ===
    sbFile.append(".data\n");

    // Symbol definitions
    // TODO data type
    // TODO constant value
    // TODO pointers
    sbFile.append("; === SYMBOL DEFINITIONS ===\n");
    for (var variable : file.variables) {
      if (variable.isExternal) {
        continue;
      }

      sbFile.append(variable.name);
      sbFile.append(' ');
      sbFile.append(MasmStringUtils.GetDeclarationString(variable.type));
      sbFile.append(' ');
      sbFile.append(MasmStringUtils.GetConstantString(variable.constant));
      sbFile.append('\n');
    }

    sbFile.append('\n');

    // === CODE ===
    sbFile.append(".code\n");

    sbFile.append("; === FUNCTION DEFINITIONS ===\n");
    // TODO extract
    for (var function : file.functions) {
      if (function.isExternal) {
        continue;
      }

      sbFile.append(function.declaration.name());
      sbFile.append(" proc\n");

      // TODO extract
      var memoryManager = new FunctionMemoryManager();

      // Code translation
      int stackSize = 0;

      // Calculate stack size
      for (var local : function.locals) {
        int localSize = local.type.GetSize();
        var masmType = MasmTypeUtils.GetMasmType(local.type);

        stackSize += localSize;

        memoryManager.locations.put(local, new SymbolLocation(new OffsetMemory(masmType, Register.RBP, null, 0, stackSize)));

        // Add memory address

        // Can we move in directly into a register?
        // IDataType -> MasmType / null

        // IDataType -> MasmSymbolInfo
        // or switch IDataType
      }

      // Prologue
      sbFile.append("  ; -- Prologue --\n");
      sbFile.append("  push rbp\n");
      sbFile.append("  mov rbp, rsp\n");

      // todo allocate stack
      if (stackSize != 0) {
        sbFile.append("  sub rsp, ").append(stackSize).append('\n');
      }

      // Code
      for (var code : function.codes) {
        sbFile.append("  ; -- ").append(code).append(" --\n");
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
                          sbFile.append("  mov ")
                            .append(location.register.name())
                            .append(", ")
                            .append(iConstant.value())
                            .append('\n');
                        }

                        sbFile.append("  mov ")
                          .append(location.memory)
                          .append(", ")
                          .append(iConstant.value())
                          .append('\n');;
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
      sbFile.append("  ; -- Epilogue --\n");
      sbFile.append("  pop rbp\n");

      sbFile.append(function.declaration.name());
      sbFile.append(" endp\n");
    }

    System.out.println(sbFile);
  }
}
