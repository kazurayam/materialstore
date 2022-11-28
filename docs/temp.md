-   <a href="#_materialstore_documentation" id="toc-_materialstore_documentation">Materialstore Documentation</a>
    -   <a href="#_materialstore_tutorial" id="toc-_materialstore_tutorial">Materialstore Tutorial</a>
        -   <a href="#_setting_up_a_project" id="toc-_setting_up_a_project">Setting up a project</a>
        -   <a href="#_the_first_test_hello_materialstore" id="toc-_the_first_test_hello_materialstore">The first test : "Hello, materialstore!"</a>
        -   <a href="#_understanding_the_basics" id="toc-_understanding_the_basics">Understanding the basics</a>
            -   <a href="#_create_a_base_directory_for_output" id="toc-_create_a_base_directory_for_output">create a base directory for output</a>
            -   <a href="#_create_the_store_directory" id="toc-_create_the_store_directory">create the "store" directory</a>
            -   <a href="#_instantiate_the_store_object" id="toc-_instantiate_the_store_object">instantiate the "store" object</a>
            -   <a href="#_create_a_jobname_object" id="toc-_create_a_jobname_object">create a JobName object</a>
            -   <a href="#_create_a_jobtimestamp_object" id="toc-_create_a_jobtimestamp_object">create a JobTimestamp object</a>
            -   <a href="#_create_a_file_tree_under_the_store_write_a_material_into_it" id="toc-_create_a_file_tree_under_the_store_write_a_material_into_it">create a file tree under the "store", write a material into it</a>
            -   <a href="#_the_file_name_of_material" id="toc-_the_file_name_of_material">the file name of "material"</a>
            -   <a href="#_the_file_name_extension" id="toc-_the_file_name_extension">the file name extension</a>
            -   <a href="#_metadata" id="toc-_metadata">Metadata</a>
            -   <a href="#_store_write_method_can_accept_many_types_of_objects_to_write" id="toc-_store_write_method_can_accept_many_types_of_objects_to_write">"store.write()" method can accept many types of objects to write</a>

# Materialstore Documentation

