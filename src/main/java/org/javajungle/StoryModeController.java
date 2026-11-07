package org.javajungle;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;
import java.util.List;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

public class StoryModeController {
    @FXML private TextArea codeArea;
    @FXML private ImageView playerAvatarImage;
    @FXML private VBox lineNumbers;
    @FXML private HBox editorRow;
    @FXML private Label editorMode;
    @FXML private Label difficultyBadge;
    @FXML private Label caseTopicLabel;
    @FXML private Label caseNumberLabel;
    @FXML private Label caseTitleLabel;
    @FXML private Label briefingLabel;
    @FXML private Label fileNameLabel;
    @FXML private Label feedbackTitle;
    @FXML private Label feedbackMessage;
    @FXML private Label feedbackTopic;
    @FXML private Button hintButton;
    @FXML private Button revealAnswerButton;
    @FXML private Label correctAnswerHeading;
    @FXML private TextArea correctAnswerArea;
    @FXML private Hyperlink playlistLink;
    @FXML private Hyperlink moshCourseLink;
    @FXML private VBox courseLinks;
    @FXML private Label feedbackHeart;
    @FXML private Label feedbackXp;
    @FXML private Label tryAgain;
    @FXML private Label heartsLabel;
    @FXML private Label xpLabel;
    @FXML private Button editButton;
    @FXML private Button submitButton;
    @FXML private Button nextQuestionButton;
    private StoryCase currentCase;
    private final Checker checker = new Checker(PlayerProgress::recordStoryMistake);
    private static final String MOSH_JAVA_COURSE = "https://youtu.be/eIrMbAQSU34?si=2HAG-ui7N3GI9cJN";

    @FXML
    private void initialize() {
        String avatar = PlayerProgress.getAvatar();
        var avatarUrl = StoryModeController.class.getResource("avatars/" + avatar + ".png");
        if (avatarUrl == null) throw new IllegalStateException("Missing avatar image: " + avatar);
        playerAvatarImage.setImage(new Image(avatarUrl.toExternalForm()));
        codeArea.setEditable(false);
        setEditing(false);
        updateLineNumbers();
        codeArea.textProperty().addListener((observable, oldText, newText) -> updateLineNumbers());
        codeArea.fontProperty().addListener((observable, oldFont, newFont) -> updateLineNumbers());
        lineNumbers.translateYProperty().bind(codeArea.scrollTopProperty().multiply(-1));

        Rectangle rowClip = new Rectangle();
        rowClip.widthProperty().bind(editorRow.widthProperty());
        rowClip.heightProperty().bind(editorRow.heightProperty());
        editorRow.setClip(rowClip);
    }

    @FXML
    private void handleEdit() {
        setEditing(!codeArea.isEditable());
        if (codeArea.isEditable()) {
            showFeedbackPrompt();
        }
        if (codeArea.isEditable()) {
            codeArea.requestFocus();
            codeArea.positionCaret(codeArea.getLength());
        }
    }

    @FXML
    private void handleBack() {
        StoryModeApp.showCaseSearch();
    }

    @FXML
    private void handleModes() {
        StoryModeApp.showModeSelect();
    }

    public void loadCase(StoryCase storyCase) {
        currentCase = storyCase;
        caseNumberLabel.setText("CASE " + storyCase.number());
        caseTitleLabel.setText(storyCase.title());
        briefingLabel.setText(storyCase.briefing());
        fileNameLabel.setText("CASE DOSSIER / " + storyCase.fileName());
        // Scenario files sometimes include a leading/trailing blank line from
        // formatting. Remove those so the visible first source line is line 1.
        String source = storyCase.code() == null ? "" : storyCase.code().strip();
        source = source.replace("\r\n", "\n").replace('\r', '\n');
        codeArea.setText(source);
        codeArea.positionCaret(0);
        codeArea.setScrollTop(0);
        caseTopicLabel.setText(storyCase.topic().toUpperCase());
        refreshProgressLabels();
        setDifficulty(storyCase.difficulty());
        boolean inChapterSession = StoryModeApp.isChapterSessionActive();
        nextQuestionButton.setVisible(inChapterSession);
        nextQuestionButton.setManaged(inChapterSession);
        nextQuestionButton.setDisable(true);
        if (inChapterSession) {
            nextQuestionButton.setText(StoryModeApp.hasNextChapterQuestion() ? "NEXT QUESTION  →" : "FINISH SET  ✓");
        }
        updateLineNumbers();
    }

