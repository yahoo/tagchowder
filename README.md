# TagChowder
> Parse and extract information from (possibly malformed) HTML/XML documents

                        TagChowder - Just Keep On Truckin'

TagChowder is a SAX-compliant parser written in Java that, instead of parsing well-formed or valid XML, parses HTML as it is found in the wild: poor, nasty and brutish, though quite often far from short. TagChowder is designed for people who need to process this information with some semblance of a rational application design. By providing a SAX interface, it allows standard XML tools to be applied to even the worst HTML. TagChowder also includes a command-line processor that reads HTML files and can generate either clean HTML or well-formed XML that is a close approximation to XHTML.

TagChowder is a fork from TagSoup library by John Cowan version 1.2.1.

## Table of Contents

- [Background](#background)
- [Install](#install)
- [Usage](#usage)
- [License](#license)
- [Attribution](#Attribution)

## Background

TagChowder is designed as a parser, not an entire application. It isn't intended to be used like HTML Tidy to permanently clean up bad HTML, rather, it's purpose is to parse HTML on the fly. Therefore, it does not convert presentation HTML to CSS. However, it does guarantee well-structured results: tags will wind up properly nested, default attributes will appear appropriately, and so on.

The semantics of TagChowder are as far as practical those of actual HTML browsers. In particular, never, never will it throw any sort of syntax error: the TagChowder motto is "Just Keep On Truckin." But there's much, much more. For example, if the first tag is LI, it will supply the application with enclosing HTML, BODY, and UL tags. Why UL? Because that's what a browser assumes in this situation. For the same reason, overlapping tags are correctly restarted whenever possible: text like:

`This is <B>bold, <I>bold italic, </b>italic, </i>normal text`

will be correctly rewritten as:

`This is <b>bold, <i>bold italic, </i></b><i>italic, </i>normal text.`

By intention, TagChowder is small and fast. The only framework it depends on is SAX, and it should work with any frameworks that accepts SAX parsers. In particular, XOM is known to work.

You can replace the low-level HTML scanner with one based on Sean McGrath's PYX format (very close to James Clark's ESIS format). You can also supply an AutoDetector that peeks at the incoming byte stream and guesses a character encoding for it. Otherwise, the platform default is used. If you need a character set detector, you can try to adapt [Mozilla's Charset Detector](https://www-archive.mozilla.org/projects/intl/chardet.html) (we'd be interested to learn about your results).

### Install

TagChowder uses maven as tool for building and managing project. Add following snippet to your pom.xml and hit "mvn" to build your project.

```
<dependency>
  <groupId>com.github.lafa.tagchowder</groupId>
  <artifactId>tagchowder.core</artifactId>
  <version>2.0.3</version>
</dependency>
```
Here are the instructions to setup maven environment.
https://maven.apache.org/what-is-maven.html
https://maven.apache.org/guides/introduction/introduction-to-the-pom.html

### Usage
#### TagChowder as a Stand-Alone Program

It is possible to run TagChowder as a program by saying java -jar tagchowder.jar [option ...] [file ...]. Files mentioned on the command line will be parsed individually. If no files are specified, the standard input is read.

The following options are understood:

* **--files** - Output into individual files, with html extensions changed to xhtml. Otherwise, all output is sent to the standard output.
* **--html** - Output is in clean HTML: the XML declaration is suppressed, as are end-tags for the known empty elements.
* **--omit-xml-declaration** - The XML declaration is suppressed.
* **--method=html** - End-tags for the known empty HTML elements are suppressed.
* **--doctype-system=systemid** - Forces the output of a DOCTYPE declaration with the specified systemid.
* **--doctype-public=publicid** - Forces the output of a DOCTYPE declaration with the specified publicid.
* **--version=version** - Sets the version string in the XML declaration.
* **--standalone=[yes|no]** - Sets the standalone declaration to yes or no.
* **--pyx** - Output is in PYX format.
* **--pyxin** - Input is in PYXoid format (need not be well-formed).
* **--nons** - Namespaces are suppressed. Normally, all elements are in the XHTML 1.x namespace, and all attributes are in no namespace.
* **--nobogons** - Bogons (unknown elements) are suppressed.
* **--nodefaults** - Default attribute values are suppressed.
* **--nocolons** - Explicit colons in element and attribute names are changed to underscores.
* **--norestart** - don't restart any normally restartable elements.
* **--ignorable** - Output whitespace in elements with element-only content.
* **--emptybogons** - Bogons are given a content model of EMPTY rather than ANY.
* **--any** - Bogons are given a content model of ANY rather than EMPTY (default).
* **--norootbogons** - Bogons are not allowed to be root elements; make them subordinate to the root.
* **--lexical** - Pass through HTML comments and DOCTYPE declarations. Has no effect when output is in PYX format.
* **--reuse** - Reuse a single instance of TagChowder parser throughout. Normally, a new one is instantiated for each input file.
* **--nocdata** - Change the content models of the script and style elements to treat them as ordinary #PCDATA (text-only) elements, as in XHTML, rather than with the special CDATA content model.
* **--encoding=encoding** - Specify the input encoding. The default is the Java platform default.
* **--output-encoding=encoding** - Specify the output encoding. The default is the Java platform default.
* **--help** - Print help.
* **--version** - Print the version number.

### Other TagChowder and related things

TagChowder is a fork from Tagsoup 1.2.1 java implementation. TagChowder not only lexes its input it also attempts to balance tags in the style of TagSoup 1.2.1.

### More information

There is a tagsoup-friends mailing list hosted at Google Groups. You can join via the Web, or by sending a blank email to tagsoup-friends-subscribe@googlegroups.com. The archives are open to all. https://groups.google.com/forum/#!forum/tagsoup-friends

### Contribute

Please refer to the [Contributing.md](Contributing.md) file for information about how to get involved. We welcome issues, questions, and pull requests. Pull Requests are welcome.

## License

This project is licensed under the terms of the [Apache 2.0](LICENSE-Apache-2.0) open source license. Please refer to [LICENSE](LICENSE) for the full terms.

## Attribution

This project forked from TagSoup library by John Cowan (org.ccil.cowan.tagsoup.tagsoup.1.2.1, http://vrici.lojban.org/~cowan/XML/tagsoup/).
