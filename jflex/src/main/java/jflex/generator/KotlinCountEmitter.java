/*
 * Copyright (C) 1998-2018  Gerwin Klein <lsf@jflex.de>
 * SPDX-License-Identifier: BSD-3-Clause
 */

package jflex.generator;

/**
 * An emitter for an array encoded as count/value pairs in a string.
 *
 * @author Gerwin Klein
 * @version JFlex 1.10.0-SNAPSHOT
 */
public class KotlinCountEmitter extends KotlinPackEmitter {
  /** number of entries in expanded array */
  protected int numEntries;

  /** translate all values by this amount */
  protected int translate;

  /**
   * Create a count/value emitter for a specific field with values in the range of [0..0xFFFF].
   *
   * @param name name of the generated array
   */
  protected KotlinCountEmitter(String name) {
    this(name, 0);
  }

  /**
   * Create a count/value emitter for a specific field with translated values in the range of
   * [0..0xFFFF].
   *
   * @param name name of the generated array
   * @param translate translate all values by this amount, e.g provide +1 to allow values in [-1,
   *     0xFFFE]
   */
  protected KotlinCountEmitter(String name, int translate) {
    super(name);
    this.translate = translate;
  }

  /**
   * Emits count/value unpacking code for the generated array.
   *
   * @see KotlinPackEmitter#emitUnpack()
   */
  @Override
  public void emitUnpack() {
    // close last string chunk:
    println("\";");

    nl();
    println("  private fun zzUnpack" + name + "(): IntArray {");
    println("    val result: IntArray = IntArray(" + numEntries + ");");
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

    emitUnpackChunk();

    nl();

    out.append("  private val ")
        .append(constName())
        .append(": IntArray = zzUnpack")
        .append(name)
        .append("()");

    nl();
  }

  /**
   * Emits count/value unpacking code for one chunk of the generated array. Base class assumes
   * values are in [0, 0xFFFF]. Subclasses may override.
   *
   * @see #emitUnpack()
   */
  protected void emitUnpackChunk() {
    println(
        "  private fun zzUnpack"
            + name
            + "(packed: String , offset: Int, result: IntArray): Int {");
    println("    var i: Int = 0;       /* index in packed string  */");
    println("    var j: Int = offset;  /* index in unpacked array */");
    println("    var l: Int = packed.length;");
    println("    while (i < l) {");
    println("      var count: Int = packed[i++].code;");
    println("      var value: Int = packed[i++].code;");
    if (translate == 1) {
      println("      value--;");
    } else if (translate != 0) {
      println("      value-= " + translate);
    }
    println("      do");
    println("          result[j++] = value");
    println("      while (--count > 0)");
    println("    }");
    println("    return j;");
    println("  }");
  }

  /**
   * Emit one count/value pair.
   *
   * <p>Automatically translates value by the {@code translate} value.
   *
   * @param count a int.
   * @param value a int.
   */
  public void emit(int count, int value) {
    numEntries += count;
    breaks();

    // unlikely, but count could be >= 0x10000
    while (count > 0xFFFF) {
      emitUC(0xFFFF);
      emitValue(value + translate);
      count -= 0xFFFF;
    }

    emitUC(count);
    emitValue(value + translate);
  }

  /**
   * Emit one value. Base class assumes range [0, 0xFFFF], subclasses may override.
   *
   * @param value integer value to emit, assumed to be in the range [0, 0xFFFF]
   */
  protected void emitValue(int value) {
    emitUC(value);
  }

  /**
   * Emits a plain int array as a count/value string. Expects the preamble code (declaration,
   * javadoc) to already be emitted. Values in the array must be no larger than 0xFFFF (encoded as
   * char), array must have at least one element.
   */
  public void emitCountValueString(int[] a) {
    assert a.length > 0;

    int count = 0;
    int value = a[0];
    for (int i = 0; i < a.length; i++) {
      if (value == a[i]) {
        count++;
      } else {
        emit(count, value);
        count = 1;
        value = a[i];
      }
    }
    emit(count, value);
  }

  public static KotlinCountEmitter emitter(int states, int translation, String name) {
    if (states <= 0xFFFF) {
      return new KotlinCountEmitter(name, translation);
    } else {
      return new KotlinHiCountEmitter(name, translation);
    }
  }
}
