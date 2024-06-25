package com.github.starowo.mirai.game.comb;

import com.google.gson.Gson;
import com.github.starowo.mirai.data.Skin;
import com.github.starowo.mirai.player.GamePlayer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class HexagonSpecialGenerator extends ImageGenerator {

    private final HashMap<String, Integer[][]> position;

    public HexagonSpecialGenerator() {
        HashMap<String, List<List<Double>>> a;
        HashMap<String, Integer[][]> position1;
        try {
            a = new Gson().fromJson(new FileReader("./resources/comb/skin/special/hexagon.json"), HashMap.class);
            position1 = new HashMap<>();
            HashMap<String, Integer[][]> finalPosition = position1;
            a.forEach((k, v) -> {
                finalPosition.put(k, new Integer[][]{new Integer[]{v.get(0).get(0).intValue(), v.get(0).get(1).intValue()}, new Integer[]{v.get(1).get(0).intValue(), v.get(1).get(1).intValue()}});
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            position1 = null;
        }
        position = position1;
    }

    @Override
    public BufferedImage drawBoard(NumberHive.Board board, GamePlayer player, int index) throws IOException {
        if(position == null) {
            return super.drawBoard(board, player, index);
        }
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
        for (int i = 0; i < 19; i++) {
            if (board.board[i] == null)
                continue;
            Integer[][] pos = position.get(String.valueOf(i + 1));
            int[] directions = board.board[i].directions;
            BufferedImage iPiece = getCardImg(dir, directions[0], directions[1], directions[2]);
            gb.drawImage(iPiece, pos[0][0], pos[0][1], pos[1][0], pos[1][1], null);
        }
        gb.dispose();
        g.drawImage(iBoard, 65, 61, null);
        g.dispose();
        return iBlank;
    }

    @Override
    public BufferedImage getSkinPreview(Skin skin) throws IOException {
        if(position == null) {
            return super.getSkinPreview(skin);
        }
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
        for (int i = 0; i < 19; i++) {
            if (board.board[i] == null)
                continue;
            Integer[][] pos = position.get(String.valueOf(i + 1));
            int[] directions = board.board[i].directions;
            BufferedImage iPiece = getCardImg(dir, directions[0], directions[1], directions[2]);
            gb.drawImage(iPiece, pos[0][0], pos[0][1], pos[1][0], pos[1][1], null);
        }
        gb.dispose();
        g.drawImage(iBoard, 65, 61, null);
        g.dispose();
        return iBlank;
    }

    public static void main(String[] args) throws FileNotFoundException {
        HashMap<String, List<List<Double>>> a;
        HashMap<String, Integer[][]> position1;
        try {
            a = new Gson().fromJson(new FileReader("F:\\Tencent Files\\1273300377\\FileRecv\\data.json"), HashMap.class);
            position1 = new HashMap<>();
            HashMap<String, Integer[][]> finalPosition = position1;
            a.forEach((k, v) -> {
                finalPosition.put(k, new Integer[][]{new Integer[]{v.get(0).get(0).intValue()}});
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            position1 = null;
        }
        return;
    }

}
