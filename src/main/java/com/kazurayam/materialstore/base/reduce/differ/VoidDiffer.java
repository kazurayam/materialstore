package com.kazurayam.materialstore.base.reduce.differ;

import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.Jobber;
import com.kazurayam.materialstore.core.Material;
import com.kazurayam.materialstore.core.MaterialLocator;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Metadata;
import com.kazurayam.materialstore.core.Store;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * VoidDiffer does nothing
 */
public final class VoidDiffer implements Differ {

    private final Store store;
    private final Configuration cfg;

    public VoidDiffer(Store store) {
        this.store = store;
        // FreeMarker Configuration
        cfg = new Configuration(Configuration.VERSION_2_3_31);
        // we will load FreeMarker templates from CLASSPATH
        cfg.setTemplateLoader(new ClassTemplateLoader(VoidDiffer.class.getClassLoader(), "freemarker_templates"));
        // Recommended FreeMarker settings
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
    }

    @Override
    public MaterialProduct generateDiff(MaterialProduct mProduct) throws MaterialstoreException {
        Objects.requireNonNull(mProduct);
        Objects.requireNonNull(mProduct.getLeft());
        Objects.requireNonNull(mProduct.getRight());
        Material left = mProduct.getLeft();
        Material right = mProduct.getRight();
        //
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("left", left.toString());
        dataModel.put("right", right.toString());

        try {
            // Get the template
            Template template = cfg.getTemplate("com/kazurayam/materialstore/reduce/differ/VoidDifferTemplate.ftlh");

            // Merge data model with Template
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Writer out = new OutputStreamWriter(baos);
            template.process(dataModel, out);
            out.close();

            byte[] diffData = baos.toByteArray();

            // materialize the byte[] into the store
            Metadata diffMetadata =
                    Metadata.builder()
                            .put("category", "diff")
                            // no "ratio" attribute for non-diff-able type of files
                            .put("left", new MaterialLocator(left).toString())
                            .put("right", new MaterialLocator(right).toString())
                            .build();

            assert store != null;
            Jobber jobber = new Jobber(store, right.getJobName(), mProduct.getReducedTimestamp());
            Material diffMaterial =
                    jobber.write(diffData,
                            FileType.HTML,
                            diffMetadata,
                            Jobber.DuplicationHandling.CONTINUE);
            //
            MaterialProduct result = new MaterialProduct.Builder(mProduct).build();
            result.setDiff(diffMaterial);
            return result;
        } catch (TemplateException | IOException e) {
            throw new MaterialstoreException(e);
        }
    }

}
