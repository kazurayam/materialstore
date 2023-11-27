package com.kazurayam.materialstore.base.reduce;

import com.kazurayam.materialstore.base.reduce.differ.TextDifferToHTML;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * learning a Baeldung article
 * "Java Split String and Keep Delimiters"
 * https://www.baeldung.com/java-split-string-keep-delimiters
 */
public class SplittingStringWhileKeepingDelimitersTest {

    private final String text = "Hello@World@This@Is@A@Java@Program";
    private final String textMixed = "@HelloWorld@This:Is@A#Java#Program";

    /**
     * (?=pattern)
     */
    @Test
    public void test_4_1_positive_lookahead() {
        List<String> actual = Arrays.asList(text.split("((?=@))"));
        List<String> expected = Arrays.asList(
                "Hello",
                "@World",
                "@This",
                "@Is",
                "@A",
                "@Java",
                "@Program");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void test_4_2_positive_lookbehind() {
        List<String> actual = Arrays.asList(text.split("((?<=@))"));
        List<String> expected = Arrays.asList(
                "Hello@",
                "World@",
                "This@",
                "Is@",
                "A@",
                "Java@",
                "Program");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void test_4_3_positive_lookahead_and_lookbehind() {
        List<String> actual = Arrays.asList(text.split("((?=@)|(?<=@))"));
        List<String> expected = Arrays.asList(
                "Hello",
                "@",
                "World",
                "@",
                "This",
                "@",
                "Is",
                "@",
                "A",
                "@",
                "Java",
                "@",
                "Program");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void test_4_3_positive_lookahead_and_lookbehind_mixed_delimiters() {
        List<String> actual = Arrays.asList(textMixed.split("((?=:|#|@)|(?<=:|#|@))"));
        // @HelloWorld@This:Is@A#Java#Program
        List<String> expected = Arrays.asList(
                "@",
                "HelloWorld",
                "@",
                "This",
                ":",
                "Is",
                "@",
                "A",
                "#",
                "Java",
                "#",
                "Program");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void test_smoke() {
        final String OT = TextDifferToHTML.OLD_TAG;
        final String NT = TextDifferToHTML.NEW_TAG;
        String given = "  if " + OT + "foo" + OT + " is " + NT + "bar" + NT + " {";
        final List<String> actual =
                Arrays.asList(given.split("((?=" + OT + ")|(?<=" + OT + ")|(?=" + NT + ")|(?<=" + NT + "))"));
        List<String> expected = Arrays.asList(
                "  if ",
                OT,
                "foo",
                OT,
                " is ",
                NT,
                "bar",
                NT,
                " {");
        Assertions.assertEquals(expected.size(), actual.size());
        IntStream.range(0,expected.size())
                .forEach(index -> {
                    final String e = expected.get(index);
                    final String a = actual.get(index);
                    Assertions.assertEquals(e, a, index + ": " + e + " != " + a);
                    });
    }

}
