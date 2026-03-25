package com.compiler.ir.codes;

public sealed interface ICode permits CodeAssign, CodeCall, CodeReturn {}

