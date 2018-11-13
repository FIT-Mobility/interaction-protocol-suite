<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema">

    <!-- Identity template -->
    <xsl:template match="node()|@*" name="identity">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/xs:schema/@xpathDefaultNamespace" name="remove-xpathDefaultNamespace-on-schema"/>
    <xsl:template match="/xs:schema/@defaultAttributes" name="remove-defaultAttributes-on-schema"/>

    <xsl:template match="/xs:schema/xs:defaultOpenContent" name="remove-defaultOpenContent"/>
    <xsl:template match="/xs:schema/xs:override" name="remove-override"/>

    <xsl:template match="//xs:openContent" name="remove-openContent"/>

    <xsl:template match="//xs:anyAttribute/@notNamespace" name="remove-anyAttribute-notNamespace"/>
    <xsl:template match="//xs:anyAttribute/@notQName" name="remove-anyAttribute-notQName"/>

    <xsl:template match="//xs:any/@notNamespace" name="remove-any-notNamespace"/>
    <xsl:template match="//xs:any/@notQName" name="remove-any-notQName"/>

    <xsl:template match="//xs:element/@targetNamespace" name="remove-element-targetNamespace"/>
    <xsl:template match="//xs:attribute/@targetNamespace" name="remove-attribute-targetNamespace"/>
    <xsl:template match="//xs:attribute/@inheritable" name="remove-attribute-inheritable"/>

    <xsl:template match="//xs:assert" name="remove-assert"/>
    <xsl:template match="//xs:assertion" name="remove-assertion"/>

    <xsl:template match="//xs:alternative" name="remove-alternative"/>
    <xsl:template match="//xs:explicitTimezone" name="remove-explicitTimezone"/>

    <xsl:template match="//xs:complexType/@defaultAttributesApply" name="remove-complexType-defaultAttributesApply"/>

    <xsl:template match="//xs:selector/@xpathDefaultNamespace" name="remove-selector-xpathDefaultNamespace"/>
    <xsl:template match="//xs:field/@xpathDefaultNamespace" name="remove-field-xpathDefaultNamespace"/>

    <xsl:template match="//@*[.='dateTimeStamp']" name="replace-dateTimeStamp-dateTime-local">
        <xsl:attribute name="{name()}" namespace="{namespace-uri()}">dateTime</xsl:attribute>
    </xsl:template>
    <xsl:template match="//@*[substring-after(., ':')='dateTimeStamp']" name="replace-dateTimeStamp-dateTime-prefix">
        <xsl:attribute name="{name()}" namespace="{namespace-uri()}"><xsl:value-of select="substring-before(., ':')"/>:dateTime</xsl:attribute>
    </xsl:template>

    <xsl:template match="//@*[.='anyAtomicType']" name="replace-anyAtomicType-anySimpleType-local">
        <xsl:attribute name="{name()}" namespace="{namespace-uri()}">anySimpleType</xsl:attribute>
    </xsl:template>
    <xsl:template match="//@*[substring-after(., ':')='anyAtomicType']" name="replace-anyAtomicType-anySimpleType-prefix">
        <xsl:attribute name="{name()}" namespace="{namespace-uri()}"><xsl:value-of select="substring-before(., ':')"/>:anySimpleType</xsl:attribute>
    </xsl:template>

    <xsl:template match="//@*[.='dayTimeDuration']" name="replace-dayTimeDuration-duration-local">
        <xsl:attribute name="{name()}" namespace="{namespace-uri()}">duration</xsl:attribute>
    </xsl:template>
    <xsl:template match="//@*[substring-after(., ':')='dayTimeDuration']" name="replace-dayTimeDuration-duration-prefix">
        <xsl:attribute name="{name()}" namespace="{namespace-uri()}"><xsl:value-of select="substring-before(., ':')"/>:duration</xsl:attribute>
    </xsl:template>

    <xsl:template match="//@*[.='yearMonthDuration']" name="replace-yearMonthDuration-duration-local">
        <xsl:attribute name="{name()}" namespace="{namespace-uri()}">duration</xsl:attribute>
    </xsl:template>
    <xsl:template match="//@*[substring-after(., ':')='yearMonthDuration']" name="replace-yearMonthDuration-duration-prefix">
        <xsl:attribute name="{name()}" namespace="{namespace-uri()}"><xsl:value-of select="substring-before(., ':')"/>:duration</xsl:attribute>
    </xsl:template>

</xsl:stylesheet>
