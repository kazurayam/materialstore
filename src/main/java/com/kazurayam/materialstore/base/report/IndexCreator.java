package com.kazurayam.materialstore.base.report;

import com.kazurayam.materialstore.core.FileSystemFactory;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.Jsonifiable;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.TemplateReady;
import com.kazurayam.materialstore.util.JsonUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.Collections;
import java.util.Comparator;
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

    Logger logger = LoggerFactory.getLogger(IndexCreator.class);

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
                "/com/kazurayam/materialstore/base/reduce/differ/style.css"));
        ReportFileList rfl = new ReportFileList(store);

        Map<String, Object> model = Collections.emptyMap();
        try {
            model = rfl.toTemplateModel();
        } catch (Exception e) {
            logger.warn("rtf.toString(): " + rfl.toString());
        }
        dataModel.put("title", makeTitle(model));
        dataModel.put("model", model);

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

    public String makeTitle(Map<String, Object> model) throws IOException {
        String s = (String)model.get("store");
        Path parent = FileSystemFactory.newFileSystem().getPath(s);
        return parent.getFileName().toString() + "/index.html";
    }

    static class ReportFileList implements TemplateReady {
        private final Store store;
        public ReportFileList(Store store) {
            this.store = store;
        }
        public Store getStore() { return this.store; }
        public List<ReportFile> getFiles() throws IOException {
            try (Stream<Path> stream = Files.list(store.getRoot())) {
                return stream
                        .filter(p -> !Files.isDirectory(p))
                        .filter(p -> p.getFileName().toString().endsWith(".html"))
                        .filter(p -> ! p.getFileName().toString().equals("index.html"))
                        .map(ReportFile::new)
                        // sort the report files by the lastModified value in descending order
                        .sorted(new ReportFileComparatorByLastModified().reversed())
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
            sb.append("{")
                    .append("\"store\":\"")
                    .append(JsonUtil.escapeAsJsonString(store.getRoot().toString()))
                    .append("\"")
                    .append(",")
                    .append("\"files\":[");
            try {
                for (int i = 0; i < this.getFiles().size(); i++) {
                    ReportFile rf = this.getFiles().get(i);
                    if (i > 0) { sb.append(","); }
                    sb.append(rf.toJson());
                }
            } catch (IOException e) {
                sb.append("\"")
                        .append(JsonUtil.escapeAsJsonString(e.getMessage()))
                        .append("\"");
            }
            sb.append("]")
                    .append("}");
            return sb.toString();
        }
    }

    static class ReportFile implements Jsonifiable {
        private final Path path;
        private final DateTimeFormatter DATE_TIME_FORMATTER = JobTimestamp.FORMATTER;
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
            return DATE_TIME_FORMATTER.format(ldt);
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
                    .append("\"fileName\":\"")
                    .append(JsonUtil.escapeAsJsonString(this.getFileName()))
                    .append("\"")
                    .append(",")
                    .append("\"lastModified\":\"")
                    .append(JsonUtil.escapeAsJsonString(this.getDateTimeLastModified()))
                    .append("\"")
                    .append("}");
            return sb.toString();
        }
    }
    static class ReportFileComparatorByLastModified implements Comparator<ReportFile> {
        @Override
        public int compare(ReportFile first, ReportFile second) {
            return first.getDateTimeLastModified()
                    .compareTo(second.getDateTimeLastModified());
        }
    }
}
