package de.fraunhofer.fit.ips.server;

import de.fraunhofer.fit.ips.proto.javabackend.CreateReportRequest;
import de.fraunhofer.fit.ips.proto.structure.MultilingualPlaintext;
import de.fraunhofer.fit.ips.proto.structure.MultilingualRichtext;
import lombok.RequiredArgsConstructor;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
public class Helper {
    private final String primaryLanguage;
    private final String additionalLanguage;

    public MultilingualPlaintext mlPlaintext(final String primaryLanguageContent,
                                             final String additionalLanguageContent) {
        return MultilingualPlaintext.newBuilder()
                                    .putLanguageToPlaintext(primaryLanguage, primaryLanguageContent)
                                    .putLanguageToPlaintext(additionalLanguage, additionalLanguageContent)
                                    .build();
    }

    public MultilingualPlaintext slPlaintext(final String primaryLanguageContent) {
        return MultilingualPlaintext.newBuilder()
                                    .putLanguageToPlaintext(primaryLanguage, primaryLanguageContent)
                                    .build();
    }

    public MultilingualRichtext mlRichtext(final String primaryLanguageContent,
                                           final String additionalLanguageContent) {
        return MultilingualRichtext.newBuilder()
                                   .putLanguageToRichtext(primaryLanguage, primaryLanguageContent)
                                   .putLanguageToRichtext(additionalLanguage, additionalLanguageContent)
                                   .build();
    }

    public MultilingualRichtext slRichtext(final String primaryLanguageContent) {
        return MultilingualRichtext.newBuilder()
                                   .putLanguageToRichtext(primaryLanguage, primaryLanguageContent)
                                   .build();
    }

    public CreateReportRequest.Configuration.Builder getDefaultConfiguration() {
        return CreateReportRequest.Configuration.newBuilder()
                                                .addLanguages(primaryLanguage)
                                                .addLanguages(additionalLanguage);
    }
}
