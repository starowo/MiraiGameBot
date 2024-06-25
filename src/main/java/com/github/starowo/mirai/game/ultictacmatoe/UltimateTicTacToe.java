package com.github.starowo.mirai.game.ultictacmatoe;

import com.google.common.collect.Lists;
import com.github.starowo.mirai.MSGHandler;
import com.github.starowo.mirai.MiraiGamePlugin;
import com.github.starowo.mirai.command.CommandRandom;
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
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class UltimateTicTacToe implements IGame {

    public static final List<Integer[]> WIN_CHECK = Lists.newArrayList(
            new Integer[]{0, 1, 2},
            new Integer[]{3, 4, 5},
            new Integer[]{6, 7, 8},
            new Integer[]{0, 3, 6},
            new Integer[]{1, 4, 7},
            new Integer[]{2, 5, 8},
            new Integer[]{0, 4, 8},
            new Integer[]{2, 4, 6}
    );

    public Group group;
    public GamePlayer player1;
    public GamePlayer player2;
    public int status = 0;
    public int round = 0;
    public int last = -1;

    private final ReentrantLock lock = new ReentrantLock();

    public int[] bigboard = new int[9];
    public int[][] board = new int[9][9];
    public int[] timecard = new int[]{3, 3};

    Thread thread = new ThreadTime(this);

    public int time = 90;

    public UltimateTicTacToe(Group group) {
        this.group = group;
    }

    @Override
    public void start() {
        lock.lock();
        try {
            if (status != 0)
                return;
            status = 1;
            group.sendMessage("游戏开始，" + (player1.name) + "先手，请选择一个位置落子");
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
            if (text.length() !=2) {
                if (at)
                    return new MessageChainBuilder().append("输入错误：").append(text).append(" 不是一个有效的棋盘位置").build();
                else
                    return null;
            }
            int bigIndex = text.charAt(0) - '1';
            int index = text.charAt(1) - '1';
            if (bigIndex < 0 || bigIndex > 8 || index < 0 || index > 8) {
                if (at)
                    return new MessageChainBuilder().append("输入错误：").append(text).append(" 不是一个有效的棋盘位置").build();
                else
                    return null;
            }
            if (last != -1 && last != bigIndex) {
                return new MessageChainBuilder().append("非法操作：你被限制在第").append(String.valueOf(last + 1)).append("棋盘宫格落子").build();
            }
            if (bigboard[bigIndex] != 0) {
                return new MessageChainBuilder().append("输入错误：").append(text).append(" 所在的大棋盘已经结束了").build();
            }
            if (board[bigIndex][index] != 0) {
                return new MessageChainBuilder().append("输入错误：").append(text).append(" 已经有棋子了").build();
            }
            board[bigIndex][index] = p + 1;
            last = index;
            int check = check(bigIndex);
            if (check != -1) {
                last = -1;
                sendImage();
                if (check == 3) {
                    status = 3;
                    stop();
                    return null;
                }
                status = check;
                stop();
                return null;
            }
            if (bigboard[index] == 0) {
                last = index;
            } else {
                last = -1;
            }
            sendImage();
            time = 90;
            status = status % 2 + 1;
            group.sendMessage(new MessageChainBuilder().append(player.name).append("在").append(text).append("落子；请").append(p == 1 ? player1.name : player2.name).append("落子").build());
            return null;
        } finally {
            lock.unlock();
        }
    }

    public int check(int i) {
        OUTER:
        for (Integer[] win_lane : WIN_CHECK) {
            for (int p : win_lane) {
                if (board[i][p] != board[i][win_lane[0]] || board[i][p] == 0) {
                    break;
                }
                if (p == win_lane[2]) {
                    bigboard[i] = board[i][win_lane[0]];
                    break OUTER;
                }
            }
        }
        if (bigboard[i] == 0) {
            for (int p : board[i]) {
                if (p == 0) {
                    return -1;
                }
            }
            bigboard[i] = 3;
        }
        for (Integer[] win_lane : WIN_CHECK) {
            for (int p : win_lane) {
                if (bigboard[p] != bigboard[win_lane[0]] || bigboard[p] == 0 || bigboard[p] == 3) {
                    break;
                }
                if (p == win_lane[2]) {
                    return bigboard[p];
                }
            }
        }
        for (int p : bigboard) {
            if (p == 0) {
                return -1;
            }
        }
        return 3;
    }

    private void sendImage() {
        round++;
        BufferedImage iBlank = new BufferedImage(9*48+1, 9*48+1, 2);
        Graphics2D g = iBlank.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, 9*48+1, 9*48+1);
        g.setColor(Color.black);
        g.setFont(new Font("微软雅黑", Font.PLAIN, 24));
        for (int i = 0; i < 9; i++) {
            int bigX = i % 3;
            int bigY = i / 3;
            int bigCoordX = bigX * 3 * 48;
            int bigCoordY = bigY * 3 * 48;
            for (int j = 0; j < 9; j++) {
                int x = j % 3;
                int y = j / 3;
                int coordX = bigCoordX + x * 48;
                int coordY = bigCoordY + y * 48;
                String text = i + 1 + String.valueOf((j+1));
                if (board[i][j] == 1) {
                    g.setFont(new Font("微软雅黑", Font.BOLD, 24));
                    g.setColor(Color.PINK);
                    text = "O";
                } else if (board[i][j] == 2) {
                    g.setFont(new Font("微软雅黑", Font.BOLD, 24));
                    g.setColor(Color.BLUE);
                    text = "X";
                }
                int w = g.getFontMetrics().stringWidth(text);
                int h = g.getFontMetrics().getHeight();
                int centerX = coordX + (48 - w) / 2;
                int centerY = coordY + (48 - h) / 2 + g.getFontMetrics().getAscent();
                g.drawString(text, centerX, centerY);
                g.setFont(new Font("微软雅黑", Font.PLAIN, 24));
                g.setColor(Color.black);
                g.drawRect(coordX, coordY, 48, 48);
            }
            if (bigboard[i] != 0) {
                g.setColor(new Color(0, 0, 0, 128));
                g.fillRect(bigCoordX, bigCoordY, 3 * 48, 3 * 48);
                g.setFont(new Font("微软雅黑", Font.BOLD, 72));
                String text;
                if (bigboard[i] == 1) {
                    g.setColor(Color.PINK);
                    text = "O";
                } else if (bigboard[i] == 2) {
                    g.setColor(Color.BLUE);
                    text = "X";
                } else {
                    g.setColor(Color.white);
                    text = "平";
                }
                int w = g.getFontMetrics().stringWidth(text);
                int h = g.getFontMetrics().getHeight();
                int centerX = bigCoordX + (3 * 48 - w) / 2;
                int centerY = bigCoordY + (3 * 48 - h) / 2 + g.getFontMetrics().getAscent();
                g.drawString(text, centerX, centerY);
                g.setColor(Color.black);
                g.setFont(new Font("微软雅黑", Font.PLAIN, 24));
            }
            g.setColor(last == i ? Color.orange : Color.black);
            g.drawRect(bigCoordX, bigCoordY, 3 * 48, 3 * 48);
            g.drawRect(bigCoordX + 1, bigCoordY + 1, 3 * 48 - 2, 3 * 48 - 2);
            g.setColor(Color.black);
        }
        if (last == -1) {
            g.setColor(Color.orange);
            g.drawRect(0, 0, 9 * 48, 9 * 48);
            g.drawRect(1, 1, 9 * 48 - 2, 9 * 48 - 2);
        } else {
            int bigX = last % 3;
            int bigY = last / 3;
            int bigCoordX = bigX * 3 * 48;
            int bigCoordY = bigY * 3 * 48;
            g.setColor(Color.orange);
            g.drawRect(bigCoordX, bigCoordY, 3 * 48, 3 * 48);
            g.drawRect(bigCoordX + 1, bigCoordY + 1, 3 * 48 - 2, 3 * 48 - 2);
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

    @Override
    public boolean needAt() {
        return false;
    }

    public void stop() {
        GamePlayer player;
        player = status == 1 ? player1 : player2;
        int exp_win = Math.min(Math.max(0, (round - 20) * 80), 2000);
        int exp_lost = exp_win / 2;
        if (status == 3) {
            exp_win = (exp_win + exp_lost) / 2;
            exp_lost = exp_win;
        }
        MessageChainBuilder endMsg = new MessageChainBuilder().append("游戏结束！\n\n");
        List<GamePlayer> ranks = Manager.rankValid(player1, player2);
        if(ranks.size() >= 2) {
            int avgRank = (player1.rank.scores.getOrDefault("终极井字棋", 1200) + player2.rank.scores.getOrDefault("终极井字棋", 1200)) / 2;
            // sine ease from 40 to 16 by avg rank
            int k = 40 - (int) (24 * Math.sin((avgRank - 1200) * Math.PI / 1600));
            if (avgRank > 2000) {
                k = 16;
            }
            if (avgRank < 1200) {
                k = 40;
            }
            k *= 2;
            int r1 = player2.rank.scores.getOrDefault("终极井字棋", 1200);
            int r2 = player1.rank.scores.getOrDefault("终极井字棋", 1200);
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
                    rank1 = player1.rank.eloRank("终极井字棋", status == 1 ? 1f : status == 3 ? 0.5f : 0f, k, r1);
                    rank2 = player2.rank.eloRank("终极井字棋", status == 1 ? 0f : status == 3 ? 0.5f : 1f, k1, r2);
                } else {
                    int k1 = 40 - (int) (24 * Math.sin((r2 - 1200) * Math.PI / 1600));
                    if (avgRank > 2000) {
                        k1 = 16;
                    }
                    if (avgRank < 1200) {
                        k1 = 40;
                    }
                    rank1 = player1.rank.eloRank("终极井字棋", status == 1 ? 1f : status == 3 ? 0.5f : 0f, k1, r1);
                    rank2 = player2.rank.eloRank("终极井字棋", status == 1 ? 0f : status == 3 ? 0.5f : 1f, k, r2);
                }
            } else {
                rank1 = player1.rank.eloRank("终极井字棋", status == 1 ? 1f : status == 3 ? 0.5f : 0f, k, r1);
                rank2 = player2.rank.eloRank("终极井字棋", status == 1 ? 0f : status == 3 ? 0.5f : 1f, k, r2);
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

        if (status != 3) {
            endMsg.append("\n\n恭喜胜者——")
                    .append(new At(player.id))
                    .append(" !!!");
        } else {
            endMsg.append("\n\n平局！");
        }
        group.sendMessage(endMsg.build());
        status = 0;
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
        if ((player1 == null && CommandRandom.rd.nextBoolean()) || player2 != null) {
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
        if (player1 != null && player1.equals(activePlayer))
            player1 = null;
        else
            player2 = null;
    }

    public static class ThreadTime extends Thread {
        private final UltimateTicTacToe game;

        public ThreadTime(UltimateTicTacToe game) {
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
