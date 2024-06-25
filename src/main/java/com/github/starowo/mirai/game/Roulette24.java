package com.github.starowo.mirai.game;

import com.github.starowo.mirai.MiraiGamePlugin;
import com.github.starowo.mirai.data.Manager;
import com.github.starowo.mirai.player.GamePlayer;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Roulette24 implements IGame {

    private static final BufferedImage unknown;
    private static final BufferedImage bullet;
    private static final BufferedImage empty;

    static {
        BufferedImage image = new BufferedImage(40, 40, 2);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLACK);
        g.drawArc(0, 0, 40, 40, 0, 360);
        g.drawArc(4, 4, 32, 32, 0, 360);
        Font font = new Font("微软雅黑", Font.BOLD, 24);
        g.setFont(font);
        int w = g.getFontMetrics().charWidth('?');
        int h = g.getFontMetrics().getHeight();
        int centerX = (40 - w) / 2;
        int centerY = (40 - h) / 2 + g.getFontMetrics().getAscent();
        g.drawString("?", centerX, centerY);
        g.dispose();
        unknown = image;
        image = new BufferedImage(40, 40, 2);
        g = image.createGraphics();
        g.setColor(Color.BLACK);
        g.drawArc(0, 0, 40, 40, 0, 360);
        g.fillArc(4, 4, 32, 32, 0, 360);
        g.dispose();
        bullet = image;
        image = new BufferedImage(40, 40, 2);
        g = image.createGraphics();
        g.setColor(Color.BLACK);
        g.drawArc(0, 0, 40, 40, 0, 360);
        g.drawArc(4, 4, 32, 32, 0, 360);
        g.dispose();
        empty = image;
    }

    public Group group;
    public GamePlayer player1;
    public GamePlayer player2;
    public int status = 0;
    public boolean received1 = false;
    public boolean received2 = false;
    public int index = 0;
    public int shot = 0;
    public int chips1 = 100;
    public int chips2 = 100;
    public int chips_on_table = 0;
    public List<Boolean> bullets = new ArrayList<>();
    public List<Integer> repeated = new ArrayList<>();
    public int lost1 = 0;
    public int lost2 = 0;
    public int winner = -1;
    public int stack = 0;
    public int time = 40;
    public int lostChips = 0;
    Random rand = new Random();
    public boolean acting = rand.nextBoolean();
    Thread thread = new ThreadTime(this);
    private final ReentrantLock lock = new ReentrantLock();

    public int rate;

    public Roulette24(Group group, int rate) {
        this.group = group;
        this.rate = rate;
    }

    @Override
    public void start() {
        lock.lock();
        try {
            player1.data.creditInGame = rate * 300;
            player2.data.creditInGame = rate * 300;
            status = 1;
            for (int i = 0; i < 24; i++)
                bullets.add(false);
            group.sendMessage("请私聊裁判3个要放置子弹的弹位，格式为\"弹位1 弹位2 弹位3\"");
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Message input(GamePlayer player, MessageChain msg) {
        return null;
    }

    @Override
    public Message input(GamePlayer player, MessageChain msg, boolean isGroup) {
        lock.lock();
        try {
            if (status == 1) {
                if (isGroup) {
                    return new MessageChainBuilder().append(new QuoteReply(msg)).append("请私聊裁判").build();
                }
                if (player1.equals(player)) {
                    if (received1) {
                        return new MessageChainBuilder().append("你已经设置过子弹了").build();
                    }
                    String text = msg.contentToString();
                    String[] indices = text.split(" ");
                    try {
                        int index0 = Integer.parseInt(indices[0]) - 1;
                        int index1 = Integer.parseInt(indices[1]) - 1;
                        int index2 = Integer.parseInt(indices[2]) - 1;
                        if (index0 > 23 || index0 < 0 || index1 > 23 || index1 < 0 || index2 > 23 || index2 < 0) {
                            return new MessageChainBuilder().append("弹位超界！").build();
                        }
                        if (index0 == index1 || index0 == index2 || index2 == index1)
                            return new MessageChainBuilder().append("请勿设置重复弹位！").build();
                        if (bullets.set(index0, true))
                            repeated.add(index0);
                        if (bullets.set(index1, true))
                            repeated.add(index1);
                        if (bullets.set(index2, true))
                            repeated.add(index2);
                        received1 = true;
                        update();
                        return new MessageChainBuilder().append("设置成功！").build();
                    } catch (NumberFormatException | IndexOutOfBoundsException throwable) {
                        return new MessageChainBuilder().append("格式错误！").build();
                    }
                } else {
                    if (received2) {
                        return new MessageChainBuilder().append("你已经设置过子弹了").build();
                    }
                    String text = msg.contentToString();
                    String[] indices = text.split(" ");
                    try {
                        int index0 = Integer.parseInt(indices[0]) - 1;
                        int index1 = Integer.parseInt(indices[1]) - 1;
                        int index2 = Integer.parseInt(indices[2]) - 1;
                        if (index0 > 23 || index0 < 0 || index1 > 23 || index1 < 0 || index2 > 23 || index2 < 0) {
                            return new MessageChainBuilder().append("弹位超界！").build();
                        }
                        if (index0 == index1 || index0 == index2 || index2 == index1)
                            return new MessageChainBuilder().append("请勿设置重复弹位！").build();
                        if (bullets.set(index0, true))
                            repeated.add(index0);
                        if (bullets.set(index1, true))
                            repeated.add(index1);
                        if (bullets.set(index2, true))
                            repeated.add(index2);
                        received2 = true;
                        update();
                        return new MessageChainBuilder().append("设置成功！").build();
                    } catch (NumberFormatException | IndexOutOfBoundsException throwable) {
                        return new MessageChainBuilder().append("格式错误！").build();
                    }
                }
            }
            if (status == 2) {
                if (!isGroup) {
                    return new MessageChainBuilder().append("请公屏发送").build();
                }
                String text = msg.contentToString().replaceAll("@" + group.getBot().getId(), "").trim();
                if (text.equals("投降")) {
                    if (player.equals(player1)) {
                        chips2 += chips_on_table;
                        winner = 2;
                    } else {
                        chips1 += chips_on_table;
                        winner = 1;
                    }
                    stop();
                    return null;
                }
                if ((player.equals(player1) && !acting) || player.equals(player2) && acting) {
                    return new MessageChainBuilder()
                            .append("现在不是你的回合哦~")
                            .append(new QuoteReply(msg))
                            .build();
                }
                if (text.equals("跳过")) {
                    int chips = (int) Math.pow(2, stack);
                    if (acting) {
                        chips1 -= chips;
                    } else {
                        chips2 -= chips;
                    }
                    chips_on_table += chips;
                    update();
                    return null;
                }
                if (text.equals("开枪")) {
                    stack = -1;
                    update();
                }
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    public void update() {
        if (status == 1) {
            if (received1 && received2) {
                if (repeated.size() > 0) {
                    MessageChainBuilder builder = new MessageChainBuilder().append("弹位");
                    builder.append(String.valueOf(repeated.get(0) + 1));
                    for (int i = 1; i < repeated.size(); i++) {
                        builder.append(",");
                        builder.append(String.valueOf(repeated.get(i) + 1));
                    }
                    group.sendMessage(builder.append("重复，请两位选手重新设置").build());
                    received1 = (received2 = false);
                    for (int i = 0; i < 24; i++) {
                        bullets.set(i, false);
                    }
                    repeated.clear();
                    return;
                }
                status = 2;
                int r = rand.nextInt(24);
                for (int i = 0; i < r; i++) {
                    bullets.add(bullets.get(i));
                }
                bullets.subList(0, r).clear();
                GamePlayer player = acting ? player1 : player2;
                group.sendMessage(new MessageChainBuilder()
                        .append("弹位设置完毕\n")
                        .append(new At(player.id))
                        .append(" 先手，请公屏选择开枪或跳过，限时40秒")
                        .build());
                thread.start();
                return;
            }
        }
        if (status == 2) {
            MessageChainBuilder builder = new MessageChainBuilder();
            GamePlayer player = acting ? player1 : player2;
            if (stack == 4) {
                lostChips += chips_on_table;
                chips_on_table = 0;
                if (bullets.get(index)) {
                    shot++;
                    builder.append("跳过达到上限，当前弹位有子弹，不计流失，桌上积分清零，结束后交给胜者");
                } else {
                    int lost = acting ? ++lost1 : ++lost2;
                    builder.append("跳过达到上限，当前弹位无子弹，桌上积分清零，结束后交给胜者\n").append(player.name).append(" 记流失一次， 当前流失次数:").append(String.valueOf(lost));
                }
                if (check()) {
                    group.sendMessage(builder.build());
                    stop();
                    return;
                }
            } else if (stack == -1) {
                if (bullets.get(index)) {
                    shot++;
                    builder.append(player.name).append(" 中枪！额外支付50积分");
                    if (acting) {
                        chips2 += chips_on_table + 50;
                        chips_on_table = 0;
                        chips1 -= 50;
                    } else {
                        chips1 += chips_on_table + 50;
                        chips_on_table = 0;
                        chips2 -= 50;
                    }
                    if (check()) {
                        stop();
                        return;
                    }
                } else {
                    builder.append("空枪，").append(player.name).append(" 获得桌上积分:").append(String.valueOf(chips_on_table));
                    if (acting) {
                        chips1 += chips_on_table;
                        chips_on_table = 0;
                    } else {
                        chips2 += chips_on_table;
                        chips_on_table = 0;
                    }
                }
            } else {
                int chips = (int) Math.pow(2, stack);
                builder.append(player.name).append(" 选择跳过，积分-").append(String.valueOf(chips)).build();
            }
            if (stack == -1 || stack++ == 4) {
                index++;
                stack = 0;
            }
            builder.append("\n\n").append(player1.name).append(" 当前积分:").append(String.valueOf(chips1)).append(" 流失:").append(String.valueOf(lost1))
                    .append("\n").append(player2.name).append(" 当前积分:").append(String.valueOf(chips2)).append(" 流失:").append(String.valueOf(lost2))
                    .append("\n桌上积分:").append(String.valueOf(chips_on_table));
            builder.append("\n\n");
            builder.append(index > 0 ? bullets.get(0) ? "*" : "_" : "?");
            for (int i = 1; i < index; i++) {
                builder.append(",").append(bullets.get(i) ? "*" : "_");
            }
            for (int i = index > 0 ? index : 1; i < 24; i++) {
                builder.append(",").append(String.valueOf(i + 1));
            }
            group.sendMessage(builder.build());
            acting = !acting;
            time = 40;
            player = acting ? player1 : player2;
            group.sendMessage(new MessageChainBuilder()
                    .append("R")
                    .append(String.valueOf(index))
                    .append(". ")
                    .append(new At(player.id))
                    .append(" 请公屏选择开枪或跳过，限时40秒")
                    .build());
        }
    }

    public boolean check() {
        if (lost1 >= 3 && lost1 - 2 >= lost2) {
            winner = 2;
            chips2 += lostChips;
            return true;
        }
        if (lost2 >= 3 && lost2 - 2 >= lost1) {
            winner = 1;
            chips1 += lostChips;
            return true;
        }
        if (shot >= 6) {
            if (chips1 > chips2) {
                winner = 1;
                chips1 += lostChips;
                return true;
            }
            if (chips2 > chips1) {
                winner = 2;
                chips2 += lostChips;
                return true;
            }
            if (lost1 > lost2) {
                winner = 2;
                chips2 += lostChips;
                return true;
            }
            if (lost2 > lost1) {
                winner = 1;
                chips1 += lostChips;
                return true;
            }
            chips1 += lostChips / 2;
            chips2 += lostChips / 2;
            winner = 0;
            return true;
        }
        return false;
    }

    @Override
    public void stop() {
        player1.data.creditInGame = 0;
        player2.data.creditInGame = 0;
        if (winner == -1) {
            status = 0;
            MiraiGamePlugin.INSTANCE.players_map.get(this.group.getId()).clear();
            MiraiGamePlugin.INSTANCE.games.remove(group.getId());
            group.sendMessage("游戏中断");
            return;
        }
        MessageChainBuilder builder = new MessageChainBuilder();
        if (winner == 0) {
            builder.append("游戏结束，平局\n");
        }
        if (winner == 1) {
            builder.append("游戏结束，恭喜胜者——").append(new At(player1.id)).append("！\n");
        }
        if (winner == 2) {
            builder.append("游戏结束，恭喜胜者——").append(new At(player2.id)).append("！\n");
        }

        List<GamePlayer> ranks = Manager.rankValid(player1, player2);

        if(rate > 0) {
            chips1 = (chips1 - 100) * rate + 100;
            chips2 = (chips2 - 100) * rate + 100;
            player1.data.credit += chips1;
            player2.data.credit += chips2;
        }else {
            player1.data.credit += 100;
            player2.data.credit += 100;
        }
        if(ranks.size() >= 2) {
            int avgRank = (player1.rank.scores.getOrDefault("24轮盘", 1200) + player2.rank.scores.getOrDefault("24轮盘", 1200)) / 2;
            int rank1 = player1.rank.process("24轮盘", winner == 1 ? 1f : -1f, avgRank);
            int rank2 = player2.rank.process("24轮盘", winner == 1 ? -1f : 1f, avgRank);
            builder.append(new At(player1.id)).append(" 积分:").append(String.valueOf(chips1)).append(" 流失次数:").append(String.valueOf(lost1))
                    .append(" rank分：")
                    .append(rank1 > 0 ? "+" : "")
                    .append(String.valueOf(rank1)).append("\n");
            builder.append(new At(player2.id)).append(" 积分:").append(String.valueOf(chips2)).append(" 流失次数:").append(String.valueOf(lost2))
                    .append(" rank分：")
                    .append(rank2 > 0 ? "+" : "")
                    .append(String.valueOf(rank2));
        }else {
            builder.append(new At(player1.id)).append(" 积分:").append(String.valueOf(chips1)).append(" 流失次数:").append(String.valueOf(lost1));
            builder.append(new At(player2.id)).append(" 积分:").append(String.valueOf(chips2)).append(" 流失次数:").append(String.valueOf(lost2));
        }

        if(rate == 0) {
            builder.append("\n\n本局游戏倍率为0，不记实际积分");
        }

        MiraiGamePlugin.INSTANCE.players_map.get(this.group.getId()).clear();
        MiraiGamePlugin.INSTANCE.games.remove(group.getId());
        group.sendMessage(builder.build());
        status = 0;
        try {
            Manager.saveRank();
            Manager.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawPosition(BufferedImage main, int index, boolean hasBullet) {

    }

    @Override
    public boolean allowGroup() {
        return true;
    }

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
        private final Roulette24 game;

        public ThreadTime(Roulette24 game) {
            this.game = game;
        }

        @Override
        public void run() {
            while (true) {

                game.lock.lock();
                try {
                    if (game.isWaiting())
                        break;
                    if (game.time == 20)
                        game.group.sendMessage("剩余时间20秒，超时自动选择开枪");
                    if (game.time == 10)
                        game.group.sendMessage("剩余时间10秒");
                    if (game.time == 5)
                        game.group.sendMessage("剩余时间5秒");
                    if (game.time-- <= 0) {
                        game.stack = -1;
                        game.update();
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
