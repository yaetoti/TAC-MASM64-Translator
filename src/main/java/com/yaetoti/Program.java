package com.yaetoti;

import java.util.ArrayList;
import java.util.List;

class PhysicalStructure {
  private ArrayList<GlobalSymbolTable.Symbol> staticVariables = new ArrayList<>();
  private ArrayList<Function> staticFunctions = new ArrayList<>();

  // MASM data
  // extern function imports
  // extern variable imports

  // global function imports
  // global variable imports

  // module function/variable imports
  // static class function/variable imports
  // class function imports (that's just functions after all)
}

class LogicalStructure {
  private ArrayList<GlobalSymbolTable.Symbol> globalVariables = new ArrayList<>();
  private ArrayList<Function> globalFunctions = new ArrayList<>();
}

public class Program {
  private ArrayList<GlobalSymbolTable.Symbol> globalSymbols = new ArrayList<>();
  private ArrayList<TranslationUnit> translationUnits = new ArrayList<>();

  public List<TranslationUnit> GetTranslationUnits() {
    return translationUnits;
  }

  public List<GlobalSymbolTable.Symbol> GetGlobalSymbols() {
    return globalSymbols;
  }

  public void AddTranslationUnit(TranslationUnit unit) {
    translationUnits.add(unit);
  }

  public void AddGlobalSymbol(GlobalSymbolTable.Symbol symbol) {
    globalSymbols.add(symbol);
  }
}

class TranslationUnit {
  private Program parent;
  private ArrayList<GlobalSymbolTable.Symbol> globalSymbols = new ArrayList<>();
  private ArrayList<GlobalSymbolTable.Symbol> externalSymbols = new ArrayList<>();
  private ArrayList<GlobalSymbolTable.Symbol> staticSymbols = new ArrayList<>();
  private ArrayList<FunctionDeclaration> functionImports = new ArrayList<>();
  private ArrayList<Function> functions = new ArrayList<>();

  public Program GetParent() {
    return parent;
  }

  public List<Function> GetFunctions() {
    return functions;
  }

  public List<FunctionDeclaration> GetFunctionImports() {
    return functionImports;
  }

  public FunctionDeclaration GetFunctionDeclaration(String name) {
    for (var declaration : functionImports) {
      if (declaration.name().equals(name)) {
        return declaration;
      }
    }

    for (var function : functions) {
      if (function.GetDeclaration().name().equals(name)) {
        return function.GetDeclaration();
      }
    }

    return null;
  }

  public List<GlobalSymbolTable.Symbol> GetStaticSymbols() {
    return staticSymbols;
  }

  public void SetParent(Program parent) {
    this.parent = parent;
  }

  public void AddFunctions(List<Function> functions) {
    this.functions.addAll(functions);
  }

  public void AddFunctionImports(List<FunctionDeclaration> imports) {
    functionImports.addAll(imports);
  }
}

record FunctionDeclaration(String name, CallingConvention convention, GlobalSymbolTable.Symbol[] parameters, DataType[] returnTypes) {}

class Function {
  // TODO function visibility public private

  // TODO extern functions does not have code, unit? maybe unit of definition, but not the unit of declaration
  private TranslationUnit parent;
  private FunctionDeclaration declaration;
  private final ArrayList<GlobalSymbolTable.Symbol> localSymbols = new ArrayList<>();
  private final ArrayList<TAC.Instruction> instructions = new ArrayList<>();

  public TranslationUnit GetParent() {
    return parent;
  }

  public FunctionDeclaration GetDeclaration() {
    return declaration;
  }

  public List<TAC.Instruction> GetInstructions() {
    return instructions;
  }

  public List<GlobalSymbolTable.Symbol> GetLocalSymbols() {
    return localSymbols;
  }

  public void SetParent(TranslationUnit parent) {
    this.parent = parent;
  }

  public void SetDeclaration(FunctionDeclaration declaration) {
    this.declaration = declaration;
  }

  public void AddLocalSymbols(List<GlobalSymbolTable.Symbol> symbols) {
    localSymbols.addAll(symbols);
  }

  public void AddInstructions(List<TAC.Instruction> instructions) {
    this.instructions.addAll(instructions);
  }
}
