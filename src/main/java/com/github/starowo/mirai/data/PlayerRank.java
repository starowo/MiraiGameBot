package com.github.starowo.mirai.data;

import com.google.common.collect.Lists;
import com.github.starowo.mirai.command.CommandRank;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerRank implements Serializable {

    public static final long serialVersionUID = -4822923959795393426L;

    public long id;
    public Map<String, Integer> scores;

    public PlayerRank(long id) {
        this.id = id;
        this.scores = new HashMap<>();
    }

    public boolean banned() {
        return Manager.ban.contains(id);
    }

    public int process(String game, float k, int avg) {
        int score = scores.getOrDefault(game, 1200);
        if(k > 0) {
            double sigm = game.equals("云顶之巢") ? sigmoid_comb(score - avg) : sigmoid(score - avg);
            if(sigm > 1)
                sigm = 1 + (sigm - 1) * (game.equals("云顶之巢") ? 0.36f : 0.77f);
            int change = (int) Math.ceil(k * 40 * sigm);
            score += change;
            scores.put(game, score);
            return change;
        }else {
            double sigm = game.equals("云顶之巢") ? sigmoid_comb(avg - score) : sigmoid(avg - score);
            if(sigm > 1)
                sigm = 1 + (sigm - 1) * (game.equals("云顶之巢") ? 0.23f : 0.77f);
            int change = (int) Math.ceil(k * 40 * sigm);
            score += change;
            scores.put(game, score);
            return change;
        }
    }

    public int eloRank(String game, float sa, int k, int otherScore) {
        // elo rank algorithm
        int score = scores.getOrDefault(game, 1200);
        float ea = 1 / (1 + (float) Math.pow(10, (otherScore - score) / 400f));
        int change = Math.round(k * (sa - ea));
        score += change;
        scores.put(game, score);
        return change;
    }

    public String getRanks() {
        StringBuilder s = new StringBuilder();
        scores.forEach((name, score) -> {
            if (CommandRank.GAMELIST.contains(name))
            s.append("  ").append(name).append("：").append(score).append("\n");
        });
        return s.toString();
    }

    private static double sigmoid(double x) {
        return 2.0d / (1.0d + Math.pow(Math.E, x / 180.0d));
    }

    private static double sigmoid_comb(double x) {
        return 2.0d / (1.0d + Math.pow(Math.E, x / 400.0d));
    }

    /*public static void main(String[] args) {
        System.out.println(new PlayerRank(1l).process("24轮盘", 1, 1000));
    }*/

    public static void main(String[] args) {
        List<Integer> allPlayer = Lists.newArrayList(8, 12, 3);
        allPlayer.sort((ele1, ele2) -> Integer.compare(ele2,ele1));
        allPlayer.forEach(System.out::println);
    }

}
