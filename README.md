# TagChowder
Parsing and extracting information from (possibly malformed) HTML/XML documents

                        TagChowder - Just Keep On Truckin'

### Introduction

TagChowder is a SAX-compliant parser written in Java that, instead of parsing well-formed or valid XML, parses HTML as it is found in the wild: poor, nasty and brutish, though quite often far from short. TagChowder is designed for people who have to process this stuff using some semblance of a rational application design. By providing a SAX interface, it allows standard XML tools to be applied to even the worst HTML. TagSoup also includes a command-line processor that reads HTML files and can generate either clean HTML or well-formed XML that is a close approximation to XHTML.

This is also the README file packaged with TagChowder.

TagChowder is free and Open Source software licensed under the Apache License, Version 2.0, see LICENSE files for more details.

TagChowder is a fork from TagSoup library by John Cowan version 1.2.1.

## Table of Contents

- [What TagSoup does](#background)
- [Install](#install)
- [Configuration](#configuration)
- [Usage](#usage)
- [Security](#security)
- [API](#api)
- [Contribute](#contribute)
- [License](#license)

### What TagSoup does

TagChowder is designed as a parser, not a whole application; it isn't intended to permanently clean up bad HTML, as HTML Tidy does, only to parse it on the fly. Therefore, it does not convert presentation HTML to CSS or anything similar. It does guarantee well-structured results: tags will wind up properly nested, default attributes will appear appropriately, and so on.

The semantics of TagChowder are as far as practical those of actual HTML browsers. In particular, never, never will it throw any sort of syntax error: the TagChowder motto is "Just Keep On Truckin'". But there's much, much more. For example, if the first tag is LI, it will supply the application with enclosing HTML, BODY, and UL tags. Why UL? Because that's what browsers assume in this situation. For the same reason, overlapping tags are correctly restarted whenever possible: text like:

This is <B>bold, <I>bold italic, </b>italic, </i>normal text
gets correctly rewritten as:

This is <b>bold, <i>bold italic, </i></b><i>italic, </i>normal text.
By intention, TagChowder is small and fast. It does not depend on the existence of any framework other than SAX, and should be able to work with any framework that can accept SAX parsers. In particular, XOM is known to work.

You can replace the low-level HTML scanner with one based on Sean McGrath's PYX format (very close to James Clark's ESIS format). You can also supply an AutoDetector that peeks at the incoming byte stream and guesses a character encoding for it. Otherwise, the platform default is used. If you need an autodetector of character sets, consider trying to adapt the Mozilla one; if you succeed, let me know.

### TagChowder 2.0.0 released

TagChowder 2.0.0 is a modernization release, it includes all features available on tagsoup 1.2.1. The goal of this project is to keep updating the code with small fixes and features. This project requires unit tests and code coverage by default, and uses github and a automated pipe-line to publish new versions in a automated fashion for every commit that is merged, without humans need for each release.

 - The default content model for bogons (unknown elements) is ANY rather than EMPTY. This is a breaking change. It can be undone on the command line with the --emptybogons switch, or programmatically with parser.setFeature(Parser.emptyBogonsFeature, true).

 - The processing of entity references in attribute values has finally been fixed to do what browsers do. That is, a reference is only recognized if it is properly terminated by a semicolon; otherwise it is treated as plain text. This means that URIs like foo?cdown=32&cup=42 are no longer seen as containing an instance of the ∪ character (whose name happens to be cup).

 - Several new switches have been added:

   --doctype-system and --doctype-public force a DOCTYPE declaration to be output and allow setting the system and public identifiers.

   --standalone and --version allow control of the XML declaration that is output. (Note that the XML output is always version 1.0, even if you use --version=1.1.)

   --norootbogons causes unknown elements not to be allowed as the document root element. Instead, they are made children of the default root element (the html element for HTML).

 - The TagChowder core supports character entities with values above U+FFFF. As a consequence, the HTML schema now supports all 2,210 standard character entities from the 2007-12-14 draft of XML Entity Definitions for Characters, except the 94 which require more than one Unicode character to represent.

 - A number of content models have been loosened to allow elements to appear in new and non-standard (but commonly found) places. In particular, tables are allowed inside paragraphs, against the letter of the W3C specification. Since the span element is intended for fine control of appearance using CSS, it should never have been a restartable element. This very long-standing bug has now been fixed.

 - The following non-standard elements are partly supported: bgsound, blink, canvas, comment, listing, marquee, nobr, rbc, rb, rp, rtc, rt, ruby, wbr, xmp.

 - In HTML output mode, boolean attributes like checked are output as such, rather than in XML style as checked="checked".

 - Runs of < characters such as << and <<< are handled correctly in text rather than being transformed into extremely bogus start-tags.


Here is a detailed list of the features:
 - DOCTYPE is now recognized even in lower case.
 - We make sure to buffer the reader, eliminating a long-standing bug that would crash on certain inputs, such as & followed by CR+LF.
 - The HTML scanner's table is precompiled at run time for efficiency, causing a 4x speedup on large input documents.
 - ]] within a CDATA section no longer causes input to be discarded.
 - Remove bogus newline after printing children of the root element.
 - Allow the noscript element anywhere, the same as the script element.
 - Updated to the 2011 edition of the W3C character entity list.
 - Match DOCTYPE case-blind
 - Extend PushbackReader's size for oddball cases like & followed by CR
Leo Sutic's 2x-4x speedup by precompiling HTMLScanner table
Support new DOCTYPE output switches --doctype-system and --doctype-public
Support new XML declaration output switches --standalone and --version
New --norootbogons switch makes bogons children of the root
Don't resolve entity references in attribute values unless semicolon-terminated
Support character entities above U+FFFF
Add character entities from the 2007-12-14 draft of xml-entity-names
Call SAX events startPrefixMapping and endPrefixMapping to report prefixes
Clean up newline processing, shrinking html.stml considerably
Allow link elements in the body as well as the head, to avoid excess bodies
Allow tables inside paragraphs
Allow cells and forms in thead and tfoot elements without intervening tr element
The span element is no longer restartable
Support non-standard elements bgsound, blink, canvas, comment, listing, marquee, nobr, ruby, rbc, rtc, rb, rt, rp, wbr, xmp
In HTML mode, boolean attributes like checked are output in minimized form
Correctly handle runs of less-than characters
Suppress all but the first DOCTYPE declaration
Modify PI targets containing colons to have underscores instead
The case of element tags is now canonicalized to the schema
PI targets are no longer forced to lower case
Allow setting the LexicalHandler feature to be null in both cases means "use default behavior"
Added lexical handler calls to startCDATA/endCDATA from CDATA section handling
Added CDATAElementsFeature, the programmatic equivalent of the --nocdata switch
Add Tatu Saloranta's JAXP support package
Allow &#X as well as &#x
Add net.sf.saxon to list of supported XSLT engines
Blocked XML declaration from specifying an encoding of ""
Leading and trailing hyphens now trimmed properly from comments
Added --output-encoding switch to control encoding
If output encoding is Unicode, don't generate character references
Whitespace compressed and junk stripped from public identifiers
Added ignorableWhitespaceFeature and --ignorable to report ignorable whitespace
Insert spaces to break up -- in comments
Change bogus chars in publicids to spaces
--lexical switch now outputs DOCTYPE if there is one
Added feature to control restartability
Added corresponding --norestart switch in CommandLine
Made translate-colons feature actually work
If there is a publicid but no systemid, set systemid to ""
LexicalHandler now gets DOCTYPE information (publicid and systemid)
HTMLScanner now reports more useful debug output when not commented out
Change "<memberOfAny>" to exclude "<root>" pseudo-element - This prevents "script" from being output as a root
If namespaceFeature is false, uri and localname are passed as empty strings
The namespacePrefixesFeature is now always false
Command line switch --html implies --nons
XMLWriter is told directly to use the schema's URI as default namespace
XMLWriter takes the element name from the qname if localname is empty
The --nodefault switch removes only default attributes, not all of them
The --nocolons switch and --translate-colons feature converts ":" in names to "_" (thus suppressing namespaces other than the basic one)
The root element can be unknown without problem
Empty <script/> and <style/> tags now work
Added all standard SAX2 features to feature hashtable
Remove trailing ? from processing instructions (in case the input is XHTML)
Added Javadocs for all SAX standard and TagSoup-specific features and properties
Fixed termination conditions for entity/character references
Fixed EOF-pushback bug that was generating bogus &#x65535; references
Added Parser feature and --nodefaults switch to ignore default attribute values
Added support for SAX Locator
Updated AFL license to version 3.0
Scanner buffer size increases as needed, allowing large attribute values
Look for various XSLT implementations as available (still fails in raw 5.0)
Clean up handling of XML empty tags and SGML minimized end-tags
Support proper options and help message internally
Use Hashtable in CommandLine class instead of HashMap
Do proper buffering of InputStream and Reader
Clean up content model of noframes element
Removed htmlMode in XMLWriter
Added support for XSLT output options METHOD=html and OMIT_XML_DECLARATION=yes
Command line option --html sets both of these
Wrote simple validator for TSSL schemas (tssl/tssl-validator.xslt)
Removed various validity problems in html.tssl
When processing a start-tag, don't restart elements that aren't in the new
	element's content model
Remove bogus double param in tssl.xslt
Convert CR and CRLF to LF in comments and PIs
Force empty elements to close immediately
Match close tags of CDATA elements more precisely (but case-blind)
Process switches on the command line
Man page available
Isolated & and &# now don't crash parser
TagSoup no longer depends on /dev/stdin existing
Refactored Parser class, removing main method to new CommandLine class
Changes to content models of form, button, table, and tr elements in html.tssl
'</scr' + 'ipt>' in a script element no longer terminates it
Introduced "uncloseability" of form and table elements
"pyxin" property specifies that input is in PYX format
Correctly cope with unexpected characters around colons, also with multiple colons
Correctly output comments with "--" in them (by adding a space)
Script can now appear anywhere
Switch -nocdata correctly implemented
Eliminated useless M_n constants in Schema
Introduced <memberofAny> and <isRoot> as alternatives to
	<memberOf> in TSSL
Allow prefixes in element names
Attributes are now normalized
Expanded public API for Element and ElementType
Javadoc improved
Removed misfeature whereby > terminated a tag even inside quotes
Added licensing language to XSLT scripts, RELAX NG schemas
Removed long-standing mishandling of entity references in attributes
Cleaned up logic for converting junky strings to proper XML Names
Correctly handle empty tag that has no whitespace or attributes
Restore correct 0.9.3 handling of an apparent end-tag in a CDATA element
Added script element to content model of head element
Convert to XSLT configuration exclusively;
	Perl code and tab-separated tables are gone
Remove xmlns:* attributes
Append "_" to attribute names ending in ":"
Don't prepend "_" to an attribute name starting in "_"
Handle namespace prefixes in attributes:
	"xml" prefix is handled correctly
	other prefixes are mapped to "urn:x-prefix:foo"
Ignore XML declarations
-Dnocdata=true turns off F_CDATA on script and style elements
Fixed off-by-one errors in character references that made them uninterpreted
Start-tags ending in a minimized attribute are no longer being dropped
XML empty tags are now supported (though slashes are still allowed in
	unquoted attribute values)
Upgraded AFL to version 2.1
Passed through newlines in character content (very old bug)
Script element can appear directly in body
">" terminates a start-tag even inside a quoted attribute,
	to protect against unbalanced quotes
"_" is prepended to attributes that don't begin with a letter
Remove "xmlns" attributes from the input
All standard features can now be set
	(although there is no effect from doing so)
New "bogons-empty" feature can be set to false to give bogons
	 content model of ANY rather than EMPTY;
	-Dany switch sets this feature to false
TSSL now has an explicit group element to declare an element group
STML is a new XML format for modeling state-table changes
S in the statetable now means \r and \n and \t as well as space
	(as was always intended; brain fart!)
Ins and del elements are now allowed everywhere
TSSL now correctly supports attributes that are legal on all elements
Fixed paper-bag bug that revealed attribute type BOOLEAN to applications.
Obsolete ABSTRACT removed in favor of README.
Improved implementation of CDATA restart after bogus end-tag.
Allowed hyphen, underscore, and period in names as well as colon.
First cut at TagSoup Schema Language -- doesn't do anything yet.
Support CDATA sections on input.
Don't generate built-in entities within CDATA elements.
Convenience main program "tagsoup" in bin directory.
Begin to integrate tests.
Introduced BOOLEAN type (currently just converted to NMTOKEN).
Features that actually work are now named constants in Parser.
Double root elements are really gone now.
ID attributes weren't being removed from restarted elements.
Fixed a bug that made unknown elements disappear in some cases.
Parser is now safely reusable.
PYXWriter and XMLWriter now implement LexicalHandler.
Parser reports comments, startCDATA, and endCDATA events to a LexicalHandler.
ScanHandler methods now throw only SAXException, not also IOException.
-Dlexical=true switch sets the ContentHandler as a LexicalHandler as well
	(XMLWriter prints comments, ignores CDATA sections; PYXWriter ignores all).
-Dreuse=true switch reuses a single Parser object (no great speed gain).
We now disallow an a element as the child of another a element.
An empty input is now treated as zero-length character content.
HTMLWriter is gone in favor of an extended XMLWriter with get/setHTMLMode methods.
CDATA elements only terminaate with matching end-tags (thanks to Sebastien Bardoux).
No longer inserts bogus ; after unknown entity reference without ;.
Consecutive entity references now work correctly.
Setting namespaces and namespace-prefixes methods now works.
-Dnons=true option turns off namespace and prefix.
New feature http://www.ccil.org/~cowan/tagsoup/features/ignore-bogons"
	suppresses unknown start-tags (any end-tag will be automatically ignored).
-Dnobogons=true option turns ignore-bogons on.
Suppress unknown and/or empty initial start-tag always
	(prevents double root element).
Schema now allows style as an inline element, like script.
Schema now allows tr as a child of table to avoid problems with embedded tables.
Clear Parser instance variables to make Parsers properly reusable.
Incorporated patch for -jar support by Joseph Walton.
Incorporated patch for Megginson XMLWriter support by Joseph Walton.
Changed existing XMLWriter to HTMLWriter.
Rewrote Parsermain for better features, removed Tester class.
-Dnewline=true removed, now implied by -DHTML=true.
-Dfiles=true now used to generate separate outputs (old Tester behavior)
	with extension xhtml (removing any old extension).
Fixed nasty bug in HTMLScanner that was failing to fix unusual entities.
Don't attempt to smash whitespace to spaces any more.
Ant-ified by Martin Rademacher.
Don't suppress colons in element names.
Entity problems fixed (I hope).
Can now set namespace and namespace-prefixes features (without effect).
Properly templatize HTMLModels.java.
Attributes are no longer in the HTML namespace.

### TagChowder as a stand-alone program

It is possible to run TagChowder as a program by saying java -jar tagchowder.jar [option ...] [file ...]. Files mentioned on the command line will be parsed individually. If no files are specified, the standard input is read.

The following options are understood:

--files
Output into individual files, with html extensions changed to xhtml. Otherwise, all output is sent to the standard output.
--html
Output is in clean HTML: the XML declaration is suppressed, as are end-tags for the known empty elements.
--omit-xml-declaration
The XML declaration is suppressed.
--method=html
End-tags for the known empty HTML elements are suppressed.
--doctype-system=systemid
Forces the output of a DOCTYPE declaration with the specified systemid.
--doctype-public=publicid
Forces the output of a DOCTYPE declaration with the specified publicid.
--version=version
Sets the version string in the XML declaration.
--standalone=[yes|no]
Sets the standalone declaration to yes or no.
--pyx
Output is in PYX format.
--pyxin
Input is in PYXoid format (need not be well-formed).
--nons
Namespaces are suppressed. Normally, all elements are in the XHTML 1.x namespace, and all attributes are in no namespace.
--nobogons
Bogons (unknown elements) are suppressed.
--nodefaults
Default attribute values are suppressed.
--nocolons
Explicit colons in element and attribute names are changed to underscores.
--norestart
don't restart any normally restartable elements.
--ignorable
Output whitespace in elements with element-only content.
--emptybogons
Bogons are given a content model of EMPTY rather than ANY.
--any
Bogons are given a content model of ANY rather than EMPTY (default).
--norootbogons
Bogons are not allowed to be root elements; make them subordinate to the root.
--lexical
Pass through HTML comments and DOCTYPE declarations. Has no effect when output is in PYX format.
--reuse
Reuse a single instance of TagSoup parser throughout. Normally, a new one is instantiated for each input file.
--nocdata
Change the content models of the script and style elements to treat them as ordinary #PCDATA (text-only) elements, as in XHTML, rather than with the special CDATA content model.
--encoding=encoding
Specify the input encoding. The default is the Java platform default.
--output-encoding=encoding
Specify the output encoding. The default is the Java platform default.
--help
Print help.
--version
Print the version number.

### SAX features and properties

TagChowder supports the following SAX features in addition to the standard ones:

http://www.ccil.org/~cowan/tagsoup/features/ignore-bogons
A value of "true" indicates that the parser will ignore unknown elements.
http://www.ccil.org/~cowan/tagsoup/features/bogons-empty
A value of "true" indicates that the parser will give unknown elements a content model of EMPTY; a value of "false", a content model of ANY.
http://www.ccil.org/~cowan/tagsoup/features/root-bogons
A value of "true" indicates that the parser will allow unknown elements to be the root of the output document.
http://www.ccil.org/~cowan/tagsoup/features/default-attributes
A value of "true" indicates that the parser will return default attribute values for missing attributes that have default values.
http://www.ccil.org/~cowan/tagsoup/features/translate-colons
A value of "true" indicates that the parser will translate colons into underscores in names.
http://www.ccil.org/~cowan/tagsoup/features/restart-elements
A value of "true" indicates that the parser will attempt to restart the restartable elements.
http://www.ccil.org/~cowan/tagsoup/features/ignorable-whitespace
A value of "true" indicates that the parser will transmit whitespace in element-only content via the SAX ignorableWhitespace callback. Normally this is not done, because HTML is an SGML application and SGML suppresses such whitespace.
http://www.ccil.org/~cowan/tagsoup/features/cdata-elements
A value of "true" indicates that the parser will process the script and style elements (or any elements with type='cdata' in the TSSL schema) as SGML CDATA elements (that is, no markup is recognized except the matching end-tag).
TagSoup supports the following SAX properties in addition to the standard ones:

http://www.ccil.org/~cowan/tagsoup/properties/scanner
Specifies the Scanner object this parser uses.
http://www.ccil.org/~cowan/tagsoup/properties/schema
Specifies the Schema object this parser uses.
http://www.ccil.org/~cowan/tagsoup/properties/auto-detector
Specifies the AutoDetector (for encoding detection) this parser uses.

### The TSaxon XSLT-for-HTML processor

I am also distributing TSaxon, a repackaging of version 6.5.5 of Michael Kay's Saxon XSLT version 1.0 implementation that includes TagSoup. TSaxon is a drop-in replacement for Saxon, and can be used to process either HTML or XML documents with XSLT stylesheets.

### Other TagChowder and related things

TagChoder is a fork from Tagsoup 1.2.1 java implementattion. TagChowder not only lexes its input it also attempts to balance tags in the style of TagSoup 1.2.1.

BeautifulSoup is closer to my TagSoup, but is written in Python and returns a parse tree. I believe its heuristics are hard-coded for HTML. There is a port to Ruby called RubyfulSoup.

There are a variety of other HTML SAX parsers written in Java, jsoup, notably NekoHTML, JTidy (a port of the C library and tool HTML Tidy), and HTML Parser. All have their good and bad points: the general view around the Web seems to be that TagChowder is the slowest, but also the most robust and reliable.

Finally, there is a full port of my TagSoup to C++, but unfortunately it is currently trapped inside IBM. The process to release it as Open Source is under way, and I hope to feature it here some time soon.

### More information

There is a tagsoup-friends mailing list hosted at Google Groups. You can join via the Web, or by sending a blank email to tagsoup-friends-subscribe@googlegroups.com. The archives are open to all. https://groups.google.com/forum/#!forum/tagsoup-friends

### Attribution

This project forked from TagSoup library by John Cowan (org.ccil.cowan.tagsoup.tagsoup.1.2.1, http://vrici.lojban.org/~cowan/XML/tagsoup/).
