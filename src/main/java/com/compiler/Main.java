package com.compiler;

// TAC


import com.compiler.symbols.*;

import java.util.*;

// Assumptions
// - Extern is a modifier, because we can do it, symbols don't store values. also because extern may be static or global

class FunctionContext {
  public RegisterManager registerManager;
  public FunctionMemoryManager memoryManager;
}

class FunctionMemoryManager {
  public HashMap<ISymbol, SymbolLocation> locations = new HashMap<>();
}

class SymbolLocation {
  public Memory memory;
  public Register.Type register;
  public boolean isDirty; // If both locations present, but data in register is newer

  public SymbolLocation(Memory memory) {
    this.memory = memory;
  }

  public SymbolLocation(Register.Type register) {
    this.register = register;
  }

  public SymbolLocation(Memory memory, Register.Type register) {
    this.memory = memory;
    this.register = register;
  }

  public SymbolLocation(Memory memory, Register.Type register, boolean isDirty) {
    this.memory = memory;
    this.register = register;
    this.isDirty = isDirty;
  }
}

// Tasks
// - add symbol with memory location (locals)
// - add symbol with register location (parameters)
// - add symbol with both memory and register location (?)
// - move symbol from memory to register
// - move symbol from register to memory
// - get register with symbol (if its not in a register - place it into one)
// - get memory with symbol (if newer value is in register - move to memory)
// - move from memory to memory (memory -> register -> memory in MASM)

public class Main {
  static void main() {
    test1();
    //test2();
  }

  static void test2() {
    // Register Allocator
    // Get all free registers
    // Get free register
    RegisterManager rm = new RegisterManager();
    var registers = rm.GetRegisters();
    var free = rm.GetFreeRegister();
    var frees = rm.GetFreeRegisters(3);

    System.out.println("end");
  }

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
    file0.name = "file0";
    file0.fullPath = "file0.y";

    var file1 = new File();
    physicalStructure.files.add(file1);
    file1.parentProgram = program;
    file1.parentStructure = physicalStructure;
    file1.name = "file1";
    file1.fullPath = "file1.y";

    // Symbol factory
    var symbolFactory = new SymbolFactory();

    // Variables
    var globalVar0 = symbolFactory.CreateSymbolGlobalVariable(file0, rootModule, false, false, "number0", DtInteger.i8, new IntegerConstant("64"));
    file0.variables.add(globalVar0);
    rootModule.variables.add(globalVar0);

    var globalVar1 = symbolFactory.CreateSymbolGlobalVariable(file0, rootModule, false, false, "pointer0", new DtPointer(DtInteger.i8), new PointerConstant(globalVar0));
    file0.variables.add(globalVar1);
    rootModule.variables.add(globalVar1);

    var globalVar2 = symbolFactory.CreateSymbolGlobalVariable(file0, rootModule, true, false, "number1", DtInteger.i64, null);
    file0.variables.add(globalVar2);
    rootModule.variables.add(globalVar2);

    var staticVar0 = symbolFactory.CreateSymbolGlobalVariable(file0, rootModule, true, true, "sNumber0", new DtPointer(DtInteger.i8), new PointerConstant(globalVar0));
    file0.variables.add(staticVar0);
    rootModule.variables.add(staticVar0);

    // TODO test imported
    var importedVar0 = symbolFactory.CreateSymbolGlobalVariable(file1, rootModule, false, false, "NvOptimusEnabled", DtInteger.i8, new IntegerConstant("64"));
    file1.variables.add(importedVar0);
    rootModule.variables.add(importedVar0);
    file0.importedVariables.add(importedVar0);

    // Functions
    var function0 = symbolFactory.CreateSymbolGlobalFunction(
      file0, rootModule, false,
      new FunctionDeclaration("main", CallingConvention.MS_ABI, new ISymbol[0], new IDataType[0]),
      new ArrayList<>()
    );
    file0.functions.add(function0);
    rootModule.functions.add(function0);

    // Locals
    var localVar0 = symbolFactory.CreateSymbolLocalVariable(function0, "temp0", DtInteger.u64);
    function0.locals.add(localVar0);

    var localVar1 = symbolFactory.CreateSymbolLocalVariable(function0, "temp1", DtInteger.i32);
    function0.locals.add(localVar1);

    // Code
    function0.codes.add(new CodeAssign(localVar0, new IntegerConstant("69")));
    function0.codes.add(new CodeAssign(localVar1, new IntegerConstant("420")));

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
}
