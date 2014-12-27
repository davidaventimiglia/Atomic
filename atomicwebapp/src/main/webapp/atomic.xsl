<?xml version="1.0"?>
<xsl:stylesheet 
    version="1.0"
    xmlns:edmx="http://schemas.microsoft.com/ado/2007/06/edmx"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/">
    <?xml-stylesheet type="text/xsl" href="class.xsl"?>
    <xsl:copy-of select="*"/>
  </xsl:template>
</xsl:stylesheet>
