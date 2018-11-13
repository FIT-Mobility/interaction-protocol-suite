<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/">
    <xsl:template match="/wsdl:definitions/wsdl:types">
        <xsl:copy>
            <xsl:copy-of select="document('schema.xsd')"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:transform>
