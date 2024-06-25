package com.github.starowo.mirai.game.comb;

import com.google.common.collect.Lists;
import com.github.starowo.mirai.MSGHandler;
import com.github.starowo.mirai.MiraiGamePlugin;
import com.github.starowo.mirai.data.Manager;
import com.github.starowo.mirai.data.Skin;
import com.github.starowo.mirai.game.IGame;
import com.github.starowo.mirai.player.GamePlayer;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberHive implements IGame {

    public boolean ranked = true;

    public Group group;
    public List<Piece> pieces = new ArrayList<>();
    public List<Piece> pieces_public = new ArrayList<>();
    public List<GamePlayer> players;
    public List<GamePlayer> public_players = new ArrayList<>();
    public List<GamePlayer> dead_players;
    public HashSet<GamePlayer> received;
    public Map<GamePlayer, Board> boards;
    public List<Map<GamePlayer, GamePlayer>> fought_list;
    public GamePlayer last;
    public GamePlayer last2;
    public int min_round;
    public Piece current;
    public GamePlayer current_player;
    public List<Piece> current_public = new ArrayList<>();
    public int round = 0;
    public int state = 0;
    public int sec = 120;
    public Thread thread;
    public ReentrantLock lock = new ReentrantLock();
    Random rand = new Random();
    long seed = rand.nextLong();
    private int special;
    private boolean test = false;
    private double dRate = 0.0d;
    private double iRate;
    private int playerSize;
    private int foughtRound = 0;

    public NumberHive(List<GamePlayer> players, Group group) {
        this(players, group, -1);
    }

    public NumberHive(List<GamePlayer> players, Group group, int special) {
        rand = new Random(seed);
        this.group = group;
        received = new HashSet<>();
        dead_players = new ArrayList<>();
        boards = new HashMap<>();
        fought_list = new ArrayList<>();
        this.players = players;
        this.special = special;
        if (special == 1919810) {
            test = true;
        }
        if (special == 114514) {
            test = true;
            this.special = -1;
        }
    }

    @Override
    public void start() {
        lock.lock();
        try {
            if (state != 0)
                return;
            init();
            state = 1;
            Collections.shuffle(this.players, rand);
            min_round = (players.size() + 1) / 2 - 1;
            if (players.size() == 2)
                min_round = 0;
            for (GamePlayer player : players) {
                Board board = new Board();
                board.special = special;
                boards.put(player, board);
            }
            update();
            (thread = new ThreadTime(this)).start();
        } finally {
            lock.unlock();
        }
    }


    private void init() {
        playerSize = players.size();
        dRate = Math.pow(Math.E, players.size() / 6.0d) / Math.E;
        iRate = dRate;
        int j = special == -1 ? rand.nextInt(100) : special;
        special = j / 12 + 1;
        switch (special) {
            case 1:
                group.sendMessage("本局特殊事件：调色盘——卡池中添加大量癞子");
                break;
            case 2:
                group.sendMessage("本局特殊事件：大的没了——卡池中没有9");
                break;
            case 3:
                group.sendMessage("本局特殊事件：大的要来了——卡池中没有1");
                break;
            case 4:
                group.sendMessage("本局特殊事件：两极分化——卡池中没有5");
                break;
            case 5:
                group.sendMessage("本局特殊事件：有1吗——每行1额外加12分");
                break;
            case 6:
                group.sendMessage("本局特殊事件：小透不算挂——提前公布下一轮的卡（右侧为下一轮）");
                break;
            case 7:
                group.sendMessage("本局特殊事件：天降恩泽——第一轮每人发一个癞子");
                break;
            case 100:
                group.sendMessage("本局特殊事件：传世经典——本局游戏采用传统数字蜂巢规则");
                break;
            default:
                group.sendMessage("本局特殊事件：无");
                break;
        }
        pieces.clear();
        newPieces(false);
        pieces_public.clear();
        newPieces(true);
        int bound = rand.nextInt(15) + 6;
        for (int i = 0; i < players.size(); i++) {
            if(special == 7) {
                pieces_public.add(0, new Piece(0, 0, 0));
            }else {
                Piece piece = pieces_public.get(i);
                int sum = piece.directions[0] + piece.directions[1] + piece.directions[2];
                if (sum < bound || sum > bound + 4) {
                    int dif = 99;
                    int index = -1;
                    for (int k = players.size(); k < pieces_public.size(); k++) {
                        Piece p1 = pieces_public.get(k);
                        int sum1 = p1.directions[0] + p1.directions[1] + p1.directions[2];
                        if (sum1 >= bound && sum1 <= bound + 4) {
                            pieces_public.set(i, p1);
                            pieces_public.set(k, piece);
                            break;
                        }
                        if(Math.abs(sum1 - bound) < dif) {
                            dif = Math.abs(sum1 - bound);
                            index = k;
                        }
                        if (k == pieces_public.size() - 1) {
                            pieces_public.set(i, pieces_public.get(index));
                            pieces_public.set(index, piece);
                        }
                    }
                }
            }
        }
        // sort the first n(playerSize) pieces in pieces_public by the sum of directions in ascending order
        pieces_public.subList(0, playerSize).sort((o1, o2) -> {
            int sum1 = o1.directions[0] + o1.directions[1] + o1.directions[2];
            int sum2 = o2.directions[0] + o2.directions[1] + o2.directions[2];
            return sum1 - sum2;
        });
        newPieces(true);
        if (j == 1919810) {
            this.pieces_public.clear();
            pieces.clear();
            for (int i = 0; i < 40; i++) {
                pieces.add(new Piece(0, 0, 0));
                this.pieces_public.add(new Piece(0, 0, 0));
            }
        }
    }

    private void newPieces(boolean inPublic) {
        ArrayList<Piece> pieces = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            if(special != 3) {
                pieces.add(new Piece(3, 1, 2));
                pieces.add(new Piece(3, 1, 6));
                pieces.add(new Piece(3, 1, 7));
                pieces.add(new Piece(4, 1, 2));
                pieces.add(new Piece(4, 1, 6));
                pieces.add(new Piece(4, 1, 7));
                pieces.add(new Piece(8, 1, 2));
                pieces.add(new Piece(8, 1, 6));
                pieces.add(new Piece(8, 1, 7));
            }
            if(special != 4) {
                pieces.add(new Piece(3, 5, 2));
                pieces.add(new Piece(3, 5, 6));
                pieces.add(new Piece(3, 5, 7));
                pieces.add(new Piece(4, 5, 2));
                pieces.add(new Piece(4, 5, 6));
                pieces.add(new Piece(4, 5, 7));
                pieces.add(new Piece(8, 5, 2));
                pieces.add(new Piece(8, 5, 6));
                pieces.add(new Piece(8, 5, 7));
            }
            if(special != 2) {
                pieces.add(new Piece(3, 9, 2));
                pieces.add(new Piece(3, 9, 6));
                pieces.add(new Piece(3, 9, 7));
                pieces.add(new Piece(4, 9, 2));
                pieces.add(new Piece(4, 9, 6));
                pieces.add(new Piece(4, 9, 7));
                pieces.add(new Piece(8, 9, 2));
                pieces.add(new Piece(8, 9, 6));
                pieces.add(new Piece(8, 9, 7));
            }
            if(!inPublic || special == 1)
                pieces.add(new Piece(0, 0, 0));
        }
        if(inPublic && special == 1) {
            pieces.add(new Piece(0, 0, 0));
        }
        Collections.shuffle(pieces, rand);
        if (!inPublic && special == 1) {
            pieces.add(rand.nextInt(pieces.size()), new Piece(0, 0, 0));
            pieces.add(rand.nextInt(18), new Piece(0, 0, 0));
            pieces.add(rand.nextInt(19), new Piece(0, 0, 0));
        }
        if (!inPublic && special == 100) {
            for (int i = 0; i < 20; i++) {
                if (pieces.get(i).directions[0] == 0) {
                    break;
                }
                if (i == 19) {
                    for (int j = 0; j < 20; j++) {
                        pieces.remove(0);
                    }
                }
            }
        }
        if(inPublic) {
            pieces_public.addAll(pieces);
        }else {
            this.pieces.addAll(pieces);
        }
    }

    @Override
    public Message input(GamePlayer player, MessageChain msg) {
        String text = msg.contentToString().replaceAll("@" + group.getBot().getId(), "").trim();
        //System.out.println(text);
        lock.lock();
        try {
            if (players.contains(player)) {
                if (text.contains("投降")) {
                    boards.get(player).health = -99;
                    received.add(player);
                    return new MessageChainBuilder()
                            .append("你死啦(悲)")
                            .append(new QuoteReply(msg))
                            .build();
                }
                if (round > 1 && (round - 1) % 7 == 0 || special == 100) {
                    String[] args = text.trim().split(" ");
                    String regEx = "[^0-9]";
                    Pattern p = Pattern.compile(regEx);
                    Matcher m = p.matcher(args[0]);
                    if (m.find()) {
                        return new MessageChainBuilder()
                                .append("指令错误了呢，请使用纯数字发送游戏指令哦~")
                                .append(new QuoteReply(msg))
                                .build();
                    }
                    int id = Integer.parseInt(m.replaceAll("")) - 1;
                    m = p.matcher(args[1]);
                    int pos = Integer.parseInt(m.replaceAll(""));
                    if (current_player != player)
                        return new MessageChainBuilder()
                                .append("现在不是你的回合哦~")
                                .append(new QuoteReply(msg))
                                .build();
                    if (id >= current_public.size())
                        return new MessageChainBuilder()
                                .append("选牌ID错误")
                                .append(new QuoteReply(msg))
                                .build();
                    if (pos > 19 || pos < 0) {
                        return new MessageChainBuilder()
                                .append("没有这个格子的啊~")
                                .append(new QuoteReply(msg))
                                .build();
                    }
                    Piece current = current_public.remove(id);
                    Message reply;
                    received.add(player);
                    if (pos == 0) {
                        reply = new MessageChainBuilder()
                                .append("弃牌成功！")
                                .append(new QuoteReply(msg))
                                .build();
                    } else {
                        Board board = boards.get(player);
                        board.board[pos - 1] = current;
                        int score = board.score;
                        board.check();
                        if (board.score != score)
                            reply = new MessageChainBuilder()
                                    .append("设置成功！获得积分:")
                                    .append(String.valueOf(board.score - score))
                                    .append(new QuoteReply(msg))
                                    .build();
                        else
                            reply = new MessageChainBuilder()
                                    .append("设置成功！")
                                    .append(new QuoteReply(msg))
                                    .build();
                    }
                    return reply;
                } else if (!received.contains(player)) {
                    Piece current = round == 1 ? current_public.get(players.indexOf(player)) : this.current;
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
                    if (pos > 19 || pos < 0) {
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
                        Board board = boards.get(player);
                        board.board[pos - 1] = current;
                        int score = board.score;
                        board.check();
                        if (board.score != score)
                            reply = new MessageChainBuilder()
                                    .append("设置成功！获得积分:")
                                    .append(String.valueOf(board.score - score))
                                    .append(new QuoteReply(msg))
                                    .build();
                        else
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
            } else if (dead_players.contains(player)) {
                return new MessageChainBuilder()
                        .append("已经死了就不要挣扎啦~")
                        .append(new QuoteReply(msg))
                        .build();
            }
        } catch (Exception E) {
            return new MessageChainBuilder()
                    .append("指令错误了呢~ 好好检查一下吧")
                    .append(new QuoteReply(msg))
                    .build();
        } finally {
            lock.unlock();
        }
        return new MessageChainBuilder()
                .append("指令无效：游戏已经开始，你不在游戏中")
                .append(new QuoteReply(msg))
                .build();
    }

    public boolean check() {
        return (players.size() <= 1 && !test) || pieces.isEmpty();
    }

    @Override
    public void stop() {
        try {
            MiraiGamePlugin.INSTANCE.players_map.get(this.group.getId()).clear();
            MessageChainBuilder endMsg = new MessageChainBuilder();
            if (players.size() <= 1)
                endMsg.append("玩家全部淘汰,");
            else
                endMsg.append("卡池耗尽,");
            endMsg.append("游戏结束！\r\n\r\n本局战果：\r\n");
            players.sort((ele1, ele2) -> {
                Board board1 = boards.get(ele1);
                Board board2 = boards.get(ele2);
                if (board1.health > board2.health)
                    return -1;
                else if (board2.health > board1.health)
                    return 1;
                else return Integer.compare(board2.score, board1.score);
            });
            List<GamePlayer> allPlayer = Lists.newArrayList(dead_players);
            allPlayer.addAll(players);
            Collections.reverse(allPlayer);
            /*
            allPlayer.sort((ele1, ele2) -> {
                Board board1 = boards.get(ele1);
                Board board2 = boards.get(ele2);
                return Integer.compare(board2.score, board1.score);
            });*/
            int least = (players.size() + dead_players.size()) * (special ==  2 ? 100 : 50);
            allPlayer = Manager.rankValid(allPlayer.toArray(new GamePlayer[0]));
            float total = 0f;
            int totalRank = 0;
            float median = 0;
            int playerSize1 = allPlayer.size();
            for (int i = 0; i < playerSize1; i++) {
                GamePlayer player = allPlayer.get(i);
                Board board = boards.get(player);
                if(i == playerSize1 / 2) {
                    median += board.score;
                }else if(playerSize1 % 2 == 0 && i == (playerSize1 - 1) / 2) {
                    median += board.score;
                }
                total += board.score;
                totalRank += player.rank.scores.getOrDefault("云顶之巢", 1200);
            }
            if(playerSize1 % 2 == 0)
                median /= 2f;
            float avg = total / playerSize1;
            int avgRank = totalRank / playerSize1;
            int highest = boards.get(allPlayer.get(0)).score;
            for (int i = 0; i < players.size(); i++) {
                GamePlayer player = players.get(i);
                Board board = boards.get(player);
                int credits = board.score > 0 ? board.score + least : 0;
                for (GamePlayer dead : dead_players) {
                    credits += boards.get(dead).score;
                }
                credits = player.data.reducedCreditEarn(credits);
                int exp = Math.max(Math.min(credits * 3, 2000), board.score > 0 ? 500 : 0);
                if(playerSize1 >= 2 && !player.rank.banned()) {
                    int r = allPlayer.indexOf(player);
                    float k ;
                    if(playerSize1 % 2 == 1 && r == playerSize1 / 2) {
                        k = Math.max(-1, (board.score - avg) / (highest - avg)) * Math.min(Math.max(1, 1 + 10 * (board.score / (playerSize * avg) - 1.17f / playerSize)), 1.5f);
                    }else {
                        float s = (playerSize1 - 1f) / 2f;
                        k = (s - r) / s * Math.min(Math.max(1, 1 + 10 * (board.score / (playerSize1 * avg) - 1.17f / playerSize)), 1.5f);
                    }
                    int rank = ranked ? player.rank.process("云顶之巢", k, avgRank) : 0;
                    endMsg.append(String.valueOf(i + 1))
                            .append(". ")
                            .append(new At(player.id))
                            .append(" 血量：")
                            .append(String.valueOf(board.health))
                            .append(" 得分：")
                            .append(String.valueOf(board.score))
                            .append(" 积分：+")
                            .append(String.valueOf(credits))
                            .append(" 经验：+")
                            .append(String.valueOf(exp))
                            .append(" rank分：").append(rank > 0 ? "+" : "")
                            .append(String.valueOf(rank))
                            .append("\r\n");
                }else {
                    endMsg.append(String.valueOf(i + 1))
                            .append(". ")
                            .append(new At(player.id))
                            .append(" 血量：")
                            .append(String.valueOf(board.health))
                            .append(" 得分：")
                            .append(String.valueOf(board.score))
                            .append(" 积分：+")
                            .append(String.valueOf(credits))
                            .append(" 经验：+")
                            .append(String.valueOf(exp))
                            .append("\r\n");
                }
                player.data.credit += credits;
                player.data.addExp(exp);
            }
            for (int i = dead_players.size() - 1; i >= 0; i--) {
                GamePlayer player = dead_players.get(i);
                Board board = boards.get(player);
                int credits = 0;
                for (int j = i; j >= 0; j--) {
                    GamePlayer dead = dead_players.get(j);
                    credits += boards.get(dead).score;
                }
                credits = credits > 0 ? credits + least : 0;
                credits = player.data.reducedCreditEarn(credits);
                int exp = Math.max(Math.min(credits * 3, 2000), board.score > 0 ? 500 : 0);
                if(playerSize1 >= 2 && !player.rank.banned()) {
                    int r = allPlayer.indexOf(player);
                    float k ;
                    if(playerSize1 % 2 == 1 && r == playerSize1 / 2) {
                        k = Math.max(-1, (board.score - avg) / (highest - avg)) * Math.min(Math.max(1, 1 + 10 * (board.score / (playerSize * avg) - 1.17f / playerSize)), 1.5f);
                    }else {
                        float s = (playerSize1 - 1f) / 2f;
                        k = (s - r) / s * Math.min(Math.max(1, 1 + 10 * (board.score / (playerSize1 * avg) - 1.17f / playerSize)), 1.5f);
                    }
                    int rank = ranked ? player.rank.process("云顶之巢", k, avgRank) : 0;
                    endMsg.append(String.valueOf(players.size() + dead_players.size() - i))
                            .append(". ")
                            .append(new At(player.id))
                            .append(" 得分：")
                            .append(String.valueOf(board.score))
                            .append(" 积分：+")
                            .append(String.valueOf(credits))
                            .append(" 经验：+")
                            .append(String.valueOf(exp))
                            .append(" rank分：").append(rank > 0 ? "+" : "")
                            .append(String.valueOf(rank))
                            .append("\r\n");
                }else {
                    endMsg.append(String.valueOf(players.size() + dead_players.size() - i))
                            .append(". ")
                            .append(new At(player.id))
                            .append(" 得分：")
                            .append(String.valueOf(board.score))
                            .append(" 积分：+")
                            .append(String.valueOf(credits))
                            .append(" 经验：+")
                            .append(String.valueOf(exp))
                            .append("\r\n");
                }
                player.data.credit += credits;
                player.data.addExp(exp);
            }
            GamePlayer player = players.size() > 0 ? players.get(0) : dead_players.get(dead_players.size() - 1);
            endMsg.append("\r\n 恭喜胜者——")
                    .append(new At(player.id))
                    .append(" !!!");
            group.sendMessage(endMsg.build());
            group.sendMessage("本局随机数种子：" + seed);
        } catch (Throwable e) {
            group.sendMessage("结算失败，游戏中止！");
            group.sendMessage(e + Arrays.toString(e.getStackTrace()));
        }
        MiraiGamePlugin.INSTANCE.players_map.get(this.group.getId()).clear();
        MiraiGamePlugin.INSTANCE.games.remove(group.getId());
        thread.interrupt();
        try {
            Manager.saveRank();
            Manager.save();
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
        return state == 0;
    }

    @Override
    public int getMaxPlayer() {
        return 8;
    }

    @Override
    public void addPlayer(GamePlayer activePlayer) {
        players.add(activePlayer);
    }

    @Override
    public void remove(GamePlayer activePlayer) {
        players.remove(activePlayer);
    }

    private boolean update() {
        min_round = (players.size() + 1) / 2 - 1;
        if (players.size() == 2)
            min_round = 0;
        while (fought_list.size() > min_round) {
            fought_list.remove(0);
        }
        MessageChainBuilder msg1 = new MessageChainBuilder();
        if ((round >= 3 && (round - 1) % 7 > 0) && !test) {
            Map<GamePlayer, GamePlayer> fight = new HashMap<>();
            List<GamePlayer> list1 = new ArrayList<>(players);
            boolean b;
            do {
                b = false;
                fight.clear();
                Collections.shuffle(list1, rand);
                if (list1.size() % 2 == 1) {
                    if (list1.get(list1.size() - 1).equals(last)) {
                        b = true;
                        continue;
                    }
                }
                Out:
                for (int i = 0; i < list1.size() - 1; i += 2) {
                    for (Map<GamePlayer, GamePlayer> hist : fought_list) {
                        if (hist.containsKey(list1.get(i)) && hist.get(list1.get(i)).equals(list1.get(i + 1))) {
                            b = true;
                            break Out;
                        }
                        if (hist.containsKey(list1.get(i + 1)) && hist.get(list1.get(i + 1)).equals(list1.get(i))) {
                            b = true;
                            break Out;
                        }
                    }
                    fight.put(list1.get(i), list1.get(i + 1));
                }

            } while (b);
            fought_list.add(fight);
            while (fought_list.size() > min_round) {
                fought_list.remove(0);
            }
            for (Map.Entry<GamePlayer, GamePlayer> entry : fight.entrySet()) {
                GamePlayer player = entry.getKey();
                GamePlayer player1 = entry.getValue();
                    /*if(list1.size() % 2 == 1 && !it.hasNext()) {
                        Board board = boards.get(player);
                        Board board1 = boards.get(player1);
                        if (board.score >= board1.score) {
                            msg1.append(player.name).append(" vs ").append(player1.name).append("(镜像)");
                        } else {
                            msg1.append(player.name).append("(").append(String.valueOf(board.score - board1.score)).append(")").append(" vs ").append(player1.name).append("(镜像)");
                            board.health -= board1.score - board.score;
                        }
                    }else {*/
                Board board = boards.get(player);
                Board board1 = boards.get(player1);
                if (board.score > board1.score) {
                    msg1.append(player.name).append(" vs ").append(player1.name).append("(").append(String.valueOf((int) ((board1.score - board.score) * dRate))).append(")").append("\r\n");
                    board1.health += (int) ((board1.score - board.score) * dRate);
                } else if (board.score < board1.score) {
                    msg1.append(player.name).append("(").append(String.valueOf((int) ((board.score - board1.score) * dRate))).append(")").append(" vs ").append(player1.name).append("\r\n");
                    board.health -= (int) ((board1.score - board.score) * dRate);
                } else {
                    msg1.append(player.name).append(" vs ").append(player1.name).append(" (0)").append("\r\n");
                }
                //}

            }
            if (list1.size() % 2 == 1) {
                GamePlayer player2 = list1.get(list1.size() - 1);
                GamePlayer player3;
                do {
                    player3 = list1.get(rand.nextInt(list1.size() - 1));
                } while (player3.equals(last2));
                last2 = player3;
                Board board2 = boards.get(player2);
                Board board3 = boards.get(player3);
                if (board2.score >= board3.score) {
                    msg1.append(player2.name).append(" vs ").append(player3.name).append("(镜像)");
                } else {
                    msg1.append(player2.name).append("(").append(String.valueOf((int) ((board2.score - board3.score) * dRate))).append(")").append(" vs ").append(player3.name).append("(镜像)");
                    board2.health -= (int) ((board3.score - board2.score) * dRate);
                }
            }
            if(dRate < 1) {
                dRate = Math.min(Math.pow(Math.E, ++foughtRound * 0.22/playerSize) * iRate, 1);
            }
            if(dRate > 1) {
                dRate = Math.max(Math.pow(Math.E, ++foughtRound * -0.44/playerSize) * iRate, 1);
            }
        } else {
            fought_list.clear();
            msg1.append("本轮不进行玩家对战");
        }
        if ((round - 1) % 7 != 0 || public_players.isEmpty())
            round++;
        Iterator<GamePlayer> it = players.iterator();
        while (it.hasNext()) {
            GamePlayer player = it.next();
            Board board = boards.get(player);
            if (board.health <= 0) {
                it.remove();
                dead_players.add(player);
                public_players.remove(player);
                MessageChainBuilder msg = new MessageChainBuilder();
                msg.append(new At(player.id)).append(" 已被淘汰！");
                group.sendMessage(msg.build());
                MiraiGamePlugin.INSTANCE.players_map.get(this.group.getId()).remove(player);
            }
        }
        group.sendMessage(msg1.build());
        BufferedImage head = new BufferedImage(580 * 2, 40, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = head.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, 580 * 2, 40);
        g.setColor(Color.BLACK);
        Font font = new Font("微软雅黑", Font.BOLD, 20);
        g.setFont(font);
        g.drawString("第" + round + String.format("轮 (伤害倍率:%.2f)", dRate), 20, 20);
        g.setColor(Color.WHITE);
        g.dispose();
        BufferedImage main = new BufferedImage(580 * 2, 40 + (players.size() + dead_players.size() + 1) / 2 * 510, 1);
        Graphics2D gMain = main.createGraphics();
        gMain.setColor(Color.white);
        gMain.fillRect(0, 0, main.getWidth(), main.getHeight());
        gMain.drawImage(head, 0, 0, null);
        int aliveSize = players.size();
        try {
            for (int i = 0; i < aliveSize; i++) {
                if (i % 2 == 0) {
                    gMain.drawImage(getBoardImage(players.get(i), i + 1), 0, 40 + 510 * (i / 2), null);
                } else
                    gMain.drawImage(getBoardImage(players.get(i), i + 1), 580, 40 + 510 * ((i - 1) / 2), null);
            }
            for (int i = 0; i < dead_players.size(); i++) {
                int j = i + aliveSize;
                if (j % 2 == 0)
                    gMain.drawImage(getBoardImage(dead_players.get(i), -1 - i), 0, 40 + 510 * (j / 2), null);
                else
                    gMain.drawImage(getBoardImage(dead_players.get(i), -1 - i), 580, 40 + 510 * ((j - 1) / 2), null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            gMain.dispose();
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] baImage = null;
        try {
            ImageIO.write(main, "png", os);
            baImage = os.toByteArray();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (baImage != null) {
            MSGHandler.asyncSendImage(group, baImage);
        }
        sec = 120;
        font = new Font("微软雅黑", Font.BOLD, 15);
        if (check()) {
            return true;
        }
        if (round == 1) {
            group.sendMessage("第一轮进行随机配牌，请认准自己的ID");
            BufferedImage card = new BufferedImage(94 * players.size(), 64, 1);
            Graphics2D gCard = card.createGraphics();
            for (int i = 0; i < players.size(); i++) {
                Piece p = pieces_public.get(0);
                pieces_public.remove(0);
                current_public.add(p);
                BufferedImage blank = new BufferedImage(94, 64, 1);
                Graphics2D gBlank = blank.createGraphics();
                gBlank.setColor(Color.WHITE);
                gBlank.fillRect(0, 0, 94, 64);
                gBlank.setColor(Color.BLACK);
                try {
                    BufferedImage image = ImageGenerator.INSTANCE.getCardImg("./resources/comb/", p.directions[0], p.directions[1], p.directions[2]);
                    gBlank.setFont(font);
                    gBlank.drawImage(image, 30, 0, null);
                    gBlank.drawString((i + 1) + "·", 5, 32);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                gBlank.dispose();
                gCard.drawImage(blank, 94 * i, 0, null);
                    /*try {
                        FileInputStream fis = new FileInputStream("./resources/comb/" + p.directions[0] + p.directions[1] + p.directions[2] + ".png");
                        group.sendMessage(new MessageChainBuilder()
                                .append(String.valueOf(i + 1))
                                .append(".")
                                .append(Contact.uploadImage(group, fis))
                                .build());
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
            }
            gCard.dispose();
            os = new ByteArrayOutputStream();
            baImage = null;
            try {
                ImageIO.write(card, "png", os);
                baImage = os.toByteArray();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (baImage != null) {
                MSGHandler.asyncSendImage(group, baImage);
            }
        }
        else if ((round - 1) % 7 == 0) {
            if (public_players.isEmpty()) {
                group.sendMessage("公共配牌阶段，按照排名逆序选牌");
                group.sendMessage("选牌格式：@机器人 卡牌id+空格+放入位置");
                public_players = new ArrayList<>(players);
                public_players.sort((element1, element2) -> {
                    Board b1 = boards.get(element1);
                    Board b2 = boards.get(element2);
                    if (b1.health < b2.health)
                        return -1;
                    else if (b1.health > b2.health)
                        return 1;
                    else
                        return Integer.compare(b1.score, b2.score);
                });
                current_public.clear();
                for (int i = 0; i < players.size() + 1; i++) {
                    Piece p = pieces_public.remove(0);
                    current_public.add(p);
                }
            }
            current_player = public_players.remove(0);
            BufferedImage card = new BufferedImage(94 * current_public.size(), 64, 1);
            Graphics2D gCard = card.createGraphics();
            for (int i = 0; i < current_public.size(); i++) {
                Piece p = current_public.get(i);
                BufferedImage blank = new BufferedImage(94, 64, 1);
                Graphics2D gBlank = blank.createGraphics();
                gBlank.setColor(Color.WHITE);
                gBlank.fillRect(0, 0, 94, 64);
                gBlank.setColor(Color.BLACK);
                try {
                    BufferedImage image = ImageGenerator.INSTANCE.getCardImg("./resources/comb/", p.directions[0], p.directions[1], p.directions[2]);
                    gBlank.setFont(font);
                    gBlank.drawImage(image, 30, 0, null);
                    gBlank.drawString((i + 1) + "·", 5, 32);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                gBlank.dispose();
                gCard.drawImage(blank, 94 * i, 0, null);
                    /*try {
                        FileInputStream fis = new FileInputStream("./resources/comb/" + p.directions[0] + p.directions[1] + p.directions[2] + ".png");
                        group.sendMessage(new MessageChainBuilder()
                                .append(String.valueOf(i + 1))
                                .append(".")
                                .append(Contact.uploadImage(group, fis))
                                .build());
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
            }
            gCard.dispose();
            os = new ByteArrayOutputStream();
            baImage = null;
            try {
                ImageIO.write(card, "png", os);
                baImage = os.toByteArray();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (baImage != null) {
                group.sendMessage(new MessageChainBuilder()
                        .append("请 ")
                        .append(new At(current_player.id))
                        .append(" 选择")
                        .build());
                MSGHandler.asyncSendImage(group, baImage);
            }

        }
        else {
            Piece p = pieces.get(0);
            pieces.remove(0);
            current = p;
            if(special == 6 && pieces.size() > 0) {
                Piece p1 = pieces.get(0);
                try {
                    BufferedImage card = new BufferedImage(148, 64, 1);
                    Graphics2D gCard = card.createGraphics();
                    gCard.setColor(Color.white);
                    gCard.fillRect(0, 0, 148, 64);
                    BufferedImage image = ImageGenerator.INSTANCE.getCardImg("./resources/comb/", p.directions[0], p.directions[1], p.directions[2]);
                    gCard.drawImage(image, 0, 0, null);
                    BufferedImage image1 = ImageGenerator.INSTANCE.getCardImg("./resources/comb/", p1.directions[0], p1.directions[1], p1.directions[2]);
                    gCard.drawImage(image1, 84, 0, null);
                    gCard.setFont(font);
                    gCard.setColor(Color.black);
                    gCard.drawString("←", 69, 32);
                    os = new ByteArrayOutputStream();
                    ImageIO.write(card, "png", os);
                    baImage = os.toByteArray();
                    os.close();
                    MSGHandler.asyncSendImage(group, baImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                try {
                    BufferedImage image = ImageGenerator.INSTANCE.getCardImg("./resources/comb/", p.directions[0], p.directions[1], p.directions[2]);
                    os = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", os);
                    baImage = os.toByteArray();
                    os.close();
                    MSGHandler.asyncSendImage(group, baImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        received.clear();
        for (GamePlayer player : dead_players) {
            Board board = boards.get(player);
            board.health = 0;
        }
        return false;
    }

    public BufferedImage getBoardImage(GamePlayer player, int index) throws IOException {

        /*if(player.id == 1273300377L)
            dir = Skin.gold.getPath();*/
        Board board = boards.get(player);
        return ImageGenerator.getInstanceForSkin(player.data.getSkin()).drawBoard(board, player, index);
    }

    public static BufferedImage getSkinPreview(Skin skin) throws IOException {
        return ImageGenerator.getInstanceForSkin(skin).getSkinPreview(skin);
    }

    public static class Piece {
        int[] directions;

        public Piece(int dir1, int dir2, int dir3) {
            directions = new int[]{dir1, dir2, dir3};
        }
    }

    public static class Board {

        public static final List<int[]> lines1 = new ArrayList<>();
        public static final List<int[]> lines2 = new ArrayList<>();
        public static final List<int[]> lines3 = new ArrayList<>();

        static {
            lines2.add(new int[]{0, 1, 2});
            lines2.add(new int[]{3, 4, 5, 6});
            lines2.add(new int[]{7, 8, 9, 10, 11});
            lines2.add(new int[]{12, 13, 14, 15});
            lines2.add(new int[]{16, 17, 18});
            lines1.add(new int[]{7, 12, 16});
            lines1.add(new int[]{3, 8, 13, 17});
            lines1.add(new int[]{0, 4, 9, 14, 18});
            lines1.add(new int[]{1, 5, 10, 15});
            lines1.add(new int[]{2, 6, 11});
            lines3.add(new int[]{0, 3, 7});
            lines3.add(new int[]{1, 4, 8, 12});
            lines3.add(new int[]{2, 5, 9, 13, 16});
            lines3.add(new int[]{6, 10, 14, 17});
            lines3.add(new int[]{11, 15, 18});
        }

        public int score;
        public int health;
        public Piece[] board;
        public int special = 0;

        public Board() {
            score = 0;
            health = 150;
            board = new Piece[19];
        }

        public void check() {
            int total = 0;
            for (int[] line : lines2) {
                int cur = 0;
                StringBuilder linenums = new StringBuilder();
                for (int i : line) {
                    linenums.append(i).append(",");
                }
                for (int i : line) {
                    //System.out.println("calculating line:"+linenums);
                    if (board[i] == null) {
                        //System.out.println("blank:"+i);
                        break;
                    }
                    if (cur == 0) {
                        cur = board[i].directions[1];
                        //System.out.println("new line number - pos:"+i+" num:"+cur);
                    }
                    if (board[i].directions[1] != cur && board[i].directions[1] != 0) {
                        //System.out.println("line numbers are not same - pos:"+i+" cur:"+cur+" num:"+board[i].directions[1]);
                        break;
                    } else {
                        //System.out.println("line numbers are same - pos:"+i+" cur:"+cur+" num:"+board[i].directions[1]);
                    }
                    if (i == line[line.length - 1]) {
                        cur = cur == 0 ? 10 : cur;
                        total += cur * line.length;
                        if(special == 5 && cur == 1) {
                            total += 12;
                        }
                    }
                }
            }
            for (int[] line : lines1) {
                int cur = 0;
                StringBuilder linenums = new StringBuilder();
                for (int i : line) {
                    linenums.append(i).append(",");
                }
                for (int i : line) {
                    //System.out.println("calculating line:"+linenums);
                    if (board[i] == null) {
                        //System.out.println("blank:"+i);
                        break;
                    }
                    if (cur == 0) {
                        cur = board[i].directions[0];
                        //System.out.println("new line number - pos:"+i+" num:"+cur);
                    }
                    if (board[i].directions[0] != cur && board[i].directions[0] != 0) {
                        //System.out.println("line numbers are not same - pos:"+i+" cur:"+cur+" num:"+board[i].directions[0]);
                        break;
                    } else {
                        //System.out.println("line numbers are same - pos:"+i+" cur:"+cur+" num:"+board[i].directions[0]);
                    }
                    if (i == line[line.length - 1]) {
                        cur = cur == 0 ? 10 : cur;
                        total += cur * line.length;
                    }
                }
            }
            for (int[] line : lines3) {
                int cur = 0;
                StringBuilder linenums = new StringBuilder();
                for (int i : line) {
                    linenums.append(i).append(",");
                }
                for (int i : line) {
                    //System.out.println("calculating line:"+linenums);
                    if (board[i] == null) {
                        //System.out.println("blank:"+i);
                        break;
                    }
                    if (cur == 0) {
                        cur = board[i].directions[2];
                        //System.out.println("new line number - pos:"+i+" num:"+cur);
                    }
                    if (board[i].directions[2] != cur && board[i].directions[2] != 0) {
                        // System.out.println("line numbers are not same - pos:"+i+" cur:"+cur+" num:"+board[i].directions[2]);
                        break;
                    } else {
                        //System.out.println("line numbers are same - pos:"+i+" cur:"+cur+" num:"+board[i].directions[2]);
                    }
                    if (i == line[line.length - 1]) {
                        //System.out.println("line3 score:+"+cur+"*"+ line.length+"["+linenums);
                        cur = cur == 0 ? 10 : cur;
                        total += cur * line.length;
                    }
                }
            }
            score = total;
        }

    }

    public static class ThreadTime extends Thread {
        private final NumberHive game;

        public ThreadTime(NumberHive game) {
            this.game = game;
        }

        public void run() {
            while (true) {

                game.lock.lock();
                try {
                    if (game.received.size() == game.players.size() || ((game.round - 1) % 7 == 0 && game.received.contains(game.current_player))) {
                        if (game.update()) {
                            game.stop();
                            break;
                        }
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
                        if (game.sec-- < 0) {
                            Iterator<GamePlayer> it = game.players.iterator();
                            while (it.hasNext()) {
                                GamePlayer player = it.next();
                                if (game.round > 1 && (game.round - 1) % 7 == 0) {
                                    if (player.equals(game.current_player) && !game.received.contains(player)) {
                                        game.dead_players.add(player);
                                        game.group.sendMessage(new MessageChainBuilder()
                                                .append(new At(player.id))
                                                .append("超时未发送消息，自动淘汰！")
                                                .build());
                                        game.received.add(player);
                                        it.remove();
                                    }
                                } else if (!game.received.contains(player)) {
                                    it.remove();
                                    game.boards.get(player).health = -99;
                                    game.dead_players.add(player);
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
