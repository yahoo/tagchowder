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

// Interface to objects that translate InputStreams to Readers by auto-detection

package com.lafaspot.tagchowder;

import java.io.InputStream;
import java.io.Reader;

/**
 * Classes which accept an InputStream and provide a Reader which figures out the encoding of the InputStream and reads characters from it should
 * conform to this interface.
 *
 * @see java.io.InputStream
 * @see java.io.Reader
 */

public interface AutoDetector {

    /**
     * Given an InputStream, return a suitable Reader that understands the presumed character encoding of that InputStream. If bytes are consumed from
     * the InputStream in the process, they <i>must</i> be pushed back onto the InputStream so that they can be reinterpreted as characters.
     *
     * @param i The InputStream
     * @return A Reader that reads from the InputStream
     */

    Reader autoDetectingReader(final InputStream i);

}
