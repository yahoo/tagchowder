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

// PYX Writer
// FIXME: does not do escapes in attribute values
// FIXME: outputs entities as bare '&' character

package com.yahoo.tagchowder;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * A ContentHandler that generates PYX format instead of XML. Primarily useful for debugging.
 **/
public class PYXWriter implements ScanHandler, ContentHandler, LexicalHandler {

    private PrintWriter theWriter; // where we write to
    private static char[] dummy = new char[1];
    private String attrName; // saved attribute name

    // ScanHandler implementation

    @Override
    public void adup(final char[] buff, final int offset, final int length) throws SAXException {
        theWriter.println(attrName);
        attrName = null;
    }

    @Override
    public void aname(final char[] buff, final int offset, final int length) throws SAXException {
        theWriter.print('A');
        theWriter.write(buff, offset, length);
        theWriter.print(' ');
        attrName = new String(buff, offset, length);
    }

    @Override
    public void aval(final char[] buff, final int offset, final int length) throws SAXException {
        theWriter.write(buff, offset, length);
        theWriter.println();
        attrName = null;
    }

    @Override
    public void cmnt(final char[] buff, final int offset, final int length) throws SAXException {
        // theWriter.print('!');
        // theWriter.write(buff, offset, length);
        // theWriter.println();
    }

    @Override
    public void entity(final char[] buff, final int offset, final int length) throws SAXException {
    }

    @Override
    public int getEntity() {
        return 0;
    }

    @Override
    public void eof(final char[] buff, final int offset, final int length) throws SAXException {
        theWriter.close();
    }

    @Override
    public void etag(final char[] buff, final int offset, final int length) throws SAXException {
        theWriter.print(')');
        theWriter.write(buff, offset, length);
        theWriter.println();
    }

    @Override
    public void decl(final char[] buff, final int offset, final int length) throws SAXException {
    }

    @Override
    public void gi(final char[] buff, final int offset, final int length) throws SAXException {
        theWriter.print('(');
        theWriter.write(buff, offset, length);
        theWriter.println();
    }

    @Override
    public void cdsect(final char[] buff, final int offset, final int length) throws SAXException {
        pcdata(buff, offset, length);
    }

    @Override
    public void pcdata(final char[] buff, final int offset, final int length) throws SAXException {
        int l = length;
        if (l == 0) {
            return; // nothing to do
        }
        boolean inProgress = false;
        l += offset;
        for (int i = offset; i < l; i++) {
            if (buff[i] == '\n') {
                if (inProgress) {
                    theWriter.println();
                }
                theWriter.println("-\\n");
                inProgress = false;
            } else {
                if (!inProgress) {
                    theWriter.print('-');
                }
                switch (buff[i]) {
                case '\t':
                    theWriter.print("\\t");
                    break;
                case '\\':
                    theWriter.print("\\\\");
                    break;
                default:
                    theWriter.print(buff[i]);
                }
                inProgress = true;
            }
        }
        if (inProgress) {
            theWriter.println();
        }
    }

    @Override
    public void pitarget(final char[] buff, final int offset, final int length) throws SAXException {
        theWriter.print('?');
        theWriter.write(buff, offset, length);
        theWriter.write(' ');
    }

    @Override
    public void pi(final char[] buff, final int offset, final int length) throws SAXException {
        theWriter.write(buff, offset, length);
        theWriter.println();
    }

    @Override
    public void stagc(final char[] buff, final int offset, final int length) throws SAXException {
        // theWriter.println("!"); // FIXME
    }

    @Override
    public void stage(final char[] buff, final int offset, final int length) throws SAXException {
        theWriter.println("!"); // FIXME
    }

    // SAX ContentHandler implementation

    @Override
    public void characters(final char[] buff, final int offset, final int length) throws SAXException {
        pcdata(buff, offset, length);
    }

    @Override
    public void endDocument() throws SAXException {
        theWriter.close();
    }

    @Override
    public void endElement(final String uri, final String localname, final String qname) throws SAXException {
        String qn = qname;
        if (qn.length() == 0) {
            qn = localname;
        }
        theWriter.print(')');
        theWriter.println(qn);
    }

    @Override
    public void endPrefixMapping(final String prefix) throws SAXException {
    }

    @Override
    public void ignorableWhitespace(final char[] buff, final int offset, final int length) throws SAXException {
        characters(buff, offset, length);
    }

    @Override
    public void processingInstruction(final String target, final String data) throws SAXException {
        theWriter.print('?');
        theWriter.print(target);
        theWriter.print(' ');
        theWriter.println(data);
    }

    @Override
    public void setDocumentLocator(final Locator locator) {
    }

    @Override
    public void skippedEntity(final String name) throws SAXException {
    }

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void startElement(final String uri, final String localname, final String qname, final Attributes atts) throws SAXException {
        String qn = qname;
        if (qn.length() == 0) {
            qn = localname;
        }
        theWriter.print('(');
        theWriter.println(qn);
        AttributesImpl attributes = (AttributesImpl) atts;
        Iterator<Integer> iterator = attributes.getIndexes();
        while (iterator.hasNext()) {
            int i = iterator.next();
            qn = atts.getQName(i);
            if (qn.length() == 0) {
                qn = atts.getLocalName(i);
            }
            theWriter.print('A');
            // theWriter.print(atts.getType(i)); // DEBUG
            theWriter.print(qn);
            theWriter.print(' ');
            theWriter.println(atts.getValue(i));
        }
    }

    @Override
    public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
    }

    // Default LexicalHandler implementation

    @Override
    public void comment(final char[] ch, final int start, final int length) throws SAXException {
        cmnt(ch, start, length);
    }

    @Override
    public void endCDATA() throws SAXException {
    }

    @Override
    public void endDTD() throws SAXException {
    }

    @Override
    public void endEntity(final String name) throws SAXException {
    }

    @Override
    public void startCDATA() throws SAXException {
    }

    @Override
    public void startDTD(final String name, final String publicId, final String systemId) throws SAXException {
    }

    @Override
    public void startEntity(final String name) throws SAXException {
    }

    /**
     * Constructor.
     *
     * @param w writer
     */

    public PYXWriter(final Writer w) {
        if (w instanceof PrintWriter) {
            theWriter = (PrintWriter) w;
        } else {
            theWriter = new PrintWriter(w);
        }
    }
}
