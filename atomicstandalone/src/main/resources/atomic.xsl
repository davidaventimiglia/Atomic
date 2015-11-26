<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
		xmlns:a="http://www.w3.org/2005/Atom"
		xmlns:p="http://www.w3.org/2007/app"
                xmlns:m="http://schemas.microsoft.com/ado/2007/08/dataservices/metadata" 
                xmlns:d="http://schemas.microsoft.com/ado/2007/08/dataservices"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" indent="yes"/>

  <!-- Service Document  -->

  <xsl:template match="/p:service"> 
    <html>
      <head>
      </head>
      <body>
	<ul>
	  <xsl:apply-templates/>
	</ul>
      </body>
    </html>
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

  <!-- Feed -->

  <xsl:template match="/a:feed">
    <html>
      <head>
      </head>
      <body>
	<table border="1">
	  <caption><strong><xsl:value-of select="a:title"/></strong></caption>
	  <xsl:apply-templates select="a:entry[1]" mode="table-header"/>
	  <xsl:apply-templates select="a:entry" mode="table-data"/>
	</table>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="a:entry" mode="table-header">
    <tr>
      <td><strong>Select</strong></td>
      <xsl:apply-templates select="a:content/m:properties" mode="table-header"/>
    </tr>
  </xsl:template>

  <xsl:template match="d:*" mode="table-header">
    <td><strong><xsl:value-of select="local-name()"/></strong></td>
  </xsl:template>

  <xsl:template match="a:entry" mode="table-data">
    <tr>
      <td>
        <a href="{a:link/@href}">Link</a>
      </td>
      <xsl:apply-templates select="a:content/m:properties" mode="table-data"/>
    </tr>
  </xsl:template>

  <xsl:template match="/a:feed/a:entry/a:content/m:properties/d:*[1]" mode="table-data">
    <td><xsl:apply-templates/></td>
  </xsl:template>

  <xsl:template match="/a:feed/a:entry/a:content/m:properties/d:*" mode="table-data">
    <td><xsl:apply-templates/></td>
  </xsl:template>

  <!-- Entry Detail -->

  <xsl:template match="/a:entry">
    <html>
      <head>
      </head>
      <body>
	<form action="{link/@href}">
	  <table border="1">
            <xsl:apply-templates select="a:content/m:properties" mode="entry-detail"/>
	  </table>
	  <input type="hidden" name="method" value="put"/>
	  <input type="submit" value="Submit"/>
	</form>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="d:*" mode="entry-detail">
    <tr>
      <td>
        <xsl:value-of select="local-name()"/>
      </td>
      <td>
        <input type="text" name="{name()}" value="{text()}"/>
      </td>
    </tr>
  </xsl:template>

</xsl:stylesheet> 
