package com.kazurayam.materialstore.demo

import com.kazurayam.subprocessj.CompletedProcess
import com.kazurayam.subprocessj.Subprocess

class MyKeyChainAccessor {

    MyKeyChainAccessor() {}

    String findPassword(String server, String account) {
        Objects.requireNonNull(server)
        Objects.requireNonNull(account)

        List<String> command = [
                "/usr/bin/security", "find-internet-password",
                "-s", server, "-a", account, "-w"]
        //println "command:${command}"
        CompletedProcess cp = new Subprocess().run(command)
        assert cp.returncode() == 0
        //println "stdout:${cp.stdout()}"
        //println "stderr:${cp.stderr()}"
        if (cp.stdout().size() > 0) {
            return cp.stdout().get(0)
        } else {
            return null
        }
    }

}
