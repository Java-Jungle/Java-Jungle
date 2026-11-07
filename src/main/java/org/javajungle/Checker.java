package org.javajungle;

import java.util.List;
import java.util.ArrayList;

/** Checks a Story Mode repair, accepts formatting/comment differences, and tracks wrong topics. */
public class Checker {
    private final Tracker tracker;

    public Checker(Tracker tracker) { this.tracker = tracker; }

    public Result check(Case theCase, String playerCode) {
        String player = CodeCleaner.clean(playerCode);
        if (player.isEmpty()) return wrong(theCase, false, true);
        if (player.equals(CodeCleaner.clean(theCase.getCorrectFix()))) {
            return new Result(true, null, -1, "");
        }
        for (String alternative : theCase.getAlternativeFixes()) {
            if (player.equals(CodeCleaner.clean(alternative))) return new Result(true, null, -1, "");
        }
        boolean unchanged = player.equals(CodeCleaner.clean(theCase.getBuggyCode()));
        LineDifference difference = firstDifferentLine(theCase.getCorrectFix(), playerCode);
        int line = difference == null ? -1 : difference.submittedLine();
        if (line < 1 && !theCase.getBugLines().isEmpty()) line = theCase.getBugLines().get(0);
        String message = FeedbackBuilder.build(theCase.getTopic(), line, unchanged, false);
        if (line > 0) {
            String submittedLine = sourceLine(playerCode, line);
            int expectedLineNumber = difference == null ? line : difference.expectedLine();
            String expectedDescription = expectedLineNumber > 0
                    ? "the expected line " + expectedLineNumber + " is “"
                    + sourceLine(theCase.getCorrectFix(), expectedLineNumber) + "”"
                    : "the expected repair has no line at that position";
            message += "\nWhere you went wrong: line " + line + " contains “" + submittedLine
                    + "”; " + expectedDescription + ".";
        }
        if (tracker != null) tracker.mistake(theCase.getTopic());
        return new Result(false, theCase.getTopic(), line, message);
    }

    private Result wrong(Case theCase, boolean unchanged, boolean empty) {
        List<Integer> lines = theCase.getBugLines();
        int line = lines.isEmpty() ? -1 : lines.get(0);
        String message = FeedbackBuilder.build(theCase.getTopic(), line, unchanged, empty);
        if (tracker != null) tracker.mistake(theCase.getTopic());
        return new Result(false, theCase.getTopic(), line, message);
    }

    private LineDifference firstDifferentLine(String expected, String submitted) {
        List<SourceLine> expectedLines = meaningfulLines(expected);
        List<SourceLine> submittedLines = meaningfulLines(submitted);
        int count = Math.max(expectedLines.size(), submittedLines.size());
        for (int i = 0; i < count; i++) {
            SourceLine expectedLine = i < expectedLines.size() ? expectedLines.get(i) : null;
            SourceLine submittedLine = i < submittedLines.size() ? submittedLines.get(i) : null;
            if (expectedLine == null || submittedLine == null
                    || !expectedLine.tokens().equals(submittedLine.tokens())) {
                return new LineDifference(submittedLine == null ? -1 : submittedLine.number(),
                        expectedLine == null ? -1 : expectedLine.number());
            }
        }
        return null;
    }

    private List<SourceLine> meaningfulLines(String source) {
        List<SourceLine> lines = new ArrayList<>();
        String[] sourceLines = source == null ? new String[0] : source.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        for (int i = 0; i < sourceLines.length; i++) {
            String tokens = CodeCleaner.clean(sourceLines[i]);
            if (!tokens.isBlank()) lines.add(new SourceLine(i + 1, tokens));
        }
        return lines;
    }

    private String sourceLine(String source, int lineNumber) {
        if (source == null || lineNumber < 1) return "";
        String[] lines = source.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        return lineNumber <= lines.length ? lines[lineNumber - 1].trim() : "<missing>";
    }

    private record SourceLine(int number, String tokens) {}
    private record LineDifference(int submittedLine, int expectedLine) {}
}
