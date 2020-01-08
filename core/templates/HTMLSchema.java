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
/**
This class provides a Schema that has been preinitialized with HTML
elements, attributes, and character entity declarations.  All the declarations
normally provided with HTML 4.01 are given, plus some that are IE-specific
and NS4-specific.  Attribute declarations of type CDATA with no default
value are not included.
*/

package com.yahoo.tagchowder.templates;

import com.yahoo.tagchowder.Schema;
import com.yahoo.tagchowder.Parser;

public class HTMLSchema extends Schema implements HTMLModels {

	/**
	Returns a newly constructed HTMLSchema object independent of
	any existing ones.
	 @param parser the parser object
	*/
	public HTMLSchema(final Parser parser) {
		super(parser);
		// Start of Schema calls
		@@SCHEMA_CALLS@@
		// End of Schema calls
	}
}
