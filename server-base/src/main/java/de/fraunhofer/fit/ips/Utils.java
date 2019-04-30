package de.fraunhofer.fit.ips;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class Utils {
    private Utils() {
    }

    public static byte[] readResourceIntoByteArray(@Nonnull final String absoluteResourceName) throws IOException {
        try (final InputStream inputStream = Utils.class.getResourceAsStream(absoluteResourceName)) {
            return readIntoByteArray(inputStream);
        }
    }

    public static String readResourceIntoString(@Nonnull final String absoluteResourceName) throws IOException {
        try (final InputStream inputStream = Utils.class.getResourceAsStream(absoluteResourceName)) {
            return readIntoString(inputStream);
        }
    }

    public static byte[] readIntoByteArray(@Nonnull final InputStream inputStream) throws IOException {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            streamToStreamHelper(inputStream, out);
            return out.toByteArray();
        }
    }

    public static String readIntoString(@Nonnull final InputStream inputStream) throws IOException {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            streamToStreamHelper(inputStream, out);
            return out.toString(StandardCharsets.UTF_8.name());
        }
    }

    private static void streamToStreamHelper(@Nonnull final InputStream inputStream, @Nonnull final OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }
    }

    public static void writeToFile(@Nonnull final InputStream inputStream, @Nonnull final File target)
            throws IOException {
        try (final OutputStream out = new FileOutputStream(target)) {
            byte[] buf = new byte[1024];
            int length;
            while ((length = inputStream.read(buf)) > 0) {
                out.write(buf, 0, length);
            }
        }
    }
}
