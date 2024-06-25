package com.github.starowo.mirai.game;

import com.google.common.collect.Lists;
import com.github.starowo.mirai.MSGHandler;
import com.github.starowo.mirai.MiraiGamePlugin;
import com.github.starowo.mirai.data.Manager;
import com.github.starowo.mirai.player.GamePlayer;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class SkatingChess implements IGame {

    public Group group;
    public GamePlayer player1;
    public GamePlayer player2;
    public int status = 0;
    private final ReentrantLock lock = new ReentrantLock();

    public int[][] board = new int[5][5];
    public int[] tile1 = new int[3];
    public int[] tile2 = new int[3];

    public ArrayList<String> directions = Lists.newArrayList("上", "右上", "右", "右下", "下", "左下", "左", "左上");

    Thread thread = new ThreadTime(this);
    public int time = 90;

    public SkatingChess(Group group) {
        this.group = group;
    }

    public void start() {
        lock.lock();
        try {
            if (status != 0)
                return;
            board[1][0] = 1;
            board[3][0] = 1;
            board[2][1] = 2;
            board[2][3] = 1;
            board[1][4] = 2;
            board[3][4] = 2;
            tile1[0] = c2i(1, 0);
            tile1[1] = c2i(3, 0);
            tile1[2] = c2i(2, 3);
            tile2[0] = c2i(3, 4);
            tile2[1] = c2i(1, 4);
            tile2[2] = c2i(2, 1);
            status = (int) (1 + Math.round(Math.random()));
            group.sendMessage("游戏开始，" + (status == 1 ? player1.name : player2.name) + "先手，执" + (status == 1 ? "红" : "蓝"));
            sendImage();
            thread.start();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Message input(GamePlayer player, MessageChain msg, boolean isGroup, boolean at) {
        lock.lock();
        try {
            if (player1.equals(player)) {
                if (status == 2) {
                    if (at)
                        return new MessageChainBuilder().append("你先别急").build();
                    return null;
                }
            } else if (status == 1) {
                if (at)
                    return new MessageChainBuilder().append("你先别急").build();
                return null;
            }
            String text = msg.contentToString().replaceAll("@" + group.getBot().getId(), "").trim();
            try {
                int num = Integer.parseInt(text.substring(0, 1));
                int dir = directions.indexOf(text.substring(1));
                if (dir < 0) {
                    return new MessageChainBuilder().append("未知的方向").build();
                }
                if (!move(status, num, (dir == 7 || dir < 2), (dir > 0 && dir < 4), (dir > 2 && dir < 6), dir > 4)) {
                    return new MessageChainBuilder().append("无效操作").build();
                }
                sendImage();
                if (check(status)) {
                    stop();
                    return null;
                }
                status = status % 2 + 1;
                time = 90;
                return new MessageChainBuilder().append("请").append(status == 1 ? player1.name : player2.name).append("移动").build();
            } catch (NumberFormatException | IndexOutOfBoundsException throwable) {
                if (at)
                    return new MessageChainBuilder().append("格式错误；正确格式为【棋子id 目标方向】如【3左上】").build();
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    private void sendImage() {
        BufferedImage iBlank = new BufferedImage(5 * 64 + 1, 5 * 64 + 1, 2);
        Graphics2D g = iBlank.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, 5 * 64 + 1, 5 * 64 + 1);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (board[i][j] == 1) {
                    g.setColor(Color.pink);
                    g.fillRect(64 * i, 64 * j, 64, 64);
                }else if(board[i][j] == 2) {
                    g.setColor(Color.cyan);
                    g.fillRect(64 * i, 64 * j, 64, 64);
                }
                g.setColor(Color.gray);
                g.drawRect(64 * i, 64 * j, 64, 64);
            }
        }
        g.setColor(Color.black);
        Font font = new Font("微软雅黑", Font.BOLD, 32);
        g.setFont(font);
        for (int i = 0; i < 3; i++) {
            int[] coord = i2c(tile1[i]);
            int x = coord[0], y = coord[1];
            String s = (i + 1) + "";
            int w = g.getFontMetrics().stringWidth(s);
            int h = g.getFontMetrics().getHeight();
            int centerX = 64 * (x) + (64 - w) / 2;
            int centerY = 64 * (y) + (64 - h) / 2 + g.getFontMetrics().getAscent();
            g.drawString(String.valueOf(i + 1), centerX, centerY);
        }
        for (int i = 0; i < 3; i++) {
            int[] coord = i2c(tile2[i]);
            int x = coord[0], y = coord[1];
            String s = (i + 1) + "";
            int w = g.getFontMetrics().stringWidth(s);
            int h = g.getFontMetrics().getHeight();
            int centerX = 64 * (x) + (64 - w) / 2;
            int centerY = 64 * (y) + (64 - h) / 2 + g.getFontMetrics().getAscent();
            g.drawString(String.valueOf(i + 1), centerX, centerY);
        }
        g.dispose();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] baImage = null;
        try {
            ImageIO.write(iBlank, "png", os);
            baImage = os.toByteArray();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (baImage != null) {
            MSGHandler.asyncSendImage(group, baImage);
        }
    }

    private boolean check(int side) {
        int[] tiles = side == 1 ? tile1 : tile2;
        int[] coord = i2c(tiles[0]);
        int x3 = coord[0], y3 = coord[1];
        int[] coord1 = i2c(tiles[1]);
        int x1 = coord1[0], y1 = coord1[1];
        int[] coord2 = i2c(tiles[2]);
        int x2 = coord2[0], y2 = coord2[1];
        // 判断x坐标是否相同
        if (x1 == x2 && x2 == x3) {
            int dy1 = Math.abs(y1 - y2);
            int dy2 = Math.abs(y2 - y3);
            int dy3 = Math.abs(y1 - y3);
            return dy1 + dy2 + dy3 == 4;
        }else
        // 判断y坐标是否相同
        if (y1 == y2 && y2 == y3) {
            int dx1 = Math.abs(x1 - x2);
            int dx2 = Math.abs(x2 - x3);
            int dx3 = Math.abs(x1 - x3);
            return dx1 + dx2 + dx3 == 4;
        }else
        // 判断斜率是否相同
        if ((x1 - x2) * (y1 - y3) == (x1 - x3) * (y1 - y2)) {
            int dx1 = Math.abs(x1 - x2);
            int dx2 = Math.abs(x2 - x3);
            int dx3 = Math.abs(x1 - x3);
            int dy1 = Math.abs(y1 - y2);
            int dy2 = Math.abs(y2 - y3);
            int dy3 = Math.abs(y1 - y3);
            return dx1 + dx2 + dx3 + dy1 + dy2 + dy3 == 8;
        }
        return false;
    }


    private boolean move(int side, int i, boolean up, boolean right, boolean down, boolean left) {
        int[] tiles = side == 1 ? tile1 : tile2;
        int[] coord = i2c(tiles[i - 1]);
        int x = coord[0], y = coord[1];
        board[x][y] = 0;
        int finalX = x, finalY = y;
        boolean changed = false;
        while (x < 5 && y < 5) {
            if(right) x++;
            if(left) x--;
            if(up) y--;
            if (down) y ++;
            if(x >= 0 && x < 5 && y >= 0 && y < 5 && board[x][y] == 0) {
                changed = true;
                finalX = x;
                finalY = y;
            }else {
                if(side == 1) {
                    tile1[i - 1] = c2i(finalX, finalY);
                }else {
                    tile2[i - 1] = c2i(finalX, finalY);
                }
                board[finalX][finalY] = side;
                return changed;
            }
        }
        board[finalX][finalY] = side;
        if(side == 1) {
            tile1[i - 1] = c2i(finalX, finalY);
        }else {
            tile2[i - 1] = c2i(finalX, finalY);
        }
        return false;
    }

    private int[] i2c(int i) {
        return new int[]{i >> 3, i & 7};
    }

    private int c2i(int x, int y) {
        return x << 3 | y;
    }

    public boolean needAt() {
        return false;
    }

    @Override
    public void stop() {
        GamePlayer player;
        player = status == 1 ? player1 : player2;
        MessageChainBuilder endMsg = new MessageChainBuilder().append("游戏结束！\n\n");
        List<GamePlayer> ranks = Manager.rankValid(player1, player2);
        if(ranks.size() >= 2) {
            int avgRank = (player1.rank.scores.getOrDefault("溜冰棋", 1200) + player2.rank.scores.getOrDefault("溜冰棋", 1200)) / 2;
            // sine ease from 40 to 16 by avg rank
            int k = 40 - (int) (24 * Math.sin((avgRank - 1200) * Math.PI / 1600));
            if (avgRank > 2000) {
                k = 16;
            }
            if (avgRank < 1200) {
                k = 40;
            }
            k *= 2;
            int r1 = player2.rank.scores.getOrDefault("溜冰棋", 1200);
            int r2 = player1.rank.scores.getOrDefault("溜冰棋", 1200);
            int rank1;
            int rank2;
            if (Math.abs(r1 - r2) > 300) {
                if (r1 > r2) {
                    int k1 = 40 - (int) (24 * Math.sin((r1 - 1200) * Math.PI / 1600));
                    if (avgRank > 2000) {
                        k1 = 16;
                    }
                    if (avgRank < 1200) {
                        k1 = 40;
                    }
                    rank1 = player1.rank.eloRank("溜冰棋", status == 1 ? 1f : 0f, k, r1);
                    rank2 = player2.rank.eloRank("溜冰棋", status == 1 ? 0f : 1f, k1, r2);
                } else {
                    int k1 = 40 - (int) (24 * Math.sin((r2 - 1200) * Math.PI / 1600));
                    if (avgRank > 2000) {
                        k1 = 16;
                    }
                    if (avgRank < 1200) {
                        k1 = 40;
                    }
                    rank1 = player1.rank.eloRank("溜冰棋", status == 1 ? 1f : 0f, k1, r1);
                    rank2 = player2.rank.eloRank("溜冰棋", status == 1 ? 0f :1f, k, r2);
                }
            } else {
                rank1 = player1.rank.eloRank("溜冰棋", status == 1 ? 1f : 0f, k, r1);
                rank2 = player2.rank.eloRank("溜冰棋", status == 1 ? 0f : 1f, k, r2);
            }
            endMsg.append(new At(player1.id))
                    .append(" rank分：")
                    .append(rank1 > 0 ? "+" : "")
                    .append(String.valueOf(rank1)).append("\n");
            endMsg.append(new At(player2.id))
                    .append(" rank分：")
                    .append(rank2 > 0 ? "+" : "")
                    .append(String.valueOf(rank2));
        }
        status = 0;
        endMsg.append("\n\n恭喜胜者——")
                .append(new At(player.id))
                .append(" !!!");
        group.sendMessage(endMsg.build());
        MiraiGamePlugin.INSTANCE.players_map.get(this.group.getId()).clear();
        MiraiGamePlugin.INSTANCE.games.remove(group.getId());
        try {
            Manager.saveRank();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean allowGroup() {
        return true;
    }

    @Override
    public boolean isWaiting() {
        return status == 0;
    }

    @Override
    public int getMaxPlayer() {
        return 2;
    }

    @Override
    public void addPlayer(GamePlayer activePlayer) {
        if (player1 == null)
            player1 = activePlayer;
        else
            player2 = activePlayer;
        if(activePlayer.rank.banned()) {
            group.sendMessage("玩家"+activePlayer.name+"的排位信息被封禁，本局不会记录rank分");
        }
    }

    @Override
    public void remove(GamePlayer activePlayer) {
        if (player1.equals(activePlayer))
            player1 = null;
        else
            player2 = null;
    }

    public static class ThreadTime extends Thread {
        private final SkatingChess game;

        public ThreadTime(SkatingChess game) {
            this.game = game;
        }

        @Override
        public void run() {
            while (true) {

                game.lock.lock();
                try {
                    if (game.isWaiting())
                        break;
                    if (game.time == 60)
                        game.group.sendMessage("剩余时间60秒");
                    if (game.time == 30)
                        game.group.sendMessage("剩余时间30秒");
                    if (game.time == 20)
                        game.group.sendMessage("剩余时间20秒");
                    if (game.time == 10)
                        game.group.sendMessage("剩余时间10秒");
                    if (game.time == 5)
                        game.group.sendMessage("剩余时间5秒");
                    if (game.time-- <= 0) {
                        game.status = game.status % 2 + 1;
                        game.stop();
                    }
                } finally {
                    game.lock.unlock();
                }
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
