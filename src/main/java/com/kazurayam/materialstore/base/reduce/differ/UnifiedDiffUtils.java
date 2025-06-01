package com.kazurayam.materialstore.base.reduce.differ;

import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Here I will replicate the generateUnifiedDiff method of com.github.difflib.UnifiedDiffUtils class:
 *     https://github.com/java-diff-utils/java-diff-utils/blob/master/java-diff-utils/src/main/java/com/github/difflib/UnifiedDiffUtils.java
 * The original code generated the well-known unified diff.
 * I, @kazurayam, want to generate an HTML fragment that can presents the diff with HTML tags with
 * various styles --- background-color etc. So I will study the internal of the original class here, and
 * try to modify it to achieve what I want.
 */
public class UnifiedDiffUtils {

    private static final String NULL_FILE_INDICATOR = "/dev/null";

    public static List<String> generateUnifiedDiff(
            String originalFileName,
            String revisedFileName,
            List<String> originalLines,
            Patch<String> patch,
            int contextSize) {
        if (!patch.getDeltas().isEmpty()) {
            List<String> ret = new ArrayList<>();
            ret.add("--- " + Optional.ofNullable(originalFileName).orElse(NULL_FILE_INDICATOR));
            ret.add("+++ " + Optional.ofNullable(revisedFileName).orElse(NULL_FILE_INDICATOR));

            List<AbstractDelta<String>> patchDeltas = new ArrayList<>(
                    patch.getDeltas());

            // code outside the if block also works for single-delta issues.
            List<AbstractDelta<String>> deltas = new ArrayList<>(); // current

            // list of Delta's to process
            AbstractDelta<String> delta = patchDeltas.get(0);
            deltas.add(delta); // add the first Delta to the current set
            // if there's more than 1 Delta, we may need to output them together
            if (patchDeltas.size() > 1) {
                for (int i = 1; i < patchDeltas.size(); i++) {
                    // store the current position of the first Delta
                    int position = delta.getSource().getPosition();
                    // check if the next Delta is too close to the current position.
                    // And if it is, add it to the current set
                    AbstractDelta<String> nextDelta = patchDeltas.get(i);
                    if ((position + delta.getSource().size() + contextSize) >=
                            (nextDelta.getSource().getPosition() - contextSize)) {
                        deltas.add(nextDelta);
                    } else {
                        // if it isn't, output the current set,
                        // then create a new set and add the current Delta to it
                        List<String> curBlock = processDeltas(originalLines,
                                deltas, contextSize, false);
                        ret.addAll(curBlock);
                        deltas.clear();
                        deltas.add(nextDelta);
                    }
                    delta = nextDelta;
                }
            }

            // don't forget to process the last set of Deltas
            List<String> curBlock = processDeltas(originalLines, deltas,
                    contextSize,
                    patchDeltas.size() == 1 && originalFileName == null);
            ret.addAll(curBlock);
            return ret;
        }
        return new ArrayList<>();
    }

    /**
     * processDelta takes a list of Deltas and outputs them together in a single block of
     * Unified-Diff-format text.
     *
     * @return
     */
    private static List<String> processDeltas(
            List<String> origLines,
            List<AbstractDelta<String>> deltas,
            int contextSize, boolean newFile) {
        List<String> buffer = new ArrayList<>();
        int origTotal = 0; // counter for total lines output from Original
        int revTotal = 0; // counter for total lines output from Original
        int line;

        AbstractDelta<String> curDelta = deltas.get(0);
        int origStart;
        if (newFile) {
            origStart = 0;
        } else {
            // NOTE: +1 to overcome the 0-offset Position
            origStart = curDelta.getSource().getPosition() + 1 - contextSize;
            if (origStart < 0) {
                origStart = 1;
            }
        }

        int revStart = curDelta.getTarget().getPosition() + 1 - contextSize;
        if (revStart < 0) {
            revStart = 1;
        }

        // find the start of the wrapper context code
        int contextStart = curDelta.getSource().getPosition() - contextSize;
        if (contextStart < 0) {
            contextStart = 0; // clamp to the start of the file
        }

        // output the context before the first Delta
        for (line = contextStart; line < curDelta.getSource().getPosition(); line++) {
            buffer.add(" " + origLines.get(line));
            origTotal++;
            revTotal++;
        }

        // output the first Delta
        buffer.addAll(getDeltaText(curDelta));
        origTotal += curDelta.getSource().getLines().size();
        revTotal += curDelta.getTarget().getLines().size();

        int deltaIndex = 1;
        while (deltaIndex < deltas.size()) { // for each of the other Deltas
            AbstractDelta<String> nextDelta = deltas.get(deltaIndex);
            int intermediateStart = curDelta.getSource().getPosition()
                    + curDelta.getSource().getLines().size();
            for (line = intermediateStart;
                 line < nextDelta.getSource().getPosition();
                 line++) {
                // output the code between the last Delta and this one
                buffer.add(" " + origLines.get(line));
                origTotal++;
                revTotal++;
            }
            buffer.addAll(getDeltaText(nextDelta)); // output the Delta
            origTotal += nextDelta.getSource().getLines().size();
            revTotal += nextDelta.getTarget().getLines().size();
            curDelta = nextDelta;
            deltaIndex++;
        }

        // Now output the post-Delta context code, clamping the end of the file
        contextStart = curDelta.getSource().getPosition()
                + curDelta.getSource().getLines().size();
        for (line = contextStart;
             (line < (contextStart + contextSize))
                     && (line < origLines.size());
             line++) {
            buffer.add(" " + origLines.get(line));
            origTotal++;
            revTotal++;
        }

        // Create and insert the block header,
        // conforming to the Unified Diff standard
        StringBuilder header = new StringBuilder();
        header.append("@@ -");
        header.append(origStart);
        header.append(",");
        header.append(origTotal);
        header.append(" +");
        header.append(revStart);
        header.append(",");
        header.append(revTotal);
        header.append(" @@");
        buffer.add(0, header.toString());

        return buffer;
    }

    private static List<String> getDeltaText(AbstractDelta<String> delta) {
        List<String> buffer = new ArrayList<>();
        for (String line: delta.getSource().getLines()) {
            buffer.add("-" + line);
        }
        for (String line: delta.getTarget().getLines()) {
            buffer.add("+" + line);
        }
        return buffer;
    }

    /**
     * private constructor
     */
    private UnifiedDiffUtils() {}
}
