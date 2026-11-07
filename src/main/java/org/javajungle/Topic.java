package org.javajungle;

public enum Topic {
    VARIABLES("Variables"),
    OPERATORS("Operators"),
    CONDITIONAL_LOGIC("Conditional Logic"),
    LOOPS("Loops"),
    ARRAYS("Arrays"),
    STRINGS("Strings"),
    METHODS("Methods"),
    CLASSES("Classes"),
    STACK_MEMORY("Stack Memory (LIFO)"),
    NULL_REFERENCES("Null References");

    private final String displayName;
    Topic(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
