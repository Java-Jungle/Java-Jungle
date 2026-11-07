package org.javajungle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChapterQuestionController {
    @FXML private Label chapterHeading;
    @FXML private Label difficultyBadge;
    @FXML private Label questionCounter;
    @FXML private Label subtopicLabel;
    @FXML private Label questionPrompt;
    @FXML private Label resultHeading;
    @FXML private Label explanationLabel;
    @FXML private TextArea codePreview;
    @FXML private VBox answerOptions;
    @FXML private ProgressBar questionProgress;
    @FXML private Button nextButton;
    @FXML private Label heartsLabel;
    @FXML private Label xpLabel;

    private ChapterQuestion currentQuestion;
    private List<Button> optionButtons = new ArrayList<>();
    private int questionIndex;
    private int questionCount;
    private boolean answered;

    public void loadQuestion(ChapterQuestion question, int index, int count) {
        currentQuestion = question;
        questionIndex = index;
        questionCount = count;
        answered = false;

        chapterHeading.setText("CHAPTER " + String.format("%02d", question.chapter()) + "  //  " + question.chapterName());
        questionCounter.setText("QUESTION " + index + " OF " + count);
        subtopicLabel.setText(question.subtopic().toUpperCase());
        questionPrompt.setText(question.question());
        codePreview.setText(codeWithMissingLine(question));
        codePreview.positionCaret(0);
        questionProgress.setProgress((double) index / count);

        difficultyBadge.setText(question.difficulty().toUpperCase());
        difficultyBadge.getStyleClass().removeAll("difficulty-easy", "difficulty-medium", "difficulty-hard");
        difficultyBadge.getStyleClass().add("difficulty-" + question.difficulty().toLowerCase());

        answerOptions.getChildren().clear();
        optionButtons = new ArrayList<>();
        for (int i = 0; i < question.options().size(); i++) {
            int optionIndex = i;
            Button option = new Button((char) ('A' + i) + "   " + question.options().get(i));
            option.setWrapText(true);
            option.setMaxWidth(Double.MAX_VALUE);
            option.getStyleClass().add("answer-option");
            option.setOnAction(event -> chooseAnswer(optionIndex));
            optionButtons.add(option);
            answerOptions.getChildren().add(option);
        }
        resultHeading.setText("Choose the line that repairs the snippet.");
        resultHeading.getStyleClass().removeAll("answer-correct", "answer-incorrect");
        explanationLabel.setText("");
        nextButton.setDisable(true);
        nextButton.setText(index == count ? "FINISH SET  ✓" : "NEXT QUESTION  →");
        refreshProgressLabels();
    }

    private String codeWithMissingLine(ChapterQuestion question) {
        String code = question.code().replace("\r\n", "\n").replace('\r', '\n');
        String answer = question.options().get(question.correct()).trim();
        List<String> visibleLines = new ArrayList<>();
        boolean removedAnswer = false;
        for (String line : code.split("\n", -1)) {
            if (!removedAnswer && line.trim().equals(answer)) {
                removedAnswer = true;
            } else {
                visibleLines.add(line);
            }
        }
        return String.join("\n", visibleLines).strip();
    }

    private void chooseAnswer(int selected) {
        if (answered) return;
        answered = true;
        boolean correct = selected == currentQuestion.correct();
        int earned = PlayerProgress.recordChapterAnswer(currentQuestion, correct);
        for (int i = 0; i < optionButtons.size(); i++) {
            Button option = optionButtons.get(i);
            option.setDisable(true);
            if (i == currentQuestion.correct()) option.getStyleClass().add("answer-correct");
            else if (i == selected) option.getStyleClass().add("answer-incorrect");
        }
        resultHeading.setText(correct ? "✓  Correct repair" + (earned > 0 ? "   +" + earned + " XP" : "") : "✕  Not quite");
        resultHeading.getStyleClass().removeAll("answer-correct", "answer-incorrect");
        resultHeading.getStyleClass().add(correct ? "answer-correct" : "answer-incorrect");
        explanationLabel.setText(correct ? currentQuestion.explanation() : incorrectAnswerFeedback(selected));
        nextButton.setDisable(false);
        refreshProgressLabels();
    }

    private String incorrectAnswerFeedback(int selected) {
        String chosen = currentQuestion.options().get(selected).trim();
        String answer = currentQuestion.options().get(currentQuestion.correct()).trim();
        int line = missingLineNumber(answer);
        return "Your choice (" + (char) ('A' + selected) + "): " + chosen
                + "\nCorrect answer: " + answer + " (line " + line + ")"
                + "\nReason: " + currentQuestion.explanation()
                + "\nWhere you went wrong: " + explainDifference(chosen, answer);
    }

    private int missingLineNumber(String answer) {
        String code = currentQuestion.code().replace("\r\n", "\n");
        int position = code.indexOf(answer);
        if (position < 0) return 1;
        int line = 1;
        for (int i = 0; i < position; i++) if (code.charAt(i) == '\n') line++;
        return line;
    }

    private String explainDifference(String chosen, String answer) {
        Pattern operatorPattern = Pattern.compile("==|!=|<=|>=|\\+=|-=|\\*=|/=|=|\\+|-|\\*|/|<|>");
        List<String> wrongOps = findMatches(operatorPattern, chosen);
        List<String> rightOps = findMatches(operatorPattern, answer);
        for (int i = 0; i < Math.min(wrongOps.size(), rightOps.size()); i++) {
            if (!wrongOps.get(i).equals(rightOps.get(i))) {
                String wrong = wrongOps.get(i), right = rightOps.get(i);
                if ("==".equals(wrong) && "=".equals(right)) {
                    return "You used == to compare values, but this line needs = to assign the value.";
                }
                if ("=".equals(wrong) && "==".equals(right)) {
                    return "You used = to assign a value, but this condition needs == to compare values.";
                }
                return "The operator " + wrong + " is incorrect here; use " + right + " instead.";
            }
        }

        Pattern identifierPattern = Pattern.compile("[A-Za-z_$][\\w$]*");
        List<String> wrongIdentifiers = findMatches(identifierPattern, chosen);
        List<String> rightIdentifiers = findMatches(identifierPattern, answer);
        for (int i = 0; i < Math.min(wrongIdentifiers.size(), rightIdentifiers.size()); i++) {
            if (!wrongIdentifiers.get(i).equals(rightIdentifiers.get(i))) {
                return "The name or type " + wrongIdentifiers.get(i) + " is misplaced; this line needs "
                        + rightIdentifiers.get(i) + " at that position.";
            }
        }

        Pattern numberPattern = Pattern.compile("-?\\b\\d+(?:\\.\\d+)?\\b");
        List<String> wrongNumbers = findMatches(numberPattern, chosen);
        List<String> rightNumbers = findMatches(numberPattern, answer);
        for (int i = 0; i < Math.min(wrongNumbers.size(), rightNumbers.size()); i++) {
            if (!wrongNumbers.get(i).equals(rightNumbers.get(i))) {
                return "The value " + wrongNumbers.get(i) + " should be " + rightNumbers.get(i) + ".";
            }
        }

        int position = 0;
        while (position < chosen.length() && position < answer.length()
                && chosen.charAt(position) == answer.charAt(position)) position++;
        String got = position < chosen.length() ? "'" + chosen.charAt(position) + "'" : "the end of the line";
        String expected = position < answer.length() ? "'" + answer.charAt(position) + "'" : "the end of the line";
        return "The first mismatch is at character " + (position + 1) + ": " + got + " should be " + expected + ".";
    }

    private List<String> findMatches(Pattern pattern, String text) {
        List<String> matches = new ArrayList<>();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) matches.add(matcher.group());
        return matches;
    }

    private void refreshProgressLabels() {
        if (heartsLabel != null) heartsLabel.setText(PlayerProgress.getHeartsText());
        if (xpLabel != null) xpLabel.setText(PlayerProgress.getXp() + " XP");
    }

    @FXML
    private void handleNext() {
        if (answered) StoryModeApp.nextChapterQuestion();
    }

    @FXML
    private void handleBackToChapters() {
        StoryModeApp.showChapterMode();
    }

    @FXML
    private void handleBackToModes() {
        StoryModeApp.showModeSelect();
    }
}
