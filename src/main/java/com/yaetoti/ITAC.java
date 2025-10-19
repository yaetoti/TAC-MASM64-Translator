package com.yaetoti;
import java.util.List;

/**
 * A simple set of records to define a Three-Address Code (TAC) Intermediate Representation.
 */
public interface ITAC {
  // Represents a data type (e.g., s32, u64)
  record Type(String name, int size, boolean isSigned) {}

  // Represents an operand, which can be a variable name or a constant value
  sealed interface Operand permits Variable, Constant {}
  record Variable(String name) implements Operand {}
  record Constant(String value, Type type) implements Operand {}

  // Represents a single TAC instruction
  sealed interface Instruction permits Assignment, BinaryOperation, Label, Jump, ConditionalJump, Return {}
  // Represents 'result = source'
  record Assignment(Variable result, Operand source) implements Instruction {}
  // Represents 'result = arg1 op arg2'
  record BinaryOperation(Variable result, Operand arg1, Op op, Operand arg2) implements Instruction {}
  // NEW INSTRUCTIONS FOR CONTROL FLOW
  record Label(String name) implements Instruction {}
  record Jump(String targetLabel) implements Instruction {}
  record ConditionalJump(Operand arg1, ComparisonOp op, Operand arg2, String targetLabel) implements Instruction {}
  // Even worse, my instructions o:
  record Return(Operand... operands) implements Instruction {}


  // Enum for the arithmetic operations
  enum Op {
    ADD("+"), SUB("-"), MUL("*"), DIV("/"), MOD("%");
    public final String symbol;
    Op(String symbol) { this.symbol = symbol; }
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

  // A simple container for a list of instructions
  record Program(List<Instruction> instructions) {}
}
