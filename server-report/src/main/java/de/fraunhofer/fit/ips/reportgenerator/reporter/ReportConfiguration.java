package de.fraunhofer.fit.ips.reportgenerator.reporter;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Builder
@Getter
public class ReportConfiguration {
    @Builder.Default final List<String> languages = List.of("de", "en");
    @Builder.Default final String xsdDocumentationLanguage = "en";
    @Builder.Default final String xsdPrefix = "xs";
    @Builder.Default final String localPrefixIfMissing = "tns";
    @Builder.Default final boolean hideInheritanceInExtensions = true;
    @Builder.Default final boolean inlineEnums = true;
    @Builder.Default final boolean expandAttributeGroups = true;
    @Builder.Default final boolean expandElementGroups = true;
}
