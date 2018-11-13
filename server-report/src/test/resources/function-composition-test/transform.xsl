<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:dimo="http://www.dimo-omp.de"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.dimo-omp.de ../xsd11-test/function-schema.xsd"
        version="1.0"
>
    <xsl:template match="/*">
        <xsl:copy>
            <xsl:copy-of select="document('')/*/@xsi:schemaLocation"/>
            <xsl:copy-of select="document('request.xml')"/>
            <xsl:copy-of select="document('response.xml')"/>
        </xsl:copy>
    </xsl:template>
</xsl:transform>