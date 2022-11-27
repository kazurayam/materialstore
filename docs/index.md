# Materialstore Documentation

-   [materialstore javadoc](https://kazurayam.github.io/materialstore/api/index.html)

-   back to the [repository](https://github.com/kazurayam/materialstore)

## Materialstore Tutorial

Let us begin with a quick introduction to a Java library named "materialstore".

### Setting up a project

Here I assume you have a seasoned programming skill in Java, and you have installed the build tool [Gradle](https://gradle.org/install/). Now let us create a project where you write some Java code for practice.

You want to create a new project. Let me assume the project is named as "sampleProject". You would want to initialize it as a Gradle application project, as this:

    $ cd ~/tmp/
    $ mkdir sampleProject
    $ cd sampleProject
    $ gradle init

    Select type of project to generate:
      1: basic
      2: application
      3: library
      4: Gradle plugin
    Enter selection (default: basic) [1..4] 1

    Select build script DSL:
      1: Groovy
      2: Kotlin
    Enter selection (default: Groovy) [1..2] 1

    Generate build using new APIs and behavior (some features may change in the next minor release)? (default

    Project name (default: sampleProject):

    > Task :init
    Get more help with your project: Learn more about Gradle by exploring our samples at https://docs.gradle.org/7.4.2/samples

    BUILD SUCCESSFUL in 28s

Then you will find a file `sampleProject/settings.gradle` has been created, which looks like:

    rootProject.name = 'sampleProject'
    include('app')

You will also find a file `sampleProject/build.gradle` file, but it will be empty (comments only). So you want to edit it, like this.

    plugins {
        id 'java'
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation group: 'com.kazurayam', name: 'materialstore', version: '0.12.5'
        testImplementation group: 'org.slf4j', name: 'slf4j-api', version: '1.7.25'
        testImplementation group: 'org.slf4j', name: 'slf4j-simple', version: '1.7.25'
        testImplementation 'org.junit.jupiter:junit-jupiter-api:5.9.0'
        testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.9.0'
    }

    test {
        useJUnitPlatform()
    }

You can check if the project is properly setup by executing a command:

    $ cd ~/sampleProject/
    $ gradle dependencies --configuration testImplementation

    ------------------------------------------------------------
    Root project 'sampleProject'
    ------------------------------------------------------------

    testImplementation - Implementation only dependencies for source set 'test'. (n)
    +--- com.kazurayam:materialstore:0.12.5 (n)
    +--- org.slf4j:slf4j-api:1.7.25 (n)
    +--- org.slf4j:slf4j-simple:1.7.25 (n)
    \--- org.junit.jupiter:junit-jupiter-api:5.9.0 (n)

    (n) - Not resolved (configuration is not meant to be resolved)

    A web-based, searchable dependency report is available by adding the --scan option.

    BUILD SUCCESSFUL in 1s
    1 actionable task: 1 executed

### A test with "materialstore"

Now you want to write the first code that uses the materialstore library. `sampleProject/app/src/test/java/my/sample/T1HelloMaterialstoreTest.java`, as this:

    package my.sample;

    import com.kazurayam.materialstore.core.filesystem.FileType;
    import com.kazurayam.materialstore.core.filesystem.JobName;
    import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
    import com.kazurayam.materialstore.core.filesystem.Material;
    import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
    import com.kazurayam.materialstore.core.filesystem.Metadata;
    import com.kazurayam.materialstore.core.filesystem.Store;
    import com.kazurayam.materialstore.core.filesystem.Stores;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

    import java.io.IOException;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.Paths;

    import static org.junit.jupiter.api.Assertions.assertNotNull;

    /*
     * This code demonstrate how to save a text string into an instance of
     * "materialstore" backed with a directory on the local OS file system.
     */
    public class T1HelloMaterialstoreTest {

        // central abstraction of Material storage
        private Store store;

        private Logger logger = LoggerFactory.getLogger(T1HelloMaterialstoreTest.class);

        @BeforeEach
        public void beforeEach() {
            // create a base directory
            Path dir = createTestClassOutputDir(this);   // (1)
            // create a directory named "store"
            Path storeDir = dir.resolve("store");   // (2)
            // instantiate a Store object
            store = Stores.newInstance(storeDir);        // (3)
        }

        @Test
        public void test01_hello_materialstore() throws MaterialstoreException {
            JobName jobName =
                    new JobName("test01_hello_materialstore");       // (4)
            JobTimestamp jobTimestamp = JobTimestamp.now();          // (5)
            String text = "Hello, materialstore!";
            Material material = store.write(jobName, jobTimestamp,   // (6)
                    FileType.TXT,                            // (7)
                    Metadata.NULL_OBJECT,                    // (8)
                    text);                                   // (9)
            logger.info(String.format("wrote a text '%s'", text));
            assertNotNull(material);
        }

        //-----------------------------------------------------------------

        Path createTestClassOutputDir(Object testClass) {
            Path output = getTestOutputDir()
                    .resolve(testClass.getClass().getName());
            try {
                if (!Files.exists(output)) {
                    Files.createDirectories(output);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return output;
        }

        Path getTestOutputDir() {
            return Paths.get(System.getProperty("user.dir"))
                    .resolve("build/tmp/testOutput");
        }
    }

You would want to run this test by running Gradle test task, as this:

    $ gradle test
    > Task :compileJava NO-SOURCE
    > Task :processResources NO-SOURCE
    > Task :classes UP-TO-DATE
    > Task :compileTestJava
    > Task :processTestResources NO-SOURCE
    > Task :testClasses
    > Task :test

    BUILD SUCCESSFUL in 2s
    2 actionable tasks: 2 executed

The `test` task of Gradle will create a report in HTML format where you can all output from the test execution. Find the `build/reports/tests/test/index.html` file as:

    $ cd ~/tmp/sampleProject
    $ tree build/reports/tests/
    build/reports/tests/
    └── test
        ├── classes
        │   └── my.sample.T1HelloMaterialstoreTest.html
        ├── css
        │   ├── base-style.css
        │   └── style.css
        ├── index.html
        ├── js
        │   └── report.js
        └── packages
            └── my.sample.html

    5 directories, 6 files

You want to open the `index.html` in browser.

![01 test report](images/01_test_report.png)
