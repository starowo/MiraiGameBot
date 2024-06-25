package com.github.starowo.mirai.game.treasurehunter;

public class TreasureCard {

    public enum CardType {
        WEALTH, TREASURE, EVENT, MONSTER
    }

    public static final int PHOENIX = 1;
    public static final int CERBERUS = 2;
    public static final int MEDUSA = 3;

    public static final int UPCOMING_WEALTH = 1;
    public static final int UPCOMING_TREASURE = 2;
    public static final int UPCOMING_MONSTER = 3;
    public static final int DOUBLE_WEALTH = 4;
    public static final int EXTRA_WEALTH = 5;
    public static final int DOUBLE_TREASURE = 6;
    public static final int MAX_TREASURE = 7;
    public static final int SUDDEN_DEATH = 8;
    public static final int QUIT_REWARD = 9;
    public static final int WEALTH_INCREASE = 10;
    public static final int WEALTH_DISTRIBUTE = 11;
    public static final int WEALTH_RESET = 12;
    public static final int TREASURE_CONVERT = 13;

    protected CardType type;
    protected String name;
    protected String description;
    protected int value;
    protected int realValue;

    public TreasureCard(CardType type, String name, String description, int value) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.value = value;
        this.realValue = value;
    }

}
