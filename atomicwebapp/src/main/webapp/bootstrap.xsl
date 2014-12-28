<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/">
    <xsl:processing-instruction name="xml-stylesheet">
      <xsl:text>type="text/xsl" href="http://localhost:8080/atomic-webapp/atomic.xsl"</xsl:text>
    </xsl:processing-instruction>
    <xsl:copy-of select="*"/>
  </xsl:template>
</xsl:stylesheet>
