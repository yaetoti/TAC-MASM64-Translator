package com.yaetoti;

public interface TAC {
  // Represents an operand, which can be a variable name or a constant value
  sealed interface Operand {}
  record Constant(String value, DataType type) implements Operand {}
  record Symbol(int symbolId) implements Operand {}

  // TODO the problem with variables is that we need to create it with data type. But why tf would we create it? We have them in declarations and that's it
  // TODO yeah, we may want to create declaration and multiple assignments: int a = 3; a = 32; a = 20;
  // TODO and that is the thing. Declaration is a different stuff. There we need to state the type. But for assignment and operations string is enough
  // TODO though, we need a variable operand, so...

  // Represents a single TAC instruction
  sealed interface Instruction {}
  // Represents 'result = source'
  record Assignment(Symbol result, Operand source) implements Instruction {}
  // Represents 'result = arg1 op arg2'
  record BinaryOperation(Symbol result, Operand arg1, Op op, Operand arg2) implements Instruction {}
  // NEW INSTRUCTIONS FOR CONTROL FLOW
  record Label(String name) implements Instruction {}
  record Jump(String targetLabel) implements Instruction {}
  record ConditionalJump(Operand arg1, ComparisonOp op, Operand arg2, String targetLabel) implements Instruction {}
  // Even worse, my instructions o:
  record Return(Operand... operands) implements Instruction {}
  // returnVariables may contain nulls for '_'
  record Call(String name, Operand[] params, Symbol[] returnVariables) implements Instruction {}


  // Enum for the arithmetic operations
  enum Op {
    ADD,
    SUB,
    MUL,
    DIV,
    MOD
  }

  // NEW ENUM FOR COMPARISON OPERATIONS
  enum ComparisonOp {
    EQ,  // Equal (JE)
    NE,  // Not Equal (JNE)
    LT,  // Less Than (JL)
    LE,  // Less or Equal (JLE)
    GT,  // Greater Than (JG)
    GE;  // Greater or Equal (JGE)
  }
}
