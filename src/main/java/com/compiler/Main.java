package com.compiler;

// === Program ===

import java.util.ArrayList;

class Program {
  public PhysicalStructure physicalStructure;
  public LogicalStructure logicalStructure;
}


// === Structure Root ===


class PhysicalStructure {
  public Program parentProgram;

  public ArrayList<File> files = new ArrayList<>();
}

class LogicalStructure {
  public Program parentProgram;

  public Module rootModule;
}


// === Structure ===


class File {
  public Program parentProgram;
  public PhysicalStructure parentStructure;

  // File attributes
  public String name;
  public String fullPath;

  // Variables defined in that file
  public ArrayList<SymbolGlobalVariable> globalVariables = new ArrayList<>();
  public ArrayList<SymbolStaticVariable> staticVariables = new ArrayList<>();
}

class Module {
  public Program parentProgram;
  public LogicalStructure parentStructure;
  public Module parentModule;
  public ArrayList<Module> childModules = new ArrayList<>();

  public String name;

  // All variables
  public ArrayList<SymbolGlobalVariable> globalVariables = new ArrayList<>();
}


// === Symbols ===


// Assumptions
// - Extern is a modifier, because we can do it, symbols don't store values. also because extern may be static or global

// Constants. Are not symbols

// TODO Add float, struct, array, VLA

sealed interface IDataType {}
final class DtInteger implements IDataType {
  public enum Sign {
    SIGNED,
    UNSIGNED
  }

  public static final DtInteger i8 = new DtInteger(1, Sign.SIGNED);
  public static final DtInteger i16 = new DtInteger(2, Sign.SIGNED);
  public static final DtInteger i32 = new DtInteger(4, Sign.SIGNED);
  public static final DtInteger i64 = new DtInteger(8, Sign.SIGNED);
  public static final DtInteger u8 = new DtInteger(1, Sign.UNSIGNED);
  public static final DtInteger u16 = new DtInteger(2, Sign.UNSIGNED);
  public static final DtInteger u32 = new DtInteger(4, Sign.UNSIGNED);
  public static final DtInteger u64 = new DtInteger(8, Sign.UNSIGNED);

  private final int m_size;
  private final Sign m_sign;

  private DtInteger(int size, Sign sign) {
    m_size = size;
    m_sign = sign;
  }

  public int GetSize() {
    return m_size;
  }

  public Sign GetSign() {
    return m_sign;
  }
}
record DtPointer(IDataType underlyingType) implements IDataType {}

// TODO float
// TODO array
// TODO struct
// TODO string
// TODO enum
// TODO VLA
// TODO class



sealed interface IConstant {}
// TODO type?
record IntegerConstant(String value) implements IConstant {}
record PointerConstant(ISymbol symbol) implements IConstant {}

sealed interface ISymbol {
  String GetName();
}
//record SymbolVariable() implements ISymbol {}
// Defined in file, access from anywhere
record SymbolGlobalVariable(File file, Module module, boolean isExternal, String name, IDataType dataType, IConstant constant) implements ISymbol {
  @Override
  public String GetName() {
    return name;
  }
}
// Defined in file, access from file
record SymbolStaticVariable(File file, boolean isExternal, String name, IDataType dataType, IConstant constant) implements ISymbol {
  @Override
  public String GetName() {
    return name;
  }
}
//record SymbolFunction(File file) implements ISymbol {}
//record SymbolExternalFunction() implements ISymbol {}


// === Utils ===
final class MasmStringUtils {
  private static final String DB = "db";
  private static final String DW = "dw";
  private static final String DD = "dd";
  private static final String DQ = "dq";

  private static final String BYTE = "byte";
  private static final String WORD = "word";
  private static final String DWORD = "dword";
  private static final String QWORD = "qword";

  public static String GetDeclarationString(IDataType type) {
    switch (type) {
      case DtInteger dtInteger -> {
        return switch (dtInteger.GetSize()) {
          case 1 -> DB;
          case 2 -> DW;
          case 4 -> DD;
          case 8 -> DQ;
          default -> throw new IllegalStateException("Unexpected size: " + dtInteger.GetSize());
        };
      }
      case DtPointer dtPointer -> {
        return DQ;
      }
      default -> throw new IllegalStateException("Unexpected type: " + type);
    }
  }

  public static String GetTypeString(IDataType type) {
    switch (type) {
      case DtInteger dtInteger -> {
        return switch (dtInteger.GetSize()) {
          case 1 -> BYTE;
          case 2 -> WORD;
          case 4 -> DWORD;
          case 8 -> QWORD;
          default -> throw new IllegalStateException("Unexpected size: " + dtInteger.GetSize());
        };
      }
      case DtPointer dtPointer -> {
        return QWORD;
      }
      default -> throw new IllegalStateException("Unexpected type: " + type);
    }
  }

