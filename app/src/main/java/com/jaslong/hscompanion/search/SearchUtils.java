package com.jaslong.hscompanion.search;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public final class SearchUtils {

    public static final String[] KEYWORDS = {
            "Armor",
            "Attack",
            "Basic",
            "Battlecry",
            "Beast",
            "Charge",
            "Choose One",
            "Combo",
            "Damage",
            "Deathrattle",
            "Destroy",
            "Divine Shield",
            "Dragon",
            "Draw",
            "Druid",
            "Enrage",
            "Epic",
            "Expert",
            "Free",
            "Freeze",
            "Frozen",
            "Goblins vs Gnomes",
            "Heal",
            "Health",
            "Hunter",
            "Immune",
            "Legendary",
            "Mana",
            "Mage",
            "Mech",
            "Minion",
            "Murloc",
            "Naxxramas",
            "Overload",
            "Paladin",
            "Pirate",
            "Priest",
            "Random",
            "Rare",
            "Restore",
            "Rogue",
            "Secret",
            "Shaman",
            "Silence",
            "Spell",
            "Spell damage",
            "Stealth",
            "Summon",
            "Taunt",
            "Transform",
            "Warlock",
            "Warrior",
            "Weapon",
            "Windfury",
    };

    private static final Set<String> SEARCHABLE_KEYWORDS;
    private static final String[] DIFFERENT_KEYWORDS;
    static {
        Set<String> keywords = new HashSet<>();
        List<String> differentKeywords = new LinkedList<>();
        for (String keyword : KEYWORDS) {
            String reducedKeyword = reduce(keyword);
            String searchableKeyword = toSearchTerm(reducedKeyword);
            keywords.add(searchableKeyword);
            if (!keyword.equals(searchableKeyword)) {
                differentKeywords.add(reducedKeyword);
            }
        }
        SEARCHABLE_KEYWORDS = Collections.unmodifiableSet(keywords);
        DIFFERENT_KEYWORDS = differentKeywords.toArray(new String[differentKeywords.size()]);
    }

    public static Set<String> tokenize(String str) {
        if (str == null || str.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> tokens = new HashSet<>();
        for (String token : reduce(str).split(" ")) {
            if (!token.isEmpty()) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    public static Set<String> tokenizeAndGetAllSubstrings(String str) {
        if (str == null || str.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> tokens = tokenize(str);
        Set<String> substrings = new HashSet<>();
        for (String token : tokens) {
            for (int i = 0; i < token.length() + 1; ++i) {
                substrings.add(token.substring(0, i));
            }
        }
        return substrings;
    }

    /**
     * Converts a string to a searchable version of the string.
     */
    public static Set<String> toSearchTerms(String str) {
        if (str == null || str.isEmpty()) {
            return Collections.emptySet();
        }

        str = reduce(str);
        for (String keyword : DIFFERENT_KEYWORDS) {
            str = str.replaceAll(keyword, toSearchTerm(keyword));
        }
        return new HashSet<>(Arrays.asList(str.split("\\s+")));
    }

    public static Set<String> findKeywords(String str) {
        if (str == null || str.isEmpty()) {
            return Collections.emptySet();
        }

        HashSet<String> keywords = new HashSet<>();
        for (String searchTerm : toSearchTerms(str)) {
            if (SEARCHABLE_KEYWORDS.contains(searchTerm)) {
                keywords.add(searchTerm);
            }
        }
        return keywords;
    }

    /**
     * Reduce strings to alphabetical characters and removes extra whitespace.
     */
    private static String reduce(String str) {
        return str
                .trim()
                .toLowerCase()
                .replaceAll("'", "")
                .replaceAll("[^a-z0-9]+", " ");
    }

    private static String toSearchTerm(String str) {
        return str.replaceAll(" ", "");
    }

    private SearchUtils() {
        throw new UnsupportedOperationException();
    }

}
