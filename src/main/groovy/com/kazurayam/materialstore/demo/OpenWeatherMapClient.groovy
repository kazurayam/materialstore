package com.kazurayam.materialstore.demo

import com.kazurayam.materialstore.store.FileType
import com.kazurayam.materialstore.store.JobName
import com.kazurayam.materialstore.store.JobTimestamp
import com.kazurayam.materialstore.store.Material
import com.kazurayam.materialstore.store.Metadata
import com.kazurayam.materialstore.store.MetadataPattern
import com.kazurayam.materialstore.store.Store
import com.kazurayam.materialstore.store.Stores
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.http.NameValuePair
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class OpenWeatherMapClient {

    private final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast"

    private final Path materials = Paths.get("./build/tmp/demoOutput/${OpenWeatherMapClient.class.getSimpleName()}/Materials")
    private final Store store
    private final JobName jobName
    private final JobTimestamp jobTimestamp

    OpenWeatherMapClient() {
        // will use the materialstore library to store JSON files
        initDir(materials)
        store = Stores.newInstance(materials)
        jobName = new JobName("weather_forecasts")
        jobTimestamp = JobTimestamp.now()
    }



    void process(Map<String, String> param) {
        try {
            // download JSON from the OpenWeatherMap through API
            String rawJson = getOpenWeatherData(param)

            // store the JSON file into the Material directory
            Metadata metadata = new Metadata(param)
            store.write(jobName, jobTimestamp, FileType.JSON, metadata,
                    new JsonOutput().prettyPrint(rawJson))

            // retrieve the JSON file from the Material directory
            List<Material> materials = store.select(jobName, jobTimestamp,
                    MetadataPattern.create(metadata), FileType.JSON)
            assert materials.size() == 1
            File jsonFile = materials.get(0).toFile(store.getRoot())

            // extract a small portion of weather forecast data
            def jsonObj = new JsonSlurper().parse(jsonFile)

            // println the data to the console
            String listItemStr = JsonOutput.toJson(jsonObj["list"][0])
            println JsonOutput.prettyPrint(listItemStr)

        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    private String getOpenWeatherData(Map<String, String> param) {
        // find API_KEY to access OpenWeatherMap.
        String API_KEY = API_KEY()

        // will use Apache httpclient to interact with the OpenWeatherMap site
        CloseableHttpClient client = HttpClientBuilder.create().build()
        List<NameValuePair> baseParameters = new ArrayList<NameValuePair>()
        baseParameters.add(new BasicNameValuePair("appid", API_KEY))

        List<NameValuePair> nvpList = new ArrayList<NameValuePair>()
        param.keySet().each { key ->
            String value = param.get(key)
            nvpList.add(new BasicNameValuePair(key, value))
        }
        //
        HttpGet httpGet = new HttpGet(BASE_URL)
        URI uri = new URIBuilder(httpGet.getURI())
                .addParameters(baseParameters)
                .addParameters(nvpList)
                .build()
        ((HttpRequestBase)httpGet).setURI(uri)
        CloseableHttpResponse response = client.execute(httpGet)
        assert response.getStatusLine().getStatusCode() == 200
        String json = EntityUtils.toString(response.getEntity(), "UTF-8")
        client.close()
        return json
    }

    private static void initDir(Path dir) {
        if (Files.exists(dir)) {
            // delete the directory to clear out using Java8 API
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .map {it.toFile() }
                    .forEach {it.delete() }
        }
        Files.createDirectories(dir)
    }

    /**
     * will retrieve it from the KeyChain storage of kazurayam's Mac Book Air.
     */
    private static final String API_KEY() {
        String API_KEY = new MyKeyChainAccessor().findPassword(
                "home.openweathermap.org/api_keys",
                "myFirstKey")
        //println "API_KEY: ${API_KEY}"
        assert API_KEY != null
        return API_KEY
    }

    /**
     *
     * @param args
     */
    static void main(String[] args) {
        OpenWeatherMapClient client = new OpenWeatherMapClient()
        client.process(["id": "498817"])    // Saint Petersburg,ru
        client.process(["q": "Hachinohe"])  // Hachinohe,jp
    }
}
