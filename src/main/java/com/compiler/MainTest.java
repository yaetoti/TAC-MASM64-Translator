package com.compiler;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class MainTest {
  static void main() {
    String a = "Hello, world! Привет, мир!";
    IO.println(a.length());
    IO.println(a.getBytes(StandardCharsets.UTF_16LE).length);
    BigInteger i = new BigInteger("123");
    BigDecimal f = new BigDecimal("11.23");
  }
}
