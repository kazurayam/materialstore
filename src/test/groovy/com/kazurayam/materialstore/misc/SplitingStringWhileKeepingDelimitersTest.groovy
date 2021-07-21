package com.kazurayam.materialstore.misc

import com.kazurayam.materialstore.store.differ.TextDifferToHTML
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

/**
 * learning a Baeldung artidle
 * "Java Split String and Keep Delimitiers"
 * https://www.baeldung.com/java-split-string-keep-delimiters
 */
class SplitingStringWhileKeepingDelimitersTest {

    String text = "Hello@World@This@Is@A@Java@Program";

    String textMixed = "@HelloWorld@This:Is@A#Java#Program";

    // negative lookahead   (?!pattern)
    // negative lookbehind  (?<!pattern)
    // positive lookahead   (?=pattern)
    // positive lookbehind  (?<=pattern)

    /**
     * (?=pattern)
     */
    @Test
    void test_4_1_positive_lookahead() {
        List<String> actual = text.split("((?=@))")
        List<String> expected = [
                "Hello",
                "@World",
                "@This",
                "@Is",
                "@A",
                "@Java",
                "@Program"
        ]
        assertEquals(expected, actual)
    }

    @Test
    void test_4_2_positive_lookbehind() {
        List<String> actual = text.split("((?<=@))")
        List<String> expected = [
                "Hello@",
                "World@",
                "This@",
                "Is@",
                "A@",
                "Java@",
                "Program"
        ]
        assertEquals(expected, actual)
    }

    @Test
    void test_4_3_positive_lookahead_and_lookbehind() {
        List<String> actual = text.split("((?=@)|(?<=@))")
        List<String> expected = [
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
                "Program"
        ]
        assertEquals(expected, actual)
    }

    @Test
    void test_4_3_positive_lookahead_and_lookbehind_mixed_delimiters() {
        List<String> actual = textMixed.split("((?=:|#|@)|(?<=:|#|@))")
        // @HelloWorld@This:Is@A#Java#Program
        List<String> expected = [
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
                "Program"
        ]
        assertEquals(expected, actual)
    }

    @Test
    void test_smoke() {
        String OT = TextDifferToHTML.OLD_TAG
        String NT = TextDifferToHTML.NEW_TAG
        String given = "  if ${OT}foo${OT} is ${NT}bar${NT} {"
        List<String> actual = given.split("((?=${OT})|(?<=${OT})|(?=${NT})|(?<=${NT}))") as List
        List<String> expected = [
                "  if ",
                OT,
                "foo",
                OT,
                " is ",
                NT,
                "bar",
                NT,
                " {"
        ]
        assertEquals(expected.size(), actual.size())
        expected.eachWithIndex { e, index ->
            String a = actual.get(index)
            assertEquals(e, a, "${index}: ${e} != ${a}")
        }
    }
}
