package org.javajungle;

import java.util.Collections;
import java.util.List;

/** Story Mode checker input for one scenario-based case. */
public class Case {
    private final int id;
    private final String story;
    private final String buggyCode;
    private final String correctFix;
    private final Topic topic;
    private final List<Integer> bugLines;
    private final List<String> alternativeFixes;

    public Case(int id, String story, String buggyCode, String correctFix,
                Topic topic, List<Integer> bugLines, List<String> alternativeFixes) {
        this.id = id;
        this.story = story;
        this.buggyCode = buggyCode;
        this.correctFix = correctFix;
        this.topic = topic;
        this.bugLines = bugLines == null ? Collections.emptyList() : List.copyOf(bugLines);
        this.alternativeFixes = alternativeFixes == null ? Collections.emptyList() : List.copyOf(alternativeFixes);
    }

    public Case(int id, String story, String buggyCode, String correctFix, Topic topic, List<Integer> bugLines) {
        this(id, story, buggyCode, correctFix, topic, bugLines, null);
    }

    public int getId() { return id; }
    public String getStory() { return story; }
    public String getBuggyCode() { return buggyCode; }
    public String getCorrectFix() { return correctFix; }
    public Topic getTopic() { return topic; }
    public List<Integer> getBugLines() { return bugLines; }
    public List<String> getAlternativeFixes() { return alternativeFixes; }
}
