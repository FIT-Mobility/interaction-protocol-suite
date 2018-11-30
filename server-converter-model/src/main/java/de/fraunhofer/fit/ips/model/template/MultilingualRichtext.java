package de.fraunhofer.fit.ips.model.template;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
@Getter
public class MultilingualRichtext {
    @Nonnull final Map<String, String> languageToRichtext;
}
