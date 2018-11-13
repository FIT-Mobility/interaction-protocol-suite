<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema">

    <!-- Identity template -->
    <xsl:template match="node()|@*" name="identity">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/xs:schema/@xpathDefaultNamespace" name="remove-xpathDefaultNamespace-on-schema"/>

    <xsl:template match="//xs:assert" name="remove-assert"/>
    <xsl:template match="//xs:assertion" name="remove-assertion"/>

</xsl:stylesheet>
