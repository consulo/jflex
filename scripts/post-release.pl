#! /usr/bin/perl -w
    eval 'exec /usr/bin/perl -S $0 ${1+"$@"}'
        if 0; #$running_under_some_shell

# Copyright 2021, Gerwin Klein, Régis Décamps, Steve Rowe
# SPDX-License-Identifier: BSD-3-Clause
#
# post-release.pl
#
# Performs the following:
#
#   - switches working copy to git **new_version** branch
#   - Changes the JFlex version in all POMs to the supplied
#     version (X.Y.Z)
#   - Updates the JFlex version comments and @version tags
#

use strict;
use warnings;
use File::Find ();
use XML::LibXML;
use XML::LibXSLT;
use Getopt::Long;

my $new_release;
my $usage = "Usage: $0 --release <new-release-version>\n e.g.: $0 --release 1.6.0\n";
GetOptions("release=s" => \$new_release) or die($usage);
die $usage unless defined($new_release);

my $sheet =<<'__STYLESHEET__';
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pom="http://maven.apache.org/POM/4.0.0"
                xmlns="http://maven.apache.org/POM/4.0.0"
                exclude-result-prefixes="pom">
  <xsl:param name="release"/>
  <xsl:param name="latest-release"/>

  <!-- Replace all JFlex versions with the new JFlex release version, -->
  <!-- except for the bootstrap version in the org.jetbrains.intellij.deps.jflex:jflex POM.     -->
  <xsl:template
      match=" /pom:project[(pom:groupId='org.jetbrains.intellij.deps.jflex' or (not(pom:groupId) and pom:parent/pom:groupId='org.jetbrains.intellij.deps.jflex'))
                           and not (pom:artifactId='cup-maven-plugin')]/pom:version
             |/pom:project/pom:parent[pom:groupId='org.jetbrains.intellij.deps.jflex' and pom:artifactId='jflex-parent']/pom:version
            ">
    <version><xsl:value-of select="$release"/></version>
  </xsl:template>

  <!-- Replace the bootstrap version with the latest release version -->
  <!-- in the org.jetbrains.intellij.deps.jflex:jflex POM.                                    -->

  <!-- NOTE! we currently do not want to change the version of the maven plugin used for bootstrapping since we do not actually build the artifact -->
  <!-- If we decide to change this, this section should be re-added -->

  <!--<xsl:template -->
  <!--    match="/pom:project/pom:build/pom:plugins/pom:plugin -->
  <!--           [   /pom:project/pom:parent/pom:groupId='org.jetbrains.intellij.deps.jflex' -->
  <!--           and /pom:project/pom:artifactId='jflex' -->
  <!--           and pom:artifactId='jflex-maven-plugin'] -->
  <!--           /pom:version"> -->
  <!--  <version><xsl:value-of select="$latest-release"/></version> -->
  <!-- </xsl:template> -->

  <xsl:template match="@*|*|processing-instruction()|comment()">
    <xsl:copy>
      <xsl:apply-templates select="@*|*|text()|processing-instruction()|comment()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
__STYLESHEET__

select STDOUT;
$| = 1; # Turn on auto-flush

print "Clean checkout?  ";
my $stat_results=`git status -s`;
if ($stat_results) {
  print "NO!\n\n$stat_results\nAborting.\n";
  exit 1;
}
print "Yes.\n\n";

print "Switching to new release branch..\n";
system ("git checkout -b intellij/$new_release");
if ($?) {
  print "FAILED.\n";
  exit 1;
}
print "OK.\n\n";

# read versions after branch switch!
my $previous_release = get_latest_version();

print "Switching JFlex version -> $new_release\n";
print " and boostrap JFlex version -> $previous_release in the org.jetbrains.intellij.deps.jflex:jflex POM ...\n";
File::Find::find({wanted => \&wanted, follow => 1, follow_skip => 2}, '.');

print "Updating version in Build.java\n";
system (qq!perl -pi -e "s/\Q$previous_release\E/$new_release/" jflex/src/main/java/jflex/base/Build.java !);

print " updating version in bin/jflex*";
system (qq!perl -pi -e "s/\Q$previous_release\E/$new_release/" jflex/bin/jflex !);
system (qq!perl -pi -e "s/\Q$previous_release\E/$new_release/" jflex/bin/jflex.bat !);
print "\ndone.\n\n";

print " updating version in jflex/examples/common/include.xml";
system (qq!perl -pi -e "s/\Q$previous_release\E/$new_release/" jflex/examples/common/include.xml!);
print "\ndone.\n\n";

print " updating version in docs/xmanual.tex";
system (qq!perl -pi -e "s/\Q$previous_release\E/$new_release/" docs/xmanual.tex!);
print "\ndone.\n\n";

print " updating version in docs/docs.bzl";
system (qq!perl -pi -e "s/\Q$previous_release\E/$new_release/" docs/docs.bzl!);
print "\ndone.\n\n";

print " updating version in docs/Makefile";
system (qq!perl -pi -e "s/\Q$previous_release\E/$new_release/" docs/Makefile!);
print "\ndone.\n\n";

print " updating version in jflex/README.md and exmaples/*/README.md";
system (qq!perl -pi -e "s/\Q$previous_release\E/$new_release/" jflex/README.md!);
system (qq!perl -pi -e "s/\Q$previous_release\E/$new_release/" jflex/examples/simple/README.md!);
print "\ndone.\n\n";

print " updating version in scripts/mk-release.sh";
system (qq!perl -pi -e "s/\Q$previous_release\E/$new_release/" scripts/mk-release.sh!);
print "\ndone.\n\n";

print " updating version in comments and version tags in jflex/**.java";
system (qq!find jflex -name "*.java" | xargs perl -pi -e "s/\Q$previous_release         \E/$new_release/"!);
system (qq!find jflex -name "*.java" | xargs perl -pi -e "s/\@version \Q$previous_release\E/\@version $new_release/"!);
system (qq!find jflex -name "LexScan.flex" | xargs perl -pi -e "s/\Q$previous_release         \E/$new_release/"!);
system (qq!find jflex -name "LexParse.cup" | xargs perl -pi -e "s/\Q$previous_release         \E/$new_release/"!);
print "\ndone.\n\n";

print "Committing version update ...\n";
my $ret_val = system
   (qq!git commit -a -m "bump version: JFlex $previous_release -> $new_release"!);
if ($ret_val) {
  print STDERR "ERROR - Aborting.\n";
  exit $ret_val >> 8; # Exit with git's return value
}
print "\ndone.\n\n";

print "Now on branch master. 'git push' to publish.\n\n";


exit;

sub get_latest_version {
  # Get the previous snapshot version from the parent POM
  # (assumes master is on previous snapshot)
  my $parent_pom = XML::LibXML->load_xml(location => 'pom.xml');
  my $xpath_context = XML::LibXML::XPathContext->new($parent_pom);
  $xpath_context->registerNs('pom', 'http://maven.apache.org/POM/4.0.0');
  return $xpath_context->findvalue('/pom:project/pom:version');
}

sub wanted {
  transform($File::Find::fullname) if (/^pom\.xml\z/);
}

sub transform {
  my $pom = shift;
  my $xslt = XML::LibXSLT->new();
  my $style_doc = XML::LibXML->load_xml('string' => $sheet);
  my $stylesheet = $xslt->parse_stylesheet($style_doc);
  my $results = $stylesheet->transform_file
      ($pom, 'release' => "'$new_release'",
             'latest-release' => "'$previous_release'");
  $stylesheet->output_file($results, $pom); # replace existing file
}
