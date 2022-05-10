package issues;

import com.kazurayam.subprocessj.Subprocess;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This test executes "dot" command (GraphViz) to generate PNG images from "diagramXXX.dot files.
 */
public class Issue216Test {

    private static final Path dotDir = Paths.get(".").resolve("src/test/dot/issue261");
    private static final Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(Issue216Test.class.getName());

    @BeforeAll
    static void beforeAll() throws IOException {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }
        Files.createDirectories(outputDir);
    }

    @Test
    public void test_generatePNG() throws IOException, InterruptedException {
        List<Path> dots = Files.list(dotDir)
                .filter(p -> p.getFileName().toString().endsWith(".dot"))
                .collect(Collectors.toList());
        for (Path dot : dots) {
            String fileName = dot.getFileName().toString();
            String fileNameBody = fileName.substring(0, fileName.indexOf(".dot"));
            Path png = outputDir.resolve(fileNameBody + ".png");
            Subprocess.CompletedProcess cp;
            cp = new Subprocess().cwd(new File("."))
                    .run(Arrays.asList("dot", "-T", "png", dot.toString(), "-o", png.toString()));
            if (cp.returncode() != 0) {
                cp.stderr().forEach(System.out::println);
            }
        }
    }
}
