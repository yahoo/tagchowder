<!-- Generate Java code to be inserted into HTMLModels.java.  -->

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
	xmlns:tssl="https://github.com/lafaspot/tagchowder/tssl"
	version="1.0">

  <xsl:output method="text"/>

  <xsl:strip-space elements="*"/>

  <!-- The main template.  We are going to generate Java constant
       definitions for the groups in the file.  -->
  <xsl:template match="tssl:schema">
    <xsl:apply-templates select="tssl:group">
      <xsl:sort select="@id"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- Generate a declaration for a single group.  -->
  <xsl:template match="tssl:group" name="tssl:group">
    <xsl:param name="id" select="@id"/>
    <xsl:param name="number" select="position()"/>
    <xsl:text>&#x9;public static final int </xsl:text>
    <xsl:value-of select="$id"/>
    <xsl:text> = 1 &lt;&lt; </xsl:text>
    <xsl:value-of select="$number"/>
    <xsl:text>;&#xA;</xsl:text>
  </xsl:template>

</xsl:transform>
