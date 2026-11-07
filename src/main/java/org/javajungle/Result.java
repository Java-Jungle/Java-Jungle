package org.javajungle;

/** Result returned by the Story Mode checker. */
public class Result {
    private final boolean correct;
    private final Topic topic;
    private final int lineNumber;
    private final String message;

    public Result(boolean correct, Topic topic, int lineNumber, String message) {
        this.correct = correct;
        this.topic = topic;
        this.lineNumber = lineNumber;
        this.message = message;
    }

    public boolean isCorrect() { return correct; }
    public Topic getTopic() { return topic; }
    public int getLineNumber() { return lineNumber; }
    public String getMessage() { return message; }
}
