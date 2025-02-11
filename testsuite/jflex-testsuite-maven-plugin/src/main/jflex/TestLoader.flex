/*
 * Copyright 2019, Gerwin Klein, Régis Décamps, Steve Rowe
 * SPDX-License-Identifier: BSD-3-Clause
 */

package jflex.maven.plugin.testsuite;

import java.util.*;
import kotlinx.io.*;

%%

%unicode
%class TestLoader

%function load
%throws LoadException
%type TestCase

// %debug

%state DESCR JFLEXCMD JAVAC_FILES LINELIST VERSION

%{
  private var buffer = StringBuilder()
  private var test = TestCase()
  private lateinit var cmdLine: MutableList<String>
  private lateinit var lineList: MutableList<Int>
%}

NL = \r | \n | \r\n
DIGIT = [0-9]

%%

<YYINITIAL> {
  "name: " [^\r\n]*   { test.setTestName(yytext().toString().substring(6).trim()); }

  "description:"      { yybegin(DESCR); }

  "jflex: "           { cmdLine = ArrayList<String>(); yybegin(JFLEXCMD); }
  "javac-files: "     { cmdLine = ArrayList<String>(); yybegin(JAVAC_FILES); }

  "jflex-fail:" " "+ "true"  { test.setExpectJFlexFail(true); }
  "jflex-fail:" " "+ "false" { test.setExpectJFlexFail(false); }

  "jflex-diff:" " "+  { lineList = ArrayList<Int>();
                        test.setJFlexDiff(lineList);
                        yybegin(LINELIST);
                      }

  "javac-fail:" " "+ "true"  { test.setExpectJavacFail(true); }
  "javac-fail:" " "+ "false" { test.setExpectJavacFail(false); }

  "javac-encoding:" [^\r\n]* { test.setJavacEncoding(yytext().toString().substring(15).trim()); }

  "input-file-encoding:" [^\r\n]* { test.setInputFileEncoding(yytext().toString().substring(20).trim()); }
  "output-file-encoding:" [^\r\n]* { test.setOutputFileEncoding(yytext().toString().substring(21).trim()); }

  "common-input-file:"  [^\r\n]* { test.setCommonInputFile(yytext().toString().substring(18).trim()); }

  "jdk:" " "*         { yybegin(VERSION); }

  {NL} | [ \t]+       { /* ignore newline and whitespace */ }
  "#" [^\r\n]*        { /* ignore comments */ }
}


<VERSION> {
  {DIGIT}+ ("." {DIGIT}+)* { test.setJavaVersion(yytext().toString()); yybegin(YYINITIAL); }
}

<DESCR> {
  [^\r\n]+ | {NL}     { buffer.append(yytext()); }

  {NL}/[^\r\n ]*": "  { test.setDescription(buffer.toString()); yybegin(YYINITIAL); }
}


<JFLEXCMD, JAVAC_FILES> {
  [^ \t\r\n]+         { cmdLine.add(yytext().toString()); }
  \" ~\"              { cmdLine.add(yytext().toString().substring(1,yylength()-1));
                        /* quoted cmdline options */ }
  [ \t]+              { /* ignore whitespace */ }
  \\[ \t]+{NL}        { /* allow line continuation with \ */ }
}

<JFLEXCMD>
  {NL}                { test.setJflexCmdln(cmdLine); yybegin(YYINITIAL); }

<JAVAC_FILES>
  {NL}                { test.setJavacFiles(cmdLine); yybegin(YYINITIAL); }

<LINELIST> {
  [0-9]+              { lineList.add(Integer.valueOf(yytext().toString())); }
  [ \t]+              { }
  {NL}                { yybegin(YYINITIAL); }
}

<<EOF>>               { return test; }

[^]   { throw LoadException("Illegal character: ["+yytext()+"]"); }
