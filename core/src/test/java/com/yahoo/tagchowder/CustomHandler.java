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
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple CustomHandler class to scan HTML doc and return the list of parsed Html tags.
 *
 * @author nhant01
 */
public class CustomHandler extends DefaultHandler {
    @Override
    public void startElement(final String uri, final String localName,
                             final String qName, final Attributes attributes) throws SAXException {
        final ParsedHtmlTag parsedHtmlTag = new ParsedHtmlTag(localName, attributes);
        parsedHtmlTagSet.add(parsedHtmlTag);
    }

    /**
     * Returns the list of parsed Html tags.
     * @return the list parsed Html tags
     */
    public List<ParsedHtmlTag> getParsedHtmlTags() {
        return parsedHtmlTagSet;
    }

    /** Set of parsed Html tags */
    private List<ParsedHtmlTag> parsedHtmlTagSet = new ArrayList<>();
}
