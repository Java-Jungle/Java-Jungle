package org.javajungle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public final class ChapterQuestionBank {
    private static final Map<Integer, String> RESOURCES = Map.of(
            1, "chapter1_variables.json",
            2, "chapter2_operators.json",
            3, "chapter3_conditionals.json",
            4, "chapter4_loops.json",
            5, "chapter5_arrays.json",
            6, "chapter6_strings.json",
            7, "chapter7_methods.json",
            8, "chapter8_classes.json",
            9, "chapter9_stacks.json",
            10, "chapter10_nullpointers.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public List<ChapterQuestion> loadChapter(int chapter) {
        String file = RESOURCES.get(chapter);
        if (file == null) {
            throw new IllegalArgumentException("Unknown chapter: " + chapter);
        }
        String resourcePath = "/org/javajungle/chapter-questions/" + file;
        try (InputStream input = ChapterQuestionBank.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IllegalStateException("Missing Chapter Mode resource: " + resourcePath);
            }
            List<ChapterQuestion> questions = MAPPER.readValue(input, new TypeReference<>() {});
            if (questions.size() != 30 || questions.stream().anyMatch(question ->
                    question.chapter() != chapter || question.options() == null || question.options().size() != 4
                            || question.correct() < 0 || question.correct() >= question.options().size())) {
                throw new IllegalStateException("Unexpected question data in " + resourcePath);
            }
            return List.copyOf(questions);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load " + resourcePath, exception);
        }
    }
}
