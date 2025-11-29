package com.compiler;

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
    System.out.println("-- Translating file: " + file.fullPath + " --");

    // Initialize
    sbFile = new StringBuilder();

    // Generate

    // Public symbols
    // TODO handle modifier
    for (var variable : file.variables) {
      if (variable.isExternal() || variable.isStatic()) {
        continue;
      }

      sbFile.append("public ");
      sbFile.append(variable.name());
      sbFile.append('\n');
    }

    sbFile.append('\n');

    // Extern functions
    for (var function : file.functions) {
      if (!function.isExternal()) {
        continue;
      }

      sbFile.append("extern ");
      sbFile.append(function.declaration().name());
      sbFile.append(" : PROC");
      sbFile.append('\n');
    }

    sbFile.append('\n');

    // Extern symbols
    // TODO data type
    for (var variable : file.variables) {
      if (!variable.isExternal()) {
        continue;
      }

      sbFile.append("extern ");
      sbFile.append(variable.name());
      sbFile.append(" : ");
      sbFile.append(MasmStringUtils.GetTypeString(variable.dataType()));
      sbFile.append('\n');
    }

    sbFile.append('\n');

    // External functions

    sbFile.append('\n');

    // === DATA ===
    sbFile.append(".data\n");

    // Symbol definitions
    // TODO data type
    // TODO constant value
    // TODO pointers
    for (var variable : file.variables) {
      if (variable.isExternal()) {
        continue;
      }

      sbFile.append(variable.name());
      sbFile.append(' ');
      sbFile.append(MasmStringUtils.GetDeclarationString(variable.dataType()));
      sbFile.append(' ');
      sbFile.append(MasmStringUtils.GetConstantString(variable.constant()));
      sbFile.append('\n');
    }

    sbFile.append('\n');

    // === CODE ===
    sbFile.append(".code\n");

    // TODO extract
    for (var function : file.functions) {
      if (function.isExternal()) {
        continue;
      }

      sbFile.append(function.declaration().name());
      sbFile.append(" proc\n");

      // TODO we need to allocate a place on the stack
      sbFile.append("BRUH, we have variales:\n");
      for (var local : function.locals()) {
        sbFile.append(local.name());
        sbFile.append("\n");
      }

      sbFile.append(function.declaration().name());
      sbFile.append(" endp\n");
    }

    System.out.println(sbFile);
  }
}
