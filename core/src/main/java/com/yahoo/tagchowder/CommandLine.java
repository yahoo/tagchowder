/*
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  ====================================================================
 */

/*
 * Changes to the original project are Copyright 2019 Oath Inc.
 */

// The TagChowder command line UI

package com.yahoo.tagchowder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

import com.yahoo.tagchowder.templates.HTMLSchema;

/**
 * The stand-alone TagChowder program.
 **/
public class CommandLine {

    private static Hashtable options = new Hashtable();
    static {
        options.put("--nocdata", Boolean.FALSE); // CDATA elements are normal
        options.put("--files", Boolean.FALSE); // process arguments as separate files
        options.put("--reuse", Boolean.FALSE); // reuse a single Parser
        options.put("--nons", Boolean.FALSE); // no namespaces
        options.put("--nobogons", Boolean.FALSE); // suppress unknown elements
        options.put("--any", Boolean.FALSE); // unknowns have ANY content model
        options.put("--emptybogons", Boolean.FALSE); // unknowns have EMPTY content model
        options.put("--norootbogons", Boolean.FALSE); // unknowns can't be the root
        options.put("--pyxin", Boolean.FALSE); // input is PYX
        options.put("--lexical", Boolean.FALSE); // output comments
        options.put("--pyx", Boolean.FALSE); // output is PYX
        options.put("--html", Boolean.FALSE); // output is HTML
        options.put("--method=", Boolean.FALSE); // output method
        options.put("--doctype-public=", Boolean.FALSE); // override public id
        options.put("--doctype-system=", Boolean.FALSE); // override system id
        options.put("--output-encoding=", Boolean.FALSE); // output encoding
        options.put("--omit-xml-declaration", Boolean.FALSE); // omit XML decl
        options.put("--encoding=", Boolean.FALSE); // specify encoding
        options.put("--help", Boolean.FALSE); // display help
        options.put("--version", Boolean.FALSE); // display version
        options.put("--nodefaults", Boolean.FALSE); // no default attrs
        options.put("--nocolons", Boolean.FALSE); // colon to underscore
        options.put("--norestart", Boolean.FALSE); // no restartable elements
        options.put("--ignorable", Boolean.FALSE); // return ignorable whitespace
    }

    /**
     * Main method. Processes specified files or standard input
     *
     * @param argv argument
     * @throws IOException IOException
     * @throws SAXException SAXException
     **/

    public static void main(final String[] argv) throws IOException, SAXException {
        int optind = getopts(options, argv);
        if (hasOption(options, "--help")) {
            doHelp();
            return;
        }
        if (hasOption(options, "--version")) {
            System.err.println("TagChowder version 2.0.1");
            return;
        }
        if (argv.length == optind) {
            process("", System.out);
        } else if (hasOption(options, "--files")) {
            for (int i = optind; i < argv.length; i++) {
                String src = argv[i];
                String dst;
                int j = src.lastIndexOf('.');
                if (j == -1) {
                    dst = src + ".xhtml";
                } else if (src.endsWith(".xhtml")) {
                    dst = src + "_";
                } else {
                    dst = src.substring(0, j) + ".xhtml";
                }
                System.err.println("src: " + src + " dst: " + dst);
                OutputStream os = new FileOutputStream(dst);
                process(src, os);
            }
        } else {
            for (int i = optind; i < argv.length; i++) {
                System.err.println("src: " + argv[i]);
                process(argv[i], System.out);
            }
        }
    }

    // Print the help message

    private static void doHelp() {
        System.err.print("usage: java -jar tagchowder-*.jar ");
        System.err.print(" [ ");
        boolean first = true;
        for (Enumeration e = options.keys(); e.hasMoreElements();) {
            if (!first) {
                System.err.print("| ");
            }
            first = false;
            String key = (String) (e.nextElement());
            System.err.print(key);
            if (key.endsWith("=")) {
                System.err.print("?");
            }
            System.err.print(" ");
        }
        System.err.println("]*");
    }

    private static Parser theParser = null;
    private static HTMLSchema theSchema = null;
    private static String theOutputEncoding = null;

