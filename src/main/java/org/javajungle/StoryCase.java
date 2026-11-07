package org.javajungle;

public record StoryCase(
        String number,
        String title,
        String difficulty,
        String topic,
        Topic checkerTopic,
        String briefing,
        String fileName,
        String code,
        String fixedCode,
        String expectedError,
        String hint,
        String explanation,
        String sector,
        String youtubeLink,
        int id) {
}
