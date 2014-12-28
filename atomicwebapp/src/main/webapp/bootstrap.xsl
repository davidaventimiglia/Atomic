<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/">
    <xsl:processing-instruction name="xsl-stylesheet">
      <xsl:text>type="text/xsl" href="atomic.xsl"</xsl:text>
    </xsl:processing-instruction>
    <xsl:copy-of select="*"/>
  </xsl:template>
</xsl:stylesheet>
