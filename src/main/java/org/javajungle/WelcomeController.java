package org.javajungle;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Locale;

public class WelcomeController {
    @FXML private StackPane screenRoot;
    @FXML private ImageView welcomeBackground;
    @FXML private javafx.scene.layout.Region cardBackdrop;
    @FXML private javafx.scene.layout.Region captionMask;
    @FXML private javafx.scene.layout.Region welcomeOverlay;
    @FXML private HBox landingHotspots;
    @FXML private VBox namePanel;
    @FXML private VBox returningPanel;
    @FXML private VBox playerSelectPanel;
    @FXML private VBox playerChoices;
    @FXML private HBox avatarChoices;
    @FXML private HBox returningStats;
    @FXML private HBox returningActions;
    @FXML private TextField nameField;
    @FXML private Button startHuntButton;
    @FXML private Button continueButton;
    @FXML private Button newPlayerButton;
    @FXML private Label nameErrorLabel;
    @FXML private Label selectedAvatarLabel;
    @FXML private Label returningNameLabel;
    @FXML private ImageView returningAvatarImage;
    @FXML private Label returningLevelLabel;
    @FXML private Label returningXpLabel;
    @FXML private Label returningCasesLabel;

    private static final List<AvatarOption> AVATARS = List.of(
            new AvatarOption("monkey", "Monkey"),
            new AvatarOption("lion", "Lion"),
            new AvatarOption("snake", "Snake"),
            new AvatarOption("tiger", "Tiger"),
            new AvatarOption("bird", "Bird"),
            new AvatarOption("rhino", "Rhino"));

    private String chosenAvatar = "monkey";
    private double cardHeightRatio = 0.722;
    private Node nameEntryBackTarget;

