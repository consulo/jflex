/*
 * Copyright (c) 2020, Gerwin Klein
 * SPDX-License-Identifier: BSD-3-Clause
 */

package jflex.benchmark;

import kotlinx.io.readCodePointValue;

/*
  A scanner with minimal action code, to measure inner matching loop
  performance.
*/

%%

%public
%class NoAction

%int

%{
  private var matches = 0;
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
