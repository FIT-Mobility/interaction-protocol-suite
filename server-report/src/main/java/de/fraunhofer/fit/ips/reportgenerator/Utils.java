package de.fraunhofer.fit.ips.reportgenerator;

import com.lowagie.text.FontFactory;
import com.lowagie.text.FontFactoryImp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 09.11.2017
 */
@Slf4j
public final class Utils {

    public static String readToString(InputStream data) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = data.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
            return out.toString(StandardCharsets.UTF_8.name());
        }
    }

    private static void writeToFile(InputStream data, File target) throws IOException {
        try (OutputStream out = new FileOutputStream(target)) {
            byte[] buf = new byte[1024];
            int length;
            while ((length = data.read(buf)) > 0) {
                out.write(buf, 0, length);
            }
        }
    }

    public static String durationToHumanReadable(long millis) {
        if (millis < 0) {
            return "invalid";
        }

        return String.format("%02dd:%02dh:%02dm:%02ds",
                TimeUnit.MILLISECONDS.toDays(millis),
                TimeUnit.MILLISECONDS.toHours(millis) % 24,
                TimeUnit.MILLISECONDS.toMinutes(millis) % 60,
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        );
    }

    public static void registerFontPaths() {
        final String fontPath = "fonts";

        // This registers only some basic dirs
        FontFactory.registerDirectories();

        // Register some special ones as well
        for (String s : getOtherFontPaths()) {
            FontFactory.registerDirectory(s, true);
        }

        // Register fonts that come with this app
        URL fontsUrl = Utils.class.getClassLoader().getResource(fontPath);
        if (fontsUrl != null) {
            if (fontsUrl.getProtocol().contains("jar")) {
                registerFromJar(fontPath);
            } else {
                String path = fontsUrl.getPath();
                int count = FontFactory.registerDirectory(path, true);
                log.info("Registered {} fonts at custom path {}", count, path);
            }
        }
    }

    /**
     * Problem 1: FontFactory.registerDirectory(..) expects a path to a file system dir
     * Problem 2: A dir within the jar cannot be treated as a file system dir
     * Problem 3: It's not easy to traverse a dir within the jar
     *
     * Solution: Copy all fonts within the font dir from jar to a temp dir in file system, and register this
     * temp dir with FontFactory
     *
     * Help:
     * https://stackoverflow.com/questions/11012819/how-can-i-get-a-resource-folder-from-inside-my-jar-file
     * https://stackoverflow.com/questions/6247144/how-to-load-a-folder-from-a-jar
     * https://stackoverflow.com/questions/779519/delete-directories-recursively-in-java/27917071#27917071
     */
    private static void registerFromJar(String fontDirInJar) {
        final Path tempDirectory;
        try {
            tempDirectory = Files.createTempDirectory("report-generator-fonts");
        } catch (final IOException e) {
            log.error("Failed to create temporary font dir", e);
            return;
        }

        try {
            final URL jar = Utils.class.getProtectionDomain().getCodeSource().getLocation();

            String jarPath = jar.toString().substring("file:".length());

            // special windows treatment
            if (SystemUtils.IS_OS_WINDOWS && jarPath.startsWith("/")) {
                jarPath = jarPath.substring(1);
            }

            final Path jarFile = Paths.get(jarPath);
            try (final FileSystem fs = FileSystems.newFileSystem(jarFile, null)) {
                Files.walkFileTree(fs.getPath(fontDirInJar), new SimpleFileVisitor<>() {
                    private String cleanupInputString(Path path) {
                        return path.toString().replaceFirst("/", "");
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
                            throws IOException {
                        tempDirectory.resolve(cleanupInputString(dir)).toFile().mkdirs();
                        return super.preVisitDirectory(dir, attrs);
                    }

                    @Override
                    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                            throws IOException {
                        final File target = tempDirectory.resolve(cleanupInputString(file)).toFile();
                        writeToFile(Utils.class.getResourceAsStream(file.toString()), target);
                        return super.visitFile(file, attrs);
                    }
                });
            }

            int count = FontFactory.registerDirectory(tempDirectory.toString(), true);
            log.info("Registered {} fonts at custom path {}", count, tempDirectory);

        } catch (IOException e) {
            log.error("Error occurred", e);
        }
    }

    /**
     * Paths not in {@link FontFactoryImp#registerDirectories()} or paths which are there but whose subdirectories
     * are not further explored.
     *
     * https://github.com/UnderwaterApps/overlap2d/blob/master/overlap2d/src/com/uwsoft/editor/proxy/FontManager.java
     */
    private static List<String> getOtherFontPaths() {
        List<String> baseDirs = new ArrayList<>();

        if (SystemUtils.IS_OS_WINDOWS) {
            baseDirs.add(System.getenv("WINDIR") + "\\" + "Fonts");

        } else if (SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_MAC) {
            baseDirs.add("/Library/Fonts");
            baseDirs.add("/System/Library/Fonts");
            baseDirs.add(System.getProperty("user.home") + File.separator + "Library/Fonts");

        } else if (SystemUtils.IS_OS_LINUX) {
            File tmp = new File(System.getProperty("user.home") + File.separator + ".fonts");
            if (tmp.exists() && tmp.isDirectory() && tmp.canRead()) {
                baseDirs.add(tmp.getAbsolutePath());
            }
        }

        return baseDirs;
    }

    private static void deleteDir(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
