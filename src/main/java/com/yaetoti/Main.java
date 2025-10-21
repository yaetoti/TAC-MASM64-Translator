package com.yaetoti;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Questions
// Is


class LabelGenerator {
  int nextLabelIndex = 0;
  public String GenLabel() {
    return "L" + (nextLabelIndex++);
  }
}

class Program {
  record FunctionEntry(String name, FunctionFrame frame) {}

  private final List<FunctionFrame> m_frames;
  // TODO extern declarations
  //private final List<FunctionDeclaration> m_declarations;

  public Program(List<FunctionFrame> frames) {
    m_frames = frames;
  }

//  public List<FunctionDeclaration> GetDeclarations() {
//    return m_declarations;
//  }

  public List<FunctionFrame> GetFunctions() {
    return m_frames;
  }
}

/*
TODO For return values:
return x, y, z

- x y z are variables or constant
- Check type correspondence
- write operands to memory


TODO for parameters:
Only Type. Other things are defined by conventions

u32 a, u64 b, u32 c

call (a, b, c; x, y, z)
- Check type correspondence
- allocate space for return + params
- place params

* */

class FunctionFrame {
  private final FunctionDeclaration m_declaration;
  private final List<TAC.Instruction> m_instructions;

  public FunctionFrame(FunctionDeclaration declaration, List<TAC.Instruction> instructions) {
    m_declaration = declaration;
    m_instructions = instructions;
  }

  public List<TAC.Instruction> GetInstructions() {
    return m_instructions;
  }

  public FunctionDeclaration GetDeclaration() {
    return m_declaration;
  }
}


record Parameter(String name, TAC.Type type) {}
record FunctionDeclaration(String name, Parameter[] parameters, TAC.Type[] returnTypes) {}

// Set parameters
// +Get parameters
// +Set return value
// Get return value

// TODO add instruction call
// TODO add instruction multiple assignment
// For that we need to store all elements that are in stack into values. Or leave it as is, but append offsets. It is if we won't use stack for other purposes

// TODO handle function call (stackcall)
// TODO pass parameters
// TODO clean stack after call

// We get call("name", "param1, param2", "retVal1, _, retVal2") - name, parameters' name
// We get declaration for name: FuncDecl("hash", "u64 a, u64 b", "u8")
// IF "stackcall" convention
// Push parameters to stack one by one
// Add call "name"
// After that move result to certain variables
// deallocate parameter memory

// +Inside function (from the beginning)
// We get our function declaration by name: FuncDecl("hash", "u64 a, u64 b", "u8")
// We get our parameters (If reference - we can modify it. If can modify - need to copy. How to add offset to stack manager?)
// IF "stackcall"
// Add variables with parameter names, copy from these cells one by one

// Assignment:
// We get call() ... how do we assign call result to variables? We need to also save variables into call


// Stack parameters:
// param3
// param2
// param2
// return value
// rbp
// locals

// TODO problem. We can't write eax to stack, because there may be trash. Either clean it either
// But if we take only eax from that register, it will be fine

public class Main {
  // Define type constants for convenience

  public static void main(String[] args) {
    MasmGenerator generator = new MasmGenerator();

    // Create variables
    var a = new TAC.Variable("a");
    var b = new TAC.Variable("b");
    var number2 = new TAC.Variable("number2");
    var resultHash = new TAC.Variable("resultHash");

    // Create labels TODO separate labels for different functions
    LabelGenerator mainGen = new LabelGenerator();
    String ifLabel = mainGen.GenLabel();
    String endIfLabel = mainGen.GenLabel();

    /*

    i64, i64 hash(i64 number1, i64 number2) {
      i32 a = 5;
      return 0, number2;
    }

    i64, i64, i64 main() {
      i32 a = 5;
      i32 b = a <= 10 ? 100 : 200;
      var (_, resultHash) = hash(420, 69);
      return 0, 200, resultHash;
    }

     */

    // Create frames
    var hashFrame = new FunctionFrame(
      new FunctionDeclaration("hash", new Parameter[] {
        new Parameter("number1", TAC.Type.i64),
        new Parameter("number2", TAC.Type.i64)
      }, new TAC.Type[] { TAC.Type.i64, TAC.Type.i64 }),
      List.of(
        new TAC.Assignment(a, new TAC.Constant("5", TAC.Type.i32)),
        //new ITAC.Return(new ITAC.Constant("0", i64))
        // return 2 values: 0 and parameter 2
        new TAC.Return(new TAC.Constant("0", TAC.Type.i64), number2)
      )
    );

    var mainFrame = new FunctionFrame(
      new FunctionDeclaration("main", new Parameter[] {}, new TAC.Type[] {TAC.Type.i64, TAC.Type.i64, TAC.Type.i64}),
      List.of(
        new TAC.Assignment(a, new TAC.Constant("5", TAC.Type.i32)),

        new TAC.ConditionalJump(a, TAC.ComparisonOp.LE, new TAC.Constant("10", TAC.Type.i32), ifLabel),
        new TAC.Assignment(b, new TAC.Constant("100", TAC.Type.i32)),
        new TAC.Jump(endIfLabel),
        new TAC.Label(ifLabel),
        new TAC.Assignment(b, new TAC.Constant("200", TAC.Type.i32)),
        new TAC.Label(endIfLabel),

        // Call hash. Pass 2 parameters. Get the second return value
        new TAC.Assignment(resultHash, new TAC.Constant("0", TAC.Type.i64)),
        new TAC.Call("hash", new TAC.Operand[] {
            new TAC.Constant("420", TAC.Type.i64),
            new TAC.Constant("69", TAC.Type.i64)
          },
          new TAC.Variable[] { null, resultHash }
        ),

        new TAC.Return(new TAC.Constant("0", TAC.Type.i64), new TAC.Constant("200", TAC.Type.i64), resultHash)
      )
    );

    // Create program
    var program = new Program(List.of(
      hashFrame,
      mainFrame
    ));

    // Generate code
    String masmCode = generator.Generate(program);
    System.out.println(masmCode);
  }
}
