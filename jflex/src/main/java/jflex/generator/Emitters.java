/*
 * Copyright (C) 1998-2018  Gerwin Klein <lsf@jflex.de>
 * SPDX-License-Identifier: BSD-3-Clause
 */

package jflex.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import jflex.core.LexParse;
import jflex.dfa.DFA;
import jflex.logging.Out;
import jflex.option.Options;

/** Factory class for IEmitter */
public final class Emitters {

  private Emitters() {}

  /**
   * Creates an IEmitter that generates the java code in a file. The output file name is inferred
   * from the class defined in the grammar.
   *
   * @param inputLexFile input grammar.
   * @param parser a {@link LexParse}.
   * @param dfa a {@link DFA}.
   * @return {@link IEmitter}.
   * @throws IOException if any.
   */
  public static IEmitter createFileEmitter(File inputLexFile, LexParse parser, DFA dfa)
      throws IOException {

    String name = "";
    switch (Options.output_mode) {
      case JAVA:
        name = Emitter.getBaseName(parser.scanner.className()) + ".java";
        break;
      case KOTLIN:
        name = Emitter.getBaseName(parser.scanner.className()) + ".kt";
        break;
    }

    File outputFile = Emitter.normalize(name, inputLexFile);
    String outputFileName = outputFile.getAbsolutePath();

    Out.println("Writing code to \"" + outputFile + "\"");

    PrintWriter out =
        new PrintWriter(
            new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputFile), Options.encoding)));

    switch (Options.output_mode) {
      case JAVA:
        return new Emitter(outputFileName, inputLexFile, parser, dfa, out);
      case KOTLIN:
        return new KotlinEmitter(outputFileName, inputLexFile, parser, dfa, out);
    }
    return null;
  }

  /**
   * Create IEmitter that writes to writer
   *
   * @param parser a {@link LexParse}.
   * @param dfa a {@link DFA}.
   * @param writer output file.
   * @return {@link IEmitter}.
   */
  public static IEmitter createPrintWriterEmitter(LexParse parser, DFA dfa, PrintWriter writer) {
    switch (Options.output_mode) {
      case JAVA:
        return new Emitter(null, new File(""), parser, dfa, writer);
      case KOTLIN:
        return new KotlinEmitter(null, new File(""), parser, dfa, writer);
    }
    return null;
  }
}
