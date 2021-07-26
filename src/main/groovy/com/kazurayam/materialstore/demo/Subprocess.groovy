package com.kazurayam.materialstore.demo

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

/**
 * This Subprocess class execute a OS commands in a forked OS sub-process.
 * It returns a combination of
 * - the return code from the OS command executed. 0 if successfule.
 * - the list of strings as stdout.
 * - the list of strings as stderr.
 *
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
            builder.directory(this.dir)
            builder.command(command)
            Process process = builder.start()
            CompletedProcess cp = new CompletedProcess(command)

            // https://www.baeldung.com/java-executor-wait-for-threads
            ExecutorService threadPool = Executors.newFixedThreadPool(2)
            List<Callable<String>> callables = Arrays.asList(
                    new StreamGobbler(
                            process.getInputStream(),
                            { String s ->
                                //println "stdout: ${s}"
                                cp.appendStdout(s)
                            }
                    ),
                    new StreamGobbler(
                            process.getErrorStream(),
                            { String e ->
                                //println "stderr: ${e}"
                                cp.appendStderr(e)
                            }
                    )
            )
            // start the threads to consume the stdout & stderror out of the subprocess
            List<Future<String>> futures = threadPool.invokeAll(callables)

            // execute the subprocess
            int returnCode = process.waitFor()

            // wait for the threads to finish
            awaitTerminationAfterShutdown(threadPool)

            // now we are surely done
            cp.setReturnCode(returnCode)
            return cp

        } catch (Exception e) {
            e.printStackTrace()
        }
    }
    private static void awaitTerminationAfterShutdown(ExecutorService threadPool) {
        threadPool.shutdown()
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow()
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }

    /**
     *
     */
    class CompletedProcess {
        private final List<String> args
        private int returnCode
        private final List<String> stdout
        private final List<String> stderr
        CompletedProcess(List<String> args) {
            this.args = args
            this.returnCode = -999
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

    private static class StreamGobbler implements Callable<String> {
        private InputStream inputStream
        private Consumer<String> consumer
        StreamGobbler(InputStream inputStream,
                      Consumer<String> consumer) {
            this.inputStream = inputStream
            this.consumer = consumer
        }
        @Override
        String call() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer)
            return "done"
        }
    }

}
