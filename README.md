# Java Jungle

JavaFX and Maven learning game with Story Mode and Chapter Mode.

## Included

- Themed mode selection screen for Story Mode and Chapter Mode.
- Story Mode reads its 500 questions from `src/main/resources/org/javajungle/story-mode-questions.json`: 50 per chapter, split into 20 Easy, 20 Medium, and 10 Hard.
- Story Mode displays broken code, checks fixes against `fixedCode`, and uses each question's hint and explanation.
- Chapter Mode reads ten separate JSON files from `src/main/resources/org/javajungle/chapter-questions/`.
- Chapter Mode has 300 questions: 30 per chapter, with 10 Easy, 10 Medium, and 10 Hard questions. Players choose the missing line from the provided options and see the explanation before advancing.
- Profile and dashboard progress indicators are sample values and are not persisted.

## Requirements

- JDK 25
- IntelliJ IDEA with Maven support

## Run in IntelliJ IDEA

1. Open the repository folder in IntelliJ IDEA.
2. Set the Project SDK to JDK 25 if IntelliJ asks.
3. Load `pom.xml` as a Maven project.
4. In the Maven tool window, run **Plugins → javafx → javafx:run**.

Maven resolves JavaFX and Jackson dependencies from `pom.xml`.
