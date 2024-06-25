package com.github.starowo.mirai.game.santorini;

import com.google.common.collect.Lists;
import com.github.starowo.mirai.MSGHandler;
import com.github.starowo.mirai.MiraiGamePlugin;
import com.github.starowo.mirai.data.Manager;
import com.github.starowo.mirai.game.IGame;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Santorini implements IGame {

    public Group group;
    public GamePlayer player1;
    public GamePlayer player2;
    public int round = 0;
    public int status = 0;
    private final ReentrantLock lock = new ReentrantLock();

    public int[] board = new int[25];
    public int[][] tiles = new int[][]{{-1, -1}, {-1, -1}};
    public int[] timecard = new int[]{3, 3};

    public ArrayList<Character> cols = Lists.newArrayList('a', 'b', 'c', 'd', 'e');

    Thread thread = new ThreadTime(this);

    public int time = 90;

    public Santorini(Group group) {
        this.group = group;
    }


    private static int[] i2c(int i) {
        return new int[]{i % 5, i / 5};
    }

    private static int c2i(int x, int y) {
        return y * 5 + x;
    }

    public void start() {
        lock.lock();
        try {
            if (status != 0)
                return;
            status = (int) (3 + Math.round(Math.random()));
            group.sendMessage("游戏开始，" + (status % 2 == 1 ? player1.name : player2.name) + "先手，请选择一个位置放置角色1");
            sendImage();
            thread.start();
        } finally {
            lock.unlock();
        }
    }

    public Message input(GamePlayer player, MessageChain msg, boolean isGroup, boolean at) {
        lock.lock();
        try {
            int p = player == player1 ? 0 : 1;
            if (1 - p != status % 2) {
                if (at)
                    return new MessageChainBuilder().append("还没到你操作喔").build();
                else
                    return null;
            }
            String text = msg.contentToString().replaceAll("@" + group.getBot().getId(), "").trim().replaceAll(" +", " ");
            if (text.equals("投降") || text.equals("认输")) {
                status = status % 2 + 1;
                stop();
                return null;
            }
            if (text.equals("加时")) {
                if (timecard[p] > 0) {
                    timecard[p] --;
                    time += 48;
                    group.sendMessage(new MessageChainBuilder().append(player.name).append("已延长45秒时间，剩余加时卡：").append(String.valueOf(timecard[p])).build());
                    return null;
                }
                return new MessageChainBuilder().append("您没有额外的加时卡了").build();
            }
            if (status > 2) {
                if (text.length() !=2) {
                    if (at)
                        return new MessageChainBuilder().append("输入错误：").append(text).append(" 不是一个有效的坐标；\n输入坐标时字母在前数字在后，如【c3】").build();
                    else
                        return null;
                }
                int colMove = cols.indexOf(text.charAt(0));
                if (colMove == -1) {
                    if (at)
                        return new MessageChainBuilder().append("输入错误：").append(text).append(" 不是一个有效的坐标；\n输入坐标时字母在前数字在后，如【c3】").build();
                    else
                        return null;
                }
                int rowMove;
                try {
                    rowMove = Integer.parseInt(text.substring(1)) - 1;
                } catch (NumberFormatException e) {
                    if (at)
                        return new MessageChainBuilder().append("输入错误：").append(text).append(" 不是一个有效的坐标；\n输入坐标时字母在前数字在后，如【c3】").build();
                    else
                        return null;
                }
                if (rowMove < 0 || rowMove >= 5) {
                    return new MessageChainBuilder().append("输入错误：").append(text).append(" 不是一个有效的坐标").build();
                }
                int move = c2i(colMove, rowMove);
                if ((tiles[p][0] != -1 && tiles[p][0] == move) || (tiles[p][1] != -1 && tiles[p][1] == move) || (tiles[1 - p][0] != -1 && tiles[1 - p][0] == move) || (tiles[1 - p][1] != -1 && tiles[1 - p][1] == move)) {
                    return new MessageChainBuilder().append("非法操作：").append(text).append(" 已经有棋子了").build();
                }
                int index;
                if (tiles[p][0] == -1) {
                    index = 0;
                } else {
                    index = 1;
                }
                tiles[p][index] = move;
                status ++;
                if (tiles[0][0] != -1 && tiles[0][1] != -1 && tiles[1][0] != -1 && tiles[1][1] != -1) {
                    status = 2 - status % 2;
                }
                sendImage();
                time = 90;
                group.sendMessage(new MessageChainBuilder().append("已放置棋子").append(String.valueOf(index + 1)).append("于 ").append(text).append("\n").append("请 ").append(p == 1 ? player1.name : player2.name).append("操作").build());
                return null;
            }
            String[] operations = text.split(" ");
            if (operations.length != 3) {
                if (at)
                    return new MessageChainBuilder().append("输入错误\n请按【棋子 移动坐标 建造坐标】操作\n如【1 b3 a4】").build();
                else
                    return null;
            }
            int t;
            try {
                t = Integer.parseInt(operations[0]) - 1;
                if (t != 0 && t != 1) {
                    return new MessageChainBuilder().append("输入错误：").append(operations[0]).append(" 不是一个有效的棋子目标").build();
                }
            } catch (NumberFormatException e) {
                if (at)
                    return new MessageChainBuilder().append("输入错误：").append(operations[0]).append(" 不是一个有效的棋子目标").build();
                else
                    return null;
            }
            int tile = tiles[p][t];
            int[] pos = i2c(tile);
            int x = pos[0];
            int y = pos[1];
            int colMove = cols.indexOf(operations[1].charAt(0));
            if (colMove == -1) {
                if (at)
                    return new MessageChainBuilder().append("输入错误：").append(operations[1]).append(" 不是一个有效的坐标；\n输入坐标时字母在前数字在后，如【c3】").build();
                else
                    return null;
            }
            int rowMove;
            try {
                rowMove = Integer.parseInt(operations[1].substring(1)) - 1;
            } catch (NumberFormatException e) {
                if (at)
                    return new MessageChainBuilder().append("输入错误：").append(operations[1]).append(" 不是一个有效的坐标；\n输入坐标时字母在前数字在后，如【c3】").build();
                else
                    return null;
            }
            if (rowMove < 0 || rowMove >= 5) {
                return new MessageChainBuilder().append("输入错误：").append(operations[1]).append(" 不是一个有效的坐标").build();
            }
            int move = c2i(colMove, rowMove);
            int colBuild = cols.indexOf(operations[2].charAt(0));
            if (colBuild == -1) {
                if (at)
                    return new MessageChainBuilder().append("输入错误：").append(operations[1]).append(" 不是一个有效的坐标；\n输入坐标时字母在前数字在后，如【c3】").build();
                else
                    return null;
            }
            int rowBuild;
            try {
                rowBuild = Integer.parseInt(operations[2].substring(1)) - 1;
            } catch (NumberFormatException e) {
                if (at)
                    return new MessageChainBuilder().append("输入错误：").append(operations[1]).append(" 不是一个有效的坐标；\n输入坐标时字母在前数字在后，如【c3】").build();
                else
                    return null;
            }
            if (rowBuild < 0 || rowBuild >= 5) {
                return new MessageChainBuilder().append("输入错误：").append(operations[1]).append(" 不是一个有效的坐标").build();
            }
            int build = c2i(colBuild, rowBuild);
            if (move == tile || Math.abs(x - colMove) > 1 || Math.abs(y - rowMove) > 1) {
                return new MessageChainBuilder().append("非法操作：选定的棋子无法移动到").append(operations[1]).build();
            }
            if (board[move] == 4 || board[move] - board[tile] > 1) {
                return new MessageChainBuilder().append("非法操作：").append(operations[1]).append("太高了，选定的棋子无法移动到目标位置").build();
            }
            if ((tiles[p][0] != -1 && tiles[p][0] == move) || (tiles[p][1] != -1 && tiles[p][1] == move) || (tiles[1 - p][0] != -1 && tiles[1 - p][0] == move) || (tiles[1 - p][1] != -1 && tiles[1 - p][1] == move)) {
                return new MessageChainBuilder().append("非法操作：").append(operations[1]).append(" 已经有棋子了").build();
            }
            if (build == move || Math.abs(colBuild - colMove) > 1 || Math.abs(rowBuild - rowMove) > 1) {
                return new MessageChainBuilder().append("非法操作：移动后无法在 ").append(operations[2]).append(" 进行建造").build();
            }
            if (board[build] == 4) {
                return new MessageChainBuilder().append("非法操作：").append(operations[2]).append("已经建筑到最高层了").build();
            }
            int before = tiles[p][t];
            tiles[p][t] = move;
            if ((tiles[p][0] != -1 && tiles[p][0] == build) || (tiles[p][1] != -1 && tiles[p][1] == build) || (tiles[1 - p][0] != -1 && tiles[1 - p][0] == build) || (tiles[1 - p][1] != -1 && tiles[1 - p][1] == build)) {
                tiles[p][t] = before;
                return new MessageChainBuilder().append("非法操作：").append(operations[2]).append("有棋子，你不能在此建筑").build();
            }
            board[build] ++;
            sendImage();
            if (check(p)) {
                stop();
                return null;
            }
            time = 90;
            status = status % 2 + 1;
            group.sendMessage(new MessageChainBuilder().append(player.name).append("移动棋子").append(operations[0]).append("到").append(operations[1]).append("\n在").append(operations[2]).append("进行建造\n请").append(p == 1 ? player1.name : player2.name).append("操作").build());
            return null;
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        GamePlayer player;
        player = status == 1 ? player1 : player2;
        int exp_win = Math.min(Math.max(0, (round - 10) * 80), 2000);
        int exp_lost = exp_win / 2;
        MessageChainBuilder endMsg = new MessageChainBuilder().append("游戏结束！\n\n");
        List<GamePlayer> ranks = Manager.rankValid(player1, player2);
        if(ranks.size() >= 2) {
            int avgRank = (player1.rank.scores.getOrDefault("圣托里尼", 1200) + player2.rank.scores.getOrDefault("圣托里尼", 1200)) / 2;
            // sine ease from 40 to 16 by avg rank from 1200 to 2000
            int k = 40 - (int) (24 * Math.sin((avgRank - 1200) * Math.PI / 1600));
            if (avgRank > 2000) {
                k = 16;
            }
            if (avgRank < 1200) {
                k = 40;
            }
            k *= 2;
            int r1 = player2.rank.scores.getOrDefault("圣托里尼", 1200);
            int r2 = player1.rank.scores.getOrDefault("圣托里尼", 1200);
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
                    rank1 = player1.rank.eloRank("圣托里尼", status == 1 ? 1f : 0f, k, r1);
                    rank2 = player2.rank.eloRank("圣托里尼", status == 1 ? 0f : 1f, k1, r2);
                } else {
                    int k1 = 40 - (int) (24 * Math.sin((r2 - 1200) * Math.PI / 1600));
                    if (avgRank > 2000) {
                        k1 = 16;
                    }
                    if (avgRank < 1200) {
                        k1 = 40;
                    }
                    rank1 = player1.rank.eloRank("圣托里尼", status == 1 ? 1f : 0f, k1, r1);
                    rank2 = player2.rank.eloRank("圣托里尼", status == 1 ? 0f :1f, k, r2);
                }
            } else {
                rank1 = player1.rank.eloRank("圣托里尼", status == 1 ? 1f : 0f, k, r1);
                rank2 = player2.rank.eloRank("圣托里尼", status == 1 ? 0f : 1f, k, r2);
            }
            endMsg.append(new At(player1.id))
                    .append(" rank分：")
                    .append(rank1 > 0 ? "+" : "")
                    .append(String.valueOf(rank1))
                    .append("经验: +")
                    .append(rank1 > 0 ? String.valueOf(exp_win) : String.valueOf(exp_lost))
                    .append("\n");
            endMsg.append(new At(player2.id))
                    .append(" rank分：")
                    .append(rank2 > 0 ? "+" : "")
                    .append(String.valueOf(rank2))
                    .append("经验: +")
                    .append(rank2 > 0 ? String.valueOf(exp_win) : String.valueOf(exp_lost));
            player1.data.addExp(rank1 > 0 ? exp_win : exp_lost);
            player2.data.addExp(rank2 > 0 ? exp_win : exp_lost);
        } else {
            endMsg.append(new At(player1.id))
                    .append("经验: +")
                    .append(player == player1 ? String.valueOf(exp_win) : String.valueOf(exp_lost))
                    .append("\n");
            endMsg.append(new At(player2.id))
                    .append("经验: +")
                    .append(player == player2 ? String.valueOf(exp_win) : String.valueOf(exp_lost));
            player1.data.addExp(player == player1 ? exp_win : exp_lost);
            player2.data.addExp(player == player2 ? exp_win : exp_lost);
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

    public void sendImage() {
        round ++;
        BufferedImage iBlank = new BufferedImage(6*64+1, 6*64+1, 2);
        Graphics2D g = iBlank.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, 6*64+1, 6*64+1);
        g.setColor(new Color(200, 255, 200));
        g.fillRect(64, 64, 5*64+1, 5*64+1);
        Font font = new Font("微软雅黑", Font.BOLD, 32);
        g.setFont(font);
        g.setColor(Color.BLACK);
        char c = 'a';
        for (int i = 0; i <=5; i++, c++) {
            int w = g.getFontMetrics().stringWidth(String.valueOf(c));
            int h = g.getFontMetrics().getHeight();
            int centerX = 64 * (i + 1) + (64 - w) / 2;
            int centerY = (64 - h) / 2 + g.getFontMetrics().getAscent();
            g.drawString(String.valueOf(c), centerX, centerY);
        }
        for (int i = 0; i < 5; i++) {
            int w = g.getFontMetrics().stringWidth(String.valueOf(i+1));
            int h = g.getFontMetrics().getHeight();
            int centerX = (64 - w) / 2;
            int centerY = 64 * (i + 1) + (64 - h) / 2 + g.getFontMetrics().getAscent();
            g.drawString(String.valueOf(i+1), centerX, centerY);
        }
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                int x = 64 * (i + 1);
                int y = 64 * (j + 1);
                int h = board[c2i(i, j)];
                Color[] colors = new Color[]{new Color(215, 232, 255), new Color(155, 194, 230), new Color(47, 117, 181), new Color(0, 32, 96)};
                for (int k = 0; k < h; k++) {
                    g.setColor(colors[k]);
                    if (k == 3) {
                        g.fillOval(x + 4 * k, y + 4 * k, 64 - 8 * k, 64 - 8 * k);
                        break;
                    }
                    g.fillRect(x + 4 * k, y + 4 * k, 64 - 8 * k, 64 - 8 * k);
                }
                g.setColor(Color.gray);
                g.drawRect(x, y, 63, 63);
            }
        }
        font = new Font("微软雅黑", Font.PLAIN, 24);
        g.setFont(font);
        for (int i = 0; i < 2; i++) {
            int t = tiles[0][i];
            if (t != -1) {
                int[] coord = i2c(t);
                int x = coord[0];
                int y = coord[1];
                g.setColor(new Color(0, 176, 240));
                g.fillOval(64 * (x + 1) + 16, 64 * (y + 1) + 16, 32, 32);
                g.setColor(Color.black);
                String s = (i+1) + "";
                int w = g.getFontMetrics().stringWidth(s);
                int h = g.getFontMetrics().getHeight();
                int centerX = 64 * (x+1) + (64 - w) / 2;
                int centerY = 64 * (y+1) + (64 - h) / 2 + g.getFontMetrics().getAscent();
                g.drawString(s, centerX, centerY);
            }
            int t2 = tiles[1][i];
            if (t2 != -1) {
                int[] coord = i2c(t2);
                int x = coord[0];
                int y = coord[1];
                g.setColor(new Color(255, 255, 255));
                g.fillOval(64 * (x + 1) + 16, 64 * (y + 1) + 16, 32, 32);
                g.setColor(Color.black);
                String s = (i+1) + "";
                int w = g.getFontMetrics().stringWidth(s);
                int h = g.getFontMetrics().getHeight();
                int centerX = 64 * (x+1) + (64 - w) / 2;
                int centerY = 64 * (y+1) + (64 - h) / 2 + g.getFontMetrics().getAscent();
                g.drawString(s, centerX, centerY);
            }
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

    private boolean check(int p) {
        for (int t : tiles[p]) {
            if (board[t] == 3) {
                return true;
            }
        }
        for (int t : tiles[1 - p]) {
            int[] coord = i2c(t);
            int x = coord[0];
            int y = coord[1];
            int h = board[t];
            for (int i = x % 5 == 0 ? x : x - 1; i <= ((x + 1) % 5 == 0 ? x : x + 1); i++) {
                for (int j = y % 5 == 0 ? y : y - 1; j <= ((y + 1) % 5 == 0 ? y : y + 1); j++) {
                    int k = c2i(i, j);
                    if (k == t) {
                        continue;
                    }
                    if (board[k] != 4 && board[k] - h < 2 && k != tiles[p][0] && k != tiles[p][1]) {
                        if ((tiles[p][0] != -1 && tiles[p][0] == k) || (tiles[p][1] != -1 && tiles[p][1] == k) || (tiles[1 - p][0] != -1 && tiles[1 - p][0] == k) || (tiles[1 - p][1] != -1 && tiles[1 - p][1] == k)) {
                            continue;
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean allowGroup() {
        return true;
    }

    public boolean isWaiting() {
        return status == 0;
    }

    public int getMaxPlayer() {
        return 2;
    }

    public void addPlayer(GamePlayer activePlayer) {
        if (player1 == null) {
            player1 = activePlayer;
        }
        else {
            player2 = activePlayer;
        }
        if(activePlayer.rank.banned()) {
            group.sendMessage("玩家"+activePlayer.name+"的排位信息被封禁，本局不会记录rank分");
        }
    }

    private BufferedImage getImg(String url) {
        if (url.equalsIgnoreCase("http://q1.qlogo.cn/g?b=qq&nk=372542780&s=640")) {
            File f = new File("./resources/saiwei.png");
            if(f.exists()) {
                try {
                    return ImageIO.read(f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            URL url1 = new URL(url);
            try (InputStream is = url1.openStream()) {
                return ImageIO.read(is);
            }
        } catch (IOException ignored) {

        }
        return null;
    }

    public void remove(GamePlayer activePlayer) {
        if (player1.equals(activePlayer))
            player1 = null;
        else
            player2 = null;
    }

    public boolean needAt() {
        return false;
    }

    public static class ThreadTime extends Thread {
        private final Santorini game;

        public ThreadTime(Santorini game) {
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
                        if (game.timecard[(game.status - 1) % 2] > 0) {
                            game.time += 45;
                            game.timecard[(game.status - 1) % 2] --;
                            game.group.sendMessage("行动超时，自动使用加时卡，剩余时间增加45秒；\n剩余加时卡：" + game.timecard[(game.status - 1) % 2]);
                            continue;
                        }
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
