package com.kazurayam.materialstore.demo

import groovy.json.JsonSlurper

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class MacKeyChainWrapper {

    private MacKeyChainWrapper() {}


    static Map<String, String> loadCredential(String service) {
        try {
            Path config = Paths.get("credentials.json")
            if (! Files.exists(config)) {
                throw new IllegalArgumentException("credentials.json file is not present")
            }
            JsonSlurper slurper = new JsonSlurper()
            def creds = slurper.parse(config.toFile())

            def owm = creds[service]
            assert owm != null, "service:\"${service}\" is not found in the ${config}"

            Map m = new HashMap()
            assert owm["server"] != null, "server is not defined"
            m.put("server", owm["server"])
            assert owm["account"] != null, "account is not defined"
            m.put("account", owm["account"])
            return m
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    static String findPassword(String server, String account) {
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
        assert cp.getStdout().size() > 0
        return cp.getStdout().get(0)
    }

}
