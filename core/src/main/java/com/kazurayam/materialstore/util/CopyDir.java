package com.kazurayam.materialstore.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * original: https://www.codejava.net/java-se/file-io/java-nio-copy-file-or-directory-examples
 */
public class CopyDir extends SimpleFileVisitor<Path> {
    private static Logger logger = LoggerFactory.getLogger(CopyDir.class);
    private Path sourceDir;
    private Path targetDir;

    public CopyDir(Path sourceDir, Path targetDir) {
        this.sourceDir = sourceDir;
        this.targetDir = targetDir;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
        try {
            Path targetFile = targetDir.resolve(sourceDir.relativize(file));
            Files.copy(file, targetFile,
                    StandardCopyOption.REPLACE_EXISTING);
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

        public static void main(String[] args) throws IOException {
            Path sourceDir = Paths.get(args[0]);
            Path targetDir = Paths.get(args[1]);
            Files.walkFileTree(sourceDir, new CopyDir(sourceDir, targetDir));
        }
    }

