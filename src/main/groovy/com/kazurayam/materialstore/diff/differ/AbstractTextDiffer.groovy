package com.kazurayam.materialstore.diff.differ

import com.kazurayam.materialstore.diff.Differ

import com.github.difflib.text.DiffRow
import com.github.difflib.text.DiffRowGenerator
import com.kazurayam.materialstore.diff.DiffArtifact
import com.kazurayam.materialstore.diff.Differ
import com.kazurayam.materialstore.store.FileType
import com.kazurayam.materialstore.store.Jobber
import com.kazurayam.materialstore.store.Material
import com.kazurayam.materialstore.store.Metadata
import groovy.xml.MarkupBuilder

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

abstract class AbstractTextDiffer implements Differ {
    private Path root_

    private Charset charset = StandardCharsets.UTF_8

    AbstractTextDiffer() {}

    AbstractTextDiffer(Path root) {
        ensureRoot(root)
        this.root_ = root
    }

    void setRoot(Path root) {
        ensureRoot(root)
        this.root_ = root
    }

    private static void ensureRoot(Path root) {
        Objects.requireNonNull(root)
        if (! Files.exists(root)) {
            throw new IllegalArgumentException("${root} is not present")
        }
    }

    void setCharset(Charset chs) {
        Objects.requireNonNull(chs)
        this.charset = chs
    }

    @Override
    DiffArtifact makeDiff(DiffArtifact input) {
        Objects.requireNonNull(root_)
        Objects.requireNonNull(input)
        Objects.requireNonNull(input.getExpected())
        Objects.requireNonNull(input.getActual())
        //
        Material expected = input.getExpected()
        if (! expected.isText()) {
            throw new IllegalArgumentException("${expected} is not a text")
        }
        Material actual = input.getActual()
        if (! actual.isText()) {
            throw new IllegalArgumentException("${actual} is not a text")
        }

        //
        String content = makeContent(root_, expected, actual, charset)

        //
        byte[] diffData = toByteArray(content)
        Metadata diffMetadata = new Metadata([
                "category": "diff",
                "expected": expected.getIndexEntry().getID().toString(),
                "actual": actual.getIndexEntry().getID().toString()
        ])
        Jobber jobber = new Jobber(root_, actual.getJobName(), actual.getJobTimestamp())
        Material diffMaterial = jobber.write(diffData, FileType.HTML, diffMetadata)
        //
        DiffArtifact result = new DiffArtifact(expected, actual)
        result.setDiff(diffMaterial)
        return result
    }

    abstract String makeContent(Path root, Material original, Material revised, Charset charset)

    static List<String> readAllLines(String longText) {
        BufferedReader br = new BufferedReader(new StringReader(longText))
        List<String> lines = new ArrayList<>()
        String line
        while ((line = br.readLine()) != null) {
            lines.add(line)
        }
        return lines
    }

    static String readMaterial(Path root, Material material, Charset charset) {
        Objects.requireNonNull(root)
        Objects.requireNonNull(material)
        Objects.requireNonNull(charset)
        Jobber jobber = new Jobber(root, material.getJobName(), material.getJobTimestamp())
        byte[] data = jobber.read(material.getIndexEntry())
        return new String(data, charset)
    }

    private static byte[] toByteArray(String s) {
        return s.getBytes(StandardCharsets.UTF_8)
    }
}
