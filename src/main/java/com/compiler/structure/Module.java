package com.compiler.structure;

import com.compiler.symbols.SymbolGlobalFunction;
import com.compiler.symbols.SymbolGlobalVariable;

import java.util.ArrayList;

public class Module {
  public Program parentProgram;
  public LogicalStructure parentStructure;
  public Module parentModule;
  public ArrayList<Module> childModules = new ArrayList<>();

  public String name;

  // All variables
  public ArrayList<SymbolGlobalVariable> variables = new ArrayList<>();
  public ArrayList<SymbolGlobalFunction> functions = new ArrayList<>();
}
