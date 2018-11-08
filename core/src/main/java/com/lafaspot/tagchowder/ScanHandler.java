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
// Scanner handler

package com.lafaspot.tagchowder;

import org.xml.sax.SAXException;

/**
 * An interface that Scanners use to report events in the input stream.
 **/

public interface ScanHandler {

    /**
     * Reports an attribute name without a value.
     *
     * @param buff buffer
     * @param offset offset
     * @param length length
     * @throws SAXException SAXException
     */
    void adup(char[] buff, int offset, int length) throws SAXException;

    /**
     * Reports an attribute name; a value will follow.
     *
     * @param buff buffer
     * @param offset offset
     * @param length length
     * @throws SAXException SAXException
     */
    void aname(char[] buff, int offset, int length) throws SAXException;

    /**
     * Reports an attribute value.
     *
     * @param buff buffer
     * @param offset offset
     * @param length length
     * @throws SAXException SAXException
     */
    void aval(char[] buff, int offset, int length) throws SAXException;

    /**
     * Reports the content of a CDATA section (not a CDATA element).
     *
     * @param buff buffer
     * @param offset offset
     * @param length length
     * @throws SAXException SAXException
     */
    void cdsect(char[] buff, int offset, int length) throws SAXException;

    /**
     * Reports a <!....> declaration - typically a DOCTYPE.
     *
     * @param buff buffer
     * @param offset offset
     * @param length length
     * @throws SAXException SAXException
     */
    void decl(char[] buff, int offset, int length) throws SAXException;

    /**
     * Reports an entity reference or character reference.
     *
     * @param buff buffer
     * @param offset offset
     * @param length length
     * @throws SAXException SAXException
     */
    void entity(char[] buff, int offset, int length) throws SAXException;

    /**
     * Reports EOF.
     *
     * @param buff buffer
     * @param offset offset
     * @param length length
     * @throws SAXException SAXException
     */
    void eof(char[] buff, int offset, int length) throws SAXException;

    /**
     * Reports an end-tag.
     *
     * @param buff buffer
     * @param offset offset
     * @param length length
     * @throws SAXException SAXException
     */
    void etag(char[] buff, int offset, int length) throws SAXException;

    /**
     * Reports the general identifier (element type name) of a start-tag.
     *
     * @param buff buffer
     * @param offset offset
     * @param length length
     * @throws SAXException SAXException
     */
    void gi(char[] buff, int offset, int length) throws SAXException;

    /**
     * Reports character content.
     *
     * @param buff buffer
     * @param offset offset
     * @param length length
     * @throws SAXException SAXException
     */
    void pcdata(char[] buff, int offset, int length) throws SAXException;

    /**
     * Reports the data part of a processing instruction.
     *
     * @param buff buffer
     * @param offset offset
     * @param length length
     * @throws SAXException SAXException
     */
    void pi(char[] buff, int offset, int length) throws SAXException;

    /**
     * Reports the target part of a processing instruction.
     *
     * @param buff buffer
     * @param offset offset
     * @param length length
     * @throws SAXException SAXException
     */
    void pitarget(char[] buff, int offset, int length) throws SAXException;

    /**
     * Reports the close of a start-tag.
     *
     * @param buff buffer
     * @param offset offset
     * @param length length
     * @throws SAXException SAXException
     */
    void stagc(char[] buff, int offset, int length) throws SAXException;

    /**
     * Reports the close of an empty-tag.
     *
     * @param buff buffer
     * @param offset offset
     * @param length length
     * @throws SAXException SAXException
     */
    void stage(char[] buff, int offset, int length) throws SAXException;

    /**
     * Reports a comment.
     *
     * @param buff buffer
     * @param offset offset
     * @param length length
     * @throws SAXException SAXException
     */
    void cmnt(char[] buff, int offset, int length) throws SAXException;

    /**
     * Returns the value of the last entity or character reference reported.
     *
     * @return entity
     */
    int getEntity();
}
