/*
 * Copyright (c) 2020, Gerwin Klein
 * SPDX-License-Identifier: BSD-3-Clause
 */

package jflex.benchmark;

/*
  A scanner with minimal action code, to measure inner matching loop
  performance.
*/

%%

%public
%class NoAction

%int

%{
  private int matches;
%}

SHORT = "a"
LONG  = "b"+

%%

{SHORT}  { matches++; }
{LONG}   { matches++; }

"このマニュアルについて"  { matches++; }
"😎"                  { matches++; }

[^]      { /* nothing */ }

<<EOF>>  { return matches; }

