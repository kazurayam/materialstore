package com.kazurayam.materialstore.base.reduce.differ;

import com.github.difflib.text.DiffRow;
import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.FileTypeDiffability;
import com.kazurayam.materialstore.core.IFileType;
import com.kazurayam.materialstore.core.Jobber;
import com.kazurayam.materialstore.core.Material;
import com.kazurayam.materialstore.core.MaterialLocator;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Metadata;
import com.kazurayam.materialstore.core.Store;
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
    public MaterialProduct generateDiff(MaterialProduct mProduct) throws MaterialstoreException {
        Objects.requireNonNull(mProduct);
        Objects.requireNonNull(mProduct.getLeft());
        Objects.requireNonNull(mProduct.getRight());

        Material left = mProduct.getLeft();
        Material right = mProduct.getRight();

        if (left.getDiffability().equals(FileTypeDiffability.AS_TEXT) &&
                right.getDiffability().equals(FileTypeDiffability.AS_TEXT)) {
            logger.debug("Both of the left and right Materials are diff-able as text");
        } else if (! left.getDiffability().equals(FileTypeDiffability.AS_TEXT) &&
                right.getDiffability().equals(FileTypeDiffability.AS_TEXT)) {
            // the left Material is NOT a diff-able text, but the right is a text
            left = makeNoMaterialFoundMaterial(store, mProduct, FileType.TXT, Material.loadNoCounterpartText());
        } else if (left.getDiffability().equals(FileTypeDiffability.AS_TEXT) &&
                ! right.getDiffability().equals(FileTypeDiffability.AS_TEXT)) {
            // the left Material is a diff-able text, but the right is NOT
            right = makeNoMaterialFoundMaterial(store, mProduct, FileType.TXT, Material.loadNoCounterpartText());
        } else {
            throw new IllegalStateException("should not fall down here");
        }

        // generate the diff Material
        TextDiffContent textDiffContent = makeTextDiffContent(store, left, right, charset);
        Double diffRatio = textDiffContent.getDiffRatio();
        byte[] diffData = toByteArray(textDiffContent.getContent());
        Metadata diffMetadata = Metadata.builder()
                .put("category", "diff")
                .put("ratio", DifferUtil.formatDiffRatioAsString(diffRatio))
                .put("left", new MaterialLocator(left).toString())
                .put("right", new MaterialLocator(right).toString())
                .build();
        // write the diff Material into the store
        Material diffMaterial =
                store.write(mProduct.getJobName(), mProduct.getReducedTimestamp(),
                        FileType.HTML, diffMetadata, diffData);

        // return a MaterialProduct object with the diff Material information stuffed
        MaterialProduct result = new MaterialProduct.Builder(mProduct).build();
        result.setDiff(diffMaterial);
        result.setDiffRatio(diffRatio);
        return result;
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

    /*
     * read a full text of the Material specified.
     * If the Material is an NULL_OBJECT, this will return an empty string "".
     * @throws MaterialstoreException when something went wrong
     */
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
