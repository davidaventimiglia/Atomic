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

  <!-- <xsl:template match="/a:feed"> -->
  <!--   <html> -->
  <!--     <head> -->
  <!--     </head> -->
  <!--     <body> -->
  <!-- 	<table> -->
  <!-- 	  <caption><strong><xsl:value-of select="a:title"/></strong></caption> -->
  <!-- 	  <xsl:apply-templates select="a:entry[1]" mode="table-header"/> -->
  <!-- 	  <xsl:apply-templates select="a:entry" mode="table-data"/> -->
  <!-- 	</table> -->
  <!--     </body> -->
  <!--   </html> -->
  <!-- </xsl:template> -->

  <!-- <xsl:template match="a:entry" mode="table-header"> -->
  <!--   <tr><td><strong>Select</strong></td><xsl:apply-templates select="a:content/m:properties" mode="table-header"/></tr> -->
  <!-- </xsl:template> -->

  <!-- <xsl:template match="d:*" mode="table-header"> -->
  <!--   <td><strong><xsl:value-of select="translate(local-name(), '_', ' ')"/></strong></td> -->
  <!-- </xsl:template> -->

  <!-- <xsl:template match="a:entry" mode="table-data"> -->
  <!--   <tr><td><a href="{a:link/@href}">Link</a></td><xsl:apply-templates select="a:content/m:properties" mode="table-data"/></tr> -->
  <!-- </xsl:template> -->

  <!-- <xsl:template match="/a:feed/a:entry/a:content/m:properties/d:*[1]" mode="table-data"> -->
  <!--   <td><xsl:apply-templates/></td> -->
  <!-- </xsl:template> -->

  <!-- <xsl:template match="/a:feed/a:entry/a:content/m:properties/d:*" mode="table-data"> -->
  <!--   <td><xsl:apply-templates/></td> -->
  <!-- </xsl:template> -->

  <!-- Entry Detail -->


  <xsl:template match="/">
    <html>
      <head>
	<style>
	  caption {font-weight: bold; text-transform: uppercase}
	  td {text-align: right}
	  input {text-align: right}
	  th, label, h1 {text-transform: uppercase}
	  label {font-weight: bold}
	  table.detail tr:nth-child(even) {background:White}
	  table.detail tr:nth-child(odd) {background:LightGray}
	</style>
      </head>
      <body>
	<xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="/a:entry">
    <form action="{link/@href}">
      <xsl:apply-templates select="a:title"/>
      <table class="master">
	<xsl:apply-templates select="a:content/m:properties/d:*"/>
	<tr>
	  <td>
	    <input type="hidden" name="method" value="put"/>
	  </td>
	  <td>
	    <input type="submit" value="Submit"/>
	  </td>
	</tr>
      </table>
      <xsl:apply-templates select="a:link/m:inline"/>
    </form>
  </xsl:template>

  <xsl:template match="a:entry">
    <xsl:apply-templates select="a:title"/>
    <xsl:apply-templates select="a:content/m:properties/d:*"/>
    <xsl:apply-templates select="a:link/m:inline"/>
  </xsl:template>

  <xsl:template match="a:entry/a:title">
    <h1><xsl:apply-templates/></h1>
  </xsl:template>

  <xsl:template match="a:feed/a:title">
    <caption><xsl:apply-templates/></caption>
  </xsl:template>

  <xsl:template match="m:inline">
    <tr>
      <td><xsl:apply-templates/></td>
    </tr>
  </xsl:template>

  <xsl:template match="d:*">
    <tr>
      <td><label><xsl:value-of select="translate(local-name(), '_', ' ')"/></label></td>
      <td><input name="{name()}" value="{text()}"/></td>
    </tr>
  </xsl:template>

  <xsl:template match="a:feed/a:entry">
    <tr>
      <td><a href="{a:link[@rel='edit']/@href}">Edit</a></td>
      <xsl:apply-templates select="a:content/m:properties"/>
    </tr>
  </xsl:template>

  <xsl:template match="a:feed/a:entry[1]">
    <tr>
      <td/>
      <xsl:apply-templates select="a:content/m:properties" mode="header"/>
    </tr>
    <tr>
      <td><a href="{a:link[@rel='edit']/@href}">Edit</a></td>
      <xsl:apply-templates select="a:content/m:properties"/>
    </tr>
  </xsl:template>

  <xsl:template match="a:feed/a:entry/a:content/m:properties/d:*" mode="header">
    <th><xsl:value-of select="translate(local-name(), '_', ' ')"/></th>
  </xsl:template>

  <xsl:template match="a:feed/a:entry/a:content/m:properties/d:*">
    <td><xsl:value-of select="text()"/></td>
  </xsl:template>

  <xsl:template match="a:feed">
    <table class="detail">
      <xsl:apply-templates select="a:title"/>
      <xsl:apply-templates select="a:entry"/>
    </table>
  </xsl:template>

</xsl:stylesheet> 
