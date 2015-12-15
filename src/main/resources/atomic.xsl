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
    <tr><td><strong>Select</strong></td><xsl:apply-templates select="a:content/m:properties" mode="table-header"/></tr>
  </xsl:template>

  <xsl:template match="d:*" mode="table-header">
    <td><strong><xsl:value-of select="local-name()"/></strong></td>
  </xsl:template>

  <xsl:template match="a:entry" mode="table-data">
    <tr><td><a href="{a:link/@href}">Link</a></td><xsl:apply-templates select="a:content/m:properties" mode="table-data"/></tr>
  </xsl:template>

  <xsl:template match="/a:feed/a:entry/a:content/m:properties/d:*[1]" mode="table-data">
    <td><xsl:apply-templates/></td>
  </xsl:template>

  <xsl:template match="/a:feed/a:entry/a:content/m:properties/d:*" mode="table-data">
    <td><xsl:apply-templates/></td>
  </xsl:template>

  <!-- Entry Detail -->

  <xsl:template match="entry">
    <li><xsl:value-of select="title"/></li>
    <li><ul><xsl:apply-templates select="content/m:properties"/></ul></li>
  </xsl:template>

  <xsl:template match="/a:entry">
    <html>
      <head>
      </head>
      <body>
	<form action="{link/@href}">
	  <ul>
            <xsl:apply-templates select="a:content/m:properties"
				 mode="entry-detail"/>
	    <xsl:apply-templates select="a:link"/>
	  </ul>
	  <input type="hidden" name="method" value="put"/>
	  <input type="submit" value="Submit"/>
	</form>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="a:link">
    <li><xsl:apply-templates select="m:inline/a:feed/a:entry"/></li>
  </xsl:template>

  <xsl:template match="a:entry">
    <ul>
      <xsl:apply-templates select="a:content/m:properties"
			   mode="entry-detail"/>
    </ul>
  </xsl:template>

  <xsl:template match="a:feed/a:entry/a:content/m:properties">
    <table>
    </table>
  </xsl:template>

  <xsl:template match="a:feed/a:entry/a:content/m:properties/d:*">
    <tr><td><xsl:value-of
    select="local-name()"/></td><td><xsl:value-of select="text()"/></td></tr>
  </xsl:template>

  <xsl:template match="d:*" mode="entry-detail">
    <li><xsl:value-of select="local-name()"/>:  <input type="text" name="{name()}" value="{text()}"/></li>
  </xsl:template>

</xsl:stylesheet> 
