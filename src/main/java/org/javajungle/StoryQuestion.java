package org.javajungle;

public record StoryQuestion(
        int id,
        int chapter,
        String chapterTitle,
        String topic,
        String difficulty,
        String sector,
        String title,
        String scenario,
        String fileName,
        String brokenCode,
        String fixedCode,
        String expectedError,
        String hint,
        String explanation,
        String youtubeLink) {
}
