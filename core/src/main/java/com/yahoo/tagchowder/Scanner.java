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

// Scanner

package com.yahoo.tagchowder;

import java.io.IOException;
import java.io.Reader;

import org.xml.sax.SAXException;

/**
 * An interface allowing Parser to invoke scanners.
 **/

public interface Scanner {

    /**
     * Invoke a scanner.
     *
     * @param r A source of characters to scan
     * @param h A ScanHandler to report events to
     * @throws IOException IOException
     * @throws SAXException SAXException
     **/

    void scan(final Reader r, final ScanHandler h) throws IOException, SAXException;

    /**
     * Reset the embedded locator.
     *
     * @param publicid The publicid of the source
     * @param systemid The systemid of the source
     **/

    void resetDocumentLocator(final String publicid, final String systemid);

    /**
     * Signal to the scanner to start CDATA content mode.
     **/

    void startCDATA();

}
