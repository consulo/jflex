package jflex.generator;

import java.io.File;
import jflex.io.FileUtils;
import jflex.logging.Out;
import jflex.option.Options;

public abstract class IEmitter {
  String outputFileName;

  /**
   * Constructs a file in Options.getDir() or in the same directory as another file. Makes a backup
   * if the file already exists.
   *
   * @param name the name (without path) of the file
   * @param input fall back location if {@code path = null} (expected to be a file in the directory
   *     to write to)
   * @return The constructed File
   */
  static File normalize(String name, File input) {
    File outputFile;

    if (Options.getDir() == null) {
      if (input == null || input.getParent() == null) {
        outputFile = new File(name);
      } else {
        outputFile = new File(input.getParent(), name);
      }
    } else {
      outputFile = new File(Options.getDir(), name);
    }

    if (outputFile.exists() && !Options.no_backup) {
      File backup = new File(outputFile.toString() + "~");

      if (backup.exists()) {
        //noinspection ResultOfMethodCallIgnored
        backup.delete();
      }

      if (outputFile.renameTo(backup)) {
        Out.println("Old file \"" + outputFile + "\" saved as \"" + backup + "\"");
      } else {
        Out.println("Couldn't save old file \"" + outputFile + "\", overwriting!");
      }
    }

    return outputFile;
  }

  static String sourceFileString(File file) {
    String path = FileUtils.getRelativePath(Options.getRootDirectory(), file);
    if (File.separatorChar == '\\') {
      path = FileUtils.slashify(path);
    }
    // Character '\' can be use for Unicode representation, e.g. \\u000A is new line
    return path.replace("\\", "\\\\");
  }

  /** Main Emitter method. */
  abstract void emit();
}
