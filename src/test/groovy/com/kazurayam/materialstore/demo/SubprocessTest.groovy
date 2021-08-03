package com.kazurayam.materialstore.demo

import org.junit.jupiter.api.Test
import java.util.stream.Collectors
import static org.junit.jupiter.api.Assertions.*

import com.kazurayam.subprocessj.Subprocess
import com.kazurayam.subprocessj.CompletedProcess

class SubprocessTest {

    @Test
    void test_ls() {
        CompletedProcess cp =
                new Subprocess().run(["sh", "-c", "ls"])
        assertEquals(0, cp.returncode())
        //println "stdout: ${cp.stdout()}"
        //println "stderr: ${cp.stderr()}"
        assertTrue(cp.stdout().size() > 0)
        assertTrue(cp.stdout().contains("src"))
    }

    @Test
    void test_date() {
        CompletedProcess cp =
                new Subprocess().run(["/bin/date"])
        assertEquals(0, cp.returncode())
        //println "stdout: ${cp.stdout()}"
        //println "stderr: ${cp.stderr()}"
        assertTrue(cp.stdout().size() > 0)
        /*
        assertTrue(cp.stdout().stream()
                .filter { line ->
                    line.contains("2021")
                }.collect(Collectors.toList()).size() > 0)
         */
    }

    @Test
    void test_git() {
        CompletedProcess cp =
                new Subprocess().run(["/usr/local/bin/git", "status"])
        assertEquals(0, cp.returncode())
        //println "stdout: ${cp.stdout()}"
        //println "stderr: ${cp.stderr()}"
        assertTrue(cp.stdout().size() > 0)
        assertTrue(cp.stdout().stream()
                .filter { line ->
                    line.contains("On branch")
                }.collect(Collectors.toList()).size() == 1)

    }
}
