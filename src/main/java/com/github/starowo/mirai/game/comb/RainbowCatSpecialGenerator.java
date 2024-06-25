package com.github.starowo.mirai.game.comb;

import com.github.starowo.mirai.data.Skin;
import com.github.starowo.mirai.player.GamePlayer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class RainbowCatSpecialGenerator extends ImageGenerator {

    @Override
    public BufferedImage drawBoard(NumberHive.Board board, GamePlayer player, int index) throws IOException {
        String dir = player.data.getSkin().getPath();
        BufferedImage iBlank = ImageIO.read(new File(dir + "board.png"));
        BufferedImage iBoard = new BufferedImage(448, 448, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = iBlank.createGraphics();
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
        if (index > 0) {
            // apply a grayscale filter to the image
            BufferedImage iGray = new BufferedImage(iBlank.getWidth(), iBlank.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = iGray.createGraphics();
            g2.setColor(new Color(0, 0, 0, 255 - 192));
            g2.fillRect(0, 0, iBlank.getWidth(), iBlank.getHeight());
            g2.dispose();
            g.drawImage(iGray, 0, 0, null);
            g.dispose();
        }
        return iBlank;
    }

    @Override
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
        BufferedImage iBlank = ImageIO.read(new File(dir + "board.png"));
        BufferedImage iBoard = new BufferedImage(580, 510, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = iBlank.createGraphics();
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
