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

// XMLWriter.java - serialize an XML document.
// Written by David Megginson, david@megginson.com
// and placed by him into the public domain.

package com.yahoo.tagchowder;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

/**
 * Default implementation of the Attributes interface.
 *
 * <blockquote> <em>This module, both source code and documentation, is in the Public Domain, and comes with <strong>NO WARRANTY</strong>.</em> See
 * <a href='http://www.saxproject.org'>http://www.saxproject.org</a> for further information. </blockquote>
 *
 * <p>
 * This class provides a default implementation of the SAX2 {@link org.xml.sax.Attributes Attributes} interface, with the addition of manipulators so
 * that the list can be modified or reused.
 * </p>
 *
 * <p>
 * There are two typical uses of this class:
 * </p>
 *
 * <ol>
 * <li>to take a persistent snapshot of an Attributes object in a {@link org.xml.sax.ContentHandler#startElement startElement} event; or</li>
 * <li>to construct or modify an Attributes object in a SAX2 driver or filter.</li>
 * </ol>
 *
 * <p>
 * This class replaces the now-deprecated SAX1 {@link org.xml.sax.helpers.AttributeListImpl AttributeListImpl} class; in addition to supporting the
 * updated Attributes interface rather than the deprecated {@link org.xml.sax.AttributeList AttributeList} interface, it also includes a much more
 * efficient implementation using a single array rather than a set of Vectors.
 * </p>
 *
 * @since SAX 2.0
 * @author David Megginson
 * @version 2.0.1 (sax2r2)
 */
public class AttributesImpl implements Attributes {

    ////////////////////////////////////////////////////////////////////
    // Constructors.
    ////////////////////////////////////////////////////////////////////

    /**
     * Construct a new, empty AttributesImpl object.
     */
    public AttributesImpl() {
        length = 0;
        data = null;
        qNameIndex = new HashMap<String, Integer>();
    }

    /**
     * Copy an existing Attributes object.
     *
     * <p>
     * This constructor is especially useful inside a {@link org.xml.sax.ContentHandler#startElement startElement} event.
     * </p>
     *
     * @param atts The existing Attributes object.
     */
    public AttributesImpl(final Attributes atts) {
        qNameIndex = new HashMap<String, Integer>();
        setAttributes(atts);
    }

    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.Attributes.
    ////////////////////////////////////////////////////////////////////

    /**
     * Return the number of attributes in the list.
     *
     * @return The number of attributes in the list.
     * @see org.xml.sax.Attributes#getLength
     */
    @Override
    public int getLength() {
        return length;
    }

    /**
     * Return an attribute's Namespace URI.
     *
     * @param index The attribute's index (zero-based).
     * @return The Namespace URI, the empty string if none is available, or null if the index is out of range.
     * @see org.xml.sax.Attributes#getURI
     */
    @Override
    public String getURI(final int index) {
        if (index >= 0 && index < length) {
            return data[index * 5];
        } else {
            return null;
        }
    }

    /**
     * Return an attribute's local name.
     *
     * @param index The attribute's index (zero-based).
     * @return The attribute's local name, the empty string if none is available, or null if the index if out of range.
     * @see org.xml.sax.Attributes#getLocalName
     */
    @Override
    public String getLocalName(final int index) {
        if (index >= 0 && index < length) {
            return data[index * 5 + 1];
        } else {
            return null;
        }
    }

    /**
     * Return an attribute's qualified (prefixed) name.
     *
     * @param index The attribute's index (zero-based).
     * @return The attribute's qualified name, the empty string if none is available, or null if the index is out of bounds.
     * @see org.xml.sax.Attributes#getQName
     */
    @Override
    public String getQName(final int index) {
        if (index >= 0 && index < length) {
            return data[index * 5 + 2];
        } else {
            return null;
        }
    }

