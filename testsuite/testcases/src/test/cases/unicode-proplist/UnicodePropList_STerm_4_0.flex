%%

%unicode 4.0
%public
%class UnicodePropList_STerm_4_0

%type int
%standalone

%include ../../resources/common-unicode-all-binary-property-java

%%

\p{STerm} { setCurCharPropertyValue(); }
[^] { }

<<EOF>> { printOutput(); return 1; }
