package com.kazurayam.materialstore.demo

import groovy.json.JsonOutput
import org.apache.http.NameValuePair
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils

class OpenWeatherMapClient {

    private final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast"
    private String API_KEY

    private CloseableHttpClient client
    private List<NameValuePair> baseParameters

    OpenWeatherMapClient() {
        String API_KEY = new MyKeyChainAccessor().findPassword(
                "home.openweathermap.org/api_keys",
                "myFirstKey")
        //println "API_KEY: ${API_KEY}"
        client = HttpClientBuilder.create().build()
        baseParameters = new ArrayList<NameValuePair>()
        baseParameters.add(new BasicNameValuePair("appid", API_KEY))
    }

    void execute() {
        String json = getWeatherData()
        println new JsonOutput().prettyPrint(json)
    }

    private String getWeatherData() {
        HttpGet httpGet = new HttpGet(BASE_URL)
        URI uri = new URIBuilder(httpGet.getURI())
                .addParameters(baseParameters)
                .addParameter("id", "524901")
                .build()
        ((HttpRequestBase)httpGet).setURI(uri)
        CloseableHttpResponse response = client.execute(httpGet)
        assert response.getStatusLine().getStatusCode() == 200
        String json = EntityUtils.toString(response.getEntity(), "UTF-8")
        client.close()
        return json
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
