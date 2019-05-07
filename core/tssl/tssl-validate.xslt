<!-- Generate complaints if the schema is invalid in some way.  -->

<!--
// ====================================================================
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//  ====================================================================
-->

<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:tssl="https://github.com/yahoo/tagchowder/tssl"
	version="1.0">

  <xsl:output method="text"/>

  <xsl:strip-space elements="*"/>

  <!-- Generates a report if an element does not belong to at least
       one of the groups that its parent element contains.  -->
  <xsl:template match="tssl:element/tssl:element">
    <xsl:if test="not(tssl:memberOfAny) and not(tssl:memberOf/@group = ../tssl:contains/@group)">
      <xsl:value-of select="@name"/>
      <xsl:text> is not in the content model of </xsl:text>
      <xsl:value-of select="../@name"/>
      <xsl:text>&#xA;</xsl:text>
    </xsl:if>
    <xsl:apply-templates/>
  </xsl:template>



</xsl:transform>
