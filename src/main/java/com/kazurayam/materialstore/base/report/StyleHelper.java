package com.kazurayam.materialstore.base.report;

import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public final class StyleHelper {
    private StyleHelper() {
    }

    /**
     * The "style.css" file is generated from the "style.scss" file.
     * The SCSS file is located in the src/main/resources/com/kazurayam/materialstore/reporter directory.
     * This method loads the style.css file from the runtime CLASSPATH.
     * <p>
     * The SCSS file is compiled by the "Node scss" module and driven by IntelliJ IDEA + File Watcher.
     * See https://www.jetbrains.com/help/idea/transpiling-sass-less-and-scss-to-css.html#less_sass_scss_compiling_to_css
     *
     * @return a css content which should be embedded in the HTML file generated
     * by MaterialListReporterImplMB and MProductGroupReporterImplMB
     * @throws MaterialstoreException when filed to load the css
     */
    public static String loadStyleFromClasspath() throws MaterialstoreException {
        return loadStyleFromClasspath(CSS_PATH);
    }

    public static String loadStyleFromClasspath(final String cssPath) throws MaterialstoreException {
        Objects.requireNonNull(cssPath);
        InputStream inputStream = StyleHelper.class.getResourceAsStream(cssPath);
        if (inputStream != null) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                }
                return sb.toString();
            } catch (IOException e) {
                throw new MaterialstoreException(e);
            }
        } else {
            throw new MaterialstoreException("unable to load " + cssPath);
        }

    }

    public static String getCSS_PATH() {
        return CSS_PATH;
    }

    private static final String CSS_PATH = "/com/kazurayam/materialstore/base/report/style.css";
}
