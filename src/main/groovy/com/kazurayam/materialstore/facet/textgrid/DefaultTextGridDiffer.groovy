package com.kazurayam.materialstore.facet.textgrid

import com.kazurayam.materialstore.filesystem.FileType

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors


import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.filesystem.Metadata
import com.kazurayam.materialstore.filesystem.Store

class DefaultTextGridDiffer extends TextGridDifferBuilder {

    DefaultTextGridDiffer() {
        super()
    }

    DefaultTextGridDiffer(Path projectDir) {
        super(projectDir)
    }

    /**
     *
     */
    @Override
    void jsonifyAndStoreRows(Store store, JobName jobName, JobTimestamp jobTimestamp,
                             List<List<String>> input, Range<Integer> keyRange,
                             String inputId) {
        List<String> lines = input.stream()
                .map({ List<String> list -> new Row(list, keyRange) })
                .map({ Row row -> row.values().toJson() })
                .collect(Collectors.toList())
        //
        Path tempFile = Files.createTempFile(null, null)
        writeLinesIntoFile(lines, tempFile.toFile())
        Metadata metadata = Metadata.builder()
                .put("input", inputId)
                .put("target", "rows")
                .build()
        Material mat = store.write(jobName, jobTimestamp, FileType.JSON, metadata, tempFile)
    }

    @Override
    void jsonifyAndStoreKeys(Store store, JobName jobName, JobTimestamp jobTimestamp,
                             List<List<String>> input, Range<Integer> keyRange,
                             String inputId) {
        Set<String> keys = input.stream()
                .map({ List<String> list -> new Row(list, keyRange) })
                .map({ Row row -> row.key().toJson() })
                .collect(Collectors.toSet() )
        List<String> lines = new ArrayList<>(keys)
        Collections.sort(lines)
        //
        Path tempFile = Files.createTempFile(null, null)
        writeLinesIntoFile(lines, tempFile.toFile())
        Metadata metadata = Metadata.builder()
                .put("input", inputId)
                .put("target", "keys")
                .build()
        Material mat = store.write(jobName, jobTimestamp, FileType.JSON, metadata, tempFile)
    }
}
