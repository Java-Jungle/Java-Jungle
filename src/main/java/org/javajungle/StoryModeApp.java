package org.javajungle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class StoryModeApp extends Application {
    private static Stage primaryStage;
    private static final List<StoryQuestion> QUESTION_BANK = loadQuestionBank();
    private static final List<StoryCase> STORY_CASES = mixStoryCases(QUESTION_BANK).stream()
            .map(StoryModeApp::toStoryCase)
            .toList();
    private static final ChapterQuestionBank CHAPTER_QUESTION_BANK = new ChapterQuestionBank();
    private static List<ChapterQuestion> chapterSession = List.of();
    private static int chapterSessionIndex;
    private static boolean chapterSessionActive;
    private static WelcomeController welcomeController;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        Scene scene = new Scene(new javafx.scene.layout.StackPane(), 1280, 720);
        scene.getStylesheets().add(StoryModeApp.class.getResource("story-mode.css").toExternalForm());
        stage.setTitle("Java Jungle");
        stage.setMinWidth(980);
        stage.setMinHeight(620);
        stage.setScene(scene);
        showWelcome();
        stage.show();
    }

    public static void showWelcome() {
        try {
            FXMLLoader loader = new FXMLLoader(StoryModeApp.class.getResource("welcome.fxml"));
            Parent root = loader.load();
            welcomeController = loader.getController();
            welcomeController.showLanding();
            primaryStage.getScene().setRoot(root);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not open the Java Jungle welcome screen", exception);
        }
    }

    public static void showNameEntry() {
        if (welcomeController == null) {
            showWelcome();
        }
        welcomeController.showNameEntry();
    }

    public static void showWelcomeBack() {
        if (welcomeController == null) {
            showWelcome();
        }
        welcomeController.showWelcomeBack();
    }

    private static List<StoryQuestion> loadQuestionBank() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream input = StoryModeApp.class.getResourceAsStream("story-mode-questions.json")) {
            if (input == null) {
                throw new IllegalStateException("story-mode-questions.json is missing from the application resources");
            }
            List<StoryQuestion> questions = mapper.readValue(input, new TypeReference<>() {});
            if (questions.isEmpty()) {
                throw new IllegalStateException("The Story Mode question bank is empty");
            }
            return List.copyOf(questions);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load story-mode-questions.json", exception);
        }
    }

    private static StoryCase toStoryCase(StoryQuestion question) {
        String title = question.title();
        String chapterPrefix = question.chapterTitle() + " // ";
        if (title.startsWith(chapterPrefix)) title = title.substring(chapterPrefix.length());
        String difficulty = question.difficulty().toLowerCase(Locale.ROOT);
        String brokenCode = addDifficultyContext(question.brokenCode(), difficulty);
        String fixedCode = addDifficultyContext(question.fixedCode(), difficulty);
        return new StoryCase(
                String.format(Locale.ROOT, "%03d", question.id()),
                title,
                difficulty,
                question.topic(),
                toCheckerTopic(question.chapterTitle(), question.topic()),
                question.scenario(),
                question.fileName(),
                brokenCode,
                fixedCode,
                question.expectedError(),
                question.hint(),
                question.explanation(),
                question.sector(),
                question.youtubeLink(),
                question.id());
    }

    /** Add compile-safe investigation context so harder Story Mode cases ask players to read more code. */
    private static String addDifficultyContext(String code, String difficulty) {
        if (code == null || code.isBlank()) return code;

        String easyContext = "    int auditRows = 2;\n"
                + "    int rowsChecked = auditRows;\n"
                + "    boolean auditComplete = rowsChecked == auditRows;\n"
                + "    System.out.println(auditComplete);\n";
        String mediumContext = "    int reviewAttempts = 1;\n"
                + "    boolean reviewPending = reviewAttempts > 0;\n"
                + "    String reviewStatus = reviewPending ? \"PENDING\" : \"CLOSED\";\n"
                + "    if (reviewPending) {\n"
                + "      reviewStatus = reviewStatus + \" - verify source\";\n"
                + "    }\n"
                + "    int checkedUnits = 2;\n"
                + "    int skippedUnits = 0;\n"
                + "    checkedUnits += 1;\n"
                + "    boolean reviewReady = checkedUnits > skippedUnits;\n"
                + "    if (reviewReady) {\n"
                + "      reviewStatus = reviewStatus + \" / READY\";\n"
                + "    }\n"
                + "    System.out.println(reviewStatus);\n";
        String hardContext = mediumContext
                + "    int[] reviewTrail = {2, 4, 6};\n"
                + "    int reviewChecksum = 0;\n"
                + "    for (int trailEntry : reviewTrail) {\n"
                + "      if (trailEntry > 3) {\n"
                + "        reviewChecksum += trailEntry;\n"
                + "      } else {\n"
                + "        reviewChecksum++;\n"
                + "      }\n"
                + "    }\n"
                + "    switch (reviewChecksum) {\n"
                + "      case 11 -> reviewStatus = reviewStatus + \" / CHECKED\";\n"
                + "      default -> reviewStatus = reviewStatus + \" / REVIEW\";\n"
                + "    }\n";
        String context = "easy".equals(difficulty) ? easyContext
                : "hard".equals(difficulty) ? hardContext : mediumContext;

        // Insert inside main() immediately before its closing brace, keeping both the broken
        // and repaired versions structurally identical and compilable.
        int mainClose = code.lastIndexOf("\n    }\n}");
        if (mainClose < 0) mainClose = code.lastIndexOf("\n  }\n}");
        if (mainClose < 0) return code;
        return code.substring(0, mainClose + 1) + context + code.substring(mainClose + 1);
    }

    private static Topic toCheckerTopic(String category, String detail) {
        String normalized = category == null ? "" : category.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "variables" -> Topic.VARIABLES;
            case "operators" -> Topic.OPERATORS;
            case "conditionals", "conditional logic" -> Topic.CONDITIONAL_LOGIC;
            case "loops" -> Topic.LOOPS;
            case "arrays" -> Topic.ARRAYS;
            case "strings" -> Topic.STRINGS;
            case "methods" -> Topic.METHODS;
            case "classes" -> Topic.CLASSES;
            case "stack memory" -> Topic.STACK_MEMORY;
            case "null references" -> Topic.NULL_REFERENCES;
            default -> topicFromDetail(detail);
        };
    }

    private static Topic topicFromDetail(String detail) {
        String value = detail == null ? "" : detail.toLowerCase(Locale.ROOT);
        if (value.contains("array") || value.contains("index")) return Topic.ARRAYS;
        if (value.contains("loop") || value.contains("iteration")) return Topic.LOOPS;
        if (value.contains("operator") || value.contains("arithmetic")) return Topic.OPERATORS;
        if (value.contains("string") || value.contains("text")) return Topic.STRINGS;
        if (value.contains("method") || value.contains("parameter")) return Topic.METHODS;
        if (value.contains("class") || value.contains("object")) return Topic.CLASSES;
        if (value.contains("null")) return Topic.NULL_REFERENCES;
        if (value.contains("stack") || value.contains("memory")) return Topic.STACK_MEMORY;
        if (value.contains("condition") || value.contains("branch")) return Topic.CONDITIONAL_LOGIC;
        return Topic.VARIABLES;
    }

    /** Interleave source groups so Story Mode plays as a mixed case file, not chapter-by-chapter. */
    private static List<StoryQuestion> mixStoryCases(List<StoryQuestion> questions) {
        Map<Integer, List<StoryQuestion>> sourceGroups = new TreeMap<>();
        for (StoryQuestion question : questions) {
            sourceGroups.computeIfAbsent(question.chapter(), ignored -> new java.util.ArrayList<>()).add(question);
        }
        List<StoryQuestion> mixed = new java.util.ArrayList<>(questions.size());
        for (int index = 0; mixed.size() < questions.size(); index++) {
            boolean added = false;
            for (List<StoryQuestion> group : sourceGroups.values()) {
                if (index < group.size()) {
                    mixed.add(group.get(index));
                    added = true;
                }
            }
            if (!added) break;
        }
        return List.copyOf(mixed);
    }

    public static List<StoryQuestion> getQuestions() {
        return QUESTION_BANK;
    }

    public static List<StoryCase> getCases() {
        return STORY_CASES;
    }

    public static void startChapterSession(int chapter, String difficulty) {
        chapterSession = CHAPTER_QUESTION_BANK.loadChapter(chapter).stream()
                .filter(question -> question.difficulty().equalsIgnoreCase(difficulty))
                .toList();
        if (chapterSession.isEmpty()) {
            throw new IllegalStateException("No " + difficulty + " questions found for chapter " + chapter);
        }
        chapterSessionIndex = 0;
        chapterSessionActive = true;
        displayChapterQuestion();
    }

    public static boolean isChapterSessionActive() {
        return chapterSessionActive;
    }

    public static boolean hasNextChapterQuestion() {
        return chapterSessionActive && chapterSessionIndex + 1 < chapterSession.size();
    }

    public static void nextChapterQuestion() {
        if (!chapterSessionActive) return;
        chapterSessionIndex++;
        if (chapterSessionIndex >= chapterSession.size()) {
            chapterSessionActive = false;
            chapterSession = List.of();
            showChapterMode();
            return;
        }
        displayChapterQuestion();
    }

    private static void displayChapterQuestion() {
        try {
            FXMLLoader loader = new FXMLLoader(StoryModeApp.class.getResource("chapter-question.fxml"));
            Parent root = loader.load();
            ChapterQuestionController controller = loader.getController();
            controller.loadQuestion(chapterSession.get(chapterSessionIndex), chapterSessionIndex + 1, chapterSession.size());
            primaryStage.getScene().setRoot(root);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not open Chapter Mode question", exception);
        }
    }

    public static void showModeSelect() {
        chapterSessionActive = false;
        chapterSession = List.of();
        try {
            Parent root = FXMLLoader.load(StoryModeApp.class.getResource("mode-select.fxml"));
            primaryStage.getScene().setRoot(root);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not open mode selection screen", exception);
        }
    }

    public static void showChapterMode() {
        chapterSessionActive = false;
        chapterSession = List.of();
        try {
            Parent root = FXMLLoader.load(StoryModeApp.class.getResource("chapter-mode.fxml"));
            primaryStage.getScene().setRoot(root);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not open Chapter Mode", exception);
        }
    }

    public static void showProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(StoryModeApp.class.getResource("profile.fxml"));
            Parent root = loader.load();
            ProfileController controller = loader.getController();
            controller.setCases(STORY_CASES);
            primaryStage.getScene().setRoot(root);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not open profile screen", exception);
        }
    }

    public static void showCaseSearch() {
        try {
            FXMLLoader loader = new FXMLLoader(StoryModeApp.class.getResource("case-search.fxml"));
            Parent root = loader.load();
            CaseSearchController controller = loader.getController();
            controller.setCases(STORY_CASES);
            primaryStage.getScene().setRoot(root);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not open case search screen", exception);
        }
    }

    public static void openCase(StoryCase storyCase) {
        chapterSessionActive = false;
        chapterSession = List.of();
        PlayerProgress.recordCaseOpened(storyCase.id());
        displayCase(storyCase);
    }

    private static void displayCase(StoryCase storyCase) {
        try {
            FXMLLoader loader = new FXMLLoader(StoryModeApp.class.getResource("story-mode.fxml"));
            Parent root = loader.load();
            StoryModeController controller = loader.getController();
            controller.loadCase(storyCase);
            primaryStage.getScene().setRoot(root);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not open Story Mode", exception);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
