package com.github.starowo.mirai.game.treasurehunter;

import com.github.starowo.mirai.player.GamePlayer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImageGenerator {

    private static final String ASSETS_PATH = "./resources/treasurehunter/";

    protected static BufferedImage generateCardImage(TreasureCard card) throws IOException {
        BufferedImage image = new BufferedImage(121, 185, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = image.createGraphics();
        g.setColor(Color.GRAY);
        //g.fillRect(0, 0, 121, 183);
        switch (card.type) {
            case WEALTH:
                File wealth_bg_f = new File(ASSETS_PATH + "bg_wealth.png");
                BufferedImage wealth_bg = ImageIO.read(wealth_bg_f);
                g.drawImage(wealth_bg, 0, 0, null);
                int wealth = card.realValue;
                int level = 0;
                if (wealth >= 5) {
                    level = 4;
                } else if (wealth >= 3) {
                    level = 3;
                } else if (wealth >= 2) {
                    level = 2;
                } else if (wealth >= 1) {
                    level = 1;
                }
                if (level > 0) {
                    File wealth_f = new File(ASSETS_PATH + "wealth_" + level + ".png");
                    BufferedImage wealth_img = ImageIO.read(wealth_f);
                    g.drawImage(wealth_img, (121 - 96) / 2, 4, null);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Eras Demi ITC", Font.BOLD, 48));
                    int w = g.getFontMetrics().stringWidth(String.valueOf(wealth));
                    g.drawString(String.valueOf(wealth), (121 - w) / 2, 170);
                }
                break;
            case TREASURE:
                File bg_f = new File(ASSETS_PATH + "bg_treasure.png");
                BufferedImage bg = ImageIO.read(bg_f);
                g.drawImage(bg, 0, 0, null);
                if (card.realValue > 0) {
                    File treasure_f = new File(ASSETS_PATH + "treasure.png");
                    BufferedImage treasure = ImageIO.read(treasure_f);
                    g.drawImage(treasure, (121 - 96) / 2, 30, null);
                    g.setColor(Color.BLUE);
                    g.setFont(new Font("等线", Font.BOLD, 20));
                    String title = "仅1人独享";
                    int w = g.getFontMetrics().stringWidth(title);
                    g.drawString(title, (121 - w) / 2, 22);
                    g.setFont(new Font("Eras Demi ITC", Font.BOLD, 48));
                    g.setColor(Color.WHITE);
                    w = g.getFontMetrics().stringWidth(String.valueOf(card.realValue));
                    g.drawString(String.valueOf(card.realValue), (121 - w) / 2, 170);
                }
                break;
            case MONSTER:
                File border_f = null;
                String name = "";
                BufferedImage monster = null;
                Color color = null;
                switch (card.value) {
                    case TreasureCard.PHOENIX:
                        File phoenix_f = new File(ASSETS_PATH + "phoenix.png");
                        border_f = new File(ASSETS_PATH + "bg_phoenix.png");
                        name = "不死鸟";
                        color = new Color(255, 147, 30);
                        monster = ImageIO.read(phoenix_f);
                        break;
                    case TreasureCard.CERBERUS:
                        File cerberus_f = new File(ASSETS_PATH + "cerberus.png");
                        border_f = new File(ASSETS_PATH + "bg_cerberus.png");
                        name = "地狱犬";
                        color = new Color(173, 1, 1);
                        monster = ImageIO.read(cerberus_f);
                        break;
                    case TreasureCard.MEDUSA:
                        File medusa_f = new File(ASSETS_PATH + "medusa.png");
                        border_f = new File(ASSETS_PATH + "bg_medusa.png");
                        name = "美杜莎";
                        color = new Color(0, 126, 0);
                        monster = ImageIO.read(medusa_f);
                        break;
                }
                BufferedImage border = ImageIO.read(border_f);
                g.drawImage(border, 0, 0, null);
                g.drawImage(monster, (121 - 96) / 2 + 1, 12, null);
                Font font = new Font("楷体", Font.BOLD, 38);
                g.setFont(font);
                g.setColor(Color.BLACK);
                int nameWidth = g.getFontMetrics().stringWidth(name);
                g.drawString(name, (121 - nameWidth) / 2, 155);
                font = new Font("楷体", Font.BOLD, 36);
                g.setFont(font);
                g.setColor(color);
                nameWidth = g.getFontMetrics().stringWidth(name);
                g.drawString(name, (121 - nameWidth) / 2, 155);
                break;
            case EVENT:
                File bg_event_f = new File(ASSETS_PATH + "bg_event.png");
                BufferedImage bg_event = ImageIO.read(bg_event_f);
                g.drawImage(bg_event, 0, 0, null);
                String row1 = "";
                String row2 = "";
                switch (card.value) {
                    case TreasureCard.UPCOMING_WEALTH:
                        row1 = "前方";
                        row2 = "宝藏";
                        break;
                    case TreasureCard.UPCOMING_TREASURE:
                        row1 = "前方";
                        row2 = "珍宝";
                        break;
                    case TreasureCard.UPCOMING_MONSTER:
                        row1 = "怪物";
                        row2 = "出没";
                        break;
                    case TreasureCard.DOUBLE_WEALTH:
                        row1 = "双倍";
                        row2 = "宝藏";
                        break;
                    case TreasureCard.EXTRA_WEALTH:
                        row1 = "更多";
                        row2 = "宝藏";
                        break;
                    case TreasureCard.DOUBLE_TREASURE:
                        row1 = "双倍";
                        row2 = "珍宝";
                        break;
                    case TreasureCard.MAX_TREASURE:
                        row1 = "稀世";
                        row2 = "珍宝";
                        break;
                    case TreasureCard.SUDDEN_DEATH:
                        row1 = "怪物";
                        row2 = "突袭";
                        break;
                    case TreasureCard.QUIT_REWARD:
                        row1 = "见好";
                        row2 = "就收";
                        break;
                    case TreasureCard.WEALTH_INCREASE:
                        row1 = "宝藏";
                        row2 = "升值";
                        break;
                    case TreasureCard.WEALTH_DISTRIBUTE:
                        row1 = "天降";
                        row2 = "横财";
                        break;
                    case TreasureCard.WEALTH_RESET:
                        row1 = "宝藏";
                        row2 = "再生";
                        break;
                    case TreasureCard.TREASURE_CONVERT:
                        row1 = "点石";
                        row2 = "成金";
                        break;
                }
                g.setFont(new Font("华文新魏", Font.PLAIN, 52));
                g.setColor(Color.DARK_GRAY);
                int width = g.getFontMetrics().stringWidth(row1);
                g.drawString(row1, (121 - width) / 2 + 1, 80 + 1);
                width = g.getFontMetrics().stringWidth(row2);
                g.drawString(row2, (121 - width) / 2 + 1, 130 + 1);
                g.setColor(Color.YELLOW);
                g.drawString(row1, (121 - width) / 2, 80);
                g.drawString(row2, (121 - width) / 2, 130);
                break;
        }
        g.dispose();
        return image;
    }

    public static void main(String[] args) {
        try {
            BufferedImage card = generateCardImage(new TreasureCard(TreasureCard.CardType.EVENT, "不死鸟", "1", 4));
            ImageIO.write(card, "png", new File("test.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Integer> past = new ArrayList<>();
        HashMap<GamePlayer, Integer> wealth = new HashMap<>();
        List<GamePlayer> players = new ArrayList<>();
        List<GamePlayer> left_players = new ArrayList<>();
        players.add(new GamePlayer(1273300377L, "小明", null));
        players.add(new GamePlayer(1273300378L, "小红", null));
        players.add(new GamePlayer(1273300379L, "小蓝", null));
        left_players.add(new GamePlayer(1273300380L, "小绿", null));
        left_players.add(new GamePlayer(1273300381L, "小紫", null));

        BufferedImage img = new BufferedImage(800, 100+187*((past.size() - 1) / 7 + 1), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 800, 200+187*(past.size() / 7 + 1));
        g.setColor(Color.BLACK);
        g.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        int playerSize = players.size() + left_players.size();
        for (int i = 0; i < players.size(); i++) {
            GamePlayer player = players.get(i);
            BufferedImage avatar = getImg(player.avatar);
            if (avatar != null) {
                g.drawImage(avatar, 10 + 800 / playerSize * i, 10, 80, 80, null);
            }
            g.drawString("探险中", 90 + 800 / playerSize * i, 60);
            int pt = wealth.getOrDefault(player, 128);
            g.drawString("金币:" + pt, 90 + 800 / playerSize * i, 80);
        }
        for (int i = 0; i < left_players.size(); i++) {
            int j = i + players.size();
            GamePlayer player = left_players.get(i);
            BufferedImage avatar = getImg(player.avatar);
            if (avatar != null) {
                g.drawImage(avatar, 10 + 800 / playerSize * j, 10, 80, 80, null);
            }
            g.drawString("已撤离", 90 + 800 / playerSize * j, 60);
            int pt = wealth.getOrDefault(player, 0);
            g.drawString("金币:" + pt, 90 + 800 / playerSize * j, 80);
        }
        g.dispose();
        try {
            ImageIO.write(img, "png", new File("test2.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage getImg(String url) {
        if (url.equalsIgnoreCase("http://q1.qlogo.cn/g?b=qq&nk=372542780&s=640")) {
            File f = new File("./resources/saiwei.png");
            if(f.exists()) {
                try {
                    BufferedImage img = ImageIO.read(f);
                    return img;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            URL url1 = new URL(url);
            try (InputStream is = url1.openStream()) {
                BufferedImage img = ImageIO.read(is);
                return img;
            }
        } catch (IOException ignored) {

        }
        return null;
    }

}
