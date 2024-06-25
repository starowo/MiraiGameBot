package com.github.starowo.mirai.game.comb;

import com.github.starowo.mirai.data.Skin;
import com.github.starowo.mirai.player.GamePlayer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImageGenerator {

    public static final ImageGenerator INSTANCE = new ImageGenerator();
    public static Map<Skin, ImageGenerator> specialMap = new HashMap<>();

    public static ImageGenerator getInstanceForSkin(Skin skin) {
        return specialMap.getOrDefault(skin, INSTANCE);
    }

    public static void init() {
        specialMap.clear();
        HexagonSpecialGenerator hexagon = new HexagonSpecialGenerator();
        specialMap.put(Skin.hexagon, hexagon);
        specialMap.put(Skin.blackhole, hexagon);
        specialMap.put(Skin.hexa_universe, hexagon);
        RainbowCatSpecialGenerator rainbowCat = new RainbowCatSpecialGenerator();
        specialMap.put(Skin.rainbow_cat, rainbowCat);
        MechaSpecialGenerator mecha = new MechaSpecialGenerator();
        specialMap.put(Skin.mech, mecha);
    }

    public BufferedImage getCardImg(String dir, int d0, int d1, int d2) throws IOException {
        File f = new File(dir + d0 + d1 + d2 + ".png");
        if(f.exists()) {
            return ImageIO.read(f);
        }
        BufferedImage card = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        f = new File(dir + "card.png");
        BufferedImage back = ImageIO.read(f);
        Graphics2D graphics2D = card.createGraphics();
        graphics2D.drawImage(back, 0, 0, 64, 64, null);
        graphics2D.drawImage(ImageIO.read(new File(dir + d1 + ".png")), 0, 0, null);
        if(d1 != 0) {
            graphics2D.drawImage(ImageIO.read(new File(dir + d2 + ".png")), 0, 0, null);
            graphics2D.drawImage(ImageIO.read(new File(dir + d0 + ".png")), 0, 0, null);
        }
        graphics2D.dispose();
        return card;
    }

    public BufferedImage drawBoard(NumberHive.Board board, GamePlayer player, int index) throws IOException {
        String dir = player.data.getSkin().getPath();
        if(player.id == 3182618911L && player.data.getSkin() == Skin.pure) {
            dir = "./resources/comb/skin/mutsuki/";
        }
        BufferedImage iBlank = new BufferedImage(580, 510, BufferedImage.TYPE_INT_ARGB);
        BufferedImage iBoard = ImageIO.read(new File(dir + "board.png"));
        Graphics2D g = iBlank.createGraphics();
        g.setColor(index > 0 ? Color.WHITE : Color.LIGHT_GRAY);
        g.fillRect(0, 0, 580, 510);
        g.setColor(Color.BLACK);
        Graphics2D gb = iBoard.createGraphics();
        //gb.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER));
        Font font = new Font("微软雅黑", Font.BOLD, 15);
        g.setFont(font);
        //index = index > 0? index : players.size() + dead_players.size() + index;
        String name = player.name;
        if (name.length() > 15) {
            name = name.substring(0, 15) + "...";
        }
        g.drawString(index + ". <" + name + "(" + player.id + ")>", 20, 30);
        String score = "(得分:" + board.score + " 血量:" + board.health + ")";
        g.drawString(score, 514 - g.getFontMetrics(font).stringWidth(score), 30);
        for (int i = 0; i < 3; i++) {
            if (board.board[i] == null)
                continue;
            int[] directions = board.board[i].directions;
            BufferedImage iPiece = getCardImg(dir, directions[0], directions[1], directions[2]);
            gb.drawImage(iPiece, 64, 128 + 64 * i, null);
        }
        for (int i = 3; i < 7; i++) {
            if (board.board[i] == null)
                continue;
            int[] directions = board.board[i].directions;
            BufferedImage iPiece = getCardImg(dir, directions[0], directions[1], directions[2]);
            gb.drawImage(iPiece, 128, 96 + 64 * (i - 3), null);
        }
        for (int i = 7; i < 12; i++) {
            if (board.board[i] == null)
                continue;
            int[] directions = board.board[i].directions;
            BufferedImage iPiece = getCardImg(dir, directions[0], directions[1], directions[2]);
            gb.drawImage(iPiece, 192, 64 + 64 * (i - 7), null);
        }
        for (int i = 12; i < 16; i++) {
            if (board.board[i] == null)
                continue;
            int[] directions = board.board[i].directions;
            BufferedImage iPiece = getCardImg(dir, directions[0], directions[1], directions[2]);
            gb.drawImage(iPiece, 256, 96 + 64 * (i - 12), null);
        }
        for (int i = 16; i < 19; i++) {
            if (board.board[i] == null)
                continue;
            int[] directions = board.board[i].directions;
            BufferedImage iPiece = getCardImg(dir, directions[0], directions[1], directions[2]);
            gb.drawImage(iPiece, 320, 128 + 64 * (i - 16), null);
        }
        gb.dispose();
        g.drawImage(iBoard, 65, 61, null);
        g.dispose();
        return iBlank;
    }

    public BufferedImage getSkinPreview(Skin skin) throws IOException {
        String dir = skin.getPath();
        /*if(player.id == 1273300377L)
            dir = Skin.gold.getPath();*/
        NumberHive.Board board = new NumberHive.Board();
        board.board[0] = new NumberHive.Piece(3, 1, 2);
        board.board[2] = new NumberHive.Piece(4, 5, 6);
        board.board[7] = new NumberHive.Piece(8, 9, 7);
        board.board[18] = new NumberHive.Piece(3, 1, 2);
        board.board[16] = new NumberHive.Piece(4, 5, 6);
        board.board[11] = new NumberHive.Piece(8, 9, 7);
        board.board[9] = new NumberHive.Piece(0, 0, 0);
        BufferedImage iBlank = new BufferedImage(580, 510, BufferedImage.TYPE_INT_ARGB);
        BufferedImage iBoard = ImageIO.read(new File(dir + "board.png"));
        Graphics2D g = iBlank.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 580, 510);
        g.setColor(Color.BLACK);
        Graphics2D gb = iBoard.createGraphics();
        //gb.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER));
        Font font = new Font("微软雅黑", Font.BOLD, 15);
        g.setFont(font);
        //index = index > 0? index : players.size() + dead_players.size() + index;
        String name = skin.name() + " - "+skin.name;
        if (name.length() > 15) {
            name = name.substring(0, 15) + "...";
        }
        g.drawString(skin.ordinal() + ". <" + name + "(" + skin.price + ")>", 20, 30);
        for (int i = 0; i < 3; i++) {
            if (board.board[i] == null)
                continue;
            int[] directions = board.board[i].directions;
            BufferedImage iPiece = getCardImg(dir, directions[0], directions[1], directions[2]);
            gb.drawImage(iPiece, 64, 128 + 64 * i, null);
        }
        for (int i = 3; i < 7; i++) {
            if (board.board[i] == null)
                continue;
            int[] directions = board.board[i].directions;
            BufferedImage iPiece = getCardImg(dir, directions[0], directions[1], directions[2]);
            gb.drawImage(iPiece, 128, 96 + 64 * (i - 3), null);
        }
        for (int i = 7; i < 12; i++) {
            if (board.board[i] == null)
                continue;
            int[] directions = board.board[i].directions;
            BufferedImage iPiece = getCardImg(dir, directions[0], directions[1], directions[2]);
            gb.drawImage(iPiece, 192, 64 + 64 * (i - 7), null);
        }
        for (int i = 12; i < 16; i++) {
            if (board.board[i] == null)
                continue;
            int[] directions = board.board[i].directions;
            BufferedImage iPiece = getCardImg(dir, directions[0], directions[1], directions[2]);
            gb.drawImage(iPiece, 256, 96 + 64 * (i - 12), null);
        }
        for (int i = 16; i < 19; i++) {
            if (board.board[i] == null)
                continue;
            int[] directions = board.board[i].directions;
            BufferedImage iPiece = getCardImg(dir, directions[0], directions[1], directions[2]);
            gb.drawImage(iPiece, 320, 128 + 64 * (i - 16), null);
        }
        gb.dispose();
        g.drawImage(iBoard, 65, 61, null);
        g.dispose();
        return iBlank;
    }

}
