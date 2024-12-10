/*
 * Copyright (C) 1998-2018  Gerwin Klein <lsf@jflex.de>
 * SPDX-License-Identifier: BSD-3-Clause
 */

package jflex.generator;

/**
 * HiLowEmitter
 *
 * @author Gerwin Klein
 * @version JFlex 1.10.0-SNAPSHOT
 */
public class HiLowEmitter extends PackEmitter {

  /** number of entries in expanded array */
  private int numEntries;

  /**
   * Create new emitter for values in [0, 0xFFFFFFFF] using hi/low encoding.
   *
   * @param name the name of the generated array
   */
  public HiLowEmitter(String name) {
    super(name);
  }

  /**
   * Emits hi/low pair unpacking code for the generated array.
   *
   * @see PackEmitter#emitUnpack()
   */
  @Override
  public void emitUnpack() {
    // close last string chunk:
    println("\"\"\"");
    nl();
    println("  private fun zzUnpack" + name + "(): IntArray {");
    println("    val result: IntArray = intArrayOf(" + numEntries + ");");
    println("    var offset: Int = 0;");

    for (int i = 0; i < chunks; i++) {
      println(
          "    offset = zzUnpack"
              + name
              + "("
              + constName()
              + "_PACKED_"
              + i
              + ", offset, result);");
    }

    println("    return result;");
    println("  }");

    nl();
    println(
        "  private fun zzUnpack" + name + "(packed: String, offset: Int, result: IntArray): Int {");
    println("    var i: Int = 0;  /* index in packed string  */");
    println("    var j: Int = offset;  /* index in unpacked array */");
    println("    var l: Int = packed.length - 1;");
    println("    while (i < l) {");
    println("      val high: Int = packed[i++].code shl 16;");
    println("      result[j++] = high or packed[i++].code;");
    println("    }");
    println("    return j;");
    println("  }");
  }

  /**
   * Emit one value using two characters.
   *
   * @param val the value to emit; {@code 0 <= val <= 0xFFFFFFFF}
   */
  public void emit(int val) {
    numEntries += 1;
    breaks();
    emitUC(val >> 16);
    emitUC(val & 0xFFFF);
  }
}
