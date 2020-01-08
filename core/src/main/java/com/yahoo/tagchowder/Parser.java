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

// The TagChowder parser

package com.yahoo.tagchowder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import com.yahoo.tagchowder.templates.HTMLScanner;
import com.yahoo.tagchowder.templates.HTMLSchema;

/**
 * The SAX parser class.
 **/
public class Parser extends DefaultHandler implements ScanHandler, XMLReader, LexicalHandler {

    // XMLReader implementation

    private ContentHandler theContentHandler = this;
    private LexicalHandler theLexicalHandler = this;
    private DTDHandler theDTDHandler = this;
    private ErrorHandler theErrorHandler = this;
    private EntityResolver theEntityResolver = this;
    private Schema theSchema;
    private Scanner theScanner;
    private AutoDetector theAutoDetector;
    /** Logger. */
    private Logger logger = LoggerFactory.getLogger(Parser.class);


    private int defaultBufferSize = 2000;
    // Default values for feature flags

    private static final boolean DEFAULT_NAMESPACES = true;
    private static final boolean DEFAULT_IGNORE_BOGONS = false;
    private static final boolean DEFAULT_BOGONS_EMPTY = false;
    private static final boolean DEFAULT_ROOT_BOGONS = true;
    private static final boolean DEFAULT_DEFAULT_ATTRIBUTES = true;
    private static final boolean DEFAULT_TRANSLATE_COLONS = false;
    private static final boolean DEFAULT_RESTART_ELEMENTS = true;
    private static final boolean DEFAULT_IGNORABLE_WHITESPACE = false;
    private static final boolean DEFAULT_CDATA_ELEMENTS = true;

    // Feature flags.

    private boolean namespaces = DEFAULT_NAMESPACES;
    private boolean ignoreBogons = DEFAULT_IGNORE_BOGONS;
    private boolean bogonsEmpty = DEFAULT_BOGONS_EMPTY;
    private boolean rootBogons = DEFAULT_ROOT_BOGONS;
    private boolean defaultAttributes = DEFAULT_DEFAULT_ATTRIBUTES;
    private boolean translateColons = DEFAULT_TRANSLATE_COLONS;
    private boolean restartElements = DEFAULT_RESTART_ELEMENTS;
    private boolean ignorableWhitespace = DEFAULT_IGNORABLE_WHITESPACE;
    private boolean cdataElements = DEFAULT_CDATA_ELEMENTS;

    /**
     * Parser Context.
     */
    private ParserContext theParserContext = new ParserContext.Builder(true).build();

    /**
     * A value of "true" indicates namespace URIs and unprefixed local names for element and attribute names will be available.
     **/
    public static final String NAMESPACES_FEATURE = "http://xml.org/sax/features/namespaces";

    /**
     * A value of "true" indicates that XML qualified names (with prefixes) and attributes (including xmlns* attributes) will be available. We don't
     * support this value.
     **/
    public static final String NAMESPACE_PREFIXES_FEATURE = "http://xml.org/sax/features/namespace-prefixes";

    /**
     * Reports whether this parser processes external general entities (it doesn't).
     **/
    public static final String EXTERNAL_GENERAL_ENTITIES_FEATURE = "http://xml.org/sax/features/external-general-entities";

    /**
     * Reports whether this parser processes external parameter entities (it doesn't).
     **/
    public static final String EXTERNAL_PARAMETER_ENTITIES_FEATURE = "http://xml.org/sax/features/external-parameter-entities";

    /**
     * May be examined only during a parse, after the startDocument() callback has been completed; read-only. The value is true if the document
     * specified standalone="yes" in its XML declaration, and otherwise is false. (It's always false.)
     **/
    public static final String IS_STANDALONE_FEATURE = "http://xml.org/sax/features/is-standalone";

    /**
     * A value of "true" indicates that the LexicalHandler will report the beginning and end of parameter entities (it won't).
     **/
    public static final String LEXICAL_HANDLER_PARAMETER_ENTITIES_FEATURE = "http://xml.org/sax/features/lexical-handler/parameter-entities";

    /**
     * A value of "true" indicates that system IDs in declarations will be absolutized (relative to their base URIs) before reporting. (This returns
     * true but doesn't actually do anything.)
     **/
    public static final String RESOLVE_DTDURIS_FEATURE = "http://xml.org/sax/features/resolve-dtd-uris";

    /**
     * Has a value of "true" if all XML names (for elements, prefixes, attributes, entities, notations, and local names), as well as Namespace URIs,
     * will have been interned using java.lang.String.intern. This supports fast testing of equality/inequality against string constants, rather than
     * forcing slower calls to String.equals(). (We always intern.)
     **/
    public static final String STRING_INTERNING_FEATURE = "http://xml.org/sax/features/string-interning";

    /**
     * Returns "true" if the Attributes objects passed by this parser in ContentHandler.startElement() implement the org.xml.sax.ext.Attributes2
     * interface. (They don't.)
     **/

    public static final String USE_ATTRIBUTES2_FEATURE = "http://xml.org/sax/features/use-attributes2";

