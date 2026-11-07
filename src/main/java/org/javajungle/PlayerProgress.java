package org.javajungle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/** Persistent player XP, answer history, skill accuracy, and hearts. */
public final class PlayerProgress {
    public static final int STORY_XP = 50;
    public static final int CHAPTER_XP = 10;
    public static final int XP_PER_LEVEL = 100;
    public static final int MAX_HEARTS = 3;

    private static final Path SAVE_DIRECTORY = Path.of(System.getProperty("user.home"), ".javajungle");
    private static final Path LEGACY_SAVE_FILE = SAVE_DIRECTORY.resolve("javajungle_save.dat");
    private static final Path PLAYER_DIRECTORY = SAVE_DIRECTORY.resolve("players");
    private static final Path PLAYER_INDEX_FILE = SAVE_DIRECTORY.resolve("players.dat");
    private static final List<String> PLAYER_NAMES = new ArrayList<>();
    private static Properties DATA = new Properties();
    private static String activePlayer = "";

    static {
        initializePlayers();
    }

    private PlayerProgress() {}

    public static String getPlayerName() { return DATA.getProperty("playerName", "").trim(); }
    public static boolean hasPlayerName() { return !getPlayerName().isBlank(); }
    public static List<String> getPlayerNames() { return List.copyOf(PLAYER_NAMES); }
    public static boolean hasPlayer(String name) { return findPlayer(name) >= 0; }

    public static boolean deletePlayer(String name) {
        int index = findPlayer(name);
        if (index < 0) return false;
        String canonicalName = PLAYER_NAMES.get(index);
        boolean deletingActivePlayer = canonicalName.equalsIgnoreCase(activePlayer);
        if (deletingActivePlayer) save();
        try {
            Files.deleteIfExists(profilePath(canonicalName));
            Properties legacy = loadProperties(LEGACY_SAVE_FILE);
            if (canonicalName.equalsIgnoreCase(legacy.getProperty("playerName", "").trim())) {
                Files.deleteIfExists(LEGACY_SAVE_FILE);
            }
        } catch (IOException exception) {
            return false;
        }
        PLAYER_NAMES.remove(index);
        if (deletingActivePlayer) {
            activePlayer = "";
            DATA = new Properties();
        }
        if (deletingActivePlayer && !PLAYER_NAMES.isEmpty()) {
            selectPlayer(PLAYER_NAMES.get(0));
        } else {
            savePlayerIndex();
        }
        return true;
    }

    public static void setPlayerName(String name) {
        if (name == null || name.isBlank()) return;
        if (!createPlayer(name)) selectPlayer(name);
    }

    public static boolean createPlayer(String name) {
        String cleaned = name == null ? "" : name.trim();
        if (cleaned.isBlank()) return false;
        int existing = findPlayer(cleaned);
        if (existing >= 0) {
            selectPlayer(PLAYER_NAMES.get(existing));
            return false;
        }
        save();
        PLAYER_NAMES.add(cleaned);
        activePlayer = cleaned;
        DATA = new Properties();
        DATA.setProperty("playerName", cleaned);
        DATA.setProperty("avatar", "monkey");
        save();
        return true;
    }

    public static boolean selectPlayer(String name) {
        int index = findPlayer(name);
        if (index < 0) return false;
        String selected = PLAYER_NAMES.get(index);
        if (selected.equalsIgnoreCase(activePlayer)) return true;
        save();
        activePlayer = selected;
        Path profile = profilePath(selected);
        if (Files.exists(profile)) {
            DATA = loadProperties(profile);
        } else {
            Properties legacy = loadProperties(LEGACY_SAVE_FILE);
            DATA = selected.equalsIgnoreCase(legacy.getProperty("playerName", "").trim())
                    ? legacy : new Properties();
        }
        DATA.setProperty("playerName", selected);
        DATA.putIfAbsent("avatar", "monkey");
        save();
        return true;
    }

    public static String getAvatar() {
        return DATA.getProperty("avatar", "monkey").trim().toLowerCase(Locale.ROOT);
    }

