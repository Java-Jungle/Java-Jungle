package org.javajungle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class ProfileController {
    @FXML private VBox activeCases;
    @FXML private StackPane profileAvatar;
    @FXML private ImageView monkeyImage;
    @FXML private Label heartsLabel;
    @FXML private Label xpLabel;
    @FXML private Label casesClearedLabel;
    @FXML private Label detectiveNameLabel;
    @FXML private Label levelRankLabel;
    @FXML private Label chapterOneSkill;
    @FXML private Label chapterOnePercent;
    @FXML private Label chapterTwoSkill;
    @FXML private Label chapterTwoPercent;
    @FXML private Label chapterThreeSkill;
    @FXML private Label chapterThreePercent;
    @FXML private Label recentSignalLabel;

    @FXML
    private void initialize() {
        refreshProgress();
        Runnable fitPortrait = () -> {
            double imageWidth = Math.max(140, profileAvatar.getWidth() - 12);
            double imageHeight = imageWidth * (350.0 / 452.0);
            double frameHeight = imageHeight + 12;
            profileAvatar.setMinHeight(frameHeight);
            profileAvatar.setPrefHeight(frameHeight);
            profileAvatar.setMaxHeight(frameHeight);
            monkeyImage.setFitWidth(imageWidth);
            monkeyImage.setFitHeight(imageHeight);
        };
        profileAvatar.widthProperty().addListener((obs, oldWidth, newWidth) -> fitPortrait.run());
        profileAvatar.heightProperty().addListener((obs, oldHeight, newHeight) -> fitPortrait.run());
        fitPortrait.run();
    }

    public void setCases(List<StoryCase> cases) {
        refreshProgress();
        activeCases.getChildren().clear();
        List<StoryCase> openedCases = cases.stream()
                .filter(storyCase -> PlayerProgress.isCaseOpened(storyCase.id()))
                .toList();
        if (openedCases.isEmpty()) {
            Label emptyTitle = new Label("NO CASES OPENED YET");
            emptyTitle.getStyleClass().add("case-empty-title");
            Label emptyCopy = new Label("Your field record is waiting for its first investigation.");
            emptyCopy.setWrapText(true);
            emptyCopy.getStyleClass().add("case-empty-copy");
            Button findCase = new Button("FIND YOUR FIRST CASE  →");
            findCase.getStyleClass().add("button-submit");
            findCase.setOnAction(event -> StoryModeApp.showCaseSearch());
            activeCases.getChildren().addAll(emptyTitle, emptyCopy, findCase);
            return;
        }
        for (StoryCase storyCase : openedCases) {
            VBox descriptions = new VBox(4);
            Label heading = new Label("CASE " + storyCase.number() + "  ·  " + storyCase.title());
            heading.getStyleClass().add("active-case-title");
            Label subtitle = new Label(storyCase.topic().toUpperCase() + "  //  " + storyCase.difficulty().toUpperCase()
                    + (PlayerProgress.isCaseSolved(storyCase.id()) ? "  //  CLEARED" : "  //  OPEN"));
            subtitle.getStyleClass().add("active-case-subtitle");
            descriptions.getChildren().addAll(heading, subtitle);

            Button open = new Button("OPEN  →");
            open.getStyleClass().add("active-case-open");
            open.setOnAction(event -> StoryModeApp.openCase(storyCase));

            HBox row = new HBox(10, descriptions, new Region(), open);
            row.getStyleClass().add("active-case-row");
            HBox.setHgrow(row.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);
            activeCases.getChildren().add(row);
        }
    }

    private void refreshProgress() {
        heartsLabel.setText(PlayerProgress.getHeartsText());
        xpLabel.setText(PlayerProgress.getXp() + " XP · LV " + PlayerProgress.getLevel());
        levelRankLabel.setText("PROFILE // " + PlayerProgress.getRank().toUpperCase() + " · LV " + PlayerProgress.getLevel());
        String playerName = PlayerProgress.getPlayerName();
        detectiveNameLabel.setText(playerName.isBlank() ? "JUNGLE DETECTIVE" : playerName.toUpperCase());
        String avatar = PlayerProgress.getAvatar();
        var avatarUrl = ProfileController.class.getResource("avatars/" + avatar + ".png");
        if (avatarUrl == null) throw new IllegalStateException("Missing avatar image: " + avatar);
        monkeyImage.setImage(new javafx.scene.image.Image(avatarUrl.toExternalForm()));
        monkeyImage.setVisible(true);
        monkeyImage.setManaged(true);
        casesClearedLabel.setText(String.valueOf(PlayerProgress.getSolvedCaseCount()));
        setSkill(1, chapterOneSkill, chapterOnePercent);
        setSkill(2, chapterTwoSkill, chapterTwoPercent);
        setSkill(3, chapterThreeSkill, chapterThreePercent);
        refreshRecentSignal();
    }

    private void setSkill(int chapter, Label name, Label percent) {
        int attempts = PlayerProgress.getChapterAttemptCount(chapter);
        String topic = switch (chapter) {
            case 1 -> "VARIABLES";
            case 2 -> "OPERATORS";
            case 3 -> "CONDITIONALS";
            default -> "CHAPTER " + String.format("%02d", chapter);
        };
        name.setText(String.format("CHAPTER %02d // %s", chapter, topic));
        if (attempts == 0) {
            percent.setText("NOT ASSESSED · PLAY TO REVEAL");
            percent.getStyleClass().remove("skill-meter-ready");
            if (!percent.getStyleClass().contains("skill-meter-unassessed")) {
                percent.getStyleClass().add("skill-meter-unassessed");
            }
            return;
        }
        int value = PlayerProgress.getSkillPercent(chapter);
        int filled = value / 10;
        String answerUnit = attempts == 1 ? "ANSWER" : "ANSWERS";
        percent.setText("█".repeat(filled) + "░".repeat(10 - filled) + "  " + value + "%  ·  " + attempts + " " + answerUnit);
        percent.getStyleClass().remove("skill-meter-unassessed");
        if (!percent.getStyleClass().contains("skill-meter-ready")) percent.getStyleClass().add("skill-meter-ready");
    }

    private void refreshRecentSignal() {
        int weakestChapter = -1;
        int lowestPercent = 101;
        int totalAttempts = 0;
        for (int chapter = 1; chapter <= 3; chapter++) {
            int attempts = PlayerProgress.getChapterAttemptCount(chapter);
            totalAttempts += attempts;
            if (attempts > 0 && PlayerProgress.getSkillPercent(chapter) < lowestPercent) {
                weakestChapter = chapter;
                lowestPercent = PlayerProgress.getSkillPercent(chapter);
            }
        }
        if (weakestChapter < 0) {
            recentSignalLabel.setText("No clues yet. Answer a chapter question to reveal your first skill signal.");
            return;
        }
        String topic = switch (weakestChapter) {
            case 1 -> "Variables";
            case 2 -> "Operators";
            default -> "Conditionals";
        };
        if (lowestPercent == 100) {
            recentSignalLabel.setText("Perfect record so far. You have answered " + totalAttempts + " chapter questions correctly.");
        } else {
            recentSignalLabel.setText("Next clue: practice " + topic + ". Your current accuracy is " + lowestPercent + "%.");
        }
    }

    @FXML
    private void handleSearchCases() {
        StoryModeApp.showCaseSearch();
    }

    @FXML
    private void handleModes() {
        StoryModeApp.showModeSelect();
    }
}
