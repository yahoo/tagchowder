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

package com.lafaspot.tagchowder.jaxp;

import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.SAXParser;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import com.lafaspot.tagchowder.Parser;

/**
 * This is a simple implementation of JAXP {@link SAXParser}, to allow easier integration of TagChowder with the default JDK xml processing stack.
 *
 * @author Tatu Saloranta (cowtowncoder@yahoo.com)
 */
public class SAXParserImpl extends SAXParser {
    private final Parser parser;

    protected SAXParserImpl() { // used by factory, for prototypes
        super();
        parser = new Parser();
    }

    /**
     * Initialize new instance.
     *
     * @param features features map
     * @return new SAXParserImpl instance
     * @throws SAXException SAXException
     */
    public static SAXParserImpl newInstance(final Map features) throws SAXException {
        SAXParserImpl parser = new SAXParserImpl();
        if (features != null) {
            Iterator it = features.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                parser.setFeature((String) entry.getKey(), ((Boolean) entry.getValue()).booleanValue());
            }
        }
        return parser;
    }

    // // // JAXP API implementation:

    /**
     * To support SAX1 interface, we'll need to use an adapter.
     *
     * @deprecated
     */
    @Deprecated
    @Override
    public org.xml.sax.Parser getParser() throws SAXException {
        return new SAX1ParserAdapter(parser);
    }

    @Override
    public XMLReader getXMLReader() {
        return parser;
    }

    @Override
    public boolean isNamespaceAware() {
        try {
            return parser.getFeature(Parser.NAMESPACES_FEATURE);
        } catch (SAXException sex) { // should never happen... so:
            throw new RuntimeException(sex.getMessage());
        }
    }

    @Override
    public boolean isValidating() {
        try {
            return parser.getFeature(Parser.VALIDATION_FEATURE);
        } catch (SAXException sex) { // should never happen... so:
            throw new RuntimeException(sex.getMessage());
        }
    }

    @Override
    public void setProperty(final String name, final Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        parser.setProperty(name, value);
    }

    @Override
    public Object getProperty(final String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return parser.getProperty(name);
    }

    // // // Additional convenience methods

    /**
     * Set feature.
     *
     * @param name name of feature
     * @param value value of feature
     * @throws SAXNotRecognizedException SAXNotRecognizedException
     * @throws SAXNotSupportedException SAXNotSupportedException
     */
    public void setFeature(final String name, final boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        parser.setFeature(name, value);
    }

    /**
     * Get feature.
     *
     * @param name name of feature
     * @return value of feature
     * @throws SAXNotRecognizedException SAXNotRecognizedExceptionn
     * @throws SAXNotSupportedException SAXNotSupportedException
     */
    public boolean getFeature(final String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return parser.getFeature(name);
    }
}
