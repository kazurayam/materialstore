package com.kazurayam.materialstore.reduce.differ;

import com.github.difflib.text.DiffRow;
import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.FileTypeDiffability;
import com.kazurayam.materialstore.filesystem.Jobber;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialLocator;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.reduce.zipper.MaterialProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public abstract class AbstractTextDiffer implements Differ {

    private Logger logger = LoggerFactory.getLogger(AbstractTextDiffer.class.getName());

    protected final Store store;

    private Charset charset = StandardCharsets.UTF_8;

    public AbstractTextDiffer(Store store) {
        this.store = store;
    }

    public void setCharset(Charset chs) {
        Objects.requireNonNull(chs);
        this.charset = chs;
    }

    @Override
    public MaterialProduct stuffDiff(MaterialProduct mProduct) throws MaterialstoreException {
        Objects.requireNonNull(store);
        Objects.requireNonNull(mProduct);
        Objects.requireNonNull(mProduct.getLeft());
        Objects.requireNonNull(mProduct.getRight());
        //
        Material left = mProduct.getLeft();
        if (!left.getDiffability().equals(FileTypeDiffability.AS_TEXT)) {
            logger.warn(left + " is not a text.\n" +
                    "mProduct=" + mProduct.toJson(true));
        }

        Material right = mProduct.getRight();
        if (!right.getDiffability().equals(FileTypeDiffability.AS_TEXT)) {
            logger.warn(right + " is not a text.\n" +
                    "mProduct=" + mProduct.toJson(true));
        }

        //
        TextDiffContent textDiffContent = makeTextDiffContent(store, left, right, charset);
        Double diffRatio = textDiffContent.getDiffRatio();

        //
        byte[] diffData = toByteArray(textDiffContent.getContent());
        LinkedHashMap<String, String> map = new LinkedHashMap<>(4);
        map.put("category", "diff");
        map.put("left", new MaterialLocator(left).toString());
        map.put("right", new MaterialLocator(right).toString());
        map.put("ratio", DifferUtil.formatDiffRatioAsString(diffRatio));
        Metadata diffMetadata = Metadata.builder(map).build();
        Jobber jobber = new Jobber(store, right.getJobName(), mProduct.getReducedTimestamp());
        Material diffMaterial = jobber.write(diffData, FileType.HTML, diffMetadata, Jobber.DuplicationHandling.CONTINUE);
        //
        MaterialProduct result = new MaterialProduct(mProduct);
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

    /**
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
        List<String> segments = Arrays.asList(SPLITTER.split(line));
        return segments;
    }

}
