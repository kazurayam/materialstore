package com.kazurayam.materialstore.base.reduce.differ;

import com.github.difflib.text.DiffRow;
import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.core.filesystem.FileType;
import com.kazurayam.materialstore.core.filesystem.FileTypeDiffability;
import com.kazurayam.materialstore.core.filesystem.Jobber;
import com.kazurayam.materialstore.core.filesystem.Material;
import com.kazurayam.materialstore.core.filesystem.MaterialLocator;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Metadata;
import com.kazurayam.materialstore.core.filesystem.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public abstract class AbstractTextDiffer implements Differ {

    private final Logger logger = LoggerFactory.getLogger(AbstractTextDiffer.class.getName());

    protected final Store store;

    private Charset charset = StandardCharsets.UTF_8;

    public AbstractTextDiffer(Store store) {
        Objects.requireNonNull(store);
        this.store = store;
    }

    public void setCharset(Charset chs) {
        Objects.requireNonNull(chs);
        this.charset = chs;
    }

    @Override
    public MaterialProduct stuffDiff(MaterialProduct mProduct) throws MaterialstoreException {
        Objects.requireNonNull(mProduct);
        Objects.requireNonNull(mProduct.getLeft());
        Objects.requireNonNull(mProduct.getRight());

        Material left = complementTextMaterial(store, mProduct, mProduct.getLeft());
        Material right = complementTextMaterial(store, mProduct, mProduct.getRight());

        TextDiffContent textDiffContent = makeTextDiffContent(store, left, right, charset);
        Double diffRatio = textDiffContent.getDiffRatio();

        //
        byte[] diffData = toByteArray(textDiffContent.getContent());

        Metadata diffMetadata =
                Metadata.builder()
                        .put("category", "diff")
                        .put("left", new MaterialLocator(left).toString())
                        .put("right", new MaterialLocator(right).toString())
                        .put("ratio", DifferUtil.formatDiffRatioAsString(diffRatio))
                        .build();
        // write the diff text
        Jobber jobber =
                new Jobber(store, mProduct.getJobName(),
                        mProduct.getReducedTimestamp());
        Material diffMaterial =
                jobber.write(diffData, FileType.HTML,
                        diffMetadata, Jobber.DuplicationHandling.CONTINUE);

        MaterialProduct result =
                new MaterialProduct.Builder(mProduct)
                        .setLeft(left)
                        .setRight(right)
                        .build();
        result.setDiff(diffMaterial);
        result.setDiffRatio(diffRatio);

        return result;
    }

    private Material complementTextMaterial(Store store,
                                             MaterialProduct mProduct,
                                             Material material) throws MaterialstoreException {
        if (material.getDiffability().equals(FileTypeDiffability.AS_TEXT)) {
            return material;
        } else {
            return makeNoMaterialFoundMaterialOfText(store, mProduct);
        }
    }
    private Material makeNoMaterialFoundMaterialOfText(
            Store store, MaterialProduct mProduct)
            throws MaterialstoreException {
        return makeNoMaterialFoundMaterial(store, mProduct,
                FileType.HTML, Material.loadNoMaterialFoundText());
    }


    public abstract TextDiffContent makeTextDiffContent(Store store,
                                                        Material original, Material revised,
                                                        Charset charset)
            throws MaterialstoreException;

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

    public static String readMaterial(Store store, Material material, Charset charset)
            throws MaterialstoreException {
        Objects.requireNonNull(store);
        Objects.requireNonNull(material);
        Objects.requireNonNull(charset);
        if (!material.equals(Material.NULL_OBJECT)) {
            Jobber jobber = new Jobber(store, material.getJobName(), material.getJobTimestamp());
            byte[] data = jobber.read(material.getIndexEntry());
            return new String(data, charset);
        } else {
            return "";
        }

    }

    private static byte[] toByteArray(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }


    static final String CLASS_TD_CHANGE = "code-change";
    static final String CLASS_TD_DELETE = "code-delete";
    static final String CLASS_TD_EQUAL  = "code-equal";
    static final String CLASS_TD_INSERT = "code-insert";

    static String getClassOfDiffRow(DiffRow row) {
        switch (row.getTag()) {
            case CHANGE:
                return CLASS_TD_CHANGE;
            case DELETE:
                return CLASS_TD_DELETE;
            case EQUAL:
                return CLASS_TD_EQUAL;
            case INSERT:
                return CLASS_TD_INSERT;
            default:
                throw new IllegalArgumentException("unknown row.getTag()=${row.getTag()}");
        }
    }

    /*
     * "Java Split String and Keep Delimitiers"
     * https://www.baeldung.com/java-split-string-keep-delimiters
     * @param line
     * @param clazz
     * @return
     */
    public static final String OLD_TAG = "!_~_!";
    public static final String NEW_TAG = "!#~#!";

    private static final Pattern SPLITTER =
            Pattern.compile(String.format("((?=%s)|(?<=%s)|(?=%s)|(?<=%s))",
                            OLD_TAG, OLD_TAG, NEW_TAG, NEW_TAG));

    public static List<String> splitStringWithOldNewTags(String line) {
        return Arrays.asList(SPLITTER.split(line));
    }

}
