package com.github.starowo.mirai.game.quoridor;

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
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Quoridor13 implements IGame {

    public Group group;
    public GamePlayer player1;
    public GamePlayer player2;
    public int status = 0;
    private final ReentrantLock lock = new ReentrantLock();

    public BufferedImage p1;
    public BufferedImage p2;

    public int[][] board = new int[25][25];
    public int[] tiles = new int[]{-1, -1};

    public int[] walls = new int[]{20, 20};

    public int[] timecard = new int[]{5, 5};

    public ArrayList<Character> directions = Lists.newArrayList('上', '右', '下', '左');
    public ArrayList<Character> indices = Lists.newArrayList('1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D');
    public ArrayList<Character> rows = Lists.newArrayList('m', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x');
    public ArrayList<Character> cols = Lists.newArrayList('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l');
    Thread thread = new ThreadTime(this);
    public int time = 90;
    public int round;

    public Quoridor13(Group group) {
        this.group = group;
    }

    public void start() {
        lock.lock();
        try {
            if (status != 0)
                return;
            status = (int) (1 + Math.round(Math.random()));
            group.sendMessage("游戏开始，" + (status == 1 ? player1.name : player2.name) + "先手，请选择一个位置(1-D)放置角色");
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
                    timecard[p]--;
                    time += 48;
                    group.sendMessage(new MessageChainBuilder().append(player.name).append("已延长45秒时间，剩余加时卡：").append(String.valueOf(timecard[p])).build());
                    return null;
                }
                return new MessageChainBuilder().append("您没有额外的加时卡了").build();
            }
            char in = text.charAt(0);
            if (tiles[p] == -1) {
                try {
                    int pos = indices.indexOf(in) + 1;
                    if (pos < 1 || pos > 13) {
                        throw new NumberFormatException();
                    }
                    tiles[p] = c2i(p == 0 ? 0 : 24, 2 * (pos - 1));
                } catch (NumberFormatException e) {
                    if (at)
                        return new MessageChainBuilder().append("输入错误；请输入1-9之间的数字").build();
                    else
                        return null;
                }
                status = status % 2 + 1;
                sendImage();
                time = 90;
                if (tiles[1 - p] == -1) {
                    group.sendMessage(new MessageChainBuilder().append("请").append(p == 0 ? player2.name : player1.name).append("放置棋子").build());
                } else {
                    group.sendMessage(new MessageChainBuilder().append("请").append(p == 0 ? player2.name : player1.name).append("移动或放置障碍").build());
                }
                return null;
            }
            int dir = directions.indexOf(in);
            if (dir != -1) {
                if (text.length() != 1) {
                    return null;
                }
                if (canMove(tiles[p], dir, board)) {
                    int[] coord = i2c(tiles[p]);
                    int x = coord[0];
                    int y = coord[1];
                    if (dir % 2 == 0) {
                        y += dir == 0 ? -2 : 2;
                    } else {
                        x += dir == 3 ? -2 : 2;
                    }
                    int pos = c2i(x, y);
                    if (pos == tiles[1 - p]) {
                        if (canMove(pos, dir, board)) {
                            if (dir % 2 == 0) {
                                y += dir == 0 ? -2 : 2;
                            } else {
                                x += dir == 3 ? -2 : 2;
                            }
                            pos = c2i(x, y);
                        } else {
                            for (int i = 0; i < 4; i++) {
                                if (i != dir && i != (dir + 2) % 4) {
                                    if (canMove(pos, i, board)) {
                                        tiles[p] = pos;
                                        time = 90;
                                        group.sendMessage(new MessageChainBuilder().append(player.name).append("越过对方棋子时被阻挡，请再输入一个方向作为落脚点").build());
                                        if (isGroup) {
                                            return null;
                                        } else {
                                            return new MessageChainBuilder().append("越过对方棋子时被阻挡，请再输入一个方向作为落脚点").build();
                                        }
                                    }
                                }
                            }
                            return new MessageChainBuilder().append("非法操作：目标方向被阻挡，无法移动").build();
                        }
                    }
                    tiles[p] = pos;
                    sendImage();
                    if (check()) {
                        stop();
                        return null;
                    }
                    time = 90;
                    status = status % 2 + 1;
                    group.sendMessage(new MessageChainBuilder().append(player.name).append("向").append(in).append("移动\n请").append(p == 0 ? player2.name : player1.name).append("操作").build());
                    return null;
                }
                return new MessageChainBuilder().append("非法操作：目标方向被阻挡，无法移动").build();
            }
            boolean col = true;
            dir = cols.indexOf(in);
            if (dir == -1) {
                col = false;
                dir = rows.indexOf(in);
            }
            if (dir == -1) {
                if (at)
                    return new MessageChainBuilder().append("输入错误\n移动请输入上/下/左/右\n放置障碍请按照如【b34】的格式输入，小写字母在前，数字/大写字母在后").build();
                else
                    return null;
            }
            dir = dir * 2 + 1;
            try {
                int n1 = indices.indexOf(text.charAt(1));
                int n2 = indices.indexOf(text.charAt(2));
                if (n1 == -1 || n2 == -1) {
                    throw new NumberFormatException();
                }
                if (n1 > 12) {
                    return new MessageChainBuilder().append("输入错误：").append(text.substring(1)).append(" 位置超界；\n请按照如【b34】的格式输入，小写字母在前，数字/大写字母在后").build();
                }
                if (Math.abs(n1 - n2) != 1) {
                    return new MessageChainBuilder().append("输入错误：").append(text.substring(1)).append(" 位置不相邻").build();
                }
                if (tiles[0] == tiles[1]) {
                    return new MessageChainBuilder().append("非法操作：本回合你已选择移动且正在越过对方棋子，只能输入 上/下/左/右 来选择落脚点").build();
                }
                if (walls[p] <= 0) {
                    return new MessageChainBuilder().append("非法操作：你已经没有墙了").build();
                }
                if (n2 < n1) {
                    int n3 = n2;
                    n2 = n1;
                    n1 = n3;
                }
                n1 *= 2;
                int n3 = n1 + 1;
                n2 *= 2;
                if (col) {
                    if (board[n1][dir] != 0 || board[n2][dir] != 0 || board[n3][dir] != 0) {
                        return new MessageChainBuilder().append("非法操作：不可将墙叠放在已有障碍的位置").build();
                    }
                    board[n1][dir] = board[n2][dir] = board[n3][dir] = p + 1;
                } else {
                    if (board[dir][n1] != 0 || board[dir][n2] != 0 || board[dir][n3] != 0) {
                        return new MessageChainBuilder().append("非法操作：不可将墙叠放在已有障碍的位置").build();
                    }
                    board[dir][n1] = board[dir][n2] = board[dir][n3] = p + 1;
                }
                if (!validBoard(tiles[0], 24, board, new HashSet<>()) || !validBoard(tiles[1], 0, board, new HashSet<>())) {
                    if (col) {
                        board[n1][dir] = board[n2][dir] = board[n3][dir] = 0;
                    } else {
                        board[dir][n1] = board[dir][n2] = board[dir][n3] = 0;
                    }
                    return new MessageChainBuilder().append("非法操作：不可将棋子堵死").build();
                }
                walls[p] -= 1;
                sendImage();
                time = 90;
                status = status % 2 + 1;
                group.sendMessage(new MessageChainBuilder().append(player.name).append("放置了一个障碍\n剩余墙数：").append(String.valueOf(walls[p])).append("\n请").append(p == 0 ? player2.name : player1.name).append("操作").build());
                return null;
            } catch (NumberFormatException e) {
                if (at)
                    return new MessageChainBuilder().append("输入错误：").append(text.substring(1)).append(" 不是有效的位置；\n请按照如【b34】的格式输入，小写字母在前，数字/大写字母在后").build();
                else
                    return null;
            }

        } finally {
            lock.unlock();
        }
    }

    private void sendImage() {
        round++;
        BufferedImage iBlank = new BufferedImage(12*60+48*3+1, 12*60+48*3+1, 2);
        Graphics2D g = iBlank.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, 12*60+48*3+1, 12*60+48*3+1);
        Font font = new Font("微软雅黑", Font.BOLD, 24);
        g.setFont(font);
        g.setColor(Color.BLACK);
        char c = 'a';
        for (int i = 1; i <= 13; i++) {
            char c1 = indices.get(i - 1);
            int w = g.getFontMetrics().stringWidth(String.valueOf(c1));
            int h = g.getFontMetrics().getHeight();
            int centerX = 48 * i + (48 - w) / 2 + 12 * (i - 1);
            int centerY = (48 - h) / 2 + g.getFontMetrics().getAscent();
            g.drawString(String.valueOf(c1), centerX, centerY);
        }
        for (int i = 1; i <= 13; i++) {
            char c1 = indices.get(i - 1);
            int w = g.getFontMetrics().stringWidth(String.valueOf(c1));
            int h = g.getFontMetrics().getHeight();
            int centerX = (48 - w) / 2;
            int centerY = 48 * i + (48 - h) / 2 + g.getFontMetrics().getAscent() + 12 * (i - 1);
            g.drawString(String.valueOf(c1), centerX, centerY);
        }
        for (int i = 1; i <= 12; i++, c++) {
            int w = g.getFontMetrics().charWidth(c);
            int h = g.getFontMetrics().getHeight();
            int centerX = 48 * i + (12 - w) / 2 + 12 * (i - 1) + 48;
            int centerY = 12*60+48*2 + ((48 - h) / 2 + g.getFontMetrics().getAscent());
            g.drawString(String.valueOf(c), centerX, centerY);
        }
        for (int i = 1; i <= 12; i++, c++) {
            int w = g.getFontMetrics().charWidth(c);
            int h = g.getFontMetrics().getHeight();
            int centerX = 12*60+48*2 + ((48 - w) / 2);
            int centerY = 48 * i + (12 - h) / 2 + g.getFontMetrics().getAscent() + 12 * (i - 1) + 48;
            g.drawString(String.valueOf(c), centerX, centerY);
        }
        for (int i = 0; i < 25; i++) {
            for (int j = 0; j < 25; j++) {
                int x = i / 2 * 60 + ((i % 2 == 1) ? 48 : 0) + 48;
                int y = j / 2 * 60 + ((j % 2 == 1) ? 48 : 0) + 48;
                int width = i % 2 == 0 ? 47 : 11;
                int height = j % 2 == 0 ? 47 : 11;
                if(board[j][i] == 1) {
                    g.setColor(Color.pink);
                    g.fillRect(x, y, width, height);
                }
                if((i == 0 && j % 2 == 0)) {
                    g.setColor(new Color(255, 200, 200));
                    g.fillRect(x, y, width, height);
                }
                if(board[j][i] == 2) {
                    g.setColor(Color.cyan);
                    g.fillRect(x, y, width, height);
                }
                if((i == 24 && j % 2 == 0)) {
                    g.setColor(new Color(200, 200, 255));
                    g.fillRect(x, y, width, height);
                }
                g.setColor(Color.gray);
                g.drawRect(x, y, width, height);
            }
        }

        if(tiles[0] != -1) {
            int[] coord = i2c(tiles[0]);
            int i = coord[0], j = coord[1];
            int x = i / 2 * 60 + ((i % 2 == 1) ? 48 : 0) + 49;
            int y = j / 2 * 60 + ((j % 2 == 1) ? 48 : 0) + 49;
            int width = i % 2 == 0 ? 46 : 11;
            int height = j % 2 == 0 ? 46 : 11;
            if (p1 == null) {
                g.setColor(new Color(255, 64, 64));
                g.fillRect(x, y, width, height);
            }else {
                g.drawImage(p1, x, y, width, height, null);
            }
            g.drawRect(x, y, width, height);
        }
        if(tiles[1] != -1) {
            int[] coord = i2c(tiles[1]);
            int i = coord[0], j = coord[1];
            int x = i / 2 * 60 + ((i % 2 == 1) ? 48 : 0) + 49;
            int y = j / 2 * 60 + ((j % 2 == 1) ? 48 : 0) + 49;
            int width = i % 2 == 0 ? 46 : 11;
            int height = j % 2 == 0 ? 46 : 11;
            if (p2 == null) {
                g.setColor(new Color(64, 64, 255));
                g.fillRect(x, y, width, height);
            }else {
                g.drawImage(p2, x, y, width, height, null);
            }
            g.drawRect(x, y, width, height);
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
        return i2c(tiles[0])[0] == 24 || i2c(tiles[1])[0] == 0;
    }

    private static int[] i2c(int i) {
        return new int[]{i % 25, i / 25};
    }

    private static int c2i(int x, int y) {
        return y * 25 + x;
    }

    public static boolean canMove(int pos, int direction, int[][] board) {
        int[] coord = i2c(pos);
        int x = coord[0];
        int y = coord[1];
        if (direction % 2 == 0) {
            y += direction == 0 ? -1 : 1;
        }else {
            x += direction == 3 ? -1 : 1;
        }
        return x >= 0 && x < 25 && y >= 0 && y < 25 && board[y][x] == 0;
    }

    public static boolean validBoard(int pos, int target, int[][] board, HashSet<Integer> checked) {
        if (!checked.add(pos)) {
            return false;
        }
        for (int i = 0; i < 4; i++) {
            if (canMove(pos, i, board)) {
                int[] coord = i2c(pos);
                int x = coord[0];
                int y = coord[1];
                if (i % 2 == 0) {
                    y += i == 0 ? -2 : 2;
                }else {
                    x += i == 3 ? -2 : 2;
                }
                if (x == target)
                    return true;
                if (validBoard(c2i(x, y), target, board, checked))
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean needAt() {
        return false;
    }


    public void stop() {
        GamePlayer player;
        player = status == 1 ? player1 : player2;
        int exp_win = Math.min(Math.max(0, (round - 30) * 150), 3000);
        int exp_lost = exp_win / 2;
        MessageChainBuilder endMsg = new MessageChainBuilder().append("游戏结束！\n\n");
        List<GamePlayer> ranks = Manager.rankValid(player1, player2);
        if(ranks.size() >= 2) {
            int avgRank = (player1.rank.scores.getOrDefault("步步为营", 1200) + player2.rank.scores.getOrDefault("步步为营", 1200)) / 2;
            // sine ease from 40 to 16 by avg rank
            int k = (int) (40 - 24 * Math.sin((avgRank - 1200) * Math.PI / 1600) * 1.33);
            if (avgRank > 2000) {
                k = 16;
            }
            if (avgRank < 1200) {
                k = 40;
            }
            k *= 2;
            int r1 = player2.rank.scores.getOrDefault("步步为营", 1200);
            int r2 = player1.rank.scores.getOrDefault("步步为营", 1200);
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
                    rank1 = player1.rank.eloRank("步步为营", status == 1 ? 1f : 0f, k, r1);
                    rank2 = player2.rank.eloRank("步步为营", status == 1 ? 0f : 1f, k1, r2);
                } else {
                    int k1 = 40 - (int) (24 * Math.sin((r2 - 1200) * Math.PI / 1600));
                    if (avgRank > 2000) {
                        k1 = 16;
                    }
                    if (avgRank < 1200) {
                        k1 = 40;
                    }
                    rank1 = player1.rank.eloRank("步步为营", status == 1 ? 1f : 0f, k1, r1);
                    rank2 = player2.rank.eloRank("步步为营", status == 1 ? 0f :1f, k, r2);
                }
            } else {
                rank1 = player1.rank.eloRank("步步为营", status == 1 ? 1f : 0f, k, r1);
                rank2 = player2.rank.eloRank("步步为营", status == 1 ? 0f : 1f, k, r2);
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
            if (player1.avatar != null) {
                p1 = getImg(player1.avatar);
            }
        }
        else {
            player2 = activePlayer;
            if (player2.avatar != null) {
                p2 = getImg(player2.avatar);
            }
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

    public static class ThreadTime extends Thread {
        private final Quoridor13 game;

        public ThreadTime(Quoridor13 game) {
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
                        if (game.status == 1 && game.timecard[0] > 0) {
                            game.time += 45;
                            game.timecard[0] --;
                            game.group.sendMessage("行动超时，自动使用加时卡，剩余时间增加45秒；\n剩余加时卡：" + game.timecard[0]);
                            continue;
                        } else if (game.status == 2 && game.timecard[1] > 0) {
                            game.time += 45;
                            game.timecard[1] --;
                            game.group.sendMessage("行动超时，自动使用加时卡，剩余时间增加45秒；\n剩余加时卡：" + game.timecard[1]);
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
