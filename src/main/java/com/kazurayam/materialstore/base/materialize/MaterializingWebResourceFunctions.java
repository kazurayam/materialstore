package com.kazurayam.materialstore.base.materialize;

import com.kazurayam.materialstore.core.filesystem.FileType;
import com.kazurayam.materialstore.core.filesystem.FileTypeUtil;
import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.Material;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Metadata;
import com.kazurayam.materialstore.core.filesystem.Store;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public static MaterializingWebResourceFunction<Target, StorageDirectory, Material>
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
            FileType fileType = FileTypeUtil.ofMimeType(myResponse.getMediaType());
            Metadata metadata = Metadata.builder(target.getUrl())
                    .putAll(target.getAttributes()).build();
            byte[] bytes = myResponse.getContent();
            return store.write(jobName, jobTimestamp, fileType, metadata, bytes);
        } catch (IOException | URISyntaxException e) {
            throw new MaterialstoreException(e);
        }
    };

    private MaterializingWebResourceFunctions() {}



    /**
     *
     */
    static final class DigestedResponse {

        private static final Logger logger = LoggerFactory.getLogger(DigestedResponse.class);

        private final byte[] content;
        private String mediaType;
        private String charset;

        public DigestedResponse(byte[] content) {
            this.content = content;
            this.mediaType = null;
            this.charset = null;
        }

        public void setMediaType(String mediaType) {
            this.mediaType = mediaType;
        }

        public void setCharset(String charset) {
            this.charset = charset;
        }

        public void setContentType(Header header) {
            Matcher m = CONTENT_TYPE_PATTERN.matcher(header.getValue());
            if (m.matches()) {
                this.setMediaType(m.group(1));
                if (m.group(4) != null) {
                    this.setCharset(m.group(4));
                }
            } else {
                logger.warn("unable to parse the header(name=" +
                        header.getName() + ",value=" + header.getValue() +
                        ") with pattern=" + CONTENT_TYPE_PATTERN.toString());
            }
        }

        public byte[] getContent() {
            return this.content;
        }

        public String getMediaType() {
            return this.mediaType;
        }

        public String getCharset() {
            return this.charset;
        }

        public static final Pattern CONTENT_TYPE_PATTERN =
                Pattern.compile("\\s*([^;]+)(;\\s*(charset=(.+)|boundary=(.*)))?");
    }

}
