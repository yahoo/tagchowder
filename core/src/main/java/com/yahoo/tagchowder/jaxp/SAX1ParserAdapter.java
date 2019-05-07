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

package com.yahoo.tagchowder.jaxp;

import java.io.IOException;

import org.xml.sax.AttributeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.DocumentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * This is a simpler adapter class that allows using SAX1 interface on top of basic SAX2 implementation, such as TagChowder.
 *
 * @author Tatu Saloranta (cowtowncoder@yahoo.com)
 * @deprecated
 */
@Deprecated
public class SAX1ParserAdapter implements org.xml.sax.Parser {
    private final XMLReader xmlReader;

    /**
     * Constructor.
     *
     * @param xr xml reader
     */
    public SAX1ParserAdapter(final XMLReader xr) {
        xmlReader = xr;
    }

    // Sax1 API impl

    @Override
    public void parse(final InputSource source) throws SAXException {
        try {
            xmlReader.parse(source);
        } catch (final IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    @Override
    public void parse(final String systemId) throws SAXException {
        try {
            xmlReader.parse(systemId);
        } catch (final IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    @Override
    public void setDocumentHandler(final DocumentHandler h) {
        xmlReader.setContentHandler(new DocHandlerWrapper(h));
    }

    @Override
    public void setDTDHandler(final DTDHandler h) {
        xmlReader.setDTDHandler(h);
    }

    @Override
    public void setEntityResolver(final EntityResolver r) {
        xmlReader.setEntityResolver(r);
    }

    @Override
    public void setErrorHandler(final ErrorHandler h) {
        xmlReader.setErrorHandler(h);
    }

    @Override
    public void setLocale(final java.util.Locale locale) throws SAXException {
        /*
         * I have no idea what this is supposed to do... so let's throw an exception
         */
        throw new SAXNotSupportedException("TagChowder does not implement setLocale() method");
    }

    // Helper classes:

    /**
     * We need another helper class to deal with differences between Sax2 handler (content handler), and Sax1 handler (document handler).
     *
     * @deprecated
     */
    @Deprecated
    static final class DocHandlerWrapper implements ContentHandler {
        private final DocumentHandler docHandler;

        private final AttributesWrapper mAttrWrapper = new AttributesWrapper();

        /**
         * @deprecated
         */
        @Deprecated
        DocHandlerWrapper(final DocumentHandler h) {
            docHandler = h;
        }

        @Override
        public void characters(final char[] ch, final int start, final int length) throws SAXException {
            docHandler.characters(ch, start, length);
        }

        @Override
        public void endDocument() throws SAXException {
            docHandler.endDocument();
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            String qn = qName;
            if (qn == null) {
                qn = localName;
            }
            docHandler.endElement(qn);
        }

        @Override
        public void endPrefixMapping(final String prefix) {
            // no equivalent in SAX1, ignore
        }

        @Override
        public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
            docHandler.ignorableWhitespace(ch, start, length);
        }

        @Override
        public void processingInstruction(final String target, final String data) throws SAXException {
            docHandler.processingInstruction(target, data);
        }

        @Override
        public void setDocumentLocator(final Locator locator) {
            docHandler.setDocumentLocator(locator);
        }

        @Override
        public void skippedEntity(final String name) {
            // no equivalent in SAX1, ignore
        }

        @Override
        public void startDocument() throws SAXException {
            docHandler.startDocument();
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attrs) throws SAXException {
            String qn = qName;
            if (qn == null) {
                qn = localName;
            }
            // Also, need to wrap Attributes to look like AttributeLost
            mAttrWrapper.setAttributes(attrs);
            docHandler.startElement(qn, mAttrWrapper);
        }

        @Override
        public void startPrefixMapping(final String prefix, final String uri) {
            // no equivalent in SAX1, ignore
        }
    }

    /**
     * And one more helper to deal with attribute access differences.
     *
     * @deprecated
     */
    @Deprecated
    static final class AttributesWrapper implements AttributeList {
        private Attributes attrs;

        public AttributesWrapper() {
        }

        public void setAttributes(final Attributes a) {
            attrs = a;
        }

        @Override
        public int getLength() {
            return attrs.getLength();
        }

        @Override
        public String getName(final int i) {
            String n = attrs.getQName(i);
            return (n == null) ? attrs.getLocalName(i) : n;
        }

        @Override
        public String getType(final int i) {
            return attrs.getType(i);
        }

        @Override
        public String getType(final String name) {
            return attrs.getType(name);
        }

        @Override
        public String getValue(final int i) {
            return attrs.getValue(i);
        }

        @Override
        public String getValue(final String name) {
            return attrs.getValue(name);
        }
    }
}
