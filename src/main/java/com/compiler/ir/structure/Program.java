package com.compiler.ir.structure;

public class Program {
  public PhysicalStructure physicalStructure;
  public LogicalStructure logicalStructure;

  public Program() {
    physicalStructure = new PhysicalStructure();
    physicalStructure.parent = this;

    logicalStructure = new LogicalStructure();
    logicalStructure.parent = this;
  }
}
