package org.javajungle;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class ModeSelectController {
    @FXML private Label playerStatusLabel;
    @FXML private Label heartsLabel;
    @FXML private Label xpLabel;
    @FXML private Label levelLabel;
    @FXML private Label progressSummaryLabel;
    @FXML private ProgressBar levelProgressBar;

    @FXML
    private void initialize() {
        String playerName = PlayerProgress.getPlayerName();
        playerStatusLabel.setText((playerName.isBlank() ? "Welcome, explorer" : "Welcome, " + playerName)
                + " · " + PlayerProgress.getRank());
        levelLabel.setText("Lv " + PlayerProgress.getLevel());
        levelProgressBar.setProgress(PlayerProgress.getLevelProgress());
        heartsLabel.setText(PlayerProgress.getHeartsText());
        xpLabel.setText(PlayerProgress.getXp() + " XP");
        progressSummaryLabel.setText(PlayerProgress.getSolvedCaseCount() + " cases cleared  ·  "
                + PlayerProgress.getXp() + " XP earned");
    }

    @FXML
    private void handleBack() {
        StoryModeApp.showWelcome();
    }

    @FXML
    private void handleStoryMode() {
        StoryModeApp.showProfile();
    }

    @FXML
    private void handleChapterMode() {
        StoryModeApp.showChapterMode();
    }
}
