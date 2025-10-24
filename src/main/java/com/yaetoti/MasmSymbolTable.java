package com.yaetoti;

import java.util.HashMap;

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