    /**
     * Return an attribute's type by index.
     *
     * @param index The attribute's index (zero-based).
     * @return The attribute's type, "CDATA" if the type is unknown, or null if the index is out of bounds.
     * @see org.xml.sax.Attributes#getType(int)
     */
    @Override
    public String getType(final int index) {
        if (index >= 0 && index < length) {
            return data[index * 5 + 3];
        } else {
            return null;
        }
    }

    /**
     * Return an attribute's value by index.
     *
     * @param index The attribute's index (zero-based).
     * @return The attribute's value or null if the index is out of bounds.
     * @see org.xml.sax.Attributes#getValue(int)
     */
    @Override
    public String getValue(final int index) {
        if (index >= 0 && index < length) {
            return data[index * 5 + 4];
        } else {
            return null;
        }
    }

    /**
     * Look up an attribute's index by Namespace name.
     *
     * <p>
     * In many cases, it will be more efficient to look up the name once and use the index query methods rather than using the name query methods
     * repeatedly.
     * </p>
     *
     * @param uri The attribute's Namespace URI, or the empty string if none is available.
     * @param localName The attribute's local name.
     * @return The attribute's index, or -1 if none matches.
     * @see org.xml.sax.Attributes#getIndex(java.lang.String,java.lang.String)
     */
    @Override
    public int getIndex(final String uri, final String localName) {
        int max = length * 5;
        for (int i = 0; i < max; i += 5) {
            if (data[i].equals(uri) && data[i + 1].equals(localName)) {
                return i / 5;
            }
        }
        return -1;
    }

    /**
     * Look up an attribute's index by qualified (prefixed) name.
     *
     * @param qName The qualified name.
     * @return The attribute's index, or -1 if none matches.
     * @see org.xml.sax.Attributes#getIndex(java.lang.String)
     */
    @Override
    public int getIndex(final String qName) {
        Integer index = qNameIndex.get(qName);
        if (index == null) {
            return -1;
        }
        return index;
    }

    /**
     * Look up an attribute's type by Namespace-qualified name.
     *
     * @param uri The Namespace URI, or the empty string for a name with no explicit Namespace URI.
     * @param localName The local name.
     * @return The attribute's type, or null if there is no matching attribute.
     * @see org.xml.sax.Attributes#getType(java.lang.String,java.lang.String)
     */
    @Override
    public String getType(final String uri, final String localName) {
        int max = length * 5;
        for (int i = 0; i < max; i += 5) {
            if (data[i].equals(uri) && data[i + 1].equals(localName)) {
                return data[i + 3];
            }
        }
        return null;
    }

    /**
     * Look up an attribute's type by qualified (prefixed) name.
     *
     * @param qName The qualified name.
     * @return The attribute's type, or null if there is no matching attribute.
     * @see org.xml.sax.Attributes#getType(java.lang.String)
     */
    @Override
    public String getType(final String qName) {
        Integer index = qNameIndex.get(qName);
        if (index != null) {
            return data[index + 3];
        }
        return null;
    }

    /**
     * Look up an attribute's value by Namespace-qualified name.
     *
     * @param uri The Namespace URI, or the empty string for a name with no explicit Namespace URI.
     * @param localName The local name.
     * @return The attribute's value, or null if there is no matching attribute.
     * @see org.xml.sax.Attributes#getValue(java.lang.String,java.lang.String)
     */
    @Override
    public String getValue(final String uri, final String localName) {
        int max = length * 5;
        for (int i = 0; i < max; i += 5) {
            if (data[i].equals(uri) && data[i + 1].equals(localName)) {
                return data[i + 4];
            }
        }
        return null;
    }

