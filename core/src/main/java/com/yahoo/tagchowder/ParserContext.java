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

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to store the current context/state of the Parser object.
 * @author manishsingh
 */
final class ParserContext {
     /**
     * Flag to decide whether to do string intern or not.
     */
     private boolean useIntern = true;
     /**
     * Hash Map to be used when not using string intern.
     */
     private Map<String, Boolean> stringPoolMap  = new HashMap<>();

      /**
      * Constructor.
      * @param builder the builder object
      */
      private ParserContext(final Builder builder) {
        this.useIntern = builder.useIntern;
     }

    /**
     * Set the use intern flag.
     * @param useIntern whether to use string intern or not
     */
     void setUseIntern(final boolean useIntern) {
          this.useIntern = useIntern;
     }

    /**
     * Method to get reference with or without interning.
     * @param input the input string.
     * @return reference to the string.
     */
     String getReference(final String input) {
        if (useIntern) {
            return input.intern();
        } else {
            return input;  // TODO: will put the hashmap here.
        }
     }

     /**
     * Builder for various parameters to create instance of the parser context.
     * @author manishsingh
     *
     */
    static class Builder {
        /**
         * Flag to decide whether to do string intern or not.
         */
        private boolean useIntern;

        /**
         * Constructor for the builder.
         * @param useIntern whether to use intern or not.
         */
        Builder(@Nonnull final boolean useIntern)  {
            this.useIntern  = useIntern;
        }
        /**
         * Build the ParserContext object.
         * @return parser context object
         */
        ParserContext build() {
            return new ParserContext(this);
        }
    }

    /**
     * Clear the state.
     */
    void clear() {
        stringPoolMap.clear();
        stringPoolMap = null;
    }
}
