package com.kazurayam.materialstore.core.filesystem;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * AWS S3 Service Endpoints as listed in
 * https://docs.aws.amazon.com/general/latest/gr/s3.html
 */
public enum S3Endpoint {

    AF_SOUTH_1("af-south-1", "Africa(Cape Town)"),
    AP_EAST_1("ap-east-1", "Asia Pacific(Hong Kong)"),
    AP_NORTHEAST_1("ap-northeast-1", "Asia Pacific(Tokyo)"),
    AP_NORTHEAST_2("ap-northeast-2", "Asia Pacific(Seoul)"),
    AP_NORTHEAST_3("ap-northeast-3", "Asia Pacific(Osaka)"),
    AP_SOUTH_1("ap-south-1", "Asia Pacific(Mumbai)"),
    AP_SOUTH_2("ap-south-2", "Asia Pacific(Hyderabad)"),
    AP_SOUTHEAST_1("ap-southeast-1", "Asia Pacific(Singapore)"),
    AP_SOUTHEAST_2("ap-southeast-2", "Asia Pacific(Sydney)"),
    AP_SOUTHEAST_3("ap-southeast-3", "Asia Pacific(Jakarta)"),
    CA_CENTRAL_1("ca-entral-1", "Canada(Central)"),
    CN_NORTH_1("cn-north-1", "China(Beijing)"),
    CN_NORTHEAST_1("cn-northwest-1", "China(Ningxia)"),
    EU_CENTRAL_1("eu-central-1", "Europe(Frankfurt)"),
    EU_CENTRAL_2("eu-central-2", "Europe(Zurich)"),
    EU_NORTH_1("eu-north-1", "Europe(Stockholm)"),
    EU_SOUTH_1("eu-south-1", "Europe(Milan)"),
    EU_SOUTH_2("eu-south-2", "Europe(Spain)"),
    EU_WEST_1("eu-west-1", "Europe(Ireland)"),
    EU_WEST_2("eu-west-2", "Europe(London)"),
    EU_WEST_3("eu-west-3", "Europe(Paris)"),
    ME_CENTRAL_1("me-central-1", "Middle East(UAE)"),
    ME_SOUTH_1("me-south-1", "Middle East(Bahrain)"),
    SA_EAST_1("sa-east-1", "South America(São Paulo)"),
    US_EAST_1("us-east-1", "US East(N. Virginia)"),
    US_EAST_2("us-east-2", "US East(Ohio)"),
    US_WEST_1("us-west-1", "US West(N. California)"),
    US_WEST_2("us-west-2", "US West(Oregon)");

    private String region;
    private String name;

    S3Endpoint(String region, String name) {
        this.region = region;
        this.name = name;
    }
    public URI getURI() {
        URI uri = null;
        try {
            uri = new URI("s3://s3." + region + ".AMAZONAWS.COM");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return uri;
    }
    public String getRegion() {
        return this.region;
    }
    public String getName() {
        return this.name;
    }
    @Override
    public String toString() {
        return this.getURI().toString();
    }
}
