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

package com.yahoo.tagchowder;

/**
 * This class represents an element type in the schema. An element type has a name, a content model vector, a member-of vector, a flags vector,
 * default attributes, and a schema to which it belongs.
 *
 * @see Schema
 */

public class ElementType {

    private String theName; // element type name (Qname)
    private String theNamespace; // element type namespace name
    private String theLocalName; // element type local name
    private int theModel; // bitmap: what the element contains
    private int theMemberOf; // bitmap: what element is contained in
    private int theFlags; // bitmap: element flags
    private AttributesImpl theAtts; // default attributes
    private ElementType theParent; // parent of this element type
    private Schema theSchema; // schema to which this belongs

    /**
     * Construct an ElementType: but it's better to use Schema.element() instead. The content model, member-of, and flags vectors are specified as
     * ints.
     *  @param name The element type name
     * @param model ORed-together bits representing the content models allowed in the content of this element type
     * @param memberOf ORed-together bits representing the content models to which this element type belongs
     * @param flags ORed-together bits representing the flags associated with this element type
     * @param schema The schema with which this element type will be associated
     * @param useIntern whether to use string intern.
     */

    public ElementType(final String name, final int model, final int memberOf, final int flags, final Schema schema, final boolean useIntern) {
        theName = name;
        theModel = model;
        theMemberOf = memberOf;
        theFlags = flags;
        theAtts = new AttributesImpl();
        theSchema = schema;
        theNamespace = namespace(name, false, true);
        theLocalName = localName(name, true);
    }

    /**
     * Return a namespace name from a Qname. The attribute flag tells us whether to return an empty namespace name if there is no prefix, or use the
     * schema default instead.
     *
     * @param name The Qname
     * @param attribute True if name is an attribute name
     * @param useIntern whether to use string intern
     * @return The namespace name
     **/
    public String namespace(final String name, final boolean attribute, final boolean useIntern) {
        int colon = name.indexOf(':');
        if (colon == -1) {
            return attribute ? "" : theSchema.getURI();
        }
        String prefix = name.substring(0, colon);
        if (prefix.equals("xml")) {
            return "http://www.w3.org/XML/1998/namespace";
        } else {

            return getReference(useIntern, "urn:x-prefix:" + prefix);
        }
    }

    /**
     * Return a local name from a Qname.
     *
     * @param name The Qname
     * @param useIntern whether to use string intern
     * @return The local name
     **/
    public String localName(final String name, final boolean useIntern) {
        int colon = name.indexOf(':');
        if (colon == -1) {
            return name;
        } else {
            return getReference(useIntern, name.substring(colon + 1));
        }
    }

    /**
     * Returns the name of this element type.
     *
     * @return The name of the element type
     */

    public String name() {
        return theName;
    }

    /**
     * Returns the namespace name of this element type.
     *
     * @return The namespace name of the element type
     */

    public String namespace() {
        return theNamespace;
    }

    /**
     * Returns the local name of this element type.
     *
     * @return The local name of the element type
     */

    public String localName() {
        return theLocalName;
    }

    /**
     * Returns the content models of this element type.
     *
     * @return The content models of this element type as a vector of bits
     */

    public int model() {
        return theModel;
    }

    /**
     * Returns the content models to which this element type belongs.
     *
     * @return The content models to which this element type belongs as a vector of bits
     */

    public int memberOf() {
        return theMemberOf;
    }

    /**
     * Returns the flags associated with this element type.
     *
     * @return The flags associated with this element type as a vector of bits
     */

    public int flags() {
        return theFlags;
    }

    /**
     * Returns the default attributes associated with this element type. Attributes of type CDATA that don't have default values are typically not
     * included. Other attributes without default values have an internal value of <tt>null</tt>. The return value is an AttributesImpl to allow the
     * caller to mutate the attributes.
     *
     * @return attributes impl
     */

    public AttributesImpl atts() {
        return theAtts;
    }

    /**
     * Returns the parent element type of this element type.
     *
     * @return The parent element type
     */

