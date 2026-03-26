package com.compiler.ir.symbols;

import com.compiler.ir.types.IDataType;

// TODO type?
public record IntegerConstant(String value, IDataType type) implements IConstant {}
