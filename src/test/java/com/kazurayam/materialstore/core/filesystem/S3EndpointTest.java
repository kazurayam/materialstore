package com.kazurayam.materialstore.core.filesystem;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class S3EndpointTest {

    Logger logger = LoggerFactory.getLogger(S3EndpointTest.class);

    @Test
    public void test_AP_NORTHEAST_1() {
        URI url = S3Endpoint.AP_NORTHEAST_1.getURI();
        assertEquals("s3:///s3.ap-northeast-1.AMAZONAWS.COM/", url.toString());
        assertEquals("ap-northeast-1", S3Endpoint.AP_NORTHEAST_1.getRegion());
        assertEquals("Asia Pacific(Tokyo)", S3Endpoint.AP_NORTHEAST_1.getName());
    }

    @Test
    public void test_values() {
        for (S3Endpoint ep : S3Endpoint.values()) {
            logger.info(ep.getURI().toString());
        }
    }
}
