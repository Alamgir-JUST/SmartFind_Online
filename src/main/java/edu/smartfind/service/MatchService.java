package edu.smartfind.service;

import edu.smartfind.model.Item;
import java.util.*;
import java.time.temporal.ChronoUnit;

/** Transparent teaching example: rule-based similarity, NOT a trained AI model. */
public final class MatchService {
    private MatchService() {}
    public record Match(Item item, int score) {}
    public static int score(Item source, Item candidate) {
        if (source.id() == candidate.id() || source.type().equals(candidate.type())
                || !source.status().equals("OPEN") || !candidate.status().equals("OPEN")) return 0;
        int score = 0;
        if (equalsText(source.category(),candidate.category())) score += 30;
        if (equalsText(source.color(),candidate.color())) score += 20;
        if (equalsText(source.location(),candidate.location())) score += 20;
        Set<String> a = tokens(source.name() + " " + source.description());
        Set<String> b = tokens(candidate.name() + " " + candidate.description());
        a.retainAll(b);
        if (!a.isEmpty()) score += 20;
        long days = Math.abs(ChronoUnit.DAYS.between(source.date(),candidate.date()));
        if (days <= 7) score += 10;
        else if (days <= 30) score += 5;
        return score;
    }
    private static boolean equalsText(String a,String b) { return a.trim().equalsIgnoreCase(b.trim()); }
    private static Set<String> tokens(String s) {
        Set<String> out = new HashSet<>();
        for (String w : s.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) if (w.length() >= 3) out.add(w);
        return out;
    }
    public static List<Match> find(Item source, List<Item> all) {
        return all.stream().map(i -> new Match(i,score(source,i))).filter(m -> m.score() > 0)
                .sorted(Comparator.comparingInt(Match::score).reversed()).toList();
    }
}