    /**
     * Returns "true" if the Locator objects passed by this parser in ContentHandler.setDocumentLocator() implement the org.xml.sax.ext.Locator2
     * interface. (They don't.)
     **/
    public static final String USE_LOCATOR2_FEATURE = "http://xml.org/sax/features/use-locator2";

    /**
     * Returns "true" if, when setEntityResolver is given an object implementing the org.xml.sax.ext.EntityResolver2 interface, those new methods will
     * be used. (They won't be.)
     **/
    public static final String USE_ENTITYRESOLVER2_FEATURE = "http://xml.org/sax/features/use-entity-resolver2";

    /**
     * Controls whether the parser is reporting all validity errors (We don't report any validity errors).
     **/
    public static final String VALIDATION_FEATURE = "http://xml.org/sax/features/validation";

    /**
     * Controls whether the parser reports Unicode normalization errors as described in section 2.13 and Appendix B of the XML 1.1 Recommendation. (We
     * don't normalize.)
     **/
    public static final String UNICODE_NORMALIZATION_CHECKING_FEATURE = "http://xml.org/sax/features/unicode-normalization-checking";

    /**
     * Controls whether, when the namespace-prefixes feature is set, the parser treats namespace declaration attributes as being in the
     * http://www.w3.org/2000/xmlns/ namespace. (It doesn't.)
     **/
    public static final String XMLNS_URIS_FEATURE = "http://xml.org/sax/features/xmlns-uris";

    /**
     * Returns "true" if the parser supports both XML 1.1 and XML 1.0. (Always false.)
     **/
    public static final String XML11_FEATURE = "http://xml.org/sax/features/xml-1.1";

    /**
     * A value of "true" indicates that the parser will ignore unknown elements.
     **/
    public static final String IGNORE_BOGONS_FEATURE = "ignore-bogons";

    /**
     * A value of "true" indicates that the parser will give unknown elements a content model of EMPTY; a value of "false", a content model of ANY.
     **/
    public static final String BOGONS_EMPTY_FEATURE = "bogons-empty";

    /**
     * A value of "true" indicates that the parser will allow unknown elements to be the root element.
     **/
    public static final String ROOT_BOGONS_FEATURE = "root-bogons";

    /**
     * A value of "true" indicates that the parser will return default attribute values for missing attributes that have default values.
     **/
    public static final String DEFAULT_ATTRIBUTES_FEATURE = "default-attributes";

    /**
     * A value of "true" indicates that the parser will translate colons into underscores in names.
     **/
    public static final String TRANSLATE_COLONS_FEATURE = "translate-colons";

    /**
     * A value of "true" indicates that the parser will attempt to restart the restartable elements.
     **/
    public static final String RESTART_ELEMENTS_FEATURE = "restart-elements";

    /**
     * A value of "true" indicates that the parser will transmit whitespace in element-only content via the SAX ignorableWhitespace callback. Normally
     * this is not done, because HTML is an SGML application and SGML suppresses such whitespace.
     **/
    public static final String IGNORABLE_WHITESPACE_FEATURE = "ignorable-whitespace";

    /**
     * A value of "true" indicates that the parser will treat CDATA elements specially. Normally true, since the input is by default HTML.
     **/
    public static final String CDATA_ELEMENTS_FEATURE = "cdata-elements";

    /**
     * Used to see some syntax events that are essential in some applications: comments, CDATA delimiters, selected general entity inclusions, and the
     * start and end of the DTD (and declaration of document element name). The Object must implement org.xml.sax.ext.LexicalHandler.
     **/
    public static final String LEXICAL_HANDLER_PROPERTY = "http://xml.org/sax/properties/lexical-handler";

    /**
     * Specifies the Scanner object this Parser uses.
     **/
    public static final String SCANNER_PROPERTY = "scanner";

    /**
     * Specifies the Schema object this Parser uses.
     **/
    public static final String SCHEMA_PROPERTY = "schema";

    /**
     * Specifies the AutoDetector (for encoding detection) this Parser uses.
     **/
    public static final String AUTO_DETECTOR_PROPERTY = "auto-detector";

    /**
     * Specifies long parsing time limit.
     **/
    public static final long LONG_PARSE_TIME = 5 * 1000L; // 5 seconds

    // Due to sucky Java order of initialization issues, these
    // entries are maintained separately from the initial values of
    // the corresponding instance variables, but care must be taken
    // to keep them in sync.

