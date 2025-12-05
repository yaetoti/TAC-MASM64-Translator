package com.compiler.structure;

import com.compiler.symbols.SymbolGlobalFunction;
import com.compiler.symbols.SymbolGlobalVariable;

import java.util.ArrayList;

public class File {
  public Program parentProgram;
  public PhysicalStructure parentStructure;

  // File attributes
  public String name;
  public String fullPath;

  // Variables defined in that file
  public ArrayList<SymbolGlobalVariable> variables = new ArrayList<>();
  public ArrayList<SymbolGlobalFunction> functions = new ArrayList<>();

  // Import
  public ArrayList<SymbolGlobalVariable> importedVariables = new ArrayList<>();
}
