package tutorial.filesystem;

import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * The first demonstration how to use the Materialstore Filesystem API.
 *
 * Requires
 */
public class HelloWorld {

    private static final String DEFAULT_OUTPUT_DIR =
            "build/tmp/tutOutput/HelloWorld";

    public static void main(String[] args) throws IOException, MaterialstoreException {
        // initialize the directory into which this method writes files
        String currentDirectory = System.getProperty("user.dir");
        Path outputDir = Paths.get(currentDirectory).resolve(DEFAULT_OUTPUT_DIR);
        if (args.length > 0) {
            outputDir = Paths.get(args[0]);
        }
        initDirectory(outputDir);

        // declare a directory structure "store/jobname/jobtimestamp"
        Path storeDir = outputDir.resolve("store");
        Store store = Stores.newInstance(storeDir);
        JobName jobName = new JobName("sampleJob");
        JobTimestamp jobTimestamp = JobTimestamp.now();

        // declare a metadata for the object
        Map<String, String> metadataSource = new HashMap<String, String>();
        metadataSource.put("friendlyName", "/greeting:to*");
        Metadata metadata = Metadata.builder(metadataSource).build();

        // the body of the object to store
        String content = "hello, world";

        // write an object file into the store
        store.write(jobName, jobTimestamp, FileType.TXT, metadata, content);

        // retrieve a list of objects stored in the directory
        MaterialList materialList = store.select(jobName, jobTimestamp);
        // I know, the directory contains only 1 object
        assert materialList.size() == 1;

        // hold an instance of Material, which is really a logical reference
        // to the object file in the store directory
        Material m = materialList.get(0);

        // print the Material object in JSON format
        System.out.println(m.toJson(true));

        // read the object file out of the store
        byte[] bytes = store.read(m);

        // print it as a string
        if (m.getFileType().equals(FileType.TXT)) {
            System.out.println(new String(bytes));
        }
    }

    private static void initDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            FileUtils.deleteDirectory(dir.toFile());
        }
        Files.createDirectories(dir);
    }
}
