package org.javajungle;

import java.util.List;

public record ChapterQuestion(
        int id,
        int chapter,
        String chapterName,
        String difficulty,
        String subtopic,
        String code,
        String question,
        List<String> options,
        int correct,
        String explanation) {
}
