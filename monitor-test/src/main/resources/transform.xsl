<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="/*">
        <xsl:copy>
            <xsl:copy-of select="document('request.xml')"/>
            <xsl:copy-of select="document('response.xml')"/>
        </xsl:copy>
    </xsl:template>
</xsl:transform>
