package org.javajungle;

/** Builds topic-aware hints for an incorrect Story Mode submission. */
public final class FeedbackBuilder {
    private FeedbackBuilder() {}

    public static String build(Topic topic, int lineNumber, boolean unchanged, boolean empty) {
        if (empty) return "You haven't submitted any code yet. Edit the code to fix the bug, then press Submit.";
        String where = lineNumber > 0 ? " Look closely at line " + lineNumber + "." : "";
        String start = unchanged ? "Your code is still the same as the buggy version." : "Not quite right.";
        return start + " The bug is about " + topic.getDisplayName() + "." + where + " Hint: " + hint(topic);
    }

    private static String hint(Topic topic) {
        return switch (topic) {
            case VARIABLES -> "Check the variable's type, its name, and the value it starts with.";
            case OPERATORS -> "Check the operator. Is it the one the story needs (for example, + vs -, == vs =, or < vs <=)?";
            case CONDITIONAL_LOGIC -> "Check the condition inside the if. Is it testing the right thing, in the right order?";
            case LOOPS -> "Check where the loop starts, where it stops, and how much it counts each time.";
            case ARRAYS -> "Check the index you use. Arrays start at 0 and end at length - 1.";
            case STRINGS -> "Check how the text is compared or changed. Remember equals() instead of ==.";
            case METHODS -> "Check the parameters, what the method returns, and how it is called.";
            case CLASSES -> "Check how the object is created and which fields or methods it uses.";
            case STACK_MEMORY -> "Remember last in, first out (LIFO). Check the order items are pushed and popped.";
            case NULL_REFERENCES -> "Check whether something could be null before you use it.";
        };
    }
}
