<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />
        </xsl:copy>
    </xsl:template>

    <xsl:template match="vf-license-model/feature-group-list/feature-group/entitlement-pool-list/entitlement-pool/entitlement-metric/value/text()[.='# of software instances']">num of software instances</xsl:template>
    <xsl:template match="vf-license-model/feature-group-list/feature-group/entitlement-pool-list/entitlement-pool/firstClassCitizenId"/>
</xsl:stylesheet>
