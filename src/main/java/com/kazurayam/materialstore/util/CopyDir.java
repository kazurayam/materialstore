package com.kazurayam.materialstore.util;

import com.kazurayam.materialstore.core.FileSystemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

/**
 * original: https://www.codejava.net/java-se/file-io/java-nio-copy-file-or-directory-examples
 */
public class CopyDir extends SimpleFileVisitor<Path> {
    private static Logger logger = LoggerFactory.getLogger(CopyDir.class);
    private final Path sourceDir;
    private final Path targetDir;

    private final Option option;

    public CopyDir(Path sourceDir, Path targetDir) {
        this(sourceDir, targetDir, Option.REPLACE_EXISTING);
    }

    public CopyDir(Path sourceDir, Path targetDir, Option option) {
        Objects.requireNonNull(sourceDir, "sourceDir must not be null");
        Objects.requireNonNull(targetDir, "targetDir must not be null");
        Objects.requireNonNull(option, "option must not be null");
        this.sourceDir = sourceDir;
        this.targetDir = targetDir;
        this.option = option;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
        try {
            Path targetFile = targetDir.resolve(sourceDir.relativize(file));
            if (Files.exists(targetFile)) {
                if (option == Option.REPLACE_EXISTING) {
                    Files.copy(file, targetFile,
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.COPY_ATTRIBUTES);
                } else {
                    logger.debug(targetFile + " is found; skipped overwriting it.");
                }
            } else {
                Files.copy(file, targetFile,
                        StandardCopyOption.COPY_ATTRIBUTES);
            }

        } catch (IOException e) {
            logger.warn(e.getMessage());
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir,
                                             BasicFileAttributes attributes) {
        try {
            Path newDir = targetDir.resolve(sourceDir.relativize(dir));
            if (!Files.exists(newDir)) {
                Files.createDirectory(newDir);
            }
        } catch (IOException ex) {
            logger.warn(ex.getMessage());
        }
        return FileVisitResult.CONTINUE;
    }

    public static enum Option {
        REPLACE_EXISTING,
        SKIP_IF_EXISTING;
    }

    public static void main(String[] args) throws IOException {
        FileSystem fs = FileSystemFactory.newFileSystem();
        Path sourceDir = fs.getPath(args[0]);
        Path targetDir = fs.getPath(args[1]);
        Files.walkFileTree(sourceDir, new CopyDir(sourceDir, targetDir));
    }
}

