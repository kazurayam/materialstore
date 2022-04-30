package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.filesystem.Material;

import java.util.Random;

public class MaterialSolo implements MNode {

    private final Material material;
    private final String randomId;

    public MaterialSolo(Material material) {
        this.material = material;
        this.randomId = generateRandomAlphaNumericString(7);
    }

    @Override
    public MNodeId getMNodeId() {
        if (material != Material.NULL_OBJECT) {
            return new MNodeId("M" + material.getDescriptionSignature());
        } else {
            return new MNodeId("M" + randomId);
        }
    }

    /**
     * https://www.baeldung.com/java-random-string
     */
    private String generateRandomAlphaNumericString(int length) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = length;
        Random random = new Random();
        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