    /**
     * Look up an attribute's value by qualified (prefixed) name.
     *
     * @param qName The qualified name.
     * @return The attribute's value, or null if there is no matching attribute.
     * @see org.xml.sax.Attributes#getValue(java.lang.String)
     */
    @Override
    public String getValue(final String qName) {
        Integer index = qNameIndex.get(qName);
        if (index != null) {
            return data[index + 4];
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////
    // Manipulators.
    ////////////////////////////////////////////////////////////////////

    /**
     * Clear the attribute list for reuse.
     *
     * <p>
     * Note that little memory is freed by this call: the current array is kept so it can be reused.
     * </p>
     */
    public void clear() {
        if (data != null) {
            for (int i = 0; i < (length * 5); i++) {
                data[i] = null;
            }
        }
        qNameIndex.clear();
        length = 0;
    }

    /**
     * Copy an entire Attributes object.
     *
     * <p>
     * It may be more efficient to reuse an existing object rather than constantly allocating new ones.
     * </p>
     *
     * @param atts The attributes to copy.
     */
    public void setAttributes(final Attributes atts) {
        clear();
        length = atts.getLength();
        if (length > 0) {
            data = new String[length * 5];
            for (int i = 0; i < length; i++) {
                data[i * 5] = atts.getURI(i);
                data[i * 5 + 1] = atts.getLocalName(i);
                data[i * 5 + 2] = atts.getQName(i);
                data[i * 5 + 3] = atts.getType(i);
                data[i * 5 + 4] = atts.getValue(i);
                qNameIndex.putIfAbsent(atts.getQName(i), i);
            }
        }
    }

    /**
     * Add an attribute to the end of the list.
     *
     * <p>
     * For the sake of speed, this method does no checking to see if the attribute is already in the list: that is the responsibility of the
     * application.
     * </p>
     *
     * @param uri The Namespace URI, or the empty string if none is available or Namespace processing is not being performed.
     * @param localName The local name, or the empty string if Namespace processing is not being performed.
     * @param qName The qualified (prefixed) name, or the empty string if qualified names are not available.
     * @param type The attribute type as a string.
     * @param value The attribute value.
     */
    public void addAttribute(final String uri, final String localName, final String qName, final String type, final String value) {
        ensureCapacity(length + 1);
        data[length * 5] = uri;
        data[length * 5 + 1] = localName;
        data[length * 5 + 2] = qName;
        data[length * 5 + 3] = type;
        data[length * 5 + 4] = value;
        qNameIndex.putIfAbsent(qName, length);
        length++;
    }

    /**
     * Set an attribute in the list.
     *
     * <p>
     * For the sake of speed, this method does no checking for name conflicts or well-formedness: such checks are the responsibility of the
     * application.
     * </p>
     *
     * @param index The index of the attribute (zero-based).
     * @param uri The Namespace URI, or the empty string if none is available or Namespace processing is not being performed.
     * @param localName The local name, or the empty string if Namespace processing is not being performed.
     * @param qName The qualified name, or the empty string if qualified names are not available.
     * @param type The attribute type as a string.
     * @param value The attribute value.
     * @exception java.lang.ArrayIndexOutOfBoundsException When the supplied index does not point to an attribute in the list.
     */
    public void setAttribute(final int index, final String uri, final String localName, final String qName, final String type, final String value) {
        if (index >= 0 && index < length) {
            String qNameTemp = data[index * 5 + 2];
            qNameIndex.remove(qNameTemp);
            data[index * 5] = uri;
            data[index * 5 + 1] = localName;
            data[index * 5 + 2] = qName;
            data[index * 5 + 3] = type;
            data[index * 5 + 4] = value;
            qNameIndex.put(qName, index);
        } else {
            badIndex(index);
        }
    }

    /**
     * Remove an attribute from the list.
     *
     * @param index The index of the attribute (zero-based).
     * @exception java.lang.ArrayIndexOutOfBoundsException When the supplied index does not point to an attribute in the list.
     */
    public void removeAttribute(final int index) {
        int i = index;
        if (i >= 0 && i < length) {
            String qNameTemp = data[index * 5 + 2];
            qNameIndex.remove(qNameTemp);
            if (i < length - 1) {
                System.arraycopy(data, (i + 1) * 5, data, i * 5, (length - i - 1) * 5);
            }
            i = (length - 1) * 5;
            data[i++] = null;
            data[i++] = null;
            data[i++] = null;
            data[i++] = null;
            data[i] = null;
            // Update the index in the lookup table starting from the index to the length on new array.
            for (int j = index; j < length - 1; j++) {
                qNameTemp = data[j * 5 + 2];
                qNameIndex.put(qNameTemp, j);
            }
            length--;
        } else {
            badIndex(i);
        }
    }

    /**
     * Set the Namespace URI of a specific attribute.
     *
     * @param index The index of the attribute (zero-based).
     * @param uri The attribute's Namespace URI, or the empty string for none.
     * @exception java.lang.ArrayIndexOutOfBoundsException When the supplied index does not point to an attribute in the list.
     */
    public void setURI(final int index, final String uri) {
        if (index >= 0 && index < length) {
            data[index * 5] = uri;
        } else {
            badIndex(index);
        }
    }

    /**
     * Set the local name of a specific attribute.
     *
     * @param index The index of the attribute (zero-based).
     * @param localName The attribute's local name, or the empty string for none.
     * @exception java.lang.ArrayIndexOutOfBoundsException When the supplied index does not point to an attribute in the list.
     */
    public void setLocalName(final int index, final String localName) {
        if (index >= 0 && index < length) {
            data[index * 5 + 1] = localName;
        } else {
            badIndex(index);
        }
    }

    /**
     * Set the qualified name of a specific attribute.
     *
     * @param index The index of the attribute (zero-based).
     * @param qName The attribute's qualified name, or the empty string for none.
     * @exception java.lang.ArrayIndexOutOfBoundsException When the supplied index does not point to an attribute in the list.
     */
    public void setQName(final int index, final String qName) {
        if (index >= 0 && index < length) {
            data[index * 5 + 2] = qName;
        } else {
            badIndex(index);
        }
    }

    /**
     * Set the type of a specific attribute.
     *
     * @param index The index of the attribute (zero-based).
     * @param type The attribute's type.
     * @exception java.lang.ArrayIndexOutOfBoundsException When the supplied index does not point to an attribute in the list.
     */
    public void setType(final int index, final String type) {
        if (index >= 0 && index < length) {
            data[index * 5 + 3] = type;
        } else {
            badIndex(index);
        }
    }

    /**
     * Set the value of a specific attribute.
     *
     * @param index The index of the attribute (zero-based).
     * @param value The attribute's value.
     * @exception java.lang.ArrayIndexOutOfBoundsException When the supplied index does not point to an attribute in the list.
     */
    public void setValue(final int index, final String value) {
        if (index >= 0 && index < length) {
            data[index * 5 + 4] = value;
        } else {
            badIndex(index);
        }
    }

    ////////////////////////////////////////////////////////////////////
    // Internal methods.
    ////////////////////////////////////////////////////////////////////

    /**
     * Ensure the internal array's capacity.
     *
     * @param n The minimum number of attributes that the array must be able to hold.
     */
    private void ensureCapacity(final int n) {
        if (n <= 0) {
            return;
        }
        int max;
        if (data == null || data.length == 0) {
            max = 25;
        } else if (data.length >= n * 5) {
            return;
        } else {
            max = data.length;
        }
        while (max < n * 5) {
            max *= 2;
        }

        String[] newData = new String[max];
        if (length > 0) {
            System.arraycopy(data, 0, newData, 0, length * 5);
        }
        data = newData;
    }

    /**
     * Report a bad array index in a manipulator.
     *
     * @param index The index to report.
     * @exception java.lang.ArrayIndexOutOfBoundsException Always.
     */
    private void badIndex(final int index) throws ArrayIndexOutOfBoundsException {
        String msg = "Attempt to modify attribute at illegal index: " + index;
        throw new ArrayIndexOutOfBoundsException(msg);
    }

    ////////////////////////////////////////////////////////////////////
    // Internal state.
    ////////////////////////////////////////////////////////////////////

    private int length;
    private String[] data;
    private Map<String, Integer> qNameIndex;
}

// end of AttributesImpl.java
