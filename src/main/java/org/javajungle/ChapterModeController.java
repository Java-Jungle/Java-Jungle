package org.javajungle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class ChapterModeController {
    private record Chapter(String number, String title, String description) {}

    private static final List<Chapter> CHAPTERS = List.of(
            new Chapter("01", "The Variable Vine", "Name, store, and initialize values in your program."),
            new Chapter("02", "Operator Outpost", "Use arithmetic, comparison, and assignment operators."),
            new Chapter("03", "Conditional Canopy", "Choose a program path using conditions and branches."),
            new Chapter("04", "Loop Lagoon", "Repeat instructions and control loop boundaries."),
            new Chapter("05", "Array Adventure", "Store collections and work safely with indexes."),
            new Chapter("06", "String Swamp", "Build, inspect, and compare text values."),
            new Chapter("07", "Method Mountain", "Use methods, parameters, and return values."),
            new Chapter("08", "Class Caverns", "Model objects with fields, constructors, and methods."),
            new Chapter("09", "Stack Safari", "Trace last-in, first-out data structures."),
            new Chapter("10", "Null Pointer Marsh", "Recognize and safely handle null references.")
    );

    @FXML private VBox chapterList;
    @FXML private VBox chapterListPage;
    @FXML private VBox chapterDetailPage;
    @FXML private Label chapterNumber;
    @FXML private Label chapterTitle;
    @FXML private Label chapterDescription;
    @FXML private Label heartsLabel;
    @FXML private Label xpLabel;
    private int selectedChapter = 1;

    @FXML
    private void initialize() {
        heartsLabel.setText(PlayerProgress.getHeartsText());
        xpLabel.setText(PlayerProgress.getXp() + " XP");
        for (Chapter chapter : CHAPTERS) {
            Label number = new Label("CHAPTER " + chapter.number());
            number.getStyleClass().add("chapter-number");
            Label title = new Label(chapter.title());
            title.getStyleClass().add("chapter-card-title");
            Label description = new Label(chapter.description());
            description.getStyleClass().add("chapter-card-copy");
            description.setWrapText(true);
            Button open = new Button("OPEN  →");
            open.getStyleClass().add("active-case-open");
            open.setOnAction(event -> openChapter(chapter));

            VBox copy = new VBox(5, number, title, description);
            HBox row = new HBox(16, copy, open);
            row.getStyleClass().add("chapter-row");
            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            row.getChildren().add(1, spacer);
            chapterList.getChildren().add(row);
        }
    }

    private void openChapter(Chapter chapter) {
        selectedChapter = Integer.parseInt(chapter.number());
        chapterNumber.setText("CHAPTER " + chapter.number());
        chapterTitle.setText(chapter.title().toUpperCase());
        chapterDescription.setText(chapter.description());
        chapterListPage.setVisible(false);
        chapterListPage.setManaged(false);
        chapterDetailPage.setVisible(true);
        chapterDetailPage.setManaged(true);
    }

    @FXML
    private void handleBackToChapters() {
        chapterDetailPage.setVisible(false);
        chapterDetailPage.setManaged(false);
        chapterListPage.setVisible(true);
        chapterListPage.setManaged(true);
    }

    @FXML
    private void handleBackToModes() {
        StoryModeApp.showModeSelect();
    }

    @FXML private void handleEasy() { StoryModeApp.startChapterSession(selectedChapter, "EASY"); }
    @FXML private void handleMedium() { StoryModeApp.startChapterSession(selectedChapter, "MEDIUM"); }
    @FXML private void handleHard() { StoryModeApp.startChapterSession(selectedChapter, "HARD"); }
}