    public static String getAvatarFor(String playerName) {
        int index = findPlayer(playerName);
        if (index < 0) return "monkey";
        String canonicalName = PLAYER_NAMES.get(index);
        if (canonicalName.equalsIgnoreCase(activePlayer)) return getAvatar();
        Path profile = profilePath(canonicalName);
        Properties properties = Files.exists(profile) ? loadProperties(profile) : loadProperties(LEGACY_SAVE_FILE);
        return properties.getProperty("avatar", "monkey").trim().toLowerCase(Locale.ROOT);
    }

    public static void setAvatar(String avatar) {
        if (avatar == null || avatar.isBlank()) return;
        DATA.setProperty("avatar", avatar.trim().toLowerCase(Locale.ROOT));
        save();
    }

    private static int findPlayer(String name) {
        if (name == null) return -1;
        String candidate = name.trim();
        for (int i = 0; i < PLAYER_NAMES.size(); i++) {
            if (PLAYER_NAMES.get(i).equalsIgnoreCase(candidate)) return i;
        }
        return -1;
    }

    public static int getXp() { return getInt("xp", 0); }
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
    public static int getOpenedCaseCount() {
        Set<String> opened = getSet("openedCases");
        opened.addAll(getSet("solvedCases"));
        return opened.size();
    }
    public static boolean isCaseOpened(int caseId) {
        String key = String.valueOf(caseId);
        return getSet("openedCases").contains(key) || getSet("solvedCases").contains(key);
    }

    public static void recordCaseOpened(int caseId) {
        Set<String> opened = getSet("openedCases");
        if (opened.add(String.valueOf(caseId))) {
            putSet("openedCases", opened);
            save();
        }
    }

    /** Persist the topic missed by the Story Mode checker; this is separate from chapter accuracy. */
    public static void recordStoryMistake(Topic topic) {
        if (topic == null) return;
        String key = "storyMistakes." + topic.name();
        DATA.setProperty(key, String.valueOf(getInt(key, 0) + 1));
        save();
    }

    public static int getStoryMistakes(Topic topic) {
        return topic == null ? 0 : getInt("storyMistakes." + topic.name(), 0);
    }

    /** Accuracy for a chapter, counting Chapter Mode questions only. */
    public static int getSkillPercent(int chapter) {
        int attempts = getInt("chapterAttempts." + chapter, 0);
        return attempts == 0 ? 0 : Math.min(100, getInt("chapterCorrect." + chapter, 0) * 100 / attempts);
    }

    public static int getChapterAttemptCount(int chapter) {
        return getInt("chapterAttempts." + chapter, 0);
    }

    public static int getHearts() {
        return MAX_HEARTS;
    }

