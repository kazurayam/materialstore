package com.kazurayam.materialstore.reduce.differ;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.FileTypeDiffability;
import com.kazurayam.materialstore.filesystem.Jobber;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.reduce.MaterialProduct;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public abstract class AbstractTextDiffer implements Differ {

    private Path root_;
    private Charset charset = StandardCharsets.UTF_8;

    public AbstractTextDiffer() {
    }

    public AbstractTextDiffer(Path root) {
        ensureRoot(root);
        this.root_ = root;
    }

    @Override
    public void setRoot(Path root) {
        ensureRoot(root);
        this.root_ = root;
    }

    private static void ensureRoot(final Path root) {
        Objects.requireNonNull(root);
        if (!Files.exists(root)) {
            throw new IllegalArgumentException(root + " is not present");
        }

    }

    public void setCharset(Charset chs) {
        Objects.requireNonNull(chs);
        this.charset = chs;
    }

    @Override
    public MaterialProduct makeMProduct(MaterialProduct mProduct) throws MaterialstoreException {
        Objects.requireNonNull(root_);
        Objects.requireNonNull(mProduct);
        Objects.requireNonNull(mProduct.getLeft());
        Objects.requireNonNull(mProduct.getRight());
        //
        final Material left = mProduct.getLeft();
        if (!left.getDiffability().equals(FileTypeDiffability.AS_TEXT)) {
            throw new IllegalArgumentException(left + " is not a text");
        }

        final Material right = mProduct.getRight();
        if (!right.getDiffability().equals(FileTypeDiffability.AS_TEXT)) {
            throw new IllegalArgumentException(right + " is not a text");
        }


        //
        TextDiffContent textDiffContent = makeContent(root_, left, right, charset);
        Double diffRatio = textDiffContent.getDiffRatio();

        //
        byte[] diffData = toByteArray(textDiffContent.getContent());
        LinkedHashMap<String, String> map = new LinkedHashMap<>(4);
        map.put("category", "diff");
        map.put("left", left.getIndexEntry().getID().toString());
        map.put("right", right.getIndexEntry().getID().toString());
        map.put("ratio", DifferUtil.formatDiffRatioAsString(diffRatio));
        Metadata diffMetadata = Metadata.builder(map).build();
        Jobber jobber = new Jobber(root_, right.getJobName(), mProduct.getReducedTimestamp());
        Material diffMaterial = jobber.write(diffData, FileType.HTML, diffMetadata, Jobber.DuplicationHandling.CONTINUE);
        //
        MaterialProduct result = new MaterialProduct(mProduct);
        result.setDiff(diffMaterial);
        result.setDiffRatio(diffRatio);
        return result;
    }

    public abstract TextDiffContent makeContent(Path root, Material original, Material revised, Charset charset);

    public static List<String> readAllLines(String longText) throws MaterialstoreException {
        BufferedReader br = new BufferedReader(new StringReader(longText));
        List<String> lines = new ArrayList<>();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
        return lines;
    }

    public static String readMaterial(Path root, Material material, Charset charset)
            throws MaterialstoreException {
        Objects.requireNonNull(root);
        Objects.requireNonNull(material);
        Objects.requireNonNull(charset);
        if (!material.equals(Material.NULL_OBJECT)) {
            Jobber jobber = new Jobber(root, material.getJobName(), material.getJobTimestamp());
            byte[] data = jobber.read(material.getIndexEntry());
            return new String(data, charset);
        } else {
            return "";
        }

    }

    private static byte[] toByteArray(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }
}