    @FXML
    private void initialize() {
        createAvatarChoices();
        screenRoot.widthProperty().addListener((observable, oldValue, newValue) -> resizeReferenceArtwork());
        screenRoot.heightProperty().addListener((observable, oldValue, newValue) -> resizeReferenceArtwork());
        resizeReferenceArtwork();
        nameField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().length() <= 24 ? change : null));
        nameField.setOnAction(event -> handleStartHunt());
    }

    public void showLanding() {
        cardHeightRatio = 0.722;
        setBackground("welcome-landing-reference.jpg");
        captionMask.setVisible(false);
        welcomeOverlay.setVisible(false);
        landingHotspots.setVisible(true);
        landingHotspots.setManaged(true);
        showOnly(null);
    }

    public void showNameEntry() {
        nameEntryBackTarget = null;
        cardHeightRatio = 0.90;
        setBackground("dark-forest-bg.jpg");
        captionMask.setVisible(false);
        welcomeOverlay.setVisible(true);
        landingHotspots.setVisible(false);
        landingHotspots.setManaged(false);
        nameField.clear();
        nameErrorLabel.setText("");
        chooseAvatar("monkey");
        showOnly(namePanel);
        Platform.runLater(nameField::requestFocus);
    }

    public void showWelcomeBack() {
        cardHeightRatio = Math.max(0.74, Math.min(0.92, 670 / Math.max(1, screenRoot.getHeight())));
        setBackground("dark-forest-bg.jpg");
        captionMask.setVisible(false);
        welcomeOverlay.setVisible(true);
        landingHotspots.setVisible(false);
        landingHotspots.setManaged(false);
        String name = PlayerProgress.getPlayerName();
        returningNameLabel.setText("Detective " + name.toUpperCase(Locale.ROOT));
        returningAvatarImage.setImage(loadAvatarImage(PlayerProgress.getAvatar()));
        returningLevelLabel.setText("LV " + PlayerProgress.getLevel());
        returningXpLabel.setText(String.valueOf(PlayerProgress.getXp()));
        returningCasesLabel.setText(String.valueOf(PlayerProgress.getSolvedCaseCount()));
        showOnly(returningPanel);
    }

    private void showPlayerSelection() {
        showPlayerSelection(false);
    }

    private void showPlayerSelection(boolean keepListForSinglePlayer) {
        List<String> players = PlayerProgress.getPlayerNames();
        if (players.size() == 1 && !keepListForSinglePlayer) {
            PlayerProgress.selectPlayer(players.get(0));
            showWelcomeBack();
            return;
        }
        if (players.isEmpty()) {
            showNameEntry();
            return;
        }
        cardHeightRatio = Math.max(0.58, Math.min(0.90,
                (320 + players.size() * 90) / Math.max(1, screenRoot.getHeight())));
        setBackground("dark-forest-bg.jpg");
        captionMask.setVisible(false);
        welcomeOverlay.setVisible(true);
        landingHotspots.setVisible(false);
        landingHotspots.setManaged(false);
        playerChoices.getChildren().clear();
        for (String playerName : players) {
            String avatar = PlayerProgress.getAvatarFor(playerName);
            Button selectButton = new Button(playerName + "   ›");
            selectButton.getStyleClass().add("player-choice");
            selectButton.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            selectButton.setGraphicTextGap(12);
            ImageView avatarView = new ImageView(loadAvatarImage(avatar));
            avatarView.setFitWidth(78);
            avatarView.setFitHeight(60);
            avatarView.setPreserveRatio(true);
            avatarView.setSmooth(true);
            selectButton.setGraphic(avatarView);
            selectButton.setPrefHeight(74);
            selectButton.setOnAction(event -> {
                PlayerProgress.selectPlayer(playerName);
                showWelcomeBack();
            });

            Button deleteButton = new Button("×");
            deleteButton.getStyleClass().add("player-delete-button");
            deleteButton.setAccessibleText("Delete " + playerName);
            deleteButton.setTooltip(new Tooltip("Delete this player and their saved progress"));
            deleteButton.setOnAction(event -> confirmDeletePlayer(playerName));

            HBox playerRow = new HBox(8, selectButton, deleteButton);
            playerRow.setAlignment(javafx.geometry.Pos.CENTER);
            playerRow.getStyleClass().add("player-choice-row");
            playerChoices.getChildren().add(playerRow);
        }
        resizeReferenceArtwork();
        showOnly(playerSelectPanel);
    }

    @FXML
    private void handleEnterJungle() {
        List<String> players = PlayerProgress.getPlayerNames();
        if (players.size() > 1) {
            showPlayerSelection();
        } else if (players.size() == 1) {
            PlayerProgress.selectPlayer(players.get(0));
            showWelcomeBack();
        } else {
            showNameEntry();
        }
    }

    @FXML
    private void handleStartHunt() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        if (name.isBlank()) {
            nameErrorLabel.setText("Enter your name to continue.");
            nameField.requestFocus();
            return;
        }
        if (PlayerProgress.hasPlayer(name)) {
            showPlayerSelection();
            return;
        }
        PlayerProgress.createPlayer(name);
        PlayerProgress.setAvatar(chosenAvatar);
        StoryModeApp.showModeSelect();
    }

    @FXML
    private void handleContinue() {
        StoryModeApp.showModeSelect();
    }

    @FXML
    private void handleNewPlayer() {
        Node backTarget = returningPanel.isVisible() ? returningPanel
                : playerSelectPanel.isVisible() ? playerSelectPanel : null;
        openNameEntry(backTarget);
    }

    private void openNameEntry(Node backTarget) {
        showNameEntry();
        nameEntryBackTarget = backTarget;
    }

    @FXML
    private void handleBackFromNameEntry() {
        if (nameEntryBackTarget == returningPanel) {
            showWelcomeBack();
        } else if (nameEntryBackTarget == playerSelectPanel) {
            showPlayerSelection(true);
        } else {
            StoryModeApp.showWelcome();
        }
    }

    @FXML
    private void handleBackFromReturning() {
        if (PlayerProgress.getPlayerNames().size() > 1) {
            showPlayerSelection(true);
        } else {
            StoryModeApp.showWelcome();
        }
    }

    @FXML
    private void handleBackToWelcome() {
        StoryModeApp.showWelcome();
    }

    private void confirmDeletePlayer(String playerName) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete saved player");
        confirmation.setHeaderText("Delete " + playerName + "?");
        confirmation.setContentText("This also deletes this detective's saved progress.");
        confirmation.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
        confirmation.showAndWait().filter(ButtonType.OK::equals).ifPresent(button -> {
            if (PlayerProgress.deletePlayer(playerName)) {
                if (PlayerProgress.getPlayerNames().isEmpty()) showNameEntry();
                else showPlayerSelection(true);
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Could not delete player");
                error.setHeaderText("The saved player could not be removed.");
                error.setContentText("Check that the save folder is available, then try again.");
                error.showAndWait();
            }
        });
    }

    @FXML
    private void handleExitGame() {
        Platform.exit();
    }

    private void showOnly(Node active) {
        boolean showCard = active != null;
        cardBackdrop.setVisible(showCard);
        cardBackdrop.setManaged(showCard);
        namePanel.setVisible(active == namePanel);
        namePanel.setManaged(active == namePanel);
        returningPanel.setVisible(active == returningPanel);
        returningPanel.setManaged(active == returningPanel);
        playerSelectPanel.setVisible(active == playerSelectPanel);
        playerSelectPanel.setManaged(active == playerSelectPanel);
    }

    private void setBackground(String resource) {
        var imageUrl = WelcomeController.class.getResource(resource);
        if (imageUrl == null) throw new IllegalStateException("Missing welcome artwork: " + resource);
        welcomeBackground.setImage(new Image(imageUrl.toExternalForm()));
        resizeReferenceArtwork();
    }

    private void resizeReferenceArtwork() {
        if (screenRoot == null) return;
        double width = screenRoot.getWidth();
        double height = screenRoot.getHeight();
        welcomeBackground.setFitWidth(width);
        welcomeBackground.setFitHeight(height);

        double cardWidth = width * 0.417;
        double cardHeight = height * cardHeightRatio;
        sizeCard(cardBackdrop, cardWidth, cardHeight);
        sizeCard(namePanel, cardWidth, cardHeight);
        sizeCard(returningPanel, cardWidth, cardHeight);
        sizeCard(playerSelectPanel, cardWidth, cardHeight);
        namePanel.setTranslateY(height * 0.02);
        returningPanel.setTranslateY(height * 0.02);
        playerSelectPanel.setTranslateY(height * 0.02);

        double contentInset = cardWidth * 0.09;
        namePanel.setPadding(new Insets(30, contentInset, 30, contentInset));
        double formWidth = Math.max(1, cardWidth - contentInset * 2);
        nameField.setPrefWidth(formWidth);
        nameField.setMaxWidth(formWidth);
        startHuntButton.setPrefWidth(formWidth);
        startHuntButton.setMaxWidth(formWidth);
        startHuntButton.setPrefHeight(height * 0.096);

        double avatarGap = Math.max(3, width * 0.004);
        double avatarWidth = Math.max(1, (formWidth - avatarGap * 5) / AVATARS.size());
        avatarChoices.setSpacing(avatarGap);
        avatarChoices.setPrefWidth(formWidth);
        avatarChoices.setMaxWidth(formWidth);
        avatarChoices.getChildren().forEach(node -> node.setStyle(
                "-fx-pref-width: " + avatarWidth + "px; -fx-max-width: " + avatarWidth
                        + "px; -fx-pref-height: " + height * 0.09 + "px;"));

        double innerWidth = Math.max(1, Math.min(cardWidth * 0.76, cardWidth - 96));
        returningStats.setPrefWidth(innerWidth);
        returningStats.setMaxWidth(innerWidth);
        returningStats.setSpacing(Math.max(4, width * 0.007));
        returningStats.getChildren().forEach(node -> ((VBox) node).setMinWidth(0));
        returningActions.setPrefWidth(innerWidth);
        returningActions.setMaxWidth(innerWidth);
        double actionGap = Math.max(6, width * 0.009);
        returningActions.setSpacing(actionGap);
        double actionWidth = Math.max(1, innerWidth - actionGap);
        continueButton.setPrefWidth(actionWidth * 0.54);
        continueButton.setMaxWidth(actionWidth * 0.54);
        newPlayerButton.setPrefWidth(actionWidth * 0.34);
        newPlayerButton.setMaxWidth(actionWidth * 0.34);
        continueButton.setPrefHeight(height * 0.13);
        newPlayerButton.setPrefHeight(height * 0.13);

        double playerButtonWidth = Math.max(1, Math.min(cardWidth * 0.80, cardWidth - 92));
        playerChoices.getChildren().forEach(node -> {
            if (node instanceof HBox row && row.getChildren().size() == 2
                    && row.getChildren().get(0) instanceof Button selectButton
                    && row.getChildren().get(1) instanceof Button deleteButton) {
                double deleteWidth = Math.min(52, playerButtonWidth * 0.14);
                double selectWidth = Math.max(1, playerButtonWidth - deleteWidth - row.getSpacing());
                row.setPrefWidth(playerButtonWidth);
                row.setMaxWidth(playerButtonWidth);
                selectButton.setPrefWidth(selectWidth);
                selectButton.setMaxWidth(selectWidth);
                deleteButton.setPrefWidth(deleteWidth);
                deleteButton.setMaxWidth(deleteWidth);
            }
        });

        landingHotspots.setTranslateY(height * 0.215);
        landingHotspots.setSpacing(width * 0.04);
        double hotspotWidth = width * 0.115;
        double hotspotHeight = height * 0.24;
        landingHotspots.getChildren().forEach(node -> node.setStyle(
                "-fx-pref-width: " + hotspotWidth + "px; -fx-pref-height: " + hotspotHeight + "px;"));
        captionMask.setMaxWidth(width * 0.27);
        captionMask.setMaxHeight(height * 0.045);
    }

    private void sizeCard(javafx.scene.layout.Region card, double width, double height) {
        card.setPrefWidth(width);
        card.setPrefHeight(height);
        card.setMaxWidth(width);
        card.setMaxHeight(height);
    }

    private void createAvatarChoices() {
        for (AvatarOption avatar : AVATARS) {
            Button option = new Button(avatar.name().toUpperCase(Locale.ROOT));
            option.getStyleClass().add("avatar-choice");
            option.setWrapText(true);
            ImageView preview = new ImageView(loadAvatarImage(avatar.id()));
            preview.setFitWidth(56);
            preview.setFitHeight(45);
            preview.setPreserveRatio(true);
            preview.setSmooth(true);
            option.setGraphic(preview);
            option.setContentDisplay(javafx.scene.control.ContentDisplay.TOP);
            option.setOnAction(event -> chooseAvatar(avatar.id()));
            avatarChoices.getChildren().add(option);
        }
        chooseAvatar("monkey");
    }

    private void chooseAvatar(String avatarId) {
        chosenAvatar = avatarId;
        AvatarOption selected = avatarById(avatarId);
        selectedAvatarLabel.setText("Selected detective: " + selected.name());
        for (int i = 0; i < avatarChoices.getChildren().size(); i++) {
            Node option = avatarChoices.getChildren().get(i);
            option.getStyleClass().remove("avatar-choice-selected");
            if (AVATARS.get(i).id().equals(avatarId)) option.getStyleClass().add("avatar-choice-selected");
        }
    }

    private Image loadAvatarImage(String avatarId) {
        String safeId = avatarById(avatarId).id();
        var imageUrl = WelcomeController.class.getResource("avatars/" + safeId + ".png");
        if (imageUrl == null) throw new IllegalStateException("Missing avatar image: " + safeId);
        return new Image(imageUrl.toExternalForm());
    }

    private AvatarOption avatarById(String avatarId) {
        return AVATARS.stream().filter(avatar -> avatar.id().equalsIgnoreCase(avatarId))
                .findFirst().orElse(AVATARS.get(0));
    }

    private record AvatarOption(String id, String name) {}
}