    public static String getHeartsText() {
        int hearts = getHearts();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < MAX_HEARTS; i++) {
            if (i > 0) text.append(' ');
            text.append(i < hearts ? '♥' : '♡');
        }
        return text.toString();
    }

    /** Retained for compatibility; endless play does not use heart timers. */
    public static long getMinutesToNextHeart() {
        return 0;
    }

    /** Awards 50 XP for every correct repair while counting each cleared case once. */
    public static int recordStory(StoryCase storyCase, boolean correct) {
        int earned = 0;
        if (correct) {
            Set<String> solved = getSet("solvedCases");
            if (solved.add(String.valueOf(storyCase.id()))) {
                putSet("solvedCases", solved);
            }
            earned = STORY_XP;
            addXp(earned);
        }
        save();
        return earned;
    }

    /** Awards 10 XP for every correct chapter answer. */
    public static int recordChapterAnswer(ChapterQuestion question, boolean correct) {
        recordChapterAttempt(question.chapter(), correct);
        int earned = 0;
        if (correct) {
            Set<String> solved = getSet("solvedQuestions");
            solved.add(question.chapter() + "-" + question.id());
            putSet("solvedQuestions", solved);
            earned = CHAPTER_XP;
            addXp(earned);
        }
        save();
        return earned;
    }

    /** Clears saved progress for development. Do not call automatically at app startup. */
    public static void reset() {
        DATA.clear();
        save();
    }

    private static void recordChapterAttempt(int chapter, boolean correct) {
        DATA.setProperty("chapterAttempts." + chapter, String.valueOf(getInt("chapterAttempts." + chapter, 0) + 1));
        if (correct) DATA.setProperty("chapterCorrect." + chapter,
                String.valueOf(getInt("chapterCorrect." + chapter, 0) + 1));
    }

    private static void addXp(int amount) { DATA.setProperty("xp", String.valueOf(getXp() + amount)); }

    private static int getInt(String key, int fallback) {
        try { return Integer.parseInt(DATA.getProperty(key, String.valueOf(fallback))); }
        catch (NumberFormatException exception) { return fallback; }
    }

    private static Set<String> getSet(String key) {
        Set<String> set = new HashSet<>();
        String raw = DATA.getProperty(key, "");
        if (!raw.isBlank()) for (String part : raw.split(",")) set.add(part.trim());
        return set;
    }

    private static void putSet(String key, Set<String> set) { DATA.setProperty(key, String.join(",", set)); }

    private static void initializePlayers() {
        Properties index = loadProperties(PLAYER_INDEX_FILE);
        int playerCount = getInt(index, "playerCount", 0);
        for (int i = 0; i < playerCount; i++) addPlayerName(index.getProperty("player." + i, ""));

        Properties legacy = loadProperties(LEGACY_SAVE_FILE);
        String legacyName = legacy.getProperty("playerName", "").trim();
        if (!legacyName.isBlank()) addPlayerName(legacyName);

        String savedActivePlayer = index.getProperty("activePlayer", "").trim();
        int activeIndex = findPlayer(savedActivePlayer);
        if (activeIndex < 0 && PLAYER_NAMES.size() == 1) activeIndex = 0;
        if (activeIndex >= 0) {
            activePlayer = PLAYER_NAMES.get(activeIndex);
            Path profile = profilePath(activePlayer);
            if (Files.exists(profile)) {
                DATA = loadProperties(profile);
            } else if (activePlayer.equalsIgnoreCase(legacyName)) {
                DATA = legacy;
            }
            DATA.setProperty("playerName", activePlayer);
            DATA.putIfAbsent("avatar", "monkey");
            save();
        } else {
            savePlayerIndex();
        }
    }

    private static void addPlayerName(String name) {
        String cleaned = name == null ? "" : name.trim();
        if (!cleaned.isBlank() && findPlayer(cleaned) < 0) PLAYER_NAMES.add(cleaned);
    }

    private static Path profilePath(String playerName) {
        String stableId = UUID.nameUUIDFromBytes(playerName.toLowerCase(Locale.ROOT)
                .getBytes(StandardCharsets.UTF_8)).toString();
        return PLAYER_DIRECTORY.resolve(stableId + ".dat");
    }

    private static Properties loadProperties(Path file) {
        Properties props = new Properties();
        if (Files.exists(file)) {
            try (InputStream in = Files.newInputStream(file)) { props.load(in); }
            catch (IOException ignored) { /* Start with default progress if the save cannot be read. */ }
        }
        return props;
    }

    private static void save() {
        try {
            Files.createDirectories(SAVE_DIRECTORY);
            if (!activePlayer.isBlank()) {
                Files.createDirectories(PLAYER_DIRECTORY);
                DATA.setProperty("playerName", activePlayer);
                try (OutputStream out = Files.newOutputStream(profilePath(activePlayer))) {
                    DATA.store(out, "Java Jungle player save");
                }
            }
            savePlayerIndex();
        } catch (IOException ignored) { /* Keep the game playable if persistence is unavailable. */ }
    }

    private static void savePlayerIndex() {
        try {
            Files.createDirectories(SAVE_DIRECTORY);
            Properties index = new Properties();
            index.setProperty("playerCount", String.valueOf(PLAYER_NAMES.size()));
            for (int i = 0; i < PLAYER_NAMES.size(); i++) {
                index.setProperty("player." + i, PLAYER_NAMES.get(i));
            }
            if (!activePlayer.isBlank()) index.setProperty("activePlayer", activePlayer);
            try (OutputStream out = Files.newOutputStream(PLAYER_INDEX_FILE)) {
                index.store(out, "Java Jungle player list");
            }
        } catch (IOException ignored) { /* The game remains playable if the player list cannot be saved. */ }
    }

    private static int getInt(Properties properties, String key, int fallback) {
        try { return Integer.parseInt(properties.getProperty(key, String.valueOf(fallback))); }
        catch (NumberFormatException exception) { return fallback; }
    }
}
