package com.yaetoti;

import java.lang.Integer;
import java.util.ArrayList;
import java.util.HashMap;

class MasmLocationTable {
  /// Maps symbol id to memory location
  private HashMap<Integer, ArrayList<MASM.Location>> m_symToLoc = new HashMap<>();
  /// Maps a register type to symbol id. Value may be null if register is empty
  private HashMap<MASM.Register.Type, Integer> m_regToSym = new HashMap<>();

  public MasmLocationTable() {
    // Explicitly set all registers' content to null
    for (var registerType : MASM.Register.Type.values()) {
      m_regToSym.put(registerType, null);
    }
  }

  /// @return The first empty register or null if all registers are filled
  public MASM.Register.Type GetEmptyRegister() {
    for (var registerType : MASM.Register.Type.values()) {
      if (m_regToSym.get(registerType) == null) {
        return registerType;
      }
    }

    return null;
  }

  /// @return An array of empty registers
  public ArrayList<MASM.Register.Type> GetEmptyRegisters() {
    ArrayList<MASM.Register.Type> emptyRegisters = new ArrayList<>();

    for (var registerType : MASM.Register.Type.values()) {
      if (m_regToSym.get(registerType) == null) {
        emptyRegisters.add(registerType);
      }
    }

    return emptyRegisters;
  }

  // +Get empty registers
  // +Get first empty register
  // GetSymbolLocation
  // SetSymbolLocation (memory/register)
  // Remove location

  // Build table should be a distinct function
  // GetMASMtype only for some types. No arrays, structs. They are pointers

  // What happens later? Alright, we need to do that in runtime per function. These are initial locations

  // Globals are kinda constant
  // u64 number1
  // mov rax, number1 (number 1 in 2 locations)
  // mov number1, number2 (number1 contains value of number2. Number2 is still number2)

  // What are we moving? number2 or just a value
  // just a value. Though, what if
  // mem = number1
  // mem = number2
  // reg1 = number1
  // reg2 = number2
  // add reg1 (number1), reg2 (number2)
  // reg1 == ??
  // mov number1, reg1
  // mov number3, reg1

  // load number1 into the first free register and give its id (reg1 containing number1)
  // load number2 into the second register and give its id (reg2 containing number2)
  // perform add (reg1 no longer contains number1, contains number3 - result of operation. We move them from symbol to symbol, not from memory to memory. Symbol is a type + name)
  // result is in the first register (but number2 is still in register2. We can either free it, either have 2 locations and prefer register)
  // mov the first register to some constant location
}

public class MasmSymbolTable {
  private HashMap<Integer, MASM.Location> m_symbols = new HashMap<>();

  public MasmSymbolTable(Program program) {
    BuildTable(program);
  }

  public MASM.Location GetSymbolLocation(int symbolId) {
    return m_symbols.get(symbolId);
  }

  private void BuildTable(Program program) {
    // Global symbols
    for (var symbol : program.GetGlobalSymbols()) {
      m_symbols.put(symbol.id(), new MASM.LabelMemory(GetMasmType(symbol.type()), symbol.name(), 0));
    }

    // For each unit
    for (var unit : program.GetTranslationUnits()) {
      // Static symbols
      for (var symbol : unit.GetStaticSymbols()) {
        m_symbols.put(symbol.id(), new MASM.LabelMemory(GetMasmType(symbol.type()), symbol.name(), 0));
      }

      // For each function
      for (var function : unit.GetFunctions()) {
        // For each parameter (depending on convention)
        switch (function.GetDeclaration().convention()) {
          case STACKCALL -> HandleParametersStackCall(function);
          default -> throw new IllegalStateException("Unexpected convention: " + function.GetDeclaration().convention());
        }

        // TODO if parameters to be placed on the stack, we need to to it before or save parameter offset
        // For each local variable calculate offsets
        int offset = 0;
        for (var local : function.GetLocalSymbols()) {
          var masmType = GetMasmType(local.type());
          offset += masmType.size;

          m_symbols.put(local.id(), new MASM.OffsetMemory(masmType, MASM.Register.RBP, null, 0, -offset));
        }
      }
    }
  }

  private void HandleParametersStackCall(Function function) {
    // return rbp (8) + return value (8)
    int offset = 16;
    for (var parameter : function.GetDeclaration().parameters()) {
      var masmType = GetMasmType(parameter.type());
      m_symbols.put(parameter.id(), new MASM.OffsetMemory(masmType, MASM.Register.RBP, null, 0, offset));
      offset += masmType.size;
    }
  }

  private MASM.Type GetMasmType(DataType type) {
    // TODO enum would be really cool to exactly determine type
    // TODO and how we represent structures and arrays? How to get their size? what about VLA? What if structure's size is 8
    return switch (type.size()) {
      case 1 -> MASM.Type.BYTE;
      case 2 -> MASM.Type.WORD;
      case 4 -> MASM.Type.DWORD;
      case 8 -> MASM.Type.QWORD;
      default -> throw new IllegalStateException("Unexpected value: " + type);
    };
  }
}