-   [materialstore javadoc](https://kazurayam.github.io/materialstore/api/index.html)

-   back to the [repository](https://github.com/kazurayam/materialstore)

## Materialstore Tutorial

Let us begin with a quick introduction to a Java library named "materialstore".

### Setting up a project

Here I assume you have a seasoned programming skill in Java, and you have installed the build tool [Gradle](https://gradle.org/install/). Now let us create a project where you write some Java code for practice.

You would want to initialize it as a Gradle application project. Let me assume the project is named as "sampleProject".

    $ cd ~/tmp/
    $ mkdir sampleProject

You would operate in the console as this:

    $ cd ~/tmp/sampleProject
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

You will also find a file `sampleProject/build.gradle` file, but it will be empty (comments only). So you want to edit it, like this.

    plugins {
        id 'java'
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation group: 'com.kazurayam', name: 'materialstore', version: '0.12.5'
        testImplementation 'org.junit.jupiter:junit-jupiter-api:5.9.0'
        testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.9.0'
    }

    test {
        useJUnitPlatform()
    }

Please note that here you declared the dependency to the `materialstore` library.

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

### The first test : "Hello, materialstore!"

I have created a JUnit-based code that uses the materialstore library. See `sampleProject/src/test/java/my/sample/T1HelloMaterialstoreTest.java`:

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
            System.out.println(String.format("wrote a text '%s'", text));
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

I can ran this test by running Gradle’s `test` task, as this:

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

You can open the `index.html` in browser and see the test result.

![01 test report](images/tutorial/01_test_report.png)

### Understanding the basics

The test will result a new file tree, like this:

![02 test output file tree](images/tutorial/02_test_output_file_tree.png)

Let us read the source of the "Hello, materialstore!" test line by line to understand the fundamentals of the materialstore library. Here I assume that you are a well-trained Java programmer who needs no explanation about JUnit how-to.

#### create a base directory for output

    import java.nio.file.Path;
    ...
        @BeforeEach
        public void beforeEach() {
            Path dir = createTestClassOutputDir(this);   // (1)

The statement commented as (1) creates a directory `build/tmp/testOutput/<fully qualified test case class name>`. In this directory the test will output everything during its run. The helper method `createTestClassOutputDir(Object)` is defined later in the source file.

#### create the "store" directory

            Path storeDir = dir.resolve("store");   // (2)

The statement (2) declares a `java.nio.file.Path` object named `store` under the working directory `build` which is created at (1).

#### instantiate the "store" object

    import com.kazurayam.materialstore.core.filesystem.Store;
    ...

        private Store store;
    ...

            store = Stores.newInstance(storeDir);        // (3)

The statement (3) instantiates an object of `com.kazurayam.materialstore.core.filesystem.Store` class. The directory `store` is actually created by the statement (3).

The `Store` class is the central entry point of the materialstore library. The `Store` class implements methods to write the materials into the file tree. Also the `Store` class implements methods to select (read, retrieve) one or more materials out of the store.

#### create a JobName object

    import com.kazurayam.materialstore.core.filesystem.JobName;
    ...
        @Test
        public void test01_hello_materialstore() throws MaterialstoreException {
            JobName jobName =
                    new JobName("test01_hello_materialstore");       // (4)

The statement (4) declares the name of a sub-directory under the `store` directory. The String value specified for the constructor of `com.kazurayam.materialstore.core.filesystem.JobName` class can be any. It is just a directory name; no deep semantic meaning is enforced.

However, you should remember that some of ASCII characters are prohibited as a part of file/directory names by the underlying OS; therefore you can not use them as the `JobName` object’s value. For example, Windows OS does not allow you to use the following characters:

-   `<` (less than)

-   `>` (greater than)

-   `:` (colon)

-   `"` (double quote)

-   `/` (forward slash)

-   `\` (backslash)

-   `|` (vertical bar or pipe)

-   `?` (question mark)

-   `*` (asterisk)

You can use non-latin characters as JobName. JobName can contain white spaces if necessary. For example, you can write:

        JobName jobName = new JobName("わたしの仕事 means my job");

#### create a JobTimestamp object

    import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
    ...
            JobTimestamp jobTimestamp = JobTimestamp.now();          // (5)

The statement (5) declares the name of a new directory under the `JobName` directory, which will have a name as current timestamp. The name will be in the format of `uuuuMMdd_hhmmss` (year, month, day, hours, minutes, seconds).

#### create a file tree under the "store", write a material into it

    import com.kazurayam.materialstore.core.filesystem.FileType;
    import com.kazurayam.materialstore.core.filesystem.Material;
    import com.kazurayam.materialstore.core.filesystem.Metadata;
    ...
            String text = "Hello, materialstore!";
            Material material = store.write(jobName, jobTimestamp,   // (6)
                    FileType.TXT,                            // (7)
                    Metadata.NULL_OBJECT,                    // (8)
                    text);                                   // (9)

The lines (6) to (9) creates a file tree under the \`store\`directory, like this:

    $ tree build/tmp/testOutput/my.sample.T1HelloMaterialstoreTest/store/
    build/tmp/testOutput/my.sample.T1HelloMaterialstoreTest/store/
    └── test01_hello_materialstore
        └── 20221128_082216
            ├── index
            └── objects
                └── 4eb4efec3324a630e0d3d96e355261da638c8285.txt

The format of file tree under the `store` directory is specially designed to save the **materials**. The tree format is fixed. You are not supposed to customize it at all. You would delegate all tasks of creating + naming + locating files and directories under the `store` directory to the `Store` object.

As the line commented as (6) tells, a "material" (actually, is a file) is always located under the sub-tree `store/<JobName>/<JobTimestamp>/objects`.

The sub-directory named `objects` will contain one or more files.

#### the file name of "material"

All files under the `objects` have a fixed format of file name, that is:

**&lt;40 characters in alpha-numeric, calcurated by the SHA1 hash function&gt;.&lt;file extention&gt;**

for example,

`4eb4efec3324a630e0d3d96e355261da638c8285.txt`

Ths `Store#write()` method call produces the leading 40 characters using the [SHA1](https://en.wikipedia.org/wiki/SHA-1) message digest function taking the byte array of the file content as the input. This cryptic 40 characters uniquely identifies the input files regardless which type of the file content: a plain text, CSV, HTML, JSON, XML, PNG image, PDF, zipped archive, MS Excel’s xlsx, etc. This 20 characters is called `ID` of a material. Because the ID of material is calculated from the file content, **you do not need to name the file yourself when you write it into the OS file system.**

#### the file name extension

The line (7) specifies `FileType.TXT`.

                    FileType.TXT,                            // (7)

This gives the file extenstion `txt` to the file. The `com.kazurayam.materialstore.filesystem.FileType` enum declares many concrete FileType instances ready to use. See
<https://kazurayam.github.io/materialstore/api/com/kazurayam/materialstore/core/filesystem/FileType.html> for the complete list. Also you can create your own class that implements `com.kazurayam.materialstore.filesystem.IFileType`. See <https://kazurayam.github.io/materialstore/api/com/kazurayam/materialstore/core/filesystem/IFileType.html>

#### Metadata

You can associate various metadata to each materials. A typical metadata of a screenshot of a web page displayed on browser is the URL string (e.g., "https://www.google.com/?q=selenium"). In our first sample code we do not make use of the Metadata at all. So I wrote a placeholder:

                    Metadata.NULL_OBJECT,                (8)

We will cover how to make full use of Metadata later.

#### "store.write()" method can accept many types of objects to write

The javadoc of the [`Store`](https://kazurayam.github.io/materialstore/api/com/kazurayam/materialstore/core/filesystem/Store.html) shows that it can accept multiple types of object as input to write into the `store`:

-   `byte[]`

-   `java.io.File`

-   `java.nio.file.Path`

-   `java.lang.String`

-   `java.awt.image.BufferedImage`

These types will cover the most of the cases in the automated UI testing.
