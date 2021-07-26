package com.kazurayam.materialstore.demo

import java.util.concurrent.Executors
import java.util.function.Consumer

/**
 * based on the Baeldung article
 * https://www.baeldung.com/run-shell-command-in-java
 *
 * also referred to Python's Subprocess
 */
class Subprocess {

    File dir = new File(".")

    Subprocess() {}

    Subprocess setCurrentDir(File dir) {
        this.dir =  dir
        return this
    }

    CompletedProcess process(List<String> command) {
        Objects.requireNonNull(command)
        try {
            ProcessBuilder builder = new ProcessBuilder()
            // TODO should enable setting the directory
            builder.directory(this.dir)
            builder.command(command)
            Process process = builder.start()
            CompletedProcess cp = new CompletedProcess(command)
            StreamGobbler stdoutGobbler =
                    new StreamGobbler(
                            process.getInputStream(),
                            { String s ->
                                //println "stdout: ${s}"
                                cp.appendStdout(s)
                            }
                    )
            Executors.newSingleThreadExecutor().submit(stdoutGobbler)
            StreamGobbler stderrGobbler =
                    new StreamGobbler(
                            process.getErrorStream(),
                            { String e ->
                                //println "stderr: ${e}"
                                cp.appendStderr(e)
                            }
                    )
            Executors.newSingleThreadExecutor().submit(stderrGobbler)
            int returnCode = process.waitFor()
            cp.setReturnCode(returnCode)
            return cp
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    class CompletedProcess {
        private final List<String> args
        private int returnCode
        private List<String> stdout
        private List<String> stderr
        CompletedProcess(List<String> args) {
            this.args = args
            this.returnCode = 0
            this.stdout = new ArrayList<String>()
            this.stderr = new ArrayList<String>()
        }
        void appendStdout(String line) {
            stdout.add(line)
        }
        void appendStderr(String line) {
            stderr.add(line)
        }
        void setReturnCode(int v) {
            this.returnCode = v
        }
        int getReturnCode() {
            return this.returnCode
        }
        List<String> getStdout() {
            return this.stdout
        }
        List<String> getStderr() {
            return this.stderr
        }
    }

    private static class StreamGobbler implements Runnable {
        private InputStream inputStream
        private Consumer<String> consumer
        StreamGobbler(InputStream inputStream,
                      Consumer<String> consumer) {
            this.inputStream = inputStream
            this.consumer = consumer
        }
        @Override
        void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer)
        }
    }

}