  public static String GetConstantString(IConstant constant) {
    if (constant == null) {
      return "?";
    }

    // TODO name mangling for symbols
    switch (constant) {
      case IntegerConstant integerConstant -> {
        return integerConstant.value();
      }
      case PointerConstant pointerConstant -> {
        return pointerConstant.symbol().GetName();
      }
      default -> throw new IllegalStateException("Unexpected value: " + constant);
    }
  }
}

final class MasmTranslator {
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
    for (var globalVar : file.globalVariables) {
      if (globalVar.isExternal()) {
        continue;
      }

      sbFile.append("public ");
      sbFile.append(globalVar.name());
      sbFile.append('\n');
    }

    sbFile.append('\n');

    // === DATA ===
    sbFile.append(".data\n");

    // Extern symbols
    // TODO data type
    for (var globalVar : file.globalVariables) {
      if (!globalVar.isExternal()) {
        continue;
      }

      sbFile.append("extern ");
      sbFile.append(globalVar.name());
      sbFile.append(" : ");
      sbFile.append(MasmStringUtils.GetTypeString(globalVar.dataType()));
      sbFile.append('\n');
    }

    for (var staticVar : file.staticVariables) {
      if (!staticVar.isExternal()) {
        continue;
      }

      sbFile.append("extern ");
      sbFile.append(staticVar.name());
      sbFile.append(" : ");
      sbFile.append(MasmStringUtils.GetTypeString(staticVar.dataType()));
      sbFile.append('\n');
    }

    sbFile.append('\n');

    // Symbol definitions
    // TODO data type
    // TODO constant value
    // TODO pointers
    for (var globalVar : file.globalVariables) {
      if (globalVar.isExternal()) {
        continue;
      }

      sbFile.append(globalVar.name());
      sbFile.append(' ');
      sbFile.append(MasmStringUtils.GetDeclarationString(globalVar.dataType()));
      sbFile.append(' ');
      sbFile.append(MasmStringUtils.GetConstantString(globalVar.constant()));
      sbFile.append('\n');
    }

    for (var staticVar : file.staticVariables) {
      if (staticVar.isExternal()) {
        continue;
      }

      sbFile.append(staticVar.name());
      sbFile.append(' ');
      sbFile.append(MasmStringUtils.GetDeclarationString(staticVar.dataType()));
      sbFile.append(' ');
      sbFile.append(MasmStringUtils.GetConstantString(staticVar.constant()));
      sbFile.append('\n');
    }

    sbFile.append('\n');

    System.out.println(sbFile);
  }
}

public class Main {
  static void test1() {
    // Program
    var program = new Program();

    // Structures
    var logicalStructure = new LogicalStructure();
    logicalStructure.parentProgram = program;
    program.logicalStructure = logicalStructure;

    var physicalStructure = new PhysicalStructure();
    physicalStructure.parentProgram = program;
    program.physicalStructure = physicalStructure;

    // Modules
    var rootModule = new Module();
    logicalStructure.rootModule = rootModule;
    rootModule.parentProgram = program;
    rootModule.parentStructure = logicalStructure;

    var module0 = new Module();
    rootModule.childModules.add(module0);
    module0.parentProgram = program;
    module0.parentStructure = logicalStructure;
    module0.parentModule = rootModule;
    module0.name = "std";

    // Files
    var file0 = new File();
    physicalStructure.files.add(file0);
    file0.parentProgram = program;
    file0.parentStructure = physicalStructure;
    file0.name = "main";
    file0.fullPath = "main.y";

    // Variables
    var globalVar0 = new SymbolGlobalVariable(file0, rootModule, false, "number0", DtInteger.i8, new IntegerConstant("64"));
    file0.globalVariables.add(globalVar0);
    rootModule.globalVariables.add(globalVar0);

    var globalVar1 = new SymbolGlobalVariable(file0, rootModule, false, "pointer0", new DtPointer(DtInteger.i8), new PointerConstant(globalVar0));
    file0.globalVariables.add(globalVar1);
    rootModule.globalVariables.add(globalVar1);

    var globalVar2 = new SymbolGlobalVariable(file0, rootModule, false, "number1", DtInteger.i64, null);
    file0.globalVariables.add(globalVar2);
    rootModule.globalVariables.add(globalVar2);

    var staticVar0 = new SymbolStaticVariable(file0, true, "sNumber0", new DtPointer(DtInteger.i8), new PointerConstant(globalVar0));
    file0.staticVariables.add(staticVar0);

    // Test
    for (var file : program.physicalStructure.files) {
      System.out.println("File: " + file.name);
    }

    traverseModules(program.logicalStructure.rootModule);

    System.out.println("\n\n=== Translating ===\n\n");

    // Translation
    var translator = new MasmTranslator();
    translator.Translate(program);
  }

  static void traverseModules(Module module) {
    System.out.println("Module: " + module.name);
    for (var child : module.childModules) {
      traverseModules(child);
    }
  }

  static void main() {
    test1();
  }
}
