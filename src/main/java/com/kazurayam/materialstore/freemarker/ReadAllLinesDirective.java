package com.kazurayam.materialstore.freemarker;


import com.kazurayam.materialstore.core.FileSystemFactory;
import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ReadAllLinesDirective implements TemplateDirectiveModel {

    Logger logger = LoggerFactory.getLogger(ReadAllLinesDirective.class);

    private static final String PARAM_NAME_PATH = "path";
    private static final String VARIABLE_NAME_BASEDIR = "baseDir";

    @Override
    public void execute(Environment env,
                        Map params,
                        TemplateModel[] loopVars,
                        TemplateDirectiveBody body)
        throws TemplateException, IOException {
        // ------------------------------------------------------------
        // Processing the parameters
        String pathParam = null;

        Iterator paramIter = params.entrySet().iterator();
        while (paramIter.hasNext()) {
            Map.Entry ent = (Map.Entry)paramIter.next();
            String paramName = (String)ent.getKey();
            TemplateModel paramValue = (TemplateModel)ent.getValue();

            if (paramName.equals(PARAM_NAME_PATH)) {
                if (!(paramValue instanceof SimpleScalar)) {
                    throw new TemplateModelException(
                            "The \"" + PARAM_NAME_PATH + "\" parameter "
                            + "must be a string.");
                }
            }
            pathParam = ((TemplateScalarModel)paramValue).getAsString();
        }
        if (pathParam == null) {
            throw new TemplateModelException(
                    "The parameter \""+ PARAM_NAME_PATH + "\" is required."
            );
        }
        if (loopVars.length > 1) {
            throw new TemplateModelException(
                    "At most one loop variable is allowed."
            );
        }
        //-------------------------------------------------------------
        // Do the actual directive execution.
        // 1. resolve tha path of the file to read
        // 2. read all lines from a text file
        // 3. put each line into the loop variable
        if (env.getVariable(VARIABLE_NAME_BASEDIR) == null) {
            throw new TemplateModelException(VARIABLE_NAME_BASEDIR + " is not defined");
        }
        String sp = String.valueOf(env.getVariable(VARIABLE_NAME_BASEDIR));
        FileSystem fs = FileSystemFactory.newFileSystem();
        Path baseDir = fs.getPath(sp);

        logger.info("baseDir=" + baseDir);

        if (!baseDir.isAbsolute()) {
            baseDir = fs.getPath(System.getProperty("user.dir")).resolve(sp);
        }
        if (!Files.exists(baseDir)) {
            throw new TemplateModelException(
                    VARIABLE_NAME_BASEDIR + " \"" + sp + "\" does not exist."
            );
        }
        Path file = baseDir.resolve(pathParam);
        if (!Files.exists(file)) {
            throw new TemplateModelException(
                    "file \"" + file.toString() + "\" does not exist."
            );
        }

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);

        if (body != null) {
            for (String line : lines) {
                if (loopVars.length > 0) {
                    loopVars[0] = new SimpleScalar(line);
                }
                body.render(env.getOut());
            }
        }
    }
}
