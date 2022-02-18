package com.kazurayam.materialstore.reporter

class ReporterHelper {

    static final String CSS_PATH = "/com/kazurayam/materialstore/reporter/style.css"

    private ReporterHelper() {}

    /**
     * The "style.css" file is generated from the "style.scss" file.
     * The SCSS file is located in the src/main/resources/com/kazurayam/materialstore/reporter directory.
     * This method loads the style.css file from the runtime CLASSPATH.
     *
     * The SCSS file is compiled by the "Node scss" module and driven by IntelliJ IDEA + File Watcher.
     * See https://www.jetbrains.com/help/idea/transpiling-sass-less-and-scss-to-css.html#less_sass_scss_compiling_to_css
     *
     * @return a css content which should be embeded in the HTML file generated
     * by MaterialsBasicReporter and DiffArtifactGroupBasicReporter
     */
    static String loadStyleFromClasspath() {
        InputStream inputStream = ReporterHelper.class.getResourceAsStream(CSS_PATH)
        if (inputStream != null) {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(inputStream, "UTF-8"))
            StringBuilder sb = new StringBuilder()
            String line
            while ((line = br.readLine()) != null) {
                sb.append(line)
                sb.append(System.lineSeparator())
            }
            return sb.toString()
        } else {
            throw new IllegalArgumentException("unabled to load ${CSS_PATH}")
        }
    }
}
