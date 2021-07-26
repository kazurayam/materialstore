package com.kazurayam.materialstore.demo

import org.junit.jupiter.api.Test
import java.util.stream.Collectors
import static org.junit.jupiter.api.Assertions.*

class SubprocessTest {

    @Test
    void test_ls() {
        Subprocess.CompletedProcess cp =
                new Subprocess().process(["sh", "-c", "ls"])
        assertEquals(0, cp.getReturnCode())
        //println "stdout: ${cp.getStdout()}"
        //println "stderr: ${cp.getStderr()}"
        assertTrue(cp.getStdout().size() > 0)
        assertTrue(cp.getStdout().contains("src"))
    }

    @Test
    void test_date() {
        Subprocess.CompletedProcess cp =
                new Subprocess().process(["/bin/date"])
        assertEquals(0, cp.getReturnCode())
        //println "stdout: ${cp.getStdout()}"
        //println "stderr: ${cp.getStderr()}"
        assertTrue(cp.getStdout().size() > 0)
        /*
        assertTrue(cp.getStdout().stream()
                .filter { line ->
                    line.contains("2021")
                }.collect(Collectors.toList()).size() > 0)
         */
    }

    @Test
    void test_git() {
        Subprocess.CompletedProcess cp =
                new Subprocess().process(["/usr/local/bin/git", "status"])
        assertEquals(0, cp.getReturnCode())
        //println "stdout: ${cp.getStdout()}"
        //println "stderr: ${cp.getStderr()}"
        assertTrue(cp.getStdout().size() > 0)
        assertTrue(cp.getStdout().stream()
                .filter { line ->
                    line.contains("On branch")
                }.collect(Collectors.toList()).size() == 1)

    }
}
