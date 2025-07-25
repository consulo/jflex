/*
 * Copyright (C) 1998-2023  Gerwin Klein <lsf@jflex.de>
 * SPDX-License-Identifier: BSD-3-Clause
 */

package jflex.generator;

/**
 * An emitter for an array encoded as count/value pairs in a string where values can be in [0,
 * 0xFFFF_FFFF].
 *
 * @author Gerwin Klein
 * @version JFlex 1.10.0
 */
public class KotlinHiCountEmitter extends KotlinCountEmitter {

  /**
   * Create a count/value emitter for a specific field.
   *
   * @param name name of the generated array
   */
  protected KotlinHiCountEmitter(String name, int translate) {
    super(name, translate);
  }

  /**
   * Emits count/value unpacking code for the generated array.
   *
   * @see KotlinPackEmitter#emitUnpack()
   */
  @Override
  public void emitUnpackChunk() {
    println("  @JvmStatic");
    println(
        "  private static int zzUnpack" + name + "(String packed, int offset, int [] result) {");
    println("    int i = 0       /* index in packed string  */");
    println("    int j = offset  /* index in unpacked array */");
    println("    int l = packed.length() - 2 /* reading 3 chars per entry */");
    println("    while (i < l) {");
    println("      int count = packed.get(i++)");
    println("      int high = packed.get(i++) << 16");
    println("      int value = high | packed.get(i++)");
    if (translate == 1) {
      println("      value--");
    } else if (translate != 0) {
      println("      value-= " + translate);
    }
    println("      do result[j++] = value while (--count > 0)");
    println("    }");
    println("    return j");
    println("  }");
  }

  /**
   * Emits a single value to the current string chunk. Accepted range is [0, 0xFFFF_FFFF]
   *
   * @param val the integer value to emit
   */
  @Override
  protected void emitValue(int val) {
    emitUC(val >> 16);
    emitUC(val & 0xFFFF);
  }
}
