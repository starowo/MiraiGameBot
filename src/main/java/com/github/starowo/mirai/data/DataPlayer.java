package com.github.starowo.mirai.data;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.TimeZone;

public class DataPlayer implements Serializable {

    public long id;
    public String name;
    public int rank = 0;
    public int credit = 0;
    public int exp = 0;
    public int level = 1;
    public int maxExp = 1000;
    public String skin = "pure";
    public HashSet<String> unlocked = new HashSet<>();
    public int lastSign;
    public int creditEarned = 0;
    public int lastEarnTime = 0;
    public transient int creditInGame = 0;

    public DataPlayer(long id, String name) {
        this.id = id;
        this.name = name;
        unlocked.add("pure");
    }

    public void addExp(int exp) {
        this.exp += exp;
        if (exp >= 0) {
            while (this.exp >= maxExp) {
                this.exp -= maxExp;
                level++;
                // smooth curve
                if (level <= 80) {
                    maxExp = (int) (100 * Math.pow(level - 1, 1.2) + 1000);
                } else {
                    maxExp = (int) (100 * Math.pow(79, 1.2) + 1000);
                    // log base 30
                    maxExp += (int) (1000 * Math.log(level - 79) / Math.log(30));
                }
            }
        } else {
            while (this.exp < 0) {
                level--;
                // smooth curve
                if (level <= 80) {
                    maxExp = (int) (100 * Math.pow(level - 1, 1.2) + 1000);
                } else {
                    maxExp = (int) (100 * Math.pow(79, 1.2) + 1000);
                    // log base 30
                    maxExp += (int) (1000 * Math.log(level - 79) / Math.log(30));
                }
                this.exp += maxExp;
            }
        }
    }

    public Skin getSkin() {
        try {
            return Skin.findSkin(skin);
        } catch (Throwable e) {
            return Skin.pure;
        }
    }

    public String getLevelString() {
        if (level <= 80) {
            return "" + level;
        } else {
            String honors = "✧✦✰✮✯✵✪❂";
            String honor = level < 120 ? honors.charAt((level - 80) / 5) + "" : honors.charAt(7) + "";
            return honor + (level - 80);
        }
    }

    public int reducedCreditEarn(int credit) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
        int day = calendar.get(Calendar.DAY_OF_YEAR) + calendar.get(Calendar.YEAR) * 366;
        if (lastEarnTime == day) {
            double r = creditEarned < 2000 ? 1d : (1d / (1d + Math.pow(Math.E, (creditEarned - 10000) / 2000f)));
            int reduced = (int) (credit * r);
            creditEarned += reduced;
            return reduced;
        } else {
            creditEarned = 0;
        }
        lastEarnTime = day;
        return credit;
    }

}