    public ElementType parent() {
        return theParent;
    }

    /**
     * Returns the schema which this element type is associated with.
     *
     * @return The schema
     */

    public Schema schema() {
        return theSchema;
    }

    /**
     * Returns true if this element type can contain another element type. That is, if any of the models in this element's model vector match any of
     * the models in the other element type's member-of vector.
     *
     * @param other The other element type
     * @return boolean
     */

    public boolean canContain(final ElementType other) {
        return (theModel & other.theMemberOf) != 0;
    }

    /**
     * Method to get reference with or without interning.
     * @param useIntern whether to use string intern or not.
     * @param input the input string.
     * @return reference to the string.
     */
    public String getReference(final boolean useIntern, final String input) {
        if (useIntern) {
            return input.intern();
        } else {
            return input;  // TODO: will put the hashmap here.
        }
    }

    /**
     * Sets an attribute and its value into an AttributesImpl object. Attempts to set a namespace declaration are ignored.
     *  @param atts The AttributesImpl object
     * @param name The name (Qname) of the attribute
     * @param type The type of the attribute
     * @param value The value of the attribute
     * @param useIntern whether to use string intern or not
     */

    public void setAttribute(final AttributesImpl atts, final String name, final String type, final String value, final boolean useIntern) {
//        public void setAttribute(final AttributesImpl atts, final String name, final String type, final String value, final boolean useIntern) {
        String n = name;
        String t = type;
        String v = value;
        if (n.equals("xmlns") || n.startsWith("xmlns:")) {
            return;
        }

        String namespace = namespace(n, true, useIntern);
        String localName = localName(n, useIntern);
        int i = atts.getIndex(n);
        if (i == -1) {
            n = getReference(useIntern, n);
            if (t == null) {
                t = "CDATA";
            }
            if (!t.equals("CDATA")) {
                v = normalize(v);
            }
            atts.addAttribute(namespace, localName, n, t, v);
        } else {
            if (t == null) {
                t = atts.getType(i);
            }
            if (!t.equals("CDATA")) {
                v = normalize(v);
            }
            atts.setAttribute(i, namespace, localName, n, t, v);
        }
    }

    /**
     * Normalize an attribute value (ID-style). CDATA-style attribute normalization is already done.
     *
     * @param value The value to normalize
     * @return The normalized value
     **/
    public static String normalize(final String value) {
        String vl = value;
        if (vl == null) {
            return vl;
        }
        vl = vl.trim();
        if (vl.indexOf("  ") == -1) {
            return vl;
        }
        boolean space = false;
        int len = vl.length();
        StringBuffer b = new StringBuffer(len);
        for (int i = 0; i < len; i++) {
            char v = vl.charAt(i);
            if (v == ' ') {
                if (!space) {
                    b.append(v);
                }
                space = true;
            } else {
                b.append(v);
                space = false;
            }
        }
        return b.toString();
    }

    /**
     * Sets an attribute and its value into this element type.
     *  @param name The name of the attribute
     * @param type The type of the attribute
     * @param value The value of the attribute
     * @param useIntern whether to use string intern or not
     */

    public void setAttribute(final String name, final String type, final String value, final boolean useIntern) {
        setAttribute(theAtts, name, type, value, useIntern);
    }

    /**
     * Sets the models of this element type.
     *
     * @param model The content models of this element type as a vector of bits
     */

    public void setModel(final int model) {
        theModel = model;
    }

    /**
     * Sets the content models to which this element type belongs.
     *
     * @param memberOf The content models to which this element type belongs as a vector of bits
     */

    public void setMemberOf(final int memberOf) {
        theMemberOf = memberOf;
    }

    /**
     * Sets the flags of this element type.
     *
     * @param flags associated with this element type The flags as a vector of bits
     */

    public void setFlags(final int flags) {
        theFlags = flags;
    }

    /**
     * Sets the parent element type of this element type.
     *
     * @param parent The parent element type
     */

    public void setParent(final ElementType parent) {
        theParent = parent;
    }

}