    // Process one source onto an output stream.
    private static void process(final String src, final OutputStream os) throws IOException, SAXException {
        XMLReader r;
        if (hasOption(options, "--reuse")) {
            if (theParser == null) {
                theParser = new Parser();
            }
            r = theParser;
        } else {
            r = new Parser();
        }

        r.setProperty(Parser.SCHEMA_PROPERTY, HTMLSchema.class);
        theSchema = (HTMLSchema) ((Parser) r).getTheSchema();

        if (hasOption(options, "--nocdata")) {
            r.setFeature(Parser.CDATA_ELEMENTS_FEATURE, false);
        }

        if (hasOption(options, "--nons") || hasOption(options, "--html")) {
            r.setFeature(Parser.NAMESPACES_FEATURE, false);
        }

        if (hasOption(options, "--nobogons")) {
            r.setFeature(Parser.IGNORE_BOGONS_FEATURE, true);
        }

        if (hasOption(options, "--any")) {
            r.setFeature(Parser.BOGONS_EMPTY_FEATURE, false);
        } else if (hasOption(options, "--emptybogons")) {
            r.setFeature(Parser.BOGONS_EMPTY_FEATURE, true);
        }

        if (hasOption(options, "--norootbogons")) {
            r.setFeature(Parser.ROOT_BOGONS_FEATURE, false);
        }

        if (hasOption(options, "--nodefaults")) {
            r.setFeature(Parser.DEFAULT_ATTRIBUTES_FEATURE, false);
        }
        if (hasOption(options, "--nocolons")) {
            r.setFeature(Parser.TRANSLATE_COLONS_FEATURE, true);
        }

        if (hasOption(options, "--norestart")) {
            r.setFeature(Parser.RESTART_ELEMENTS_FEATURE, false);
        }

        if (hasOption(options, "--ignorable")) {
            r.setFeature(Parser.IGNORABLE_WHITESPACE_FEATURE, true);
        }

        Writer w;
        if (theOutputEncoding == null) {
            w = new OutputStreamWriter(os);
        } else {
            w = new OutputStreamWriter(os, theOutputEncoding);
        }
        ContentHandler h = chooseContentHandler(w);
        r.setContentHandler(h);
        if (hasOption(options, "--lexical") && h instanceof LexicalHandler) {
            r.setProperty(Parser.LEXICAL_HANDLER_PROPERTY, h);
        }
        InputSource s = new InputSource();
        if (!src.equals("")) {
            s.setSystemId(src);
        } else {
            s.setByteStream(System.in);
        }
        if (hasOption(options, "--encoding=")) {
            // System.out.println("%% Found --encoding");
            String encoding = (String) options.get("--encoding=");
            if (encoding != null) {
                s.setEncoding(encoding);
            }
        }
        r.parse(s);
    }

    // Pick a content handler to generate the desired format.

    private static ContentHandler chooseContentHandler(final Writer w) {
        XMLWriter x;
        if (hasOption(options, "--pyx")) {
            return new PYXWriter(w);
        }

        x = new XMLWriter(w);
        if (hasOption(options, "--html")) {
            x.setOutputProperty(XMLWriter.METHOD, "html");
            x.setOutputProperty(XMLWriter.OMIT_XML_DECLARATION, "yes");
        }
        if (hasOption(options, "--method=")) {
            String method = (String) options.get("--method=");
            if (method != null) {
                x.setOutputProperty(XMLWriter.METHOD, method);
            }
        }
        if (hasOption(options, "--doctype-public=")) {
            String doctypePublic = (String) options.get("--doctype-public=");
            if (doctypePublic != null) {
                x.setOutputProperty(XMLWriter.DOCTYPE_PUBLIC, doctypePublic);
            }
        }
        if (hasOption(options, "--doctype-system=")) {
            String doctypeSystem = (String) options.get("--doctype-system=");
            if (doctypeSystem != null) {
                x.setOutputProperty(XMLWriter.DOCTYPE_SYSTEM, doctypeSystem);
            }
        }
        if (hasOption(options, "--output-encoding=")) {
            theOutputEncoding = (String) options.get("--output-encoding=");
            // System.err.println("%%%% Output encoding is " + theOutputEncoding);
            if (theOutputEncoding != null) {
                x.setOutputProperty(XMLWriter.ENCODING, theOutputEncoding);
            }
        }
        if (hasOption(options, "--omit-xml-declaration")) {
            x.setOutputProperty(XMLWriter.OMIT_XML_DECLARATION, "yes");
        }
        x.setPrefix(theSchema.getURI(), "");
        return x;
    }

    // Options processing

    private static int getopts(final Hashtable options, final String[] argv) {
        int optind;
        for (optind = 0; optind < argv.length; optind++) {
            String arg = argv[optind];
            String value = null;
            if (arg.charAt(0) != '-') {
                break;
            }
            int eqsign = arg.indexOf('=');
            if (eqsign != -1) {
                value = arg.substring(eqsign + 1, arg.length());
                arg = arg.substring(0, eqsign + 1);
            }
            if (options.containsKey(arg)) {
                if (value == null) {
                    options.put(arg, Boolean.TRUE);
                } else {
                    options.put(arg, value);
                }
                // System.out.println("%% Parsed [" + arg + "]=[" + value + "]");
            } else {
                System.err.print("Unknown option ");
                System.err.println(arg);
                System.exit(1);
            }
        }
        return optind;
    }

    // Return true if an option exists.

    private static boolean hasOption(final Hashtable options, final String option) {
        if (Boolean.getBoolean(option)) {
            return true;
        } else if (options.get(option) != Boolean.FALSE) {
            return true;
        }
        return false;
    }

}