    private HashMap theFeatures = new HashMap();
    {
        theFeatures.put(NAMESPACES_FEATURE, truthValue(DEFAULT_NAMESPACES));
        theFeatures.put(NAMESPACE_PREFIXES_FEATURE, Boolean.FALSE);
        theFeatures.put(EXTERNAL_GENERAL_ENTITIES_FEATURE, Boolean.FALSE);
        theFeatures.put(EXTERNAL_PARAMETER_ENTITIES_FEATURE, Boolean.FALSE);
        theFeatures.put(IS_STANDALONE_FEATURE, Boolean.FALSE);
        theFeatures.put(LEXICAL_HANDLER_PARAMETER_ENTITIES_FEATURE, Boolean.FALSE);
        theFeatures.put(RESOLVE_DTDURIS_FEATURE, Boolean.TRUE);
        theFeatures.put(STRING_INTERNING_FEATURE, Boolean.TRUE);
        theFeatures.put(USE_ATTRIBUTES2_FEATURE, Boolean.FALSE);
        theFeatures.put(USE_LOCATOR2_FEATURE, Boolean.FALSE);
        theFeatures.put(USE_ENTITYRESOLVER2_FEATURE, Boolean.FALSE);
        theFeatures.put(VALIDATION_FEATURE, Boolean.FALSE);
        theFeatures.put(XMLNS_URIS_FEATURE, Boolean.FALSE);
        theFeatures.put(XMLNS_URIS_FEATURE, Boolean.FALSE);
        theFeatures.put(XML11_FEATURE, Boolean.FALSE);
        theFeatures.put(IGNORE_BOGONS_FEATURE, truthValue(DEFAULT_IGNORE_BOGONS));
        theFeatures.put(BOGONS_EMPTY_FEATURE, truthValue(DEFAULT_BOGONS_EMPTY));
        theFeatures.put(ROOT_BOGONS_FEATURE, truthValue(DEFAULT_ROOT_BOGONS));
        theFeatures.put(DEFAULT_ATTRIBUTES_FEATURE, truthValue(DEFAULT_DEFAULT_ATTRIBUTES));
        theFeatures.put(TRANSLATE_COLONS_FEATURE, truthValue(DEFAULT_TRANSLATE_COLONS));
        theFeatures.put(RESTART_ELEMENTS_FEATURE, truthValue(DEFAULT_RESTART_ELEMENTS));
        theFeatures.put(IGNORABLE_WHITESPACE_FEATURE, truthValue(DEFAULT_IGNORABLE_WHITESPACE));
        theFeatures.put(CDATA_ELEMENTS_FEATURE, truthValue(DEFAULT_CDATA_ELEMENTS));
    }

    // Private clone of Boolean.valueOf that is guaranteed to return
    // Boolean.TRUE or Boolean.FALSE
    private static Boolean truthValue(final boolean b) {
        return b ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     *  Set the default buffer size.
     * @param defaultBufferSize the default buffer size
     */
    public void setDefaultBufferSize(final int defaultBufferSize) {
        this.defaultBufferSize = defaultBufferSize;
    }

    /**
     *  Get the default buffer size.
     *
     * @return defaultBufferSize the default buffer size
     */
    public int getDefaultBufferSize() {
        return defaultBufferSize;
    }

    /**
     * Getter for parser context.
     * @return the parser context
     */
    public ParserContext getTheParserContext() {
        return theParserContext;
    }

    /**
     * Getter for theSchema object.
     * @return theSchema
     */
    public Schema getTheSchema() {
        return theSchema;
    }


    /**
     * Clear the state.
     */
    public void clear() {
        theParserContext.clear();
        theParserContext = null;
        theSchema.clear();
        theSchema = null;
    }

    @Override
    public boolean getFeature(final String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        Boolean b = (Boolean) theFeatures.get(name);
        if (b == null) {
            throw new SAXNotRecognizedException("Unknown feature " + name);
        }
        return b.booleanValue();
    }

    @Override
    public void setFeature(final String name, final boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        Boolean b = (Boolean) theFeatures.get(name);
        if (b == null) {
            throw new SAXNotRecognizedException("Unknown feature " + name);
        }
        if (value) {
            theFeatures.put(name, Boolean.TRUE);
        } else {
            theFeatures.put(name, Boolean.FALSE);
        }

        if (name.equals(NAMESPACES_FEATURE)) {
            namespaces = value;
        } else if (name.equals(IGNORE_BOGONS_FEATURE)) {
            ignoreBogons = value;
        } else if (name.equals(BOGONS_EMPTY_FEATURE)) {
            bogonsEmpty = value;
        } else if (name.equals(ROOT_BOGONS_FEATURE)) {
            rootBogons = value;
        } else if (name.equals(DEFAULT_ATTRIBUTES_FEATURE)) {
            defaultAttributes = value;
        } else if (name.equals(TRANSLATE_COLONS_FEATURE)) {
            translateColons = value;
        } else if (name.equals(RESTART_ELEMENTS_FEATURE)) {
            restartElements = value;
        } else if (name.equals(IGNORABLE_WHITESPACE_FEATURE)) {
            ignorableWhitespace = value;
        } else if (name.equals(CDATA_ELEMENTS_FEATURE)) {
            cdataElements = value;
        } else if (name.equals(STRING_INTERNING_FEATURE)) {
            theParserContext.setUseIntern(value);
        }
    }

    @Override
    public Object getProperty(final String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name.equals(LEXICAL_HANDLER_PROPERTY)) {
            return theLexicalHandler == this ? null : theLexicalHandler;
        } else if (name.equals(SCANNER_PROPERTY)) {
            return theScanner;
        } else if (name.equals(SCHEMA_PROPERTY)) {
            return theSchema;
        } else if (name.equals(AUTO_DETECTOR_PROPERTY)) {
            return theAutoDetector;
        } else {
            throw new SAXNotRecognizedException("Unknown property " + name);
        }
    }

