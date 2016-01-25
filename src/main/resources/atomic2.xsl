<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
		xmlns=""
		xmlns:atom="http://www.w3.org/2005/Atom"
		xmlns:jdbc="http://java.sun.com/xml/ns/jdbc"
		xmlns:m="http://schemas.microsoft.com/ado/2007/08/dataservices/metadata"
		xmlns:d="http://schemas.microsoft.com/ado/2007/08/dataservices"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes"/>

  <xsl:template match="/properties">
    <m:properties>
      <xsl:apply-templates select="entry"/>
    </m:properties>
  </xsl:template>

  <xsl:template match="entry">
    <xsl:element
	name="d:{@key}">
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="/jdbc:webRowSet">
    <xsl:apply-templates select="jdbc:data"/>
  </xsl:template>

  <xsl:template match="jdbc:data">
    <m:content>
      <xsl:apply-templates/>
    </m:content>
  </xsl:template>

  <xsl:template match="jdbc:currentRow">
    <m:properties>
      <xsl:apply-templates select="jdbc:columnValue"/>
    </m:properties>
  </xsl:template>

  <xsl:template match="jdbc:columnValue">
    <xsl:variable name="index" select="position()"/>
    <xsl:element
	name="d:{../../../jdbc:metadata/jdbc:column-definition[$index]/jdbc:column-name}">
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
