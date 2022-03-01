package com.kazurayam.materialstore.reduce.differ


import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.Jobber
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.metadata.Metadata
import com.kazurayam.materialstore.reduce.MProduct
import freemarker.cache.ClassTemplateLoader
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import freemarker.template.Template

import java.nio.file.Path

/**
 * VoidDiffer does nothing
 */
class VoidDiffer implements Differ {

    private Path root
    private Configuration cfg

    VoidDiffer() {
        // FreeMarker Configuration
        cfg = new Configuration(Configuration.VERSION_2_3_31)
        // we will load FreeMarker templates from CLASSPATH
        cfg.setTemplateLoader(new ClassTemplateLoader(
                VoidDiffer.class.getClassLoader(),
                "freemarker_templates"
        ))
        // Recommended FreeMarker settings
        cfg.setDefaultEncoding("UTF-8")
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER)
        cfg.setLogTemplateExceptions(false)
        cfg.setWrapUncheckedExceptions(true)
        cfg.setFallbackOnNullLoopVariable(false)
    }

    @Override
    void setRoot(Path root) {
        this.root = root
    }

    @Override
    MProduct makeMProduct(MProduct mProduct) {
        Objects.requireNonNull(mProduct)
        Objects.requireNonNull(mProduct.getLeft())
        Objects.requireNonNull(mProduct.getRight())
        Material left = mProduct.getLeft()
        Material right = mProduct.getRight()
        //
        Map<String, Object> dataModel = new HashMap<>()
        dataModel.put("left", left.toString())
        dataModel.put("right", right.toString())
        // Get the template
        Template template = cfg.getTemplate(
                "com/kazurayam/materialstore/differ/VoidDifferTemplate.ftlh")

        // Merge data model with Template
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        Writer out = new OutputStreamWriter(baos)
        template.process(dataModel, out)
        out.close()

        byte[] diffData = baos.toByteArray()

        // materialize the byte[] into the store
        Metadata diffMetadata = Metadata.builder([
                "category": "diff",
                "left": left.getIndexEntry().getID().toString(),
                "right": right.getIndexEntry().getID().toString()])
                .build()
        assert root != null
        Jobber jobber = new Jobber(root, right.getJobName(), mProduct.getResolventTimestamp())
        Material diffMaterial =
                jobber.write(diffData,
                        FileType.HTML,
                        diffMetadata,
                        Jobber.DuplicationHandling.CONTINUE)
        //
        MProduct result = new MProduct(mProduct)
        result.setDiff(diffMaterial)
        return result
    }
}