    @Override
    public void setProperty(final String name, final Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name.equals(LEXICAL_HANDLER_PROPERTY)) {
            if (value == null) {
                theLexicalHandler = this;
            } else if (value instanceof LexicalHandler) {
                theLexicalHandler = (LexicalHandler) value;
            } else {
                throw new SAXNotSupportedException("Your lexical handler is not a LexicalHandler");
            }
        } else if (name.equals(SCANNER_PROPERTY)) {
            if (value instanceof Scanner) {
                theScanner = (Scanner) value;
            } else {
                throw new SAXNotSupportedException("Your scanner is not a Scanner");
            }
        } else if (name.equals(SCHEMA_PROPERTY)) {
            if (value instanceof Class && Schema.class.isAssignableFrom((Class) value)) {
                try {
                     String className = ((Class) value).getName();  // Get the class name
                     Class<?> clazz = Class.forName(className);     // Class object
                    Constructor<?> constructor = clazz.getConstructor(Parser.class);
                    theSchema = (Schema) constructor.newInstance(this);  //Invoke the constructor to get new object
                } catch (IllegalAccessException | InstantiationException
                        | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
                    throw new SAXNotSupportedException("Not able to create schema object");
                }
            } else {
                throw new SAXNotSupportedException("Either your schema is not a Schema or you did not pass schema class");
            }
        } else if (name.equals(AUTO_DETECTOR_PROPERTY)) {
            if (value instanceof AutoDetector) {
                theAutoDetector = (AutoDetector) value;
            } else {
                throw new SAXNotSupportedException("Your auto-detector is not an AutoDetector");
            }
        } else {
            throw new SAXNotRecognizedException("Unknown property " + name);
        }
    }

    @Override
    public void setEntityResolver(final EntityResolver resolver) {
        theEntityResolver = (resolver == null) ? this : resolver;
    }

    @Override
    public EntityResolver getEntityResolver() {
        return (theEntityResolver == this) ? null : theEntityResolver;
    }

    @Override
    public void setDTDHandler(final DTDHandler handler) {
        theDTDHandler = (handler == null) ? this : handler;
    }

    @Override
    public DTDHandler getDTDHandler() {
        return (theDTDHandler == this) ? null : theDTDHandler;
    }

    @Override
    public void setContentHandler(final ContentHandler handler) {
        theContentHandler = (handler == null) ? this : handler;
    }

    @Override
    public ContentHandler getContentHandler() {
        return (theContentHandler == this) ? null : theContentHandler;
    }

    @Override
    public void setErrorHandler(final ErrorHandler handler) {
        theErrorHandler = (handler == null) ? this : handler;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return (theErrorHandler == this) ? null : theErrorHandler;
    }

    @Override
    public void parse(final InputSource input) throws IOException, SAXException {
        setup();
        Reader r = getReader(input);
        theContentHandler.startDocument();
        theScanner.resetDocumentLocator(input.getPublicId(), input.getSystemId());
        if (theScanner instanceof Locator) {
            theContentHandler.setDocumentLocator((Locator) theScanner);
        }
        if (!(theSchema.getURI().equals(""))) {
            theContentHandler.startPrefixMapping(theSchema.getPrefix(), theSchema.getURI());
        }
        theScanner.scan(r, this);
        clear();
    }

    @Override
    public void parse(final String systemid) throws IOException, SAXException {
        final long startTime = System.currentTimeMillis();
        parse(new InputSource(systemid));
        final long parseTime = System.currentTimeMillis() - startTime;
        // record the system id if parsing takes too long (5 seconds now)
        if (parseTime > LONG_PARSE_TIME) {
            logger.debug("Tagchowder parsing takes too long: time={}ms, system id={}", parseTime, systemid);
        }
    }

    // Sets up instance variables that haven't been set by setFeature
    private void setup() {
        if (theSchema == null) {
            theSchema = new HTMLSchema(this);
        }
        if (theScanner == null) {
            theScanner = new HTMLScanner(defaultBufferSize);
        }
        if (theAutoDetector == null) {
            theAutoDetector = new AutoDetector() {
                @Override
                public Reader autoDetectingReader(final InputStream i) {
                    return new InputStreamReader(i);
                }
            };
        }
        theStack = new Element(theSchema.getElementType("<root>"), defaultAttributes);
        thePCDATA = new Element(theSchema.getElementType("<pcdata>"), defaultAttributes);
        theNewElement = null;
        theAttributeName = null;
        thePITarget = null;
        theSaved = null;
        theEntity = 0;
        virginStack = true;
        theDoctypeName = theDoctypePublicId = theDoctypeSystemId = null;
    }

    // Return a Reader based on the contents of an InputSource
    // Buffer both the InputStream and the Reader
    private Reader getReader(final InputSource s) throws SAXException, IOException {
        Reader r = s.getCharacterStream();
        InputStream i = s.getByteStream();
        String encoding = s.getEncoding();
        String publicid = s.getPublicId();
        String systemid = s.getSystemId();
        if (r == null) {
            if (i == null) {
                i = getInputStream(publicid, systemid);
                // i = new BufferedInputStream(i);
            }
            if (encoding == null) {
                r = theAutoDetector.autoDetectingReader(i);
            } else {
                try {
                    r = new InputStreamReader(i, encoding);
                } catch (UnsupportedEncodingException e) {
                    r = new InputStreamReader(i);
                }
            }
        }
        // r = new BufferedReader(r);
        return r;
    }

    // Get an InputStream based on a publicid and a systemid
    private InputStream getInputStream(final String publicid, final String systemid) throws IOException, SAXException {
        URL basis = new URL("file", "", System.getProperty("user.dir") + "/.");
        URL url = new URL(basis, systemid);
        URLConnection c = url.openConnection();
        return c.getInputStream();
    }
    // We don't process publicids (who uses them anyhow?)

    // ScanHandler implementation

    private Element theNewElement = null;
    private String theAttributeName = null;
    private boolean theDoctypeIsPresent = false;
    private String theDoctypePublicId = null;
    private String theDoctypeSystemId = null;
    private String theDoctypeName = null;
    private String thePITarget = null;
    private Element theStack = null;
    private Element theSaved = null;
    private Element thePCDATA = null;
    private int theEntity = 0; // needs to support chars past U+FFFF

    @Override
    public void adup(final char[] buff, final int offset, final int length) throws SAXException {
        if (theNewElement == null || theAttributeName == null) {
            return;
        }
        theNewElement.setAttribute(theAttributeName, null, theAttributeName);
        theAttributeName = null;
    }

    @Override
    public void aname(final char[] buff, final int offset, final int length) throws SAXException {
        if (theNewElement == null) {
            return;
        }
        // Currently we don't rely on Schema to canonicalize
        // attribute names.
        theAttributeName = makeName(buff, offset, length).toLowerCase();
    }

    @Override
    public void aval(final char[] buff, final int offset, final int length) throws SAXException {
        if (theNewElement == null || theAttributeName == null) {
            return;
        }
        String value = new String(buff, offset, length);
        value = expandEntities(value);
        theNewElement.setAttribute(theAttributeName, null, value);
        theAttributeName = null;
    }

    // Expand entity references in attribute values selectively.
    // Currently we expand a reference iff it is properly terminated
    // with a semicolon.
    private String expandEntities(final String src) {
        int refStart = -1;
        int len = src.length();
        char[] dst = new char[len];
        int dstlen = 0;
        for (int i = 0; i < len; i++) {
            char ch = src.charAt(i);
            dst[dstlen++] = ch;
            if (ch == '&' && refStart == -1) {
                // start of a ref excluding &
                refStart = dstlen;
                // System.err.println("start of ref");
            } else if (refStart == -1) {
                // not in a ref
            } else if (Character.isLetter(ch) || Character.isDigit(ch) || ch == '#') {
                // valid entity char
            } else if (ch == ';') {
                // properly terminated ref
                int ent = lookupEntity(dst, refStart, dstlen - refStart - 1);
                // System.err.println(" = " + ent);
                if (ent > 0xFFFF) {
                    ent -= 0x10000;
                    dst[refStart - 1] = (char) ((ent >> 10) + 0xD800);
                    dst[refStart] = (char) ((ent & 0x3FF) + 0xDC00);
                    dstlen = refStart + 1;
                } else if (ent != 0) {
                    dst[refStart - 1] = (char) ent;
                    dstlen = refStart;
                }
                refStart = -1;
            } else {
                // improperly terminated ref
                refStart = -1;
            }
        }
        return new String(dst, 0, dstlen);
    }

    @Override
    public void entity(final char[] buff, final int offset, final int length) throws SAXException {
        theEntity = lookupEntity(buff, offset, length);
    }

    // Process numeric character references,
    // deferring to the schema for named ones.
    private int lookupEntity(final char[] buff, final int offset, final int length) {
        int result = 0;
        if (length < 1) {
            return result;
        }
        if (buff[offset] == '#') {
            if (length > 1 && (buff[offset + 1] == 'x' || buff[offset + 1] == 'X')) {
                try {
                    return Integer.parseInt(new String(buff, offset + 2, length - 2), 16);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
            try {
                return Integer.parseInt(new String(buff, offset + 1, length - 1), 10);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return theSchema.getEntity(new String(buff, offset, length));
    }

    @Override
    public void eof(final char[] buff, final int offset, final int length) throws SAXException {
        if (virginStack) {
            rectify(thePCDATA);
        }
        while (theStack.next() != null) {
            pop();
        }
        if (!(theSchema.getURI().equals(""))) {
            theContentHandler.endPrefixMapping(theSchema.getPrefix());
        }
        theContentHandler.endDocument();
    }

    @Override
    public void etag(final char[] buff, final int offset, final int length) throws SAXException {
        if (etagCdata(buff, offset, length)) {
            return;
        }
        etagBasic(buff, offset, length);
    }

    private static char[] etagchars = { '<', '/', '>' };

    /**
     * ETAG CDATA.
     *
     * @param buff buffer
     * @param offset offset
     * @param length length
     * @return whether CDATA is not real tag
     * @throws SAXException SAXException
     */
    public boolean etagCdata(final char[] buff, final int offset, final int length) throws SAXException {
        String currentName = theStack.name();
        // If this is a CDATA element and the tag doesn't match,
        // or isn't properly formed (junk after the name),
        // restart CDATA mode and process the tag as characters.
        if (cdataElements && (theStack.flags() & Schema.F_CDATA) != 0) {
            boolean realTag = (length == currentName.length());
            if (realTag) {
                for (int i = 0; i < length; i++) {
                    if (Character.toLowerCase(buff[offset + i]) != Character.toLowerCase(currentName.charAt(i))) {
                        realTag = false;
                        break;
                    }
                }
            }
            if (!realTag) {
                theContentHandler.characters(etagchars, 0, 2);
                theContentHandler.characters(buff, offset, length);
                theContentHandler.characters(etagchars, 2, 1);
                theScanner.startCDATA();
                return true;
            }
        }
        return false;
    }

    /**
     * ETAG basic.
     *
     * @param buff buffer
     * @param offset offset
     * @param length length
     * @throws SAXException SAXException
     */
    public void etagBasic(final char[] buff, final int offset, final int length) throws SAXException {
        theNewElement = null;
        String name;
        if (length != 0) {
            // Canonicalize case of name
            name = makeName(buff, offset, length);
            ElementType type = theSchema.getElementType(name);
            if (type == null) {
                return; // mysterious end-tag
            }
            name = type.name();
        } else {
            name = theStack.name();
        }

        Element sp;
        boolean inNoforce = false;
        for (sp = theStack; sp != null; sp = sp.next()) {
            if (sp.name().equals(name)) {
                break;
            }
            if ((sp.flags() & Schema.F_NOFORCE) != 0) {
                inNoforce = true;
            }
        }

        if (sp == null) {
            return; // Ignore unknown etags
        }
        if (sp.next() == null || sp.next().next() == null) {
            return;
        }
        if (inNoforce) { // inside an F_NOFORCE element?
            sp.preclose(); // preclose the matching element
        } else { // restartably pop everything above us
            while (theStack != sp) {
                restartablyPop();
            }
            pop();
        }
        // pop any preclosed elements now at the top
        while (theStack.isPreclosed()) {
            pop();
        }
        restart(null);
    }

    // Push restartables on the stack if possible
    // e is the next element to be started, if we know what it is
    private void restart(final Element e) throws SAXException {
        while (theSaved != null && theStack.canContain(theSaved) && (e == null || theSaved.canContain(e))) {
            Element next = theSaved.next();
            push(theSaved);
            theSaved = next;
        }
    }

    // Pop the stack irrevocably
    private void pop() throws SAXException {
        if (theStack == null) {
            return; // empty stack
        }
        String name = theStack.name();
        String localName = theStack.localName();
        String namespace = theStack.namespace();
        String prefix = prefixOf(name);

        if (!namespaces) {
            namespace = localName = "";
        }
        theContentHandler.endElement(namespace, localName, name);
        if (foreign(prefix, namespace)) {
            theContentHandler.endPrefixMapping(prefix);
        }
        Attributes atts = theStack.atts();
        for (int i = atts.getLength() - 1; i >= 0; i--) {
            String attNamespace = atts.getURI(i);
            String attPrefix = prefixOf(atts.getQName(i));
            if (foreign(attPrefix, attNamespace)) {
                theContentHandler.endPrefixMapping(attPrefix);
            }
        }
        theStack = theStack.next();
    }

    // Pop the stack restartably
    private void restartablyPop() throws SAXException {
        Element popped = theStack;
        pop();
        if (restartElements && (popped.flags() & Schema.F_RESTART) != 0) {
            popped.anonymize();
            popped.setNext(theSaved);
            theSaved = popped;
        }
    }

    // Push element onto stack
    private boolean virginStack = true;

    private void push(final Element e) throws SAXException {
        String name = e.name();
        String localName = e.localName();
        String namespace = e.namespace();
        String prefix = prefixOf(name);

        e.clean();
        if (!namespaces) {
            namespace = localName = "";
        }
        if (virginStack && localName.equalsIgnoreCase(theDoctypeName)) {
            try {
                theEntityResolver.resolveEntity(theDoctypePublicId, theDoctypeSystemId);
            } catch (IOException ew) {
            } // Can't be thrown for root I believe.
        }
        if (foreign(prefix, namespace)) {
            theContentHandler.startPrefixMapping(prefix, namespace);
        }
        Attributes atts = e.atts();
        int len = atts.getLength();
        for (int i = 0; i < len; i++) {
            String attNamespace = atts.getURI(i);
            String attPrefix = prefixOf(atts.getQName(i));
            if (foreign(attPrefix, attNamespace)) {
                theContentHandler.startPrefixMapping(attPrefix, attNamespace);
            }
        }
        theContentHandler.startElement(namespace, localName, name, e.atts());
        e.setNext(theStack);
        theStack = e;
        virginStack = false;
        if (cdataElements && (theStack.flags() & Schema.F_CDATA) != 0) {
            theScanner.startCDATA();
        }
    }

    /**
     * Get the prefix from a QName.
     *
     * @param name name
     * @return prefix
     */
    private String prefixOf(final String name) {
        int i = name.indexOf(':');
        String prefix = "";
        if (i != -1) {
            prefix = name.substring(0, i);
        }
        return prefix;
    }

    // Return true if we have a foreign name
    private boolean foreign(final String prefix, final String namespace) {
        boolean foreign = !(prefix.equals("") || namespace.equals("") || namespace.equals(theSchema.getURI()));
        return foreign;
    }

    /**
     * Parsing the complete XML Document Type Definition is way too complex, but for many simple cases we can extract something useful from it.
     *
     * doctypedecl ::= '&lt;!DOCTYPE' S Name (S ExternalID)? S? ('[' intSubset ']' S?)? '&gt;' DeclSep ::= PEReference | S intSubset ::= (markupdecl |
     * DeclSep)* markupdecl ::= elementdecl | AttlistDecl | EntityDecl | NotationDecl | PI | Comment ExternalID ::= 'SYSTEM' S SystemLiteral |
     * 'PUBLIC' S PubidLiteral S SystemLiteral
     */
    @Override
    public void decl(final char[] buff, final int offset, final int length) throws SAXException {
        String s = new String(buff, offset, length);
        String name = null;
        String systemid = null;
        String publicid = null;
        String[] v = split(s);
        if (v.length > 0 && "DOCTYPE".equalsIgnoreCase(v[0])) {
            if (theDoctypeIsPresent) {
                return; // one doctype only!
            }
            theDoctypeIsPresent = true;
            if (v.length > 1) {
                name = v[1];
                if (v.length > 3 && "SYSTEM".equals(v[2])) {
                    systemid = v[3];
                } else if (v.length > 3 && "PUBLIC".equals(v[2])) {
                    publicid = v[3];
                    if (v.length > 4) {
                        systemid = v[4];
                    } else {
                        systemid = "";
                    }
                }
            }
        }
        publicid = trimquotes(publicid);
        systemid = trimquotes(systemid);
        if (name != null) {
            publicid = cleanPublicid(publicid);
            theLexicalHandler.startDTD(name, publicid, systemid);
            theLexicalHandler.endDTD();
            theDoctypeName = name;
            theDoctypePublicId = publicid;
            if (theScanner instanceof Locator) { // Must resolve systemid
                theDoctypeSystemId = ((Locator) theScanner).getSystemId();
                try {
                    theDoctypeSystemId = new URL(new URL(theDoctypeSystemId), systemid).toString();
                } catch (Exception e) {
                }
            }
        }
    }

    // If the String is quoted, trim the quotes.
    private static String trimquotes(final String in) {
        String str = in;
        if (str == null) {
            return str;
        }
        int length = str.length();
        if (length == 0) {
            return str;
        }
        char s = str.charAt(0);
        char e = str.charAt(length - 1);
        // handle string with only one single or one double quote
        if (length == 1 && (s == '"' || s == '\'')) {
            return str;
        }
        if (s == e && (s == '\'' || s == '"')) {
            str = str.substring(1, str.length() - 1);
        }
        return str;
    }

    // Split the supplied String into words or phrases seperated by spaces.
    // Recognises quotes around a phrase and doesn't split it.
    private static String[] split(final String val) throws IllegalArgumentException {
        String value = val.trim();
        if (value.length() == 0) {
            return new String[0];
        } else {
            ArrayList l = new ArrayList();
            int s = 0;
            int e = 0;
            boolean sq = false; // single quote
            boolean dq = false; // double quote
            char lastc = 0;
            int len = value.length();
            for (e = 0; e < len; e++) {
                char c = value.charAt(e);
                if (!dq && c == '\'' && lastc != '\\') {
                    sq = !sq;
                    if (s < 0) {
                        s = e;
                    }
                } else if (!sq && c == '\"' && lastc != '\\') {
                    dq = !dq;
                    if (s < 0) {
                        s = e;
                    }
                } else if (!sq && !dq) {
                    if (Character.isWhitespace(c)) {
                        if (s >= 0) {
                            l.add(value.substring(s, e));
                        }
                        s = -1;
                    } else if (s < 0 && c != ' ') {
                        s = e;
                    }
                }
                lastc = c;
            }
            l.add(value.substring(s, e));
            return (String[]) l.toArray(new String[0]);
        }
    }

    // Replace junk in publicids with spaces
    private static String legal = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-'()+,./:=?;!*#@$_%";

    private String cleanPublicid(final String src) {
        if (src == null) {
            return null;
        }
        int len = src.length();
        StringBuffer dst = new StringBuffer(len);
        boolean suppressSpace = true;
        for (int i = 0; i < len; i++) {
            char ch = src.charAt(i);
            if (legal.indexOf(ch) != -1) { // legal but not whitespace
                dst.append(ch);
                suppressSpace = false;
            } else if (suppressSpace) { // normalizable whitespace or junk
                ;
            } else {
                dst.append(' ');
                suppressSpace = true;
            }
        }
        return dst.toString().trim(); // trim any final junk whitespace
    }

    @Override
    public void gi(final char[] buff, final int offset, final int length) throws SAXException {
        if (theNewElement != null) {
            return;
        }
        String name = makeName(buff, offset, length);
        ElementType type = theSchema.getElementType(name);
        if (type == null) {
            // Suppress unknown elements if ignore-bogons is on
            if (ignoreBogons) {
                return;
            }
            int bogonModel = bogonsEmpty ? Schema.M_EMPTY : Schema.M_ANY;
            int bogonMemberOf = rootBogons ? Schema.M_ANY : (Schema.M_ANY & ~Schema.M_ROOT);
            theSchema.elementType(name, bogonModel, bogonMemberOf, 0);
            if (!rootBogons) {
                theSchema.parent(name, theSchema.rootElementType().name());
            }
            type = theSchema.getElementType(name);
        }

        theNewElement = new Element(type, defaultAttributes);
    }

    @Override
    public void cdsect(final char[] buff, final int offset, final int length) throws SAXException {
        theLexicalHandler.startCDATA();
        pcdata(buff, offset, length);
        theLexicalHandler.endCDATA();
    }

    @Override
    public void pcdata(final char[] buff, final int offset, final int length) throws SAXException {
        if (length == 0) {
            return;
        }
        boolean allWhite = true;
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(buff[offset + i])) {
                allWhite = false;
            }
        }
        if (allWhite && !theStack.canContain(thePCDATA)) {
            if (ignorableWhitespace) {
                theContentHandler.ignorableWhitespace(buff, offset, length);
            }
        } else {
            rectify(thePCDATA);
            theContentHandler.characters(buff, offset, length);
        }
    }

    @Override
    public void pitarget(final char[] buff, final int offset, final int length) throws SAXException {
        if (theNewElement != null) {
            return;
        }
        thePITarget = makeName(buff, offset, length).replace(':', '_');
    }

    @Override
    public void pi(final char[] buff, final int offset, final int length) throws SAXException {
        int l = length;
        if (theNewElement != null || thePITarget == null) {
            return;
        }
        if ("xml".equalsIgnoreCase(thePITarget)) {
            return;
        }
        // if (length > 0 && buff[length - 1] == '?') System.err.println("%% Removing ? from PI");
        if (l > 0 && buff[l - 1] == '?') {
            l--; // remove trailing ?
        }
        theContentHandler.processingInstruction(thePITarget, new String(buff, offset, length));
        thePITarget = null;
    }

    @Override
    public void stagc(final char[] buff, final int offset, final int length) throws SAXException {
        if (theNewElement == null) {
            return;
        }
        rectify(theNewElement);
        if (theStack.model() == Schema.M_EMPTY) {
            // Force an immediate end tag
            etagBasic(buff, offset, length);
        }
    }

    @Override
    public void stage(final char[] buff, final int offset, final int length) throws SAXException {
        // System.err.println("%% Empty-tag");
        if (theNewElement == null) {
            return;
        }
        rectify(theNewElement);
        // Force an immediate end tag
        etagBasic(buff, offset, length);
    }

    @Override
    public void cmnt(final char[] buff, final int offset, final int length) throws SAXException {
        theLexicalHandler.comment(buff, offset, length);
    }

    // Rectify the stack, pushing and popping as needed
    // so that the argument can be safely pushed
    private void rectify(final Element element) throws SAXException {
        Element e = element;
        Element sp;
        while (true) {
            for (sp = theStack; sp != null; sp = sp.next()) {
                if (sp.canContain(e)) {
                    break;
                }
            }
            if (sp != null) {
                break;
            }
            ElementType parentType = e.parent();
            if (parentType == null) {
                break;
            }
            Element parent = new Element(parentType, defaultAttributes);
            parent.setNext(e);
            e = parent;
        }
        if (sp == null) {
            return; // don't know what to do
        }
        while (theStack != sp) {
            if (theStack == null || theStack.next() == null || theStack.next().next() == null) {
                break;
            }
            restartablyPop();
        }
        while (e != null) {
            Element nexte = e.next();
            if (!e.name().equals("<pcdata>")) {
                push(e);
            }
            e = nexte;
            restart(e);
        }
        theNewElement = null;
    }

    @Override
    public int getEntity() {
        return theEntity;
    }

    // Return the argument as a valid XML name
    // This no longer lowercases the result: we depend on Schema to
    // canonicalize case.
    private String makeName(final char[] buff, final int offset, final int length) {
        int off = offset;
        int l = length;
        final StringBuffer dst = new StringBuffer(l + 2);
        boolean seenColon = false;
        boolean start = true;
        // String src = new String(buff, offset, length); // DEBUG
        for (; l-- > 0; off++) {
            char ch = buff[off];
            if (Character.isLetter(ch) || ch == '_') {
                start = false;
                dst.append(ch);
            } else if (Character.isDigit(ch) || ch == '-' || ch == '.') {
                if (start) {
                    dst.append('_');
                }
                start = false;
                dst.append(ch);
            } else if (ch == ':' && !seenColon) {
                seenColon = true;
                if (start) {
                    dst.append('_');
                }
                start = true;
                dst.append(translateColons ? '_' : ch);
            }
        }
        int dstLength = dst.length();
        if (dstLength == 0 || dst.charAt(dstLength - 1) == ':') {
            dst.append('_');
        }
        return theParserContext.getReference(dst.toString());
    }

    // Default LexicalHandler implementation

    @Override
    public void comment(final char[] ch, final int start, final int length) throws SAXException {
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
    public void startDTD(final String name, final String publicid, final String systemid) throws SAXException {
    }

    @Override
    public void startEntity(final String name) throws SAXException {
    }
}
