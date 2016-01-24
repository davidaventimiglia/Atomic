<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
		xmlns=""
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
    <xsl:element name="d:{@key}">
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
