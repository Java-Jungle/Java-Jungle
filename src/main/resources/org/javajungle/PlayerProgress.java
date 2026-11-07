package org.javajungle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public final class PlayerProgress {
    public static final int STORY_XP = 50;
    public static final int CHAPTER_XP = 10;
    public static final int XP_PER_LEVEL = 100;

    private static final Path SAVE_FILE =
            Path.of(System.getProperty("user.home"), ".javajungle", "javajungle_save.dat");
    private static final Properties DATA = load();

    private PlayerProgress() {}

    public static String getPlayerName() { return DATA.getProperty("playerName", "").trim(); }
    public static boolean hasPlayerName() { return !getPlayerName().isBlank(); }
    public static void setPlayerName(String name) {
        if (name == null || name.isBlank()) return;
        DATA.setProperty("playerName", name.trim());
        save();
    }

    // ---------- reading ----------
    public static int getXp() { return getInt("xp"); }
    public static int getLevel() { return getXp() / XP_PER_LEVEL + 1; }
    public static double getLevelProgress() { return (getXp() % XP_PER_LEVEL) / (double) XP_PER_LEVEL; }

    public static String getRank() {
        int level = getLevel();
        if (level >= 10) return "Jungle chief";
        if (level >= 6) return "Canopy tracker";
        if (level >= 3) return "Vine swinger";
        return "Seedling";
    }

    public static int getSolvedCaseCount() { return getSet("solvedCases").size(); }
    public static boolean isCaseSolved(int caseId) { return getSet("solvedCases").contains(String.valueOf(caseId)); }

    /** Percent correct for one chapter (1-10), counting story cases and chapter questions. */
    public static int getSkillPercent(int chapter) {
        int attempts = getInt("attempts." + chapter);
        return attempts == 0 ? 0 : getInt("correct." + chapter) * 100 / attempts;
    }

    // ---------- recording ----------
    /** Returns the XP earned: 50 the first time a case is solved, otherwise 0. */
    public static int recordStory(StoryCase storyCase, boolean correct) {
        recordAttempt(storyCase.chapter(), correct);
        int earned = 0;
        if (correct) {
            Set<String> solved = getSet("solvedCases");
            if (solved.add(String.valueOf(storyCase.id()))) {
                putSet("solvedCases", solved);
                earned = STORY_XP;
                addXp(earned);
            }
        }
        save();
        return earned;
    }

    /** Returns the XP earned: 10 the first time a question is answered correctly, otherwise 0. */
    public static int recordChapterAnswer(ChapterQuestion question, boolean correct) {
        recordAttempt(question.chapter(), correct);
        int earned = 0;
        if (correct) {
            Set<String> solved = getSet("solvedQuestions");
            if (solved.add(question.chapter() + "-" + question.id())) {
                putSet("solvedQuestions", solved);
                earned = CHAPTER_XP;
                addXp(earned);
            }
        }
        save();
        return earned;
    }

    /** Wipes all progress (handy while testing). */
    public static void reset() {
        DATA.clear();
        save();
    }

    // ---------- internals ----------
    private static void recordAttempt(int chapter, boolean correct) {
        DATA.setProperty("attempts." + chapter, String.valueOf(getInt("attempts." + chapter) + 1));
        if (correct) {
            DATA.setProperty("correct." + chapter, String.valueOf(getInt("correct." + chapter) + 1));
        }
    }

    private static void addXp(int amount) {
        DATA.setProperty("xp", String.valueOf(getXp() + amount));
    }

    private static int getInt(String key) {
        try {
            return Integer.parseInt(DATA.getProperty(key, "0"));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private static Set<String> getSet(String key) {
        Set<String> set = new HashSet<>();
        String raw = DATA.getProperty(key, "");
        if (!raw.isBlank()) {
            for (String part : raw.split(",")) set.add(part.trim());
        }
        return set;
    }

    private static void putSet(String key, Set<String> set) {
        DATA.setProperty(key, String.join(",", set));
    }

    private static Properties load() {
        Properties props = new Properties();
        if (Files.exists(SAVE_FILE)) {
            try (InputStream in = Files.newInputStream(SAVE_FILE)) {
                props.load(in);
            } catch (IOException ignored) {
                // start fresh if the save file can't be read
            }
        }
        return props;
    }

    private static void save() {
        try {
            Files.createDirectories(SAVE_FILE.getParent());
            try (OutputStream out = Files.newOutputStream(SAVE_FILE)) {
                DATA.store(out, "Java Jungle save");
            }
        } catch (IOException ignored) {
            // progress just won't persist if the disk write fails
        }
    }
}
