package de.fraunhofer.fit.ips.testmonitor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class Utils {
    public static String readToString(final InputStream data) throws IOException {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            final byte[] buffer = new byte[1024];
            int length;
            while ((length = data.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
            return out.toString(StandardCharsets.UTF_8.name());
        }
    }
}
