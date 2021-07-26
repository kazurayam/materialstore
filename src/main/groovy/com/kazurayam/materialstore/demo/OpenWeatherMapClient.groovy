package com.kazurayam.materialstore.demo

class OpenWeatherMapClient {

    OpenWeatherMapClient() {
    }

    void execute() {
        println "Hello, OpenWeatherMap!"
    }

    private static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().startsWith("mac")
    }

    static void main(String[] args) {
        OpenWeatherMapClient client = new OpenWeatherMapClient()
        client.execute()
    }
}
