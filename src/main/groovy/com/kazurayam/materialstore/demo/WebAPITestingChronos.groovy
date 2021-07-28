package com.kazurayam.materialstore.demo

import com.kazurayam.materialstore.MaterialstoreException
import com.kazurayam.materialstore.store.DiffArtifacts
import com.kazurayam.materialstore.store.FileType
import com.kazurayam.materialstore.store.JobName
import com.kazurayam.materialstore.store.JobTimestamp
import com.kazurayam.materialstore.store.Material
import com.kazurayam.materialstore.store.Metadata
import com.kazurayam.materialstore.store.MetadataPattern
import com.kazurayam.materialstore.store.Store
import com.kazurayam.materialstore.store.Stores

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import groovy.json.JsonOutput

class WebAPITestingChronos {

    private Path root_ = Paths.get("./build/tmp/demoOutput/${WebAPITestingChronos.class.getSimpleName()}/Materials")

    private static void initDir(Path dir) {
        Files.createDirectories(dir)
    }

    void execute() {
        initDir(root_)
        Store store = Stores.newInstance(root_)
        JobName jobName = new JobName("WebAPITestingChronos")
        JobTimestamp currentTimestamp = JobTimestamp.now()

        // make Web API request to OpenWeatherMap
        // to get a weather forecast data in JSON,
        // save the data into the Materials directory
        doOpenWeatherAction(store, jobName, currentTimestamp, ["id": "498817"])
        doOpenWeatherAction(store, jobName, currentTimestamp, ["q": "Hachinohe"])

        // retrieve a previous weather data of the target city
        JobTimestamp baseTimestamp = currentTimestamp.minusSeconds(1)
        JobTimestamp previousTimestamp = store.findJobTimestampPriorTo(jobName, baseTimestamp)
        if (previousTimestamp == JobTimestamp.NULL_OBJECT) {
            throw new MaterialstoreException(
                    "JobTimestamp prior to ${baseTimestamp} is not found. We will quit.")
        }
        List<Material> previousData = store.select(jobName, previousTimestamp)

        // retrieve the current weather data of the target city
        List<Material> currentData  = store.select(jobName, currentTimestamp)

        // make diff
        DiffArtifacts stuffedDiffArtifacts =
                store.makeDiff(previousData, currentData)

        int countWarnings = stuffedDiffArtifacts.countWarnings(0.0d)
        println "countWarnings: ${countWarnings}"

        // compile HTML report
        Path file = store.reportDiffs(jobName, stuffedDiffArtifacts, "index.html")
        println "output: ${file.toString()}"
    }

    private static void doOpenWeatherAction(Store store,
                                     JobName jobName,
                                     JobTimestamp jobTimestamp,
                                     Map<String, String> param) {
        // make query for data to OpenWeatherMap
        OpenWeatherMapClient client = new OpenWeatherMapClient()
        String weatherData = client.getOpenWeatherData(param)

        // save the data into the store
        Metadata metadata = new Metadata(param)
        store.write(jobName, jobTimestamp, FileType.JSON,
                metadata,
                JsonOutput.prettyPrint(weatherData))
    }

    /**
     *
     * @param args
     */
    static void main(String[] args) {
        try {
            WebAPITestingChronos instance = new WebAPITestingChronos()
            instance.execute()
        } catch (Exception e) {
            e.printStackTrace()
        }
    }
}
