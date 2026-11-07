package org.javajungle;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/** Faint matrix-style binary texture behind the Story Mode panels. */
public final class BinaryRain extends Region {
    private final Canvas canvas = new Canvas();

    public BinaryRain() {
        setMouseTransparent(true);
        getChildren().add(canvas);
        widthProperty().addListener((obs, oldValue, newValue) -> requestLayout());
        heightProperty().addListener((obs, oldValue, newValue) -> requestLayout());
    }

    @Override
    protected void layoutChildren() {
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
        drawRain();
    }

    private void drawRain() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        if (width <= 0 || height <= 0) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);
        gc.setFont(Font.font("Consolas", 11));

        int rows = (int) (height / 15) + 1;
        int column = 0;
        for (double x = 10; x < width - 8; x += 18, column++) {
            int headRow = Math.floorMod(column * 29, rows + 12) - 8;
            for (int row = 0; row < rows; row++) {
                int pattern = Math.floorMod(column * 17 + row * 11, 9);
                if (pattern == 0 || pattern == 5) continue;

                double y = row * 15 + 12;
                double fade = 1.0 - (y / Math.max(height, 1));
                double alpha = 0.035 + Math.max(0, fade) * 0.12;
                int distanceFromHead = Math.abs(row - headRow);
                if (distanceFromHead < 4) alpha = 0.20 - distanceFromHead * 0.035;

                String bit = Math.floorMod(column * 7 + row * 13, 2) == 0 ? "0" : "1";
                gc.setFill(Color.web((column % 4 == 0) ? "#00dbe3" : "#17d58c", Math.max(0.025, alpha)));
                gc.fillText(bit, x, y);
            }
        }
    }
}
