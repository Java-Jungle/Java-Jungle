package org.javajungle;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.layout.Region;

/** Draws the responsive HUD and jungle vines around the Story Mode terminal. */
public final class VineFrame extends Region {
    private static final Color GREEN = Color.web("#17b873");
    private static final Color BRIGHT_GREEN = Color.web("#39e99a");
    private static final Color CYAN = Color.web("#11c8c6");
    private final Canvas canvas = new Canvas();

    public VineFrame() {
        setMouseTransparent(true);
        getChildren().add(canvas);
        widthProperty().addListener((obs, oldValue, newValue) -> requestLayout());
        heightProperty().addListener((obs, oldValue, newValue) -> requestLayout());
    }

    @Override
    protected void layoutChildren() {
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
        drawFrame();
    }

    private void drawFrame() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        if (width <= 0 || height <= 0) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);
        drawHudBorder(gc, width, height);
        drawVines(gc, width, height);
    }

    private void drawHudBorder(GraphicsContext gc, double width, double height) {
        gc.setStroke(Color.web("#0f704f", 0.95));
        gc.setLineWidth(2.2);
        gc.beginPath();
        gc.moveTo(24, 45);
        gc.lineTo(24, 24);
        gc.lineTo(45, 9);
        gc.lineTo(width - 45, 9);
        gc.lineTo(width - 24, 29);
        gc.lineTo(width - 24, height - 29);
        gc.lineTo(width - 45, height - 9);
        gc.lineTo(45, height - 9);
        gc.lineTo(24, height - 29);
        gc.closePath();
        gc.stroke();

        gc.setStroke(Color.web("#0bc7b3", 0.55));
        gc.setLineWidth(1);
        gc.strokeRoundRect(31, 18, width - 62, height - 36, 14, 14);
        gc.setStroke(CYAN.deriveColor(0, 1, 1, 0.38));
        gc.strokeLine(56, 22, 112, 22);
        gc.strokeLine(width - 112, height - 22, width - 56, height - 22);
        gc.strokeLine(22, height * 0.2, 22, height * 0.31);
        gc.strokeLine(width - 22, height * 0.68, width - 22, height * 0.8);
    }

    private void drawVines(GraphicsContext gc, double width, double height) {
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        strokeVinePaths(gc, width, height, GREEN.deriveColor(0, 1, 1, 0.26), 15);
        strokeVinePaths(gc, width, height, GREEN, 6.5);
        strokeVinePaths(gc, width, height, BRIGHT_GREEN.deriveColor(0, 1, 1, 0.78), 2.1);
        drawLeaves(gc, width, height);
    }

    private void strokeVinePaths(GraphicsContext gc, double width, double height, Color color, double lineWidth) {
        gc.setStroke(color);
        gc.setLineWidth(lineWidth);
        double sideInset = Math.max(22, width * 0.018);
        double topY = Math.max(22, height * 0.04);
        // Keep the lower vine and leaf strokes inside the canvas; their curves
        // extend below the anchor line and were getting clipped at the edge.
        double bottomY = height - topY - 22;

        gc.beginPath();
        gc.moveTo(sideInset, topY + 19);
        for (double x = sideInset; x < width - sideInset; x += 170) {
            double end = Math.min(x + 170, width - sideInset);
            gc.bezierCurveTo(x + 42, topY - 28, x + 105, topY + 39, end, topY + 3);
        }
        gc.stroke();

        gc.beginPath();
        gc.moveTo(sideInset, bottomY - 12);
        for (double x = sideInset; x < width - sideInset; x += 170) {
            double end = Math.min(x + 170, width - sideInset);
            gc.bezierCurveTo(x + 45, bottomY + 30, x + 110, bottomY - 38, end, bottomY - 2);
        }
        gc.stroke();

        for (int side = 0; side < 2; side++) {
            double x = side == 0 ? sideInset : width - sideInset;
            gc.beginPath();
            gc.moveTo(x, topY + 14);
            for (double y = topY + 14; y < height - topY - 14; y += 150) {
                double end = Math.min(y + 150, height - topY - 14);
                double bend = side == 0 ? 18 : -18;
                gc.bezierCurveTo(x + bend, y + 35, x - bend, y + 105, x, end);
            }
            gc.stroke();
        }
    }

    private void drawLeaves(GraphicsContext gc, double width, double height) {
        double sideInset = Math.max(22, width * 0.018);
        double topY = Math.max(22, height * 0.04);
        double bottomY = height - topY - 22;
        gc.setFill(Color.web("#167d51"));
        gc.setStroke(Color.web("#69efa8", 0.88));
        gc.setLineWidth(1.1);

        for (double x = 78; x < width - 50; x += 145) {
            drawLeaf(gc, x, topY + (Math.sin(x * 0.028) * 14), (x % 2 == 0) ? -42 : 42);
            drawLeaf(gc, x + 52, bottomY - (Math.sin(x * 0.023) * 13), (x % 2 == 0) ? 138 : -138);
        }
        for (double y = 120; y < height - 70; y += 150) {
            drawLeaf(gc, sideInset + 3, y, -8);
            drawLeaf(gc, width - sideInset - 3, y + 55, 188);
        }
    }

    private void drawLeaf(GraphicsContext gc, double x, double y, double angle) {
        gc.save();
        gc.translate(x, y);
        gc.rotate(angle);
        gc.beginPath();
        gc.moveTo(0, 0);
        gc.bezierCurveTo(-4, -13, -21, -19, -27, -7);
        gc.bezierCurveTo(-23, 4, -9, 9, 0, 0);
        gc.closePath();
        gc.fill();
        gc.stroke();
        gc.beginPath();
        gc.moveTo(-2, 0);
        gc.lineTo(-21, -7);
        gc.stroke();
        gc.restore();
    }
}
