package com.kazurayam.materialstore.facet.textgrid;

import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.Material;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Metadata;
import com.kazurayam.materialstore.core.Store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class DefaultTextGridDiffer extends TextGridDifferBuilder {

    public DefaultTextGridDiffer(Path projectDir) {
        super(projectDir);
    }

    /**
     *
     */
    @Override
    public void jsonifyAndStoreRows(Store store,
                                    JobName jobName,
                                    JobTimestamp jobTimestamp,
                                    List<List<String>> input,
                                    final KeyRange keyRange,
                                    String inputId) throws MaterialstoreException {
        List<String> lines =
                input.stream()
                        .map(list -> new Row(list, keyRange))
                        .map(row -> row.values().toJson())
                        .collect(Collectors.toList());
        //
        try {
            Path tempFile = Files.createTempFile(null, null);
            TextGridDifferBuilder.writeLinesIntoFile(lines, tempFile.toFile());
            Metadata metadata =
                    Metadata.builder()
                            .put("input", inputId)
                            .put("target", "rows")
                            .build();
            Material material =
                    store.write(jobName, jobTimestamp, FileType.JSON, metadata, tempFile);
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
    }

    @Override
    public void jsonifyAndStoreKeys(Store store,
                                    JobName jobName,
                                    JobTimestamp jobTimestamp,
                                    List<List<String>> input,
                                    final KeyRange keyRange,
                                    String inputId) throws MaterialstoreException {
        Set<String> keys =
                input.stream()
                        .map(list -> new Row(list, keyRange))
                        .map(row -> row.key().toJson())
                        .collect(Collectors.toSet());
        List<String> lines = new ArrayList<>(keys);
        Collections.sort(lines);
        //
        try {
            Path tempFile = Files.createTempFile(null, null);
            TextGridDifferBuilder.writeLinesIntoFile(lines, tempFile.toFile());
            Metadata metadata = Metadata.builder().put("input", inputId).put("target", "keys").build();
            Material material =
                    store.write(jobName, jobTimestamp, FileType.JSON, metadata, tempFile);
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
    }

}