    @FXML
    private void handleSubmit() {
        setEditing(false);
        if (currentCase == null) {
            showFeedback("Not Quite", "Question unavailable.", "", "0 hearts", "+0 XP");
            return;
        }
        Result result = checker.check(toCheckerCase(currentCase), codeArea.getText());
        boolean correct = result.isCorrect();
        boolean alreadySolved = PlayerProgress.isCaseSolved(currentCase.id());
        int earned = PlayerProgress.recordStory(currentCase, correct);
        if (correct) {
            showFeedback("Correct", "Your fix matches the answer in the question bank.",
                    "Why it works: " + currentCase.explanation(),
                    alreadySolved ? "Case solved again" : "Case cleared", "+" + earned + " XP");
        } else {
            showFeedback("Not Quite", result.getMessage(),
                    "Topic: " + currentCase.topic() + "\nChoose a hint to retry, or reveal the answer.",
                    "No hearts lost", "+0 XP");
        }
        if (StoryModeApp.isChapterSessionActive()) {
            nextQuestionButton.setDisable(false);
        }
        codeArea.getStyleClass().remove("editing-state");
        codeArea.getStyleClass().add("feedback-state");
        refreshProgressLabels();
    }

    private void refreshProgressLabels() {
        if (heartsLabel != null) heartsLabel.setText(PlayerProgress.getHeartsText());
        if (xpLabel != null) xpLabel.setText(String.valueOf(PlayerProgress.getXp()));
    }

    @FXML
    private void handleNextQuestion() {
        if (StoryModeApp.isChapterSessionActive() && !nextQuestionButton.isDisabled()) {
            StoryModeApp.nextChapterQuestion();
        }
    }

    @FXML
    private void openPlaylist() {
        if (currentCase != null) openExternalLink(currentCase.youtubeLink());
    }

    @FXML
    private void handleHint() {
        if (currentCase == null) return;
        feedbackTopic.setText("Hint: " + currentCase.hint() + "\nTopic: " + currentCase.topic());
        hintButton.setDisable(true);
        hintButton.setText("HINT SHOWN");
        setEditing(true);
        tryAgain.setText("Edit your code and submit again. Your hearts are safe.");
        codeArea.requestFocus();
    }

    @FXML
    private void handleRevealAnswer() {
        if (currentCase == null) return;
        correctAnswerHeading.setVisible(true);
        correctAnswerHeading.setManaged(true);
        correctAnswerArea.setText(currentCase.fixedCode().strip());
        correctAnswerArea.setVisible(true);
        correctAnswerArea.setManaged(true);
        feedbackTopic.setText("Correct answer revealed. Topic: " + currentCase.topic()
                + "\nWhy: " + currentCase.explanation());
        revealAnswerButton.setDisable(true);
        revealAnswerButton.setText("ANSWER REVEALED");
        tryAgain.setText("Use the explanation to repair the line, then submit again.");
    }

    @FXML
    private void openMoshCourse() {
        openExternalLink(MOSH_JAVA_COURSE);
    }

    private void openExternalLink(String url) {
        try {
            if (url == null || url.isBlank()) throw new IllegalArgumentException("No course link is available.");
            if (!Desktop.isDesktopSupported()) throw new UnsupportedOperationException("This computer cannot open web links.");
            Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException | IllegalArgumentException | UnsupportedOperationException exception) {
            feedbackMessage.setText("Could not open the course link: " + exception.getMessage());
        }
    }

    private Case toCheckerCase(StoryCase storyCase) {
        String[] buggyLines = storyCase.code().replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        String[] fixedLines = storyCase.fixedCode().replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        int lineCount = Math.max(buggyLines.length, fixedLines.length);
        List<Integer> bugLines = List.of();
        for (int i = 0; i < lineCount; i++) {
            String buggy = i < buggyLines.length ? buggyLines[i] : "";
            String fixed = i < fixedLines.length ? fixedLines[i] : "";
            if (!buggy.equals(fixed)) {
                bugLines = List.of(i + 1);
                break;
            }
        }
        return new Case(storyCase.id(), storyCase.briefing(), storyCase.code(), storyCase.fixedCode(),
                storyCase.checkerTopic(), bugLines, List.of());
    }

