package com.github.starowo.mirai.data;

public enum Skin {

    pure("默认皮肤", 0, new String[]{"默认"}),
    year2024("新年（2024龙年限定）", 20240, new String[]{"新年", "2024"}),
    hexa_universe("六重宇宙", 0, new String[]{"六边形宇宙"}, 25),
    tbd("45级皮肤", 0, new String[0], 45),
    tbd1("60级皮肤", 0, new String[0], 60),
    tbd2("70级皮肤", 0, new String[0], 70),
    tbd3("80级皮肤", 0, new String[0], 80),
    pink("可爱粉", 5888, new String[]{"粉"}),
    green("环保绿", 5888, new String[]{"绿"}),
    gold("奢华金", 8888, new String[]{"金"}),
    hexagon("六边形", 8888, new String[0]),
    troll("愚人节涂鸦", 8888, new String[]{"愚人节", "涂鸦"}),
    universe("暗黑宇宙", 13888, new String[]{"宇宙"}),
    blackhole("黑洞（需持有universe）", 2888, new String[]{"黑洞"}, new String[]{"universe"}),
    kuromi("酷洛米", 13888, new String[0]),
    capoo("咖波", 13888, new String[]{"猫猫虫", "bugcat"}),
    kochou_shinobu("蝴蝶忍", 13888, new String[]{"蝴蝶", "kochou", "shinobu"}),
    prince("小王子", 13888, new String[]{"王子"}),
    komeji_koishi("古明地恋", 13888, new String[]{"古明地恋", "古地明恋", "koishi", "komeji"}),
    flower_world("花花世界", 13888, new String[]{"花", "花花", "flower"}),
    minimalism("极简主义", 13888, new String[]{"极简", "mini", "minimal"}),
    wish("愿你一世平安", 13888, new String[]{"平安"}),
    hoshino_ai("星野爱", 15888, new String[]{"星野", "爱", "ai", "hoshino"}),
    sponge("海绵宝宝", 16666, new String[]{"海绵"}),
    gem("牛皮纸宝石", 18888, new String[]{"牛皮纸", "宝石"}),
    pcb("工业革命", 18888, new String[]{"工业", "电路板"}),
    cube("立方体", 18888, new String[0]),
    treasure("藏宝图", 18888, new String[0]),
    firefly("萤火虫", 23888, new String[]{"萤火"}),
    mech("未来机甲", 23888, new String[]{"机甲"}),
    rainbow_cat("彩虹猫猫教", 28888, new String[]{"彩虹", "猫猫", "彩虹猫", "rainbow", "cat", "彩虹猫猫", "猫"}, 10),
    year2023("新年（2023春节限定）", 99999999, new String[]{"旧新年", "2023"}),
    test("测试皮肤", -1, new String[]{"测试"});

    public final int price;
    public final String name;
    public final String[] aliases;
    public final String[] pre;
    public final int level;

    Skin(String name, int price, String[] aliases) {
        this.price = price;
        this.name = name;
        this.aliases = aliases;
        this.pre = new String[0];
        this.level = 0;
    }
    Skin(String name, int price, String[] aliases, int level) {
        this.price = price;
        this.name = name;
        this.aliases = aliases;
        this.pre = new String[0];
        this.level = level;
    }
    Skin(String name, int price, String[] aliases, String[] pre) {
        this.price = price;
        this.name = name;
        this.aliases = aliases;
        this.pre = pre;
        this.level = 0;
    }

    public static Skin findSkin(String name) {
        try {
            return Skin.valueOf(name);
        }catch (IllegalArgumentException illegalArgumentException) {
            for (Skin s : Skin.values()) {
                if (s.name.equalsIgnoreCase(name))
                    return s;
                for (String alias : s.aliases) {
                    if (alias.equalsIgnoreCase(name)) {
                        return s;
                    }
                }
            }
            throw illegalArgumentException;
        }
    }

    public String getPath() {
        if (this == pure)
            return "./resources/comb/";
        return "./resources/comb/skin/" + name() + "/";
    }

}
