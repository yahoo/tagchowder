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

import org.xml.sax.Attributes;

import javax.annotation.Nonnull;

/**
 * The Html ParsedHtmlTag class.
 *
 * @author nhant01
 */
public class ParsedHtmlTag {
    /**
     * Constructor.
     *
     * @param tagName the name of the underlying tag in html document.
     * @param attributes the attributes attached to the element.  If
     *  there are no attributes, it shall be an empty Attributes object.
     */
    public ParsedHtmlTag(@Nonnull final String tagName, @Nonnull final Attributes attributes) {
        this.tagName = tagName.toUpperCase();
        this.attrs = attributes;
    }

    /**
     * Lower-case tag name.
     * @return returns a lower case tag name.
     */
    public String lowerName() {
        return this.tagName.toLowerCase();
    }

    /**
     * Determine if an attribute name exists. Return true if found.
     * @param attrName attribute name
     * @return true if found. Otherwise false.
     */
    public boolean hasAttribute(final String attrName) {
        for (int i = 0; i < attrs().getLength(); i++) {
            if (attrs.getQName(i).equalsIgnoreCase(attrName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns an array of attributes. Each attribute has two fields: name and
     * value. Name is always lower-case, value is the case from the original
     * document. Values are unescaped.
     * @return returns the attributes.
     */
    public Attributes attrs() {
        return this.attrs;
    }

    /** The parsed tag name. */
    @Nonnull
    private String tagName;

    /** The attributes. */
    @Nonnull
    private final Attributes attrs;
}
