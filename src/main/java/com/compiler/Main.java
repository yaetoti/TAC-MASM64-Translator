package com.compiler;

// TAC


import com.compiler.ir.CallingConvention;
import com.compiler.ir.FunctionDeclaration;
import com.compiler.ir.codes.CodeAssign;
import com.compiler.ir.codes.CodeCall;
import com.compiler.ir.codes.CodeReturn;
import com.compiler.ir.structure.File;
import com.compiler.ir.structure.Program;
import com.compiler.ir.symbols.IVariable;
import com.compiler.ir.symbols.IntegerConstant;
import com.compiler.ir.symbols.PointerConstant;
import com.compiler.ir.symbols.SymbolFactory;
import com.compiler.ir.structure.Module;
import com.compiler.ir.types.DtInteger;
import com.compiler.ir.types.DtPointer;
import com.compiler.ir.types.IDataType;
import com.compiler.translator.masm.MasmTranslator;
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
    var logicalStructure = program.logicalStructure;
    var physicalStructure = program.physicalStructure;

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

    var staticVar0 = symbolFactory.CreateSymbolGlobalVariable(file0, rootModule, true, false, "sNumber0", new DtPointer(DtInteger.i8), new PointerConstant(globalVar0));
    file0.variables.add(staticVar0);
    rootModule.variables.add(staticVar0);

    // TODO test imported
    var importedVar0 = symbolFactory.CreateSymbolGlobalVariable(file1, rootModule, false, false, "NvOptimusEnabled", DtInteger.i8, new IntegerConstant("64"));
    file1.variables.add(importedVar0);
    rootModule.variables.add(importedVar0);
    file0.importedVariables.add(importedVar0);

    // Parameters
    var eParam0 = symbolFactory.CreateSymbolParameter("uExitCode", DtInteger.u32);

    var eParam1 = symbolFactory.CreateSymbolParameter("dwFreq", DtInteger.u32);
    var eParam2 = symbolFactory.CreateSymbolParameter("dwDuration", DtInteger.u32);

    // External functions
    var eFunction0 = symbolFactory.CreateSymbolGlobalFunction(
      file0, rootModule, true,
      new FunctionDeclaration(
        "ExitProcess",
        CallingConvention.MS_ABI,
        new IVariable[] { eParam0 },
        new IDataType[0]
      ),
      null
    );
    file0.functions.add(eFunction0);
    rootModule.functions.add(eFunction0);

    var eFunction1 = symbolFactory.CreateSymbolGlobalFunction(
      file0, rootModule, true,
      new FunctionDeclaration(
        "Beep",
        CallingConvention.MS_ABI,
        new IVariable[] { eParam1, eParam2 },
        new IDataType[0]
      ),
      null
    );
    file0.functions.add(eFunction1);
    rootModule.functions.add(eFunction1);

    // Functions
    var function0 = symbolFactory.CreateSymbolGlobalFunction(
      file0, rootModule, false,
      new FunctionDeclaration("main", CallingConvention.MS_ABI, new IVariable[0], new IDataType[0]),
      new ArrayList<>()
    );
    file0.functions.add(function0);
    rootModule.functions.add(function0);

    // Locals
    var localVar0 = symbolFactory.CreateSymbolLocalVariable(function0, "temp0", DtInteger.u32);
    function0.locals.add(localVar0);

    var localVar1 = symbolFactory.CreateSymbolLocalVariable(function0, "temp1", DtInteger.u32);
    function0.locals.add(localVar1);

    // Code
    function0.codes.add(new CodeAssign(localVar0, new IntegerConstant("800")));
    //function0.codes.add(new CodeAssign(localVar1, localVar0));
    function0.codes.add(new CodeAssign(localVar1, new IntegerConstant("2000")));
    function0.codes.add(new CodeCall(eFunction1, new IVariable[] { localVar0, localVar1 }, new IVariable[0]));

    function0.codes.add(new CodeAssign(localVar0, new IntegerConstant("69420")));
    function0.codes.add(new CodeCall(eFunction0, new IVariable[] { localVar0 }, new IVariable[0]));
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
