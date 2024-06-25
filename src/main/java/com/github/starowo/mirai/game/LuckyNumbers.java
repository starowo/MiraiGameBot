package com.github.starowo.mirai.game;

import com.github.starowo.mirai.player.GamePlayer;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.*;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LuckyNumbers implements IGame {
    public Group group;
    public List<GamePlayer> players = new ArrayList<>();
    public List<GamePlayer> received;
    public List<GamePlayer> forfeited;
    public Map<GamePlayer, Integer[]> boards;
    public int current;
    public int round = 0;
    public int state = 0;
    public int sec = 120;
    public Thread thread;
    public ReentrantLock lock = new ReentrantLock();
    Random rand = new Random();
    long seed = rand.nextLong();

    public LuckyNumbers(Group group, int special) {
        rand = new Random(seed);
        this.group = group;
        received = new ArrayList<>();
        boards = new HashMap<>();
    }

    @Override
    public void start() {
        lock.lock();
        state = 1;
        Collections.shuffle(this.players, rand);
        for (GamePlayer player : players) {
            boards.put(player, new Integer[16]);
        }
        try {
            update();
        } finally {
            lock.unlock();
        }
        (thread = new ThreadTime(this)).start();
    }

    private boolean update() {
        return false;
    }

    @Override
    public Message input(GamePlayer player, MessageChain msg) {
        String text = msg.contentToString().replaceAll("@" + group.getBot().getId(), "").trim();
        System.out.println(text);
        lock.lock();
        try {
            if (players.contains(player)) {
                if (text.contains("投降")) {
                    forfeited.add(player);
                    received.add(player);
                    return new MessageChainBuilder()
                            .append("你死啦(悲)")
                            .append(new QuoteReply(msg))
                            .build();
                }
            }
            if (!received.contains(player)) {
                String regEx = "[^0-9]";
                Pattern p = Pattern.compile(regEx);
                Matcher m = p.matcher(text);
                if (m.find()) {
                    return new MessageChainBuilder()
                            .append("指令错误了呢，请使用纯数字发送游戏指令哦~")
                            .append(new QuoteReply(msg))
                            .build();
                }
                int pos = Integer.parseInt(m.replaceAll("").trim());
                if (pos > 16 || pos < 0) {
                    return new MessageChainBuilder()
                            .append("没有这个格子的啊~")
                            .append(new QuoteReply(msg))
                            .build();
                }
                Message reply;
                received.add(player);
                if (pos == 0) {
                    reply = new MessageChainBuilder()
                            .append("弃牌成功！")
                            .append(new QuoteReply(msg))
                            .build();
                } else {
                    Integer[] board = boards.get(player);
                    board[pos - 1] = current;
                    reply = new MessageChainBuilder()
                            .append("设置成功！")
                            .append(new QuoteReply(msg))
                            .build();
                }
                return reply;
            } else {
                return new MessageChainBuilder()
                        .append("不能悔棋喔~")
                        .append(new QuoteReply(msg))
                        .build();
            }
        } catch (Exception e) {
            return new MessageChainBuilder()
                    .append("指令错误了呢~ 好好检查一下吧")
                    .append(new QuoteReply(msg))
                    .build();
        } finally {
            lock.unlock();
        }
    }

    public boolean check() {
        return false;
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean allowGroup() {
        return true;
    }

    @Override
    public boolean isWaiting() {
        return false;
    }

    @Override
    public int getMaxPlayer() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void addPlayer(GamePlayer activePlayer) {

    }

    @Override
    public void remove(GamePlayer activePlayer) {

    }

    public static class ThreadTime extends Thread {
        private final LuckyNumbers game;

        public ThreadTime(LuckyNumbers game) {
            this.game = game;
        }

        public void run() {
            while (true) {

                game.lock.lock();
                try {
                    if (game.received.size() == game.players.size()) {
                        if (game.update())
                            break;
                    }
                    if (game.check()) {
                        game.stop();
                        break;
                    }
                    if (game.state == 1) {
                        if (game.sec == 60)
                            game.group.sendMessage("剩余时间60秒");
                        if (game.sec == 30)
                            game.group.sendMessage("剩余时间30秒");
                        if (game.sec == 15)
                            game.group.sendMessage("剩余时间15秒");
                        if (game.sec == 5)
                            game.group.sendMessage("剩余时间5秒");
                        if (game.sec-- <= 0) {
                            Iterator<GamePlayer> it = game.players.iterator();
                            while (it.hasNext()) {
                                GamePlayer player = it.next();
                                if (!game.received.contains(player)) {
                                    it.remove();
                                    game.group.sendMessage(new MessageChainBuilder()
                                            .append(new At(player.id))
                                            .append("超时未发送消息，自动淘汰！")
                                            .build());
                                }
                            }
                        }
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
