<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
		xmlns:a="http://www.w3.org/2005/Atom"
		xmlns:p="http://www.w3.org/2007/app"
                xmlns:m="http://schemas.microsoft.com/ado/2007/08/dataservices/metadata" 
                xmlns:d="http://schemas.microsoft.com/ado/2007/08/dataservices" 
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" indent="yes"/>

  <xsl:template match="/">
    <html>
      <head>
      </head>
      <body>
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="p:service">
    <ul>
      <xsl:apply-templates/>
    </ul>
  </xsl:template>

  <xsl:template match="p:workspace">
    <li>
      <xsl:value-of select="a:title"/>
      <ul>
        <xsl:apply-templates/>
      </ul>
    </li>
  </xsl:template>

  <xsl:template match="a:title">
  </xsl:template>

  <xsl:template match="p:collection">
    <li>
      <a href="{@href}">
	<xsl:value-of select="a:title"/>
      </a>
    </li>
  </xsl:template>

  <xsl:template match="a:feed">
    <table border="1">
      <xsl:apply-templates select="a:entry"/>
    </table>
  </xsl:template>

  <xsl:template match="a:entry">
    <table border="1">
      <xsl:apply-templates select="a:content/m:properties"/>
    </table>
  </xsl:template>

  <xsl:template match="a:feed/a:entry">
    <tr>
      <td><xsl:value-of select="a:id"/></td>
      <xsl:apply-templates select="a:content/m:properties"/>
    </tr>
  </xsl:template>

  <xsl:template match="d:*">
    <tr><td><xsl:apply-templates/></td></tr>
  </xsl:template>

  <xsl:template match="a:feed/a:entry/a:content/m:properties/d:*">
    <td><xsl:apply-templates/></td>
  </xsl:template>

</xsl:stylesheet> 
