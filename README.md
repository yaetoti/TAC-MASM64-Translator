# TAC to MASM64 Translator

A compiler backend written in Java 25 that translates a structured intermediate representation (IR) into x64 Microsoft Macro Assembler (MASM) code.

This project implements core compiler components including symbol management, type systems, register allocation, and code generation adhering to the Microsoft x64 Calling Convention.

---

## Motivation

This project marks the next chapter in my research into the world of compiler construction.

While parser generators, intermediate representations, and high-level languages already exist to provide everything needed for software development, my goal is different: I want to master the art of creating the automation tools themselves.

> "Specific knowledge may fade, and code found on Google will need to be searched for again. But the tools for automation are used forever."

### Previous Work

My journey began with **compiler frontends**, covering lexical analysis, AST construction, and interpretation:
*   I started with a complex calculator.
*   Progressed to a truth-table generator for logical expressions.
*   Culminated in a custom interpreted language designed to solve a specific [CodinGame challenge](https://www.codingame.com/training/medium/complicated-interpreter/solution?id=36799323).
  *   [Link to Repository]

Later, I explored methods to accelerate parser development and improve error readability. I built a **Sprache-like parser combinator in C#** from scratch, implementing chained parser functions, enhanced error reporting, and greedy lexeme matching.
*   [Link to Repository]

### Current Focus & Roadmap

Now, I am exploring the **compiler backend**. At this stage, I have established the core principles of handling program structure, symbol management, register allocation, and native code generation.

**Future plans include:**
*   **Type System**: Support for standard C-like types (`float`, `array`, `struct`, `class`, `enum`).
*   **Modules**: Implementation of modular compilation and name mangling.
*   **OOP**: Full Object-Oriented Programming support.
*   **Native UI**: Capability to create windowed applications using **WinAPI** and **DirectX**.

*Optional research goals:* exploring modern memory models (similar to Rust), compiler optimizations, and security vulnerability mitigation on live examples.

---

## Features

- **Program Structure**: Hierarchical organization of code into Programs, Logical Structures (Modules), and Physical Structures (Files).
- **Type System**:
    - Support for Integers (`DtInteger`) of various widths (8, 16, 32, 64-bit) and signed/unsigned variants.
    - Pointer types (`DtPointer`).
- **Intermediate Representation (IR)**:
    - Abstract representation of logic using `CodeAssign`, `CodeCall`, and `CodeReturn`.
- **Memory Management**:
    - **Register Allocation**: Dynamic tracking of General Purpose Registers (GPR).
    - **Stack Management**: Automatic handling of stack frames, shadow space, and local variable alignment.
    - **Spilling**: Mechanism to flush registers to memory when pressure is high.
- **Code Generation**:
    - Targets **MASM (x64)**.
    - Implements **MS ABI** calling convention (handling volatile/non-volatile registers, shadow space, and parameter passing).
    - Supports external symbols and function calls (e.g., Windows API).

---

## Usage

Currently, the project does not have a frontend parser. The program structure and logic must be constructed programmatically using the `SymbolFactory`.

### Running the Compiler

The entry point is located in `com.compiler.Main`. It currently runs a test case that demonstrates:
1. Creating a program structure manually.
2. Defining global variables and external functions (`ExitProcess`, `Beep`).
3. Constructing a `main` function with local variables and IR instructions.
4. Invoking the `MasmTranslator` to generate assembly code.

To run the project:
1. Ensure you have **Java 25** installed.
2. Compile and run `com.compiler.Main`.
3. The generated MASM assembly will be output to the console.

### Example Construction

The `test1` method in `Main.java` constructs a program that essentially performs the following logic:
```java
extern void ExitProcess(u32 uExitCode);
extern void Beep(u32 dwFreq, u32 dwDuration);

void main() {
  u32 temp0 = 800;
  u32 temp1 = 2000;
  Beep(temp0, temp1);
  temp0 = 69420;
  ExitProcess(temp0);
  return temp1;
}
```

The translator converts this into x64 assembly, handling:
- Prologue/Epilogue generation.
- Stack allocation for locals.
- Parameter passing via registers (`RCX`, `RDX`, etc.) and shadow space.

The generated assembly is as follows:

**file0.y**:

```asm
public number0
public pointer0
extern ExitProcess : PROC
extern Beep : PROC
extern NvOptimusEnabled : byte
.data
number0 db 64
pointer0 dq number0
number1 dq ?
sNumber0 dq number0
.code
main proc
  push rbp
  mov rbp, rsp
  push rbx
  push rsi
  push rdi
  push r12
  push r13
  push r14
  push r15
  sub rsp, 8
  mov eax, 800
  mov ebx, 2000
  mov dword ptr [rbp - 60], eax
  mov dword ptr [rbp - 64], ebx
  sub rsp, 32
  mov ecx, dword ptr [rbp - 60]
  mov edx, dword ptr [rbp - 64]
  call Beep
  add rsp, 32
  mov ecx, 69420
  mov dword ptr [rbp - 60], ecx
  mov dword ptr [rbp - 64], edx
  sub rsp, 32
  mov ecx, dword ptr [rbp - 60]
  call ExitProcess
  add rsp, 32
  mov dword ptr [rbp - 60], ecx
  mov eax, dword ptr [rbp - 64]
  lea rsp, [rbp - 56]
  pop r15
  pop r14
  pop r13
  pop r12
  pop rdi
  pop rsi
  pop rbx
  pop rbp
  ret
main endp
end
```

**file1.y**:

```
public NvOptimusEnabled
.data
NvOptimusEnabled db 64
.code
end
```

---

## Technical Details

**Translation Flow**:

1. `MasmTranslator` iterates over files.
2. Generates `.data` section for global variables.
3. Generates `.code` section for functions.
4. For each function, `FunctionContext` initializes a local memory manager and register manager to handle instruction emission.