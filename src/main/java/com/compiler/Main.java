package com.compiler;

// TAC


import com.compiler.ir.CallingConvention;
import com.compiler.ir.FunctionDeclaration;
import com.compiler.ir.codes.*;
import com.compiler.ir.structure.File;
import com.compiler.ir.structure.Program;
import com.compiler.ir.symbols.*;
import com.compiler.ir.structure.Module;
import com.compiler.ir.types.*;
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
    var globalVar0 = symbolFactory.CreateSymbolGlobalVariable(file0, rootModule, false, false, "number0", DtInteger.i8, new IntegerConstant("64", DtInteger.i8));
    file0.variables.add(globalVar0);
    rootModule.variables.add(globalVar0);

    var globalVar1 = symbolFactory.CreateSymbolGlobalVariable(file0, rootModule, false, false, "pointer0", new DtPointer(DtInteger.i8), new PointerConstant(globalVar0));
    file0.variables.add(globalVar1);
    rootModule.variables.add(globalVar1);

    var globalVar2 = symbolFactory.CreateSymbolGlobalVariable(file0, rootModule, true, false, "number1", DtInteger.i64, null);
    file0.variables.add(globalVar2);
    rootModule.variables.add(globalVar2);

    var globalVar3 = symbolFactory.CreateSymbolGlobalVariable(file0, rootModule, true, false, "fp1", DtFloat.f32, new FloatConstant("123.45"));
    file0.variables.add(globalVar3);
    rootModule.variables.add(globalVar3);

    var staticVar0 = symbolFactory.CreateSymbolGlobalVariable(file0, rootModule, true, false, "sNumber0", new DtPointer(DtInteger.i8), new PointerConstant(globalVar0));
    file0.variables.add(staticVar0);
    rootModule.variables.add(staticVar0);

    // TODO test imported
    var importedVar0 = symbolFactory.CreateSymbolGlobalVariable(file1, rootModule, false, false, "NvOptimusEnabled", DtInteger.i8, new IntegerConstant("64", DtInteger.i8));
    file1.variables.add(importedVar0);
    rootModule.variables.add(importedVar0);
    file0.importedVariables.add(importedVar0);

    // Parameters
    var eParam0 = symbolFactory.CreateSymbolParameter("uExitCode", DtInteger.u32);

    var eParam1 = symbolFactory.CreateSymbolParameter("dwFreq", DtInteger.u32);
    var eParam2 = symbolFactory.CreateSymbolParameter("dwDuration", DtInteger.u32);

    var eParam3 = symbolFactory.CreateSymbolParameter("hWnd", DtInteger.u64);
    var eParam4 = symbolFactory.CreateSymbolParameter("lpText", DtInteger.u64);
    var eParam5 = symbolFactory.CreateSymbolParameter("lpCaption", DtInteger.u64);
    var eParam6 = symbolFactory.CreateSymbolParameter("uType", DtInteger.u32);

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

    var eFunction2 = symbolFactory.CreateSymbolGlobalFunction(
      file0, rootModule, true,
      new FunctionDeclaration(
        "MessageBoxA",
        CallingConvention.MS_ABI,
        new IVariable[] { eParam3, eParam4, eParam5, eParam6 },
        new IDataType[] { DtInteger.i32 }
      ),
      null
    );
    file0.functions.add(eFunction2);
    rootModule.functions.add(eFunction2);

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

    var localVar2 = symbolFactory.CreateSymbolLocalVariable(function0, "uType", DtInteger.u32);
    function0.locals.add(localVar2);

    var localVar3 = symbolFactory.CreateSymbolLocalVariable(function0, "arr0", new DtArray(DtInteger.u8, 13));
    function0.locals.add(localVar3);

    var localVar4 = symbolFactory.CreateSymbolLocalVariable(function0, "arrPtr", new DtPointer(DtInteger.u8));
    function0.locals.add(localVar4);

    var localVar5 = symbolFactory.CreateSymbolLocalVariable(function0, "handle", DtInteger.u64);
    function0.locals.add(localVar5);

    var localVar6 = symbolFactory.CreateSymbolLocalVariable(function0, "result", DtInteger.i32);
    function0.locals.add(localVar6);

    // Code
    function0.codes.add(new CodeAssign(localVar0, new IntegerConstant("800", DtInteger.u32)));
    function0.codes.add(new CodeAssign(localVar1, new IntegerConstant("2000", DtInteger.u32)));
    function0.codes.add(new CodeCall(eFunction1, new IVariable[] { localVar0, localVar1 }, new IVariable[0]));

    // Call MessageBoxA
    function0.codes.add(new CodeAssign(localVar2, new IntegerConstant("0", DtInteger.u32)));
    function0.codes.add(new CodeAssign(localVar5, new IntegerConstant("0", DtInteger.u64)));

    // Fill a basePointer with "Hello, world"
    // 72 101 108 108 111 44 32 119 111 114 108 100

    // TODO We can do it without LEA, using RBP + (arrayOffset + index * size)
    function0.codes.add(new CodeLoadAddress(localVar4, localVar3));
    function0.codes.add(new CodeAssignArrayElement(localVar3, new IntegerConstant("0", DtInteger.i32), new IntegerConstant("72", DtInteger.u8)));
    function0.codes.add(new CodeAssignArrayElement(localVar3, new IntegerConstant("1", DtInteger.i32), new IntegerConstant("101", DtInteger.u8)));
    function0.codes.add(new CodeAssignArrayElement(localVar3, new IntegerConstant("2", DtInteger.i32), new IntegerConstant("108", DtInteger.u8)));
    function0.codes.add(new CodeAssignArrayElement(localVar3, new IntegerConstant("3", DtInteger.i32), new IntegerConstant("108", DtInteger.u8)));
    function0.codes.add(new CodeAssignArrayElement(localVar3, new IntegerConstant("4", DtInteger.i32), new IntegerConstant("111", DtInteger.u8)));
    function0.codes.add(new CodeAssignArrayElement(localVar3, new IntegerConstant("5", DtInteger.i32), new IntegerConstant("44", DtInteger.u8)));
    function0.codes.add(new CodeAssignArrayElement(localVar3, new IntegerConstant("6", DtInteger.i32), new IntegerConstant("32", DtInteger.u8)));
    function0.codes.add(new CodeAssignArrayElement(localVar3, new IntegerConstant("7", DtInteger.i32), new IntegerConstant("119", DtInteger.u8)));
    function0.codes.add(new CodeAssignArrayElement(localVar3, new IntegerConstant("8", DtInteger.i32), new IntegerConstant("111", DtInteger.u8)));
    function0.codes.add(new CodeAssignArrayElement(localVar3, new IntegerConstant("9", DtInteger.i32), new IntegerConstant("114", DtInteger.u8)));
    function0.codes.add(new CodeAssignArrayElement(localVar3, new IntegerConstant("10", DtInteger.i32), new IntegerConstant("108", DtInteger.u8)));
    function0.codes.add(new CodeAssignArrayElement(localVar3, new IntegerConstant("11", DtInteger.i32), new IntegerConstant("100", DtInteger.u8)));
    function0.codes.add(new CodeAssignArrayElement(localVar3, new IntegerConstant("12", DtInteger.i32), new IntegerConstant("0", DtInteger.u8)));

    function0.codes.add(new CodeCall(eFunction2, new IVariable[] { localVar5, localVar4, localVar4, localVar2 }, new IVariable[] { localVar6 }));

    // function0.codes.add(new CodeAssign(localVar0, new IntegerConstant("69420")));
    // function0.codes.add(new CodeCall(eFunction0, new IVariable[] { localVar0 }, new IVariable[0]));
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