    public void showFeedback(String verdict, String message, String topic, String heartChange, String xpChange) {
        boolean success = "Correct".equalsIgnoreCase(verdict);
        feedbackTitle.setText((success ? "✓  " : "✕  ") + verdict);
        feedbackTitle.getStyleClass().removeAll("feedback-heading", "feedback-error", "feedback-success");
        feedbackTitle.getStyleClass().add(success ? "feedback-success" : "feedback-error");
        feedbackMessage.setText(message);
        feedbackTopic.setText(topic);
        boolean showCorrectAnswer = !success && currentCase != null
                && correctAnswerArea.isVisible();
        correctAnswerHeading.setVisible(showCorrectAnswer);
        correctAnswerHeading.setManaged(showCorrectAnswer);
        correctAnswerArea.setVisible(showCorrectAnswer);
        correctAnswerArea.setManaged(showCorrectAnswer);
        boolean offerHelp = !success && currentCase != null;
        hintButton.setVisible(offerHelp);
        hintButton.setManaged(offerHelp);
        hintButton.setDisable(false);
        hintButton.setText("GET A HINT");
        revealAnswerButton.setVisible(offerHelp);
        revealAnswerButton.setManaged(offerHelp);
        revealAnswerButton.setDisable(false);
        revealAnswerButton.setText("REVEAL ANSWER");
        feedbackHeart.setText(heartChange);
        feedbackXp.setText(xpChange);
        tryAgain.setText(success ? "Question complete  >" : "Try again  >");
        boolean showCourseLinks = "Not Quite".equalsIgnoreCase(verdict);
        playlistLink.setVisible(showCourseLinks && currentCase != null && currentCase.youtubeLink() != null
                && !currentCase.youtubeLink().isBlank());
        playlistLink.setManaged(playlistLink.isVisible());
        moshCourseLink.setVisible(showCourseLinks);
        moshCourseLink.setManaged(showCourseLinks);
        courseLinks.setVisible(showCourseLinks);
        courseLinks.setManaged(showCourseLinks);
    }

    public void setDifficulty(String difficulty) {
        String normalized = difficulty == null ? "medium" : difficulty.trim().toLowerCase();
        if (!normalized.equals("easy") && !normalized.equals("medium") && !normalized.equals("hard")) {
            normalized = "medium";
        }
        difficultyBadge.setText(normalized.toUpperCase());
        difficultyBadge.getStyleClass().removeAll("difficulty-easy", "difficulty-medium", "difficulty-hard");
        difficultyBadge.getStyleClass().add("difficulty-" + normalized);
    }

    private void setEditing(boolean editing) {
        codeArea.setEditable(editing);
        editorMode.setText(editing ? "editing" : "read only");
        editButton.setText(editing ? "✓  Done editing" : "✎  Edit");
        editButton.getStyleClass().removeAll("button-primary", "button-editing");
        editButton.getStyleClass().add(editing ? "button-editing" : "button-primary");
        submitButton.setDisable(!editing);
    }

    private void showFeedbackPrompt() {
        feedbackTitle.setText("> feedback");
        feedbackTitle.getStyleClass().removeAll("feedback-error", "feedback-success");
        if (!feedbackTitle.getStyleClass().contains("feedback-heading")) {
            feedbackTitle.getStyleClass().add("feedback-heading");
        }
        feedbackMessage.setText("Change the line you think is broken, then submit.");
        feedbackTopic.setText("");
        correctAnswerHeading.setVisible(false);
        correctAnswerHeading.setManaged(false);
        correctAnswerArea.setVisible(false);
        correctAnswerArea.setManaged(false);
        playlistLink.setVisible(false);
        playlistLink.setManaged(false);
        moshCourseLink.setVisible(false);
        moshCourseLink.setManaged(false);
        courseLinks.setVisible(false);
        courseLinks.setManaged(false);
        hintButton.setVisible(false);
        hintButton.setManaged(false);
        revealAnswerButton.setVisible(false);
        revealAnswerButton.setManaged(false);
        feedbackHeart.setText("");
        feedbackXp.setText("");
        tryAgain.setText("");
        codeArea.getStyleClass().remove("feedback-state");
        if (!codeArea.getStyleClass().contains("editing-state")) {
            codeArea.getStyleClass().add("editing-state");
        }
    }

    private void updateLineNumbers() {
        int lineCount = Math.max(1, codeArea.getText().split("\\R", -1).length);
        StringBuilder labels = new StringBuilder();
        for (int line = 1; line <= lineCount; line++) {
            if (line > 1) labels.append('\n');
            labels.append(line);
        }

        Text gutter = new Text(labels.toString());
        gutter.setFont(codeArea.getFont());
        gutter.setTextOrigin(VPos.TOP);
        gutter.setTextAlignment(TextAlignment.RIGHT);
        gutter.setLineSpacing(4);
        gutter.getStyleClass().add("line-number-text");

        int digitCount = Integer.toString(lineCount).length();
        Text widthSample = new Text("0".repeat(digitCount));
        widthSample.setFont(codeArea.getFont());
        gutter.setWrappingWidth(widthSample.getLayoutBounds().getWidth());
        lineNumbers.getChildren().setAll(gutter);
    }
}
