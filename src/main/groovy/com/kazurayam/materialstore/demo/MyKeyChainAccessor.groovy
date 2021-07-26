package com.kazurayam.materialstore.demo

class MyKeyChainAccessor {

    MyKeyChainAccessor() {}

    String findPassword(String server, String account) {
        Objects.requireNonNull(server)
        Objects.requireNonNull(account)

        List<String> command = [
                "/usr/bin/security", "find-internet-password",
                "-s", server, "-a", account, "-w"]
        //println "command:${command}"
        Subprocess.CompletedProcess cp = new Subprocess().process(command)
        assert cp.getReturnCode() == 0
        //println "stdout:${cp.getStdout()}"
        //println "stderr:${cp.getStderr()}"
        if (cp.getStdout().size() > 0) {
            return cp.getStdout().get(0)
        } else {
            return null
        }
    }

}
