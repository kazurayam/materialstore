package com.kazurayam.materialstore.report;

import com.kazurayam.materialstore.filesystem.Jsonifiable;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.util.JsonUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class creates the `store/index.html` file.
 * The index html presents the list of html files contained in the `store` directory.
 */
public class IndexCreator {

    private final Store store;
    private static String TEMPLATE_PATH =
            "com/kazurayam/materialstore/report/IndexCreatorTemplate.ftlh";

    private final Configuration cfg;

    public IndexCreator(Store store) throws MaterialstoreException {
        Objects.requireNonNull(store);
        this.store = store;
        this.cfg = FreeMarkerConfigurator.configureFreeMarker(store);
    }

    public Path create() throws MaterialstoreException, IOException {
        /* create a data-model */
        Map<String, Object> dataModel = new LinkedHashMap<>();
        dataModel.put("style", StyleHelper.loadStyleFromClasspath());
        dataModel.put("style2", StyleHelper.loadStyleFromClasspath(
                "/com/kazurayam/materialstore/reduce/differ/style.css"));
        dataModel.put("title", "store/index.html");
        dataModel.put("model", createModel(store));

        /* Get the template */
        Template template = null;
        try {
            template = cfg.getTemplate(TEMPLATE_PATH);
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }

        /* Merge data-model with the template */
        Writer sw = new StringWriter();
        try {
            template.process(dataModel, sw);
            sw.flush();
            sw.close();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }

        String html;

        /* pretty print the HTML using jsonp if required */
        Document doc = Jsoup.parse(sw.toString(), "", Parser.htmlParser());
        doc.outputSettings().indentAmount(2);
        html = doc.toString();

        Path filePath = store.getRoot().resolve("index.html");

        try {
            Files.write(filePath,
                    html.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }

        return filePath;
    }

    Map<String, Object> createModel(Store store) throws IOException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("reports", new ReportFileList(store));
        return m;
    }

    static class ReportFileList implements Jsonifiable {
        private final Store store;
        public ReportFileList(Store store) {
            this.store = store;
        }
        public List<ReportFile> getList() throws IOException {
            try (Stream<Path> stream = Files.list(store.getRoot())) {
                return stream
                        .filter(p -> !Files.isDirectory(p))
                        .filter(p -> p.getFileName().toString().endsWith(".html"))
                        .filter(p -> ! p.getFileName().toString().equals("index.html"))
                        .map(ReportFile::new)
                        .collect(Collectors.toList());
            }
        }
        @Override
        public String toString() {
            return this.toJson(true);
        }

        @Override
        public String toJson(boolean prettyPrint) {
            if (prettyPrint) {
                return JsonUtil.prettyPrint(this.toJson());
            } else {
                return this.toJson();
            }
        }

        @Override
        public String toJson(){
            StringBuilder sb = new StringBuilder();
            sb.append("{").append("\"store\":\"")
                    .append(store.getRoot().toString()).append("\"")
                    .append(",")
                    .append("\"reports\":[");
            try {
                for (int i = 0; i < this.getList().size(); i++) {
                    ReportFile rf = this.getList().get(i);
                    if (i > 0) { sb.append(","); }
                    sb.append(rf.toJson());
                }
            } catch (IOException e) {
                sb.append("\"").append(e.getMessage()).append("\"");
            }
            sb.append("]")
                    .append("}");
            return sb.toString();
        }
    }

    static class ReportFile implements Jsonifiable {
        private final Path path;
        private final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        public ReportFile(Path path) {
            this.path = path;
        }
        public String getFileName() {
            return path.getFileName().toString();
        }
        public String getDateTimeLastModified() {
            long lastModified = path.toFile().lastModified();
            ZoneId zone = ZoneId.systemDefault();
            LocalDateTime ldt =
                    Instant.ofEpochMilli(lastModified)
                            .atZone(zone)
                            .toLocalDateTime();
            return DATETIME_FORMATTER.format(ldt);
        }
        @Override
        public String toString() {
            return this.toJson();
        }
        @Override
        public String toJson(boolean prettyPrint) {
            if (prettyPrint) {
                return JsonUtil.prettyPrint(this.toJson());
            } else {
                return this.toJson();
            }
        }
        @Override
        public String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{")
                    .append("\"fileName\":\"" + this.getFileName() + "\"")
                    .append(",")
                    .append("\"lastModified\":\"" + this.getDateTimeLastModified() + "\"")
                    .append("}");
            return sb.toString();
        }
    }
}
