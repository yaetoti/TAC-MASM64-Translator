package com.compiler;

// TAC


import com.compiler.codes.CodeAssign;
import com.compiler.codes.CodeReturn;
import com.compiler.structure.*;
import com.compiler.structure.Module;
import com.compiler.symbols.*;
import com.compiler.types.DtInteger;
import com.compiler.types.DtPointer;
import com.compiler.types.IDataType;
import com.compiler.utils.Timer;

import java.util.*;

// Assumptions
// - Extern is a modifier, because we can do it, symbols don't store values. also because extern may be static or global

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
    var rootModule = new com.compiler.structure.Module();
    logicalStructure.rootModule = rootModule;
    rootModule.parentProgram = program;
    rootModule.parentStructure = logicalStructure;

    var module0 = new com.compiler.structure.Module();
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

    var staticVar0 = symbolFactory.CreateSymbolGlobalVariable(file0, rootModule, true, false, "sNumber0", new DtPointer(DtInteger.i8), new PointerConstant(globalVar0));
    file0.variables.add(staticVar0);
    rootModule.variables.add(staticVar0);

    // TODO test imported
    var importedVar0 = symbolFactory.CreateSymbolGlobalVariable(file1, rootModule, false, false, "NvOptimusEnabled", DtInteger.i8, new IntegerConstant("64"));
    file1.variables.add(importedVar0);
    rootModule.variables.add(importedVar0);
    file0.importedVariables.add(importedVar0);

    // Parameters


    // External functions
    var eFunction0 = symbolFactory.CreateSymbolGlobalFunction(
      file0, rootModule, true,
      new FunctionDeclaration(
        "ExitProcess",
        CallingConvention.MS_ABI,
        new IVariable[0],
        new IDataType[0]
      ),
      null
    );

    // Functions
    var function0 = symbolFactory.CreateSymbolGlobalFunction(
      file0, rootModule, false,
      new FunctionDeclaration("main", CallingConvention.MS_ABI, new IVariable[0], new IDataType[0]),
      new ArrayList<>()
    );
    file0.functions.add(function0);
    rootModule.functions.add(function0);

    // Locals
    var localVar0 = symbolFactory.CreateSymbolLocalVariable(function0, "temp0", DtInteger.i32);
    function0.locals.add(localVar0);

    var localVar1 = symbolFactory.CreateSymbolLocalVariable(function0, "temp1", DtInteger.i64);
    function0.locals.add(localVar1);

    // Code
    function0.codes.add(new CodeAssign(localVar0, new IntegerConstant("69")));
    function0.codes.add(new CodeAssign(localVar1, localVar0));
    function0.codes.add(new CodeReturn(new IVariable[] { localVar1 }));

    // Test
    for (var file : program.physicalStructure.files) {
      System.out.println("File: " + file.name);
    }

    traverseModules(program.logicalStructure.rootModule);

    System.out.println("\n\n=== Translating ===\n\n");

    // Translation
    var translator = new MasmTranslator();
    var timer = new Timer();

    timer.Start();
    translator.Translate(program);
    timer.Stop();
    System.out.println("Elapsed time: " + timer.GetElapsedMilliSeconds() + "ms");
  }

  static void traverseModules(Module module) {
    System.out.println("Module: " + module.name);
    for (var child : module.childModules) {
      traverseModules(child);
    }
  }
}
