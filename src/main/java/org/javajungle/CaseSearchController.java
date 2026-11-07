package org.javajungle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Locale;

public class CaseSearchController {
    @FXML private VBox caseCards;
    @FXML private ComboBox<String> difficultyFilter;
    @FXML private TextField searchField;
    @FXML private Label caseCountLabel;
    @FXML private Label heartsLabel;
    @FXML private Label xpLabel;
    private List<StoryCase> cases = List.of();

    @FXML
    private void initialize() {
        difficultyFilter.getItems().setAll("All difficulties", "Easy", "Medium", "Hard");
        difficultyFilter.getSelectionModel().selectFirst();
        difficultyFilter.valueProperty().addListener((observable, oldValue, newValue) -> renderCases());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> renderCases());
        heartsLabel.setText(PlayerProgress.getHeartsText());
        xpLabel.setText(PlayerProgress.getXp() + " XP");
    }

    public void setCases(List<StoryCase> cases) {
        this.cases = List.copyOf(cases);
        caseCountLabel.setText("CASE SEARCH // " + this.cases.size() + " FILES FOUND");
        renderCases();
    }

    @FXML
    private void handleProfile() {
        StoryModeApp.showProfile();
    }

    @FXML
    private void handleModes() {
        StoryModeApp.showModeSelect();
    }

    private void renderCases() {
        if (caseCards == null) return;
        caseCards.getChildren().clear();
        String selectedDifficulty = difficultyFilter.getValue();
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);

        for (StoryCase storyCase : cases) {
            boolean difficultyMatches = selectedDifficulty == null || selectedDifficulty.equals("All difficulties")
                    || selectedDifficulty.equalsIgnoreCase(storyCase.difficulty());
            boolean searchMatches = query.isEmpty()
                    || storyCase.title().toLowerCase(Locale.ROOT).contains(query)
                    || storyCase.topic().toLowerCase(Locale.ROOT).contains(query)
                    || storyCase.briefing().toLowerCase(Locale.ROOT).contains(query);
            if (difficultyMatches && searchMatches) {
                caseCards.getChildren().add(createCaseCard(storyCase));
            }
        }
        if (caseCards.getChildren().isEmpty()) {
            caseCards.getChildren().add(new Label("No cases match that search."));
        }
    }

    private VBox createCaseCard(StoryCase storyCase) {
        VBox card = new VBox(10);
        card.getStyleClass().add("case-card");

        Label dossier = new Label("CASE DOSSIER  //  " + storyCase.number());
        dossier.getStyleClass().add("case-card-id");
        Label title = new Label(storyCase.title());
        title.getStyleClass().add("case-card-title");
        Label topic = new Label("TOPIC  //  " + storyCase.topic().toUpperCase(Locale.ROOT));
        topic.getStyleClass().add("case-card-topic");
        Label difficulty = new Label(storyCase.difficulty().toUpperCase(Locale.ROOT));
        difficulty.getStyleClass().addAll("difficulty-badge", "difficulty-" + storyCase.difficulty().toLowerCase(Locale.ROOT));
        Label briefing = new Label(storyCase.briefing());
        briefing.getStyleClass().add("case-card-briefing");
        briefing.setWrapText(true);
        Button openButton = new Button("OPEN CASE  →");
        openButton.getStyleClass().add("button-submit");
        openButton.setOnAction(event -> StoryModeApp.openCase(storyCase));

        card.getChildren().addAll(dossier, title, topic, difficulty, briefing, openButton);
        return card;
    }
}
