package com.compiler.utils;

public class CodeEmitter {
  private StringBuilder sb = new StringBuilder();
  private int indent = 0;
  private int indentSize = 2;
  private boolean isDebugMode = false;

  public CodeEmitter(boolean isDebugMode) {
    this.isDebugMode = isDebugMode;
  }

  public void Reset() {
    sb = new StringBuilder();
    indent = 0;
  }

  public void SetDebugMode(boolean isDebugMode) {
    this.isDebugMode = isDebugMode;
  }

  public void SetIndentSize(int indentSize) {
    if (indentSize < 0) {
      throw new IllegalArgumentException("Indent size cannot be negative");
    }

    this.indentSize = indentSize;
  }

  public void IncreaseIndent() {
    indent += 1;
  }

  public void DecreaseIndent() {
    indent -= 1;
    if (indent < 0) {
      indent = 0;
    }
  }

  public void Emit(String line) {
    sb.append(" ".repeat(indent * indentSize));
    sb.append(line);
    sb.append("\n");
  }

  public void EmitF(String format, Object ... args) {
    sb.append(" ".repeat(indent * indentSize));
    sb.append(String.format(format, args));
    sb.append("\n");
  }

  public void Append(String line) {
    sb.append(line);
  }

  public void Append(char c) {
    sb.append(c);
  }

  public void EmitNL() {
    if (!isDebugMode) {
      return;
    }

    sb.append('\n');
  }

  public void EmitComment(String line) {
    if (!isDebugMode) {
      return;
    }

    sb.append(" ".repeat(indent * indentSize));
    sb.append("; ");
    sb.append(line);
    sb.append("\n");
  }

  public void EmitCommentF(String format, Object... args) {
    if (!isDebugMode) {
      return;
    }

    sb.append(" ".repeat(indent * indentSize));
    sb.append("; ");
    sb.append(String.format(format, args));
    sb.append("\n");
  }

  public String Collect() {
    return sb.toString();
  }
}
