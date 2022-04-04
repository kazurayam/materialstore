package com.kazurayam.materialstore.reduce.differ;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbstractTextDifferTest {

    @Test
    public void test_splitStringWithOldNewTags() {
        String input = "123" + AbstractTextDiffer.OLD_TAG +
                "456" + AbstractTextDiffer.OLD_TAG +
                "789" + AbstractTextDiffer.NEW_TAG +
                "abc" + AbstractTextDiffer.NEW_TAG + "def";
        List<String> output = AbstractTextDiffer.splitStringWithOldNewTags(input);
        assertEquals(9, output.size());
        assertEquals("123", output.get(0));
        assertEquals(AbstractTextDiffer.OLD_TAG, output.get(1));
        assertEquals("456", output.get(2));
        assertEquals(AbstractTextDiffer.OLD_TAG, output.get(3));
        assertEquals("789", output.get(4));
        assertEquals(AbstractTextDiffer.NEW_TAG, output.get(5));
        assertEquals("abc", output.get(6));
        assertEquals(AbstractTextDiffer.NEW_TAG, output.get(7));
        assertEquals("def", output.get(8));
    }
}
