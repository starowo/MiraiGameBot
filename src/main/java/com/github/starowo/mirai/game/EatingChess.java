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

public class EatingChess implements IGame {

    public Group group;
    public GamePlayer player1;
    public GamePlayer player2;
    public int status = -1;
    private final ReentrantLock lock = new ReentrantLock();

    public int[][] board = new int[7][7];
    public int tile = -1;
    public int last = -1;

    public ArrayList<String> directions = Lists.newArrayList("上", "右上", "右", "右下", "下", "左下", "左", "左上");

    Thread thread = new ThreadTime(this);
    public int time = 90;

    public EatingChess(Group group) {
        this.group = group;
    }

    @Override
    public void start() {
        lock.lock();
        try {
            if (status != -1)
                return;
            status = (int) (3 + Math.round(Math.random()));
            group.sendMessage("游戏开始，" + (status == 3 ? player1.name : player2.name) + "先手，请选择一个位置放置角色");
            sendImage();
            thread.start();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean needAt() {
        return false;
    }

    @Override
    public Message input(GamePlayer player, MessageChain msg, boolean isGroup, boolean at) {
        lock.lock();
        try {
            if (player1.equals(player)) {
                if (status % 2 == 0) {
                    if (at)
                        return new MessageChainBuilder().append("还没到你操作喔").build();
                    return null;
                }
            } else if (status % 2 == 1) {
                if (at)
                    return new MessageChainBuilder().append("还没到你操作喔").build();
                return null;
            }
            String text = msg.contentToString().replaceAll("@" + group.getBot().getId(), "").trim().replaceAll(" +", " ");
            String[] in = text.split(" ");
            try {
                int num = Integer.parseInt(in[0]) - 1;
                if(status > 2) {
                    if(num >= 0 && num < 49) {
                        tile = num;
                        int[] coord = i2c(num);
                        int x = coord[0], y = coord[1];
                        board[x][y] = 1;
                        status = status % 2 + 1;
                        sendImage();
                        time = 90;
                        return new MessageChainBuilder().append("请").append(status == 1 ? player1.name : player2.name).append("操作").build();
                    }else {
                        if (at)
                            return new MessageChainBuilder().append("数字超界").build();
                        return null;
                    }
                }else {
                    int dir = directions.indexOf(in[1]);
                    if (dir < 0) {
                        if (at)
                            return new MessageChainBuilder().append("未知的方向").build();
                        return null;
                    }
                    int[] coord = i2c(num);
                    int x = coord[0], y = coord[1];
                    if(num == tile || board[x][y] == 2) {
                        return new MessageChainBuilder().append("你不能在这个位置放置障碍").build();
                    }
                    int before = board[x][y];
                    board[x][y] = 2;
                    if (!canMove(dir)) {
                        board[x][y] = before;
                        return new MessageChainBuilder().append("你无法这样移动").build();
                    }
                    last = num;
                    move(dir);
                    sendImage();
                    if (check()) {
                        stop();
                        return null;
                    }
                    status = status % 2 + 1;
                    time = 90;
                    return new MessageChainBuilder().append("请").append(status == 1 ? player1.name : player2.name).append("操作").build();
                }
            } catch (NumberFormatException | IndexOutOfBoundsException throwable) {
                if (at)
                    return new MessageChainBuilder().append("格式错误！正确格式为【放墙的位置 移动方向】，如【13 下】").build();
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    private void sendImage() {
        BufferedImage iBlank = new BufferedImage(7 * 48 + 1, 7 * 48 + 1, 2);
        Graphics2D g = iBlank.createGraphics();
        Font font = new Font("微软雅黑", Font.PLAIN, 24);
        g.setFont(font);
        g.setColor(Color.white);
        g.fillRect(0, 0, 7 * 48 + 1, 7 * 48 + 1);
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                if (board[i][j] == 0) {
                    g.setColor(new Color(238, 238, 32));
                    g.fillArc(48 * i + 3, 48 * j + 3, 42, 42, 0, 360);
                }else if(board[i][j] == 3) {
                    board[i][j] = 1;
                    g.setColor(new Color(255, 243, 243));
                    g.fillRect(48 * i + 1, 48 * j + 1, 47, 47);
                }
                g.setColor(Color.gray);
                g.drawRect(48 * i, 48 * j, 48, 48);
                if(board[i][j] == 2) {
                    if(last == c2i(i, j)) {
                        g.setColor(new Color(103, 103, 103));
                    }else
                    g.setColor(Color.black);
                    g.fillRect(48 * i + 1, 48 * j + 1, 47, 47);
                }else {
                    g.setColor(new Color(11, 55, 55));
                    String s = (j * 7 + i + 1) + "";
                    int w = g.getFontMetrics().stringWidth(s);
                    int h = g.getFontMetrics().getHeight();
                    int centerX = 48 * (i) + (48 - w) / 2;
                    int centerY = 48 * (j) + (48 - h) / 2 + g.getFontMetrics().getAscent();
                    g.drawString(String.valueOf(j * 7 + i + 1), centerX, centerY);
                }
            }
        }
        g.setColor(new Color(255, 128, 128));
        if(tile != -1) {
            int[] coord = i2c(tile);
            int i = coord[0], j = coord[1];
            g.fillRect(48 * i + 1, 48 * j + 1, 47, 47);
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

    private boolean check() {
        for (int i = 0; i < 8; i++) {
            if(canMove(i)) {
                return false;
            }
        }
        return true;
    }

    private void move(int dir) {
        move((dir == 7 || dir < 2), (dir > 0 && dir < 4), (dir > 2 && dir < 6), dir > 4);
    }

    private void move(boolean up, boolean right, boolean down, boolean left) {
        int[] coord = i2c(tile);
        int x = coord[0], y = coord[1];
        board[x][y] = 3;
        while (x < 7 && y < 7) {
            if(right) x++;
            if(left) x--;
            if(up) y--;
            if (down) y ++;
            if(x >= 0 && x < 7 && y >= 0 && y < 7 && board[x][y] != 2) {
                board[x][y] = 3;
                tile = c2i(x, y);
            }else {
                return;
            }
        }
    }

    private boolean canMove(int dir) {
        return canMove((dir == 7 || dir < 2), (dir > 0 && dir < 4), (dir > 2 && dir < 6), dir > 4);
    }

    private boolean canMove(boolean up, boolean right, boolean down, boolean left) {
        int[] coord = i2c(tile);
        int x = coord[0], y = coord[1];
        boolean canMove = false;
        while (x < 7 && y < 7) {
            if(right) x++;
            if(left) x--;
            if(up) y--;
            if (down) y ++;
            if(x >= 0 && x < 7 && y >= 0 && y < 7 && board[x][y] != 2) {
                if(board[x][y] == 0)
                    canMove = true;
            }else {
                return canMove;
            }
        }
        return false;
    }

    @Override
    public void stop() {
        GamePlayer player;
        player = status == 1 ? player1 : player2;
        MessageChainBuilder endMsg = new MessageChainBuilder().append("游戏结束！\n\n");
        List<GamePlayer> ranks = Manager.rankValid(player1, player2);
        if(ranks.size() >= 2) {
            int avgRank = (player1.rank.scores.getOrDefault("贪吃棋", 1200) + player2.rank.scores.getOrDefault("贪吃棋", 1200)) / 2;
            int k = 40 - (int) (24 * Math.sin((avgRank - 1200) * Math.PI / 1600));
            if (avgRank > 2000) {
                k = 16;
            }
            if (avgRank < 1200) {
                k = 40;
            }
            int r1 = player2.rank.scores.getOrDefault("贪吃棋", 1200);
            int r2 = player1.rank.scores.getOrDefault("贪吃棋", 1200);
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
                    rank1 = player1.rank.eloRank("贪吃棋", status == 1 ? 1f : 0f, k, r1);
                    rank2 = player2.rank.eloRank("贪吃棋", status == 1 ? 0f : 1f, k1, r2);
                } else {
                    int k1 = 40 - (int) (24 * Math.sin((r2 - 1200) * Math.PI / 1600));
                    if (avgRank > 2000) {
                        k1 = 16;
                    }
                    if (avgRank < 1200) {
                        k1 = 40;
                    }
                    rank1 = player1.rank.eloRank("贪吃棋", status == 1 ? 1f : 0f, k1, r1);
                    rank2 = player2.rank.eloRank("贪吃棋", status == 1 ? 0f :1f, k, r2);
                }
            } else {
                rank1 = player1.rank.eloRank("贪吃棋", status == 1 ? 1f : 0f, k, r1);
                rank2 = player2.rank.eloRank("贪吃棋", status == 1 ? 0f : 1f, k, r2);
            }
            k *= 2;
            endMsg.append(new At(player1.id))
                    .append(" rank分：")
                    .append(rank1 > 0 ? "+" : "")
                    .append(String.valueOf(rank1)).append("\n");
            endMsg.append(new At(player2.id))
                    .append(" rank分：")
                    .append(rank2 > 0 ? "+" : "")
                    .append(String.valueOf(rank2));
        }
        status = -1;
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

    private static int[] i2c(int i) {
        return new int[]{i % 7, i / 7};
    }

    private static int c2i(int x, int y) {
        return y * 7 + x;
    }

    @Override
    public boolean allowGroup() {
        return true;
    }

    @Override
    public boolean isWaiting() {
        return status == -1;
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
        private final EatingChess game;

        public ThreadTime(EatingChess game) {
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
