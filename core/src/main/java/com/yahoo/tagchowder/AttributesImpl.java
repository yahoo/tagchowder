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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

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
        vectorIndex = 0;
        length = 0;
        attributeEntriesVector = new Vector<>(5);
        qNameIndex = new HashMap<String, SortedSet<Integer>>(5);
        uriLocalNameIndex = new HashMap<String, SortedSet<Integer>>(5);
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
        vectorIndex = 0;
        length = 0;
        attributeEntriesVector = new Vector<>(5);
        qNameIndex = new HashMap<String, SortedSet<Integer>>(5);
        uriLocalNameIndex = new HashMap<String, SortedSet<Integer>>(5);
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
     * Get the entry for given index.
     * @param index index in the vector
     * @return attribute entry for given index
     */
    private AttributeEntries getEntry(final int index) {
        if (index >= 0 && index < vectorIndex) {
            return attributeEntriesVector.get(index);
        } else {
            return null;
        }
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
        AttributeEntries entry = getEntry(index);
        if (entry != null) {
            return entry.getUri();
        }
        return null;
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
        AttributeEntries entry = getEntry(index);
        if (entry != null) {
            return entry.getLocalName();
        }
        return null;
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
        AttributeEntries entry = getEntry(index);
        if (entry != null) {
            return entry.getQualifiedName();
        }
        return null;
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
        AttributeEntries entry = getEntry(index);
        if (entry != null) {
            return entry.getType();
        }
        return null;
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
        AttributeEntries entry = getEntry(index);
        if (entry != null) {
            return entry.getValue();
        }
        return null;
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
        String key = createKey(uri, localName);
        SortedSet<Integer> indexes = uriLocalNameIndex.get(key);
        if (indexes != null && indexes.size() > 0) {
            return indexes.first();
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
        SortedSet<Integer> indexes = qNameIndex.get(qName);
        if (indexes != null && indexes.size() > 0) {
            return indexes.first();
        }
        return -1;
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
        String key = createKey(uri, localName);
        SortedSet<Integer> indexes = uriLocalNameIndex.get(key);
        if (indexes != null && indexes.size() > 0) {
            return getType(indexes.first());
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
        SortedSet<Integer> indexes = qNameIndex.get(qName);
        if (indexes != null && indexes.size() > 0) {
            return getType(indexes.first());
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
        String key = createKey(uri, localName);
        SortedSet<Integer> indexes = uriLocalNameIndex.get(key);
        if (indexes != null && indexes.size() > 0) {
            return getValue(indexes.first());
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
        SortedSet<Integer> indexes = qNameIndex.get(qName);
        if (indexes != null && indexes.size() > 0) {
            return getValue(indexes.first());
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
        attributeEntriesVector.clear();
        qNameIndex.clear();
        uriLocalNameIndex.clear();
        vectorIndex = 0;
        length = 0;
    }

    /**
     * Update the qname index.
     * @param key the qname
     * @param vectorIndex index where this entry is inserted
     */
    private void updateQNameIndex(final String key, final int vectorIndex) {
        if (qNameIndex.containsKey(key)) {
            qNameIndex.get(key).add(vectorIndex);
        } else {
            // Create new index
            TreeSet<Integer> set = new TreeSet<>();
            set.add(vectorIndex);
            qNameIndex.put(key, set);
        }
    }

    /**
     * Update the uri, localname index.
     * @param key the key uri + localName
     * @param vectorIndex index where this entry is inserted
     */
    private void updateUriLocalNameIndex(final String key, final int vectorIndex) {
        if (uriLocalNameIndex.containsKey(key)) {
            uriLocalNameIndex.get(key).add(vectorIndex);
        } else {
            // Create new index
            TreeSet<Integer> set = new TreeSet<>();
            set.add(vectorIndex);
            uriLocalNameIndex.put(key, set);
        }
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
        Iterator<Integer> iterator = ((AttributesImpl) atts).getIndexes().iterator();
        while (iterator.hasNext()) {
            int i = iterator.next();
            attributeEntriesVector.add(i,
                    new AttributeEntries(atts.getURI(i), atts.getLocalName(i), atts.getQName(i), atts.getType(i), atts.getValue(i)));
            updateQNameIndex(atts.getQName(i), vectorIndex);
            String key = createKey(atts.getURI(i), atts.getLocalName(i));
            updateUriLocalNameIndex(key, vectorIndex);
            vectorIndex++;
            length++;
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
        attributeEntriesVector.add(new AttributeEntries(uri, localName, qName, type, value));
        updateQNameIndex(qName, vectorIndex);
        updateUriLocalNameIndex(createKey(uri, localName), vectorIndex);
        vectorIndex++;
        length++;
    }

    /**
     * Construct key from uri and localname.
     * @param uri the uri
     * @param localName the localname
     */
    private String createKey(final String uri, final String localName) {
        String nUri = uri != null ? uri : "";
        String nLocalName = localName != null ? localName : "";
        return nUri.concat(nLocalName);
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
        if (index >= 0 && index < vectorIndex) {
            AttributeEntries oldEntry = attributeEntriesVector.get(index);
            if (oldEntry != null) {
                if (qNameIndex.containsKey(oldEntry.getQualifiedName())) {
                    qNameIndex.get(oldEntry.getQualifiedName()).remove(index);
                }
                String oldKey = createKey(oldEntry.getUri(), oldEntry.getLocalName());
                if (uriLocalNameIndex.containsKey(oldKey)) {
                    uriLocalNameIndex.get(oldKey).remove(index);
                }
            } else {
                // Inserting in place where old entry was not present.
                length++;
            }

            attributeEntriesVector.set(index, new AttributeEntries(uri, localName, qName, type, value));
            updateQNameIndex(qName, index);
            updateUriLocalNameIndex(createKey(uri, localName), index);
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
        if (index >= 0 && index < vectorIndex) {
            AttributeEntries oldEntry = attributeEntriesVector.get(index);
            if (oldEntry != null) {
                if (qNameIndex.containsKey(oldEntry.getQualifiedName())) {
                    qNameIndex.get(oldEntry.getQualifiedName()).remove(index);
                }
                String oldKey = createKey(oldEntry.getUri(), oldEntry.getLocalName());
                if (uriLocalNameIndex.containsKey(oldKey)) {
                    uriLocalNameIndex.get(oldKey).remove(index);
                }
                attributeEntriesVector.set(index, null);
                length--;
            }
        } else {
            badIndex(index);
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
        AttributeEntries entry = getEntry(index);
        if (entry != null) {
            entry.setUri(uri);
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
        AttributeEntries entry = getEntry(index);
        if (entry != null) {
            entry.setLocalName(localName);
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
        AttributeEntries entry = getEntry(index);
        if (entry != null) {
            entry.setQualifiedName(qName);
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
        AttributeEntries entry = getEntry(index);
        if (entry != null) {
            entry.setType(type);
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
        AttributeEntries entry = getEntry(index);
        if (entry != null) {
            entry.setValue(value);
        } else {
            badIndex(index);
        }
    }

    ////////////////////////////////////////////////////////////////////
    // Internal methods.
    ////////////////////////////////////////////////////////////////////

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

    /**
     * Get the list of indexes to iterate through all entries in attribute class.
     * @return List of indexes for the attribute entries.
     */
    public List<Integer> getIndexes() {
        ArrayList<Integer> indexes = new ArrayList<>(vectorIndex);
        if (length > 0) {
            for (int i = 0; i < attributeEntriesVector.size(); i++) {
                if (attributeEntriesVector.elementAt(i) != null) {
                    indexes.add(i);
                }
            }
        }
        return indexes;
    }

    /**
     * Get the list of indexes in reverse order to iterate through all entries in attribute class.
     * @return List of indexes for the attribute entries in reverse order.
     */
    public List<Integer> getReverseIndexes() {
        ArrayList<Integer> indexes = new ArrayList<>(vectorIndex);
        if (length > 0) {
            for (int i = attributeEntriesVector.size() - 1; i >= 0; i--) {
                if (attributeEntriesVector.elementAt(i) != null) {
                    indexes.add(i);
                }
            }
        }
        return indexes;
    }

    ////////////////////////////////////////////////////////////////////
    // Internal state.
    ////////////////////////////////////////////////////////////////////

    /** Index of the last entry. */
    private int vectorIndex;

    /** No of entries in this attributes. */
    private int length;

    private Vector<AttributeEntries> attributeEntriesVector;
    private Map<String, SortedSet<Integer>> qNameIndex;
    private Map<String, SortedSet<Integer>> uriLocalNameIndex;

    /**
     * Class to store each entry in a attribute.
     */
    static class AttributeEntries {
        private String uri;
        private String localName;
        private String qualifiedName;
        private String type;
        private String value;

        AttributeEntries(final String uri, final String localName, final String qualifiedName, final String type, final String value) {
            this.uri = uri;
            this.localName = localName;
            this.qualifiedName = qualifiedName;
            this.type = type;
            this.value = value;
        }

        /**
         * Get the uri from an attribute entry.
         * @return the uri
         */
        public String getUri() {
            return uri;
        }

        public void setUri(final String uri) {
            this.uri = uri;
        }

        /**
         * Get the uri from an attribute entry.
         * @return the local name
         */
        public String getLocalName() {
            return localName;
        }

        public void setLocalName(final String localName) {
            this.localName = localName;
        }

        /**
         * Get the uri from an attribute entry.
         * @return the qualified name
         */
        public String getQualifiedName() {
            return qualifiedName;
        }

        public void setQualifiedName(final String qualifiedName) {
            this.qualifiedName = qualifiedName;
        }

        /**
         * Get the uri from an attribute entry.
         * @return the type
         */
        public String getType() {
            return type;
        }

        public void setType(final String type) {
            this.type = type;
        }

        /**
         * Get the value from an attribute entry.
         * @return the value
         */
        public String getValue() {
            return value;
        }

        public void setValue(final String value) {
            this.value = value;
        }
    }
}

// end of AttributesImpl.java
