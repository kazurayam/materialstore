package com.kazurayam.materialstore.demo

class OpenWeatherMapClient {

    OpenWeatherMapClient() {
    }

    void execute() {
        String apiKey = new MyKeyChainAccessor().findPassword(
                "home.openweathermap.org/api_keys", "myFirstKey")
        println "apiKey:${apiKey}"

    }

    /**
     *
     * @param args
     */
    static void main(String[] args) {
        OpenWeatherMapClient client = new OpenWeatherMapClient()
        client.execute()
    }
}
