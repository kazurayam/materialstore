package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.Store;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

public class MaterializingWebResourceFunctions {

    private static final Logger logger =
            LoggerFactory.getLogger(MaterializingWebResourceFunctions.class);

    /**
     * get the URL, store the content into the store.
     *
     * Use Apache HTTPComponents HttpClient
     * see the original code at
     * https://github.com/apache/httpcomponents-client/blob/5.1.x/httpclient5/src/test/java/org/apache/hc/client5/http/examples/ClientWithResponseHandler.java
     */
    public static MaterializingWebResourceFunction<Target, StorageDirectory>
            storeWebResource = (target, storageDirectory) -> {
        Objects.requireNonNull(target);
        Objects.requireNonNull(storageDirectory);
        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
            final HttpGet httpget = new HttpGet(target.getUrl().toString());
            logger.debug("[storeHttpResource] " + "Executing request " +
                    httpget.getMethod() + " " + httpget.getUri());
            // Create a custom response handler
            final HttpClientResponseHandler<DigestedResponse> responseHandler =
                    response -> {
                        final int status = response.getCode();
                        if (status >= HttpStatus.SC_SUCCESS && status < HttpStatus.SC_REDIRECTION) {
                            final HttpEntity entity = response.getEntity();
                            if (entity != null) {
                                DigestedResponse digested = new DigestedResponse(EntityUtils.toByteArray(entity));
                                Header contentType = response.getHeader("Content-Type");
                                if (contentType != null) {
                                    digested.setContentType(contentType);
                                }
                                return digested;
                            } else {
                                return null;
                            }
                        } else {
                            throw new ClientProtocolException("Unexpected response status: " + status);
                        }
                    };
            final DigestedResponse myResponse = httpclient.execute(httpget, responseHandler);
            Store store = storageDirectory.getStore();
            JobName jobName = storageDirectory.getJobName();
            JobTimestamp jobTimestamp = storageDirectory.getJobTimestamp();
            FileType fileType = FileType.ofMediaType(myResponse.getMediaType());
            Metadata metadata = Metadata.builder(target.getUrl())
                    .putAll(target.getParameters()).build();
            byte[] bytes = myResponse.getContent();
            store.write(jobName, jobTimestamp, fileType, metadata, bytes);
        } catch (IOException | URISyntaxException e) {
            throw new MaterialstoreException(e);
        }
    };

    private MaterializingWebResourceFunctions() {}
}
