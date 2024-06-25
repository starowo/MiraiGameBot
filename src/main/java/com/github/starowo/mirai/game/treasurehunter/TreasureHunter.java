package com.github.starowo.mirai.game.treasurehunter;

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
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class TreasureHunter implements IGame {

    public static final HashMap<String, BufferedImage> cache = new HashMap<>();

    public int rate = 100;

    public Group group;
    public int state = 0;
    Random random = new Random(CommandRandom.rd.nextLong());
    public ReentrantLock lock = new ReentrantLock();

    public List<GamePlayer> players;
    public List<GamePlayer> left_players;
    public HashSet<GamePlayer> leavings;
    public HashSet<GamePlayer> received;
    public HashSet<GamePlayer> afk;
    public Map<GamePlayer, Integer> wealth;

    public List<TreasureCard> pool = new ArrayList<>();
    public List<TreasureCard> past = new ArrayList<>();
    public List<TreasureCard> events = new ArrayList<>();

    GameThread thread;

    public TreasureHunter(Group group, int rate) {
        this.group = group;
        this.rate = rate;
        afk = new HashSet<>();
        received = new HashSet<>();
        left_players = new ArrayList<>();
        leavings = new HashSet<>();
        wealth = new HashMap<>();
        this.players = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            int r = random.nextInt(13) + 3;
            pool.add(new TreasureCard(TreasureCard.CardType.WEALTH, "宝藏", "总计" + r + "枚金币", r));
        }
        for (int i = 0; i < 5; i++) {
            int r = random.nextInt(11) + 10;
            pool.add(new TreasureCard(TreasureCard.CardType.TREASURE, "珍宝", "价值"+r+"金币", r));
        }
        for (int i = 0; i < 6; i++) {
            int r = i % 3 + 1;
            String name = "";
            switch (r) {
                case 1:
                    name = "不死鸟";
                    break;
                case 2:
                    name = "地狱犬";
                    break;
                case 3:
                    name = "美杜莎";
                    break;
            }
            pool.add(new TreasureCard(TreasureCard.CardType.MONSTER, "怪物", "遭遇了"+name, r));
        }

        Collections.shuffle(pool, random);
        List<TreasureCard> temp = new ArrayList<>();
        for (int i = 1; i < 13; i++) {
            int r = i;
            String desc = "";
            switch (r) {
                case 1:
                    desc = "接下来三回合必定找到数额较大的宝藏";
                    break;
                case 2:
                    desc = "接下来三回合必定找到珍宝";
                    break;
                case 3:
                    desc = "接下来三回合必定遭遇怪物";
                    break;
                case 4:
                    desc = "下一次发现的宝藏数量翻倍";
                    break;
                case 5:
                    desc = "下一次发现的宝藏数量增加";
                    break;
                case 6:
                    desc = "下一次发现的珍宝价值翻倍";
                    break;
                case 7:
                    desc = "下一次发现的珍宝价值必定是20";
                    break;
                case 8:
                    desc = "接下来一旦遭遇怪物就会立刻结束游戏";
                    break;
                case 9:
                    desc = "本回合选择返回营地的玩家，平分一定数量的金币";
                    break;
                case 10:
                    desc = "过去发现的所有宝藏，额外增加1~2个金币";
                    break;
                case 11:
                    desc = "过去发现的所有宝藏，平分一定数量的金币";
                    break;
                case 12:
                    desc = "过去发现的宝藏，随机一个恢复到初始数值";
                    break;
                case 13:
                    desc = "过去发现的宝藏，随机一个变成珍宝";
                    break;
            }
            temp.add(new TreasureCard(TreasureCard.CardType.EVENT, "奇遇", desc, r));
        }
        Collections.shuffle(temp, random);
        List<TreasureCard> hold = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            TreasureCard card = temp.remove(0);
            if (card.value <= 3) {
                hold.add(card);
            }
            else if (card.value == 4 || card.value == 5) {
                boolean has = false;
                int index = -1;
                for (int j = 0; j < pool.size(); j++) {
                    TreasureCard treasureCard = pool.get(j);
                    if (treasureCard.type == TreasureCard.CardType.EVENT && (treasureCard.value == 4 || treasureCard.value == 5)) {
                        has = true;
                        index = j;
                        break;
                    }
                }
                if (!has) {
                    pool.add(random.nextInt(pool.size()), card);
                } else {
                    boolean valid = false;
                    int pos = -1;
                    while (!valid) {
                        pos = random.nextInt(pool.size());
                        if (pos < index) {
                            for (int j = pos; j < index; j++) {
                                if (pool.get(j).type == TreasureCard.CardType.WEALTH) {
                                    valid = true;
                                    break;
                                }
                            }
                        } else {
                            for (int j = index; j < pos; j++) {
                                if (pool.get(j).type == TreasureCard.CardType.WEALTH) {
                                    valid = true;
                                    break;
                                }
                            }
                        }
                    }
                    pool.add(pos, card);
                }
            }
            else if (card.value == 6 || card.value == 7) {
                boolean has = false;
                int index = -1;
                for (int j = 0; j < pool.size(); j++) {
                    TreasureCard treasureCard = pool.get(j);
                    if (treasureCard.type == TreasureCard.CardType.EVENT && (treasureCard.value == 6 || treasureCard.value == 7)) {
                        has = true;
                        index = j;
                        break;
                    }
                }
                if (!has) {
                    pool.add(random.nextInt(pool.size()), card);
                } else {
                    boolean valid = false;
                    int pos = -1;
                    while (!valid) {
                        pos = random.nextInt(pool.size());
                        if (pos < index) {
                            for (int j = pos; j < index; j++) {
                                if (pool.get(j).type == TreasureCard.CardType.TREASURE) {
                                    valid = true;
                                    break;
                                }
                            }
                        } else {
                            for (int j = index; j < pos; j++) {
                                if (pool.get(j).type == TreasureCard.CardType.TREASURE) {
                                    valid = true;
                                    break;
                                }
                            }
                        }
                    }
                    pool.add(pos, card);
                }
            } else {
                pool.add(random.nextInt(pool.size()), card);
            }
        }
        List<Integer> invalid = new ArrayList<>();
        for (int i = 0; i < hold.size(); i++) {
            TreasureCard card = hold.get(i);
            if (card.value == 1) {
                List<Integer> pos = new ArrayList<>();
                for (int j = 0; j < pool.size(); j++) {
                    if (pool.get(j).type == TreasureCard.CardType.WEALTH && pool.get(j).value >= 10) {
                        pos.add(j);
                        if (j > 0)
                            pos.add(j - 1);
                        if (j > 1)
                            pos.add(j - 2);
                    }
                }
                pos.removeAll(invalid);
                if (pos.size() > 0) {
                    int p = pos.get(random.nextInt(pos.size()));
                    pool.add(p, card);
                    invalid.add(p + 1);
                    invalid.add(p + 2);
                    invalid.add(p + 3);
                } else {
                    for (int j = 0; j < pool.size(); j++) {
                        if (pool.get(j).type == TreasureCard.CardType.WEALTH) {
                            pos.add(j);
                            if (j > 0)
                                pos.add(j - 1);
                            if (j > 1)
                                pos.add(j - 2);
                        }
                    }
                    pos.removeAll(invalid);
                    int p = pos.get(random.nextInt(pos.size()));
                    pool.add(p, card);
                    invalid.add(p + 1);
                    invalid.add(p + 2);
                    invalid.add(p + 3);
                    for (int j = p; j < pool.size(); j++) {
                        if (pool.get(j).type == TreasureCard.CardType.WEALTH) {
                            int v = 10 + random.nextInt(6);
                            pool.get(j).value = v;
                            pool.get(j).realValue = v;
                            pool.get(j).description = "总计" + v + "枚金币";
                        }
                    }
                }
            }
            else if (card.value == 2) {
                List<Integer> pos = new ArrayList<>();
                for (int j = 0; j < pool.size(); j++) {
                    if (pool.get(j).type == TreasureCard.CardType.TREASURE) {
                        pos.add(j);
                        if (j > 0)
                            pos.add(j - 1);
                        if (j > 1)
                            pos.add(j - 2);
                    }
                }
                pos.removeAll(invalid);
                int p = pos.get(random.nextInt(pos.size()));
                invalid.add(p + 1);
                invalid.add(p + 2);
                invalid.add(p + 3);
                pool.add(p, card);
            }
            else if (card.value == 3) {
                List<Integer> pos = new ArrayList<>();
                for (int j = 0; j < pool.size(); j++) {
                    if (pool.get(j).type == TreasureCard.CardType.MONSTER) {
                        pos.add(j);
                        if (j > 0)
                            pos.add(j - 1);
                        if (j > 1)
                            pos.add(j - 2);
                    }
                }
                pos.removeAll(invalid);
                int p = pos.get(random.nextInt(pos.size()));
                invalid.add(p + 1);
                invalid.add(p + 2);
                invalid.add(p + 3);
                pool.add(p, card);
            }
        }
        for (int i = 0; i < 3; i++) {
            int r = random.nextInt(13) + 3;
            pool.add(0, new TreasureCard(TreasureCard.CardType.WEALTH, "宝藏", "总计" + r + "枚金币", r));
        }
    }

    @Override
    public void start() {
        state = 1;
        for (GamePlayer player : players) {
            player.data.creditInGame = rate * 300;
        }
        thread = new GameThread(this);
        thread.start();
        group.sendMessage("探险开始！");
    }

    @Override
    public Message input(GamePlayer player, MessageChain msg, boolean group) {
        lock.lock();
        try {
            if (group) {
                return new MessageChainBuilder().append("请私聊操作").build();
            }
            if (state == 0) {
                return new MessageChainBuilder().append("游戏还未开始，理论上来说你不应该收到这条回复").build();
            }
            if (state <= 3) {
                return new MessageChainBuilder().append("暂时不需操作，请耐心等待").build();
            }
            if (!players.contains(player)) {
                return new MessageChainBuilder().append("你已经撤离，无需操作").build();
            }
            if (received.contains(player)) {
                return new MessageChainBuilder().append("你已经做出过本轮的选择").build();
            }
            if (msg.contentToString().equalsIgnoreCase("撤离")) {
                received.add(player);
                afk.remove(player);
                leavings.add(player);
                return new MessageChainBuilder().append("你选择了返回营地，将与其他撤离者平分路上遗留的金币").build();
            }
            if (msg.contentToString().equalsIgnoreCase("继续")) {
                received.add(player);
                afk.remove(player);
                return new MessageChainBuilder().append("你选择了继续探险").build();
            }
            return new MessageChainBuilder().append("未知操作，请选择【继续】或【撤离】").build();
        } finally {
            lock.unlock();
        }
    }

    public void update() throws IOException, InterruptedException {
        lock.lock();
        try {
            received.clear();
            if (state <= 3) {
                TreasureCard card = pool.remove(0);
                BufferedImage image = ImageGenerator.generateCardImage(card);
                int value = card.value;
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                byte[] baImage = null;
                try {
                    ImageIO.write(image, "png", os);
                    baImage = os.toByteArray();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (baImage != null) {
                    MSGHandler.asyncSendImage(group, baImage);
                }
                Thread.sleep(2000);
                MessageChainBuilder builder = new MessageChainBuilder();
                builder.append("探险继续，发现了：").append(card.name).append("\n").append(card.description);
                builder.append("\n");
                int distribution = value / players.size();
                for (GamePlayer player : players) {
                    wealth.put(player, wealth.getOrDefault(player, 0) + distribution);
                }
                int remain = value % players.size();
                card.realValue = remain;
                builder.append("每人分得").append(String.valueOf(distribution)).append("枚金币，留下").append(String.valueOf(remain)).append("枚金币");
                group.sendMessage(builder.build());
                past.add(card);
                Thread.sleep(2000);
                state++;
                if (state == 4) {
                    BufferedImage table = generateTableImage();
                    ByteArrayOutputStream os2 = new ByteArrayOutputStream();
                    byte[] baImage2 = null;
                    try {
                        ImageIO.write(table, "png", os2);
                        baImage2 = os2.toByteArray();
                        os2.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (baImage2 != null) {
                        MSGHandler.asyncSendImage(group, baImage2);
                    }

                    Thread.sleep(2000);
                    MessageChainBuilder builder2 = new MessageChainBuilder();
                    builder2.append("请各位玩家私信选择【继续】或【撤离】，撤离者将平分路上遗留的金币，随后退出探险，不再获得任何金币。");
                    group.sendMessage(builder2.build());
                }
            } else {
                MessageChainBuilder builder = new MessageChainBuilder();
                builder.append("选择完毕，以下玩家选择了撤离：");
                for (GamePlayer player : leavings) {
                    builder.append("\n");
                    builder.append(new At(player.id)).append(" ");
                }
                int left = leavings.size();
                if (left > 0) {
                    int distribution = 0;
                    for (TreasureCard card : past) {
                        if (card.type == TreasureCard.CardType.WEALTH) {
                            int value = card.realValue;
                            distribution += value / left;
                            card.realValue = value % left;
                        }
                        if (card.type == TreasureCard.CardType.TREASURE && left == 1) {
                            distribution += card.realValue;
                            card.realValue = 0;
                        }
                    }
                    for (TreasureCard card : events) {
                        if (card.value == TreasureCard.QUIT_REWARD) {
                            int value = card.realValue;
                            distribution += value / left;
                            card.realValue = value % left;
                        }
                    }
                    for (GamePlayer player : leavings) {
                        wealth.put(player, wealth.getOrDefault(player, 0) + distribution);
                        left_players.add(player);
                        players.remove(player);
                    }
                    leavings.clear();
                    builder.append("\n");
                    builder.append("\n");
                    if (left > 1)
                        builder.append("撤离者每人分得").append(String.valueOf(distribution)).append("枚金币");
                    else
                        builder.append("撤离者独自获得了").append(String.valueOf(distribution)).append("枚金币");
                    Thread.sleep(1500);
                    group.sendMessage(builder.build());
                } else {
                    Thread.sleep(1500);
                    builder.append("\n");
                    builder.append("无");
                    group.sendMessage(builder.build());
                }
                events.removeIf(card -> card.value == TreasureCard.QUIT_REWARD);
                if (check()) {
                    stop();
                    return;
                }
                TreasureCard card = pool.remove(0);
                BufferedImage image = ImageGenerator.generateCardImage(card);
                int value = card.value;
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                byte[] baImage = null;
                try {
                    ImageIO.write(image, "png", os);
                    baImage = os.toByteArray();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (baImage != null) {
                    MSGHandler.asyncSendImage(group, baImage);
                }
                Thread.sleep(2000);
                builder = new MessageChainBuilder();
                builder.append("探险继续，发现了：").append(card.name).append("\n").append(card.description);
                OUT:
                switch (card.type) {
                    case WEALTH:
                        for (TreasureCard eventCard : events) {
                            if (eventCard.value == TreasureCard.EXTRA_WEALTH) {
                                int extra = eventCard.realValue;
                                value += extra;
                                eventCard.realValue = 0;
                                builder.append("\n");
                                builder.append("奇遇卡生效，额外获得").append(String.valueOf(extra)).append("枚金币");
                                break;
                            }
                            if (eventCard.value == TreasureCard.DOUBLE_WEALTH) {
                                value *= 2;
                                eventCard.realValue = 0;
                                builder.append("\n");
                                builder.append("奇遇卡生效，获得的金币数翻倍");
                                break;
                            }
                        }
                        events.removeIf(card1 -> card1.value == TreasureCard.EXTRA_WEALTH || card1.value == TreasureCard.DOUBLE_WEALTH);
                        int distribution = value / players.size();
                        for (GamePlayer player : players) {
                            wealth.put(player, wealth.getOrDefault(player, 0) + distribution);
                        }
                        int remain = value % players.size();
                        card.realValue = remain;
                        builder.append("\n");
                        builder.append("每人分得").append(String.valueOf(distribution)).append("枚金币，留下").append(String.valueOf(remain)).append("枚金币");
                        break;
                    case TREASURE:
                        for (TreasureCard eventCard : events) {
                            if (eventCard.value == TreasureCard.MAX_TREASURE) {
                                value = 20;
                                card.realValue = value;
                                card.value = value;
                                builder.append("\n");
                                builder.append("奇遇卡生效，获得了价值最高的宝物");
                                break;
                            }
                            if (eventCard.value == TreasureCard.DOUBLE_TREASURE) {
                                value *= 2;
                                card.realValue = value;
                                card.value = value;
                                builder.append("\n");
                                builder.append("奇遇卡生效，获得的宝物价值翻倍");
                                break;
                            }
                        }
                        events.removeIf(eventCard -> eventCard.value == TreasureCard.MAX_TREASURE || eventCard.value == TreasureCard.DOUBLE_TREASURE);
                        builder.append("\n");
                        builder.append("只有独自撤离的玩家能得到这件宝物。");
                        break;
                    case MONSTER:
                        for (TreasureCard eventCard : events) {
                            if (eventCard.value == TreasureCard.SUDDEN_DEATH) {
                                Iterator<GamePlayer> iterator = players.iterator();
                                while (iterator.hasNext()) {
                                    GamePlayer player = iterator.next();
                                    wealth.put(player, 0);
                                    iterator.remove();
                                    left_players.add(player);
                                }
                                builder.append("\n");
                                builder.append("怪物突袭！未撤离的玩家全部被吞噬。");
                                break OUT;
                            }
                        }
                        events.removeIf(eventCard -> eventCard.value == TreasureCard.SUDDEN_DEATH);
                        for (TreasureCard pastCard : past) {
                            if (pastCard.type == TreasureCard.CardType.MONSTER) {
                                if (pastCard.value == card.value) {
                                    Iterator<GamePlayer> iterator = players.iterator();
                                    while (iterator.hasNext()) {
                                        GamePlayer player = iterator.next();
                                        wealth.put(player, 0);
                                        iterator.remove();
                                        left_players.add(player);
                                    }
                                    builder.append("\n");
                                    builder.append("遭遇相同怪物两次，未撤离的玩家全部被怪物吞噬。");
                                    break OUT;
                                }
                            }
                        }
                        builder.append("\n");
                        builder.append("遭遇相同怪物两次时，仍未撤离的玩家将被怪物吞噬，失去所有金币。");
                        break;
                    case EVENT:
                        switch (value) {
                            case TreasureCard.EXTRA_WEALTH:
                                card.realValue = 7 + random.nextInt(3);
                                builder.append("\n");
                                builder.append("将增加").append(String.valueOf(card.realValue)).append("枚金币");
                            case TreasureCard.DOUBLE_WEALTH:
                            case TreasureCard.MAX_TREASURE:
                            case TreasureCard.DOUBLE_TREASURE:
                            case TreasureCard.SUDDEN_DEATH:
                                events.add(card);
                                break;
                            case TreasureCard.QUIT_REWARD:
                                card.realValue = 5 + random.nextInt(16);
                                builder.append("\n");
                                builder.append("将平分").append(String.valueOf(card.realValue)).append("枚金币");
                                events.add(card);
                                break;
                            case TreasureCard.WEALTH_INCREASE:
                                for (TreasureCard c : past) {
                                    if (c.type == TreasureCard.CardType.WEALTH) {
                                        c.realValue += random.nextInt(2) + 1;
                                    }
                                }
                                break;
                            case TreasureCard.WEALTH_DISTRIBUTE:
                                int total = 9 + random.nextInt(5);
                                builder.append("\n");
                                builder.append("总计增加").append(String.valueOf(total)).append("枚金币");
                                while (total > 0) {
                                    for (TreasureCard c : past) {
                                        if (total == 0)
                                            break;
                                        if (c.type == TreasureCard.CardType.WEALTH) {
                                            c.realValue++;
                                            total--;
                                        }
                                    }
                                }
                                break;
                            case TreasureCard.WEALTH_RESET:
                                List<Integer> pos = new ArrayList<>();
                                for (int i = 0; i < past.size(); i++) {
                                    if (past.get(i).type == TreasureCard.CardType.WEALTH) {
                                        pos.add(i);
                                    }
                                }
                                int j = pos.get(random.nextInt(pos.size()));
                                TreasureCard c = past.get(j);
                                c.realValue = c.value;
                                break;
                            case TreasureCard.TREASURE_CONVERT:
                                List<Integer> pos_ = new ArrayList<>();
                                for (int i = 0; i < past.size(); i++) {
                                    if (past.get(i).type == TreasureCard.CardType.WEALTH) {
                                        pos_.add(i);
                                    }
                                }
                                int k = pos_.get(random.nextInt(pos_.size()));
                                int r = 10 + random.nextInt(11);
                                TreasureCard new_card = new TreasureCard(TreasureCard.CardType.TREASURE, "珍宝", "价值" + r + "金币", r);
                                past.set(k, new_card);
                                break;
                        }
                }
                Thread.sleep(2000);
                group.sendMessage(builder.build());
                past.add(card);
                if (check()) {
                    stop();
                    return;
                }
                state++;
                BufferedImage table = generateTableImage();
                ByteArrayOutputStream os2 = new ByteArrayOutputStream();
                byte[] baImage2 = null;
                try {
                    ImageIO.write(table, "png", os2);
                    baImage2 = os2.toByteArray();
                    os2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (baImage2 != null) {
                    MSGHandler.asyncSendImage(group, baImage2);
                }

                Thread.sleep(2000);
                MessageChainBuilder builder2 = new MessageChainBuilder();
                builder2.append("请各位玩家私信选择【继续】或【撤离】，撤离者将平分路上遗留的金币，随后退出探险，不再获得任何金币。");
                group.sendMessage(builder2.build());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    private boolean check() {
        return players.size() == 0;
    }

    @Override
    public void stop() {
        for (GamePlayer player : players) {
            player.data.creditInGame = 0;
        }
        for (GamePlayer player : left_players) {
            player.data.creditInGame = 0;
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int baseExp = 200;
        past.addAll(pool);
        pool.clear();
        BufferedImage table = null;
        try {
            table = this.generateTableImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream os2 = new ByteArrayOutputStream();
        byte[] baImage2 = null;
        try {
            ImageIO.write(table, "png", os2);
            baImage2 = os2.toByteArray();
            os2.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        if (baImage2 != null) {
            MSGHandler.asyncSendImage(group, baImage2);
        }
        MessageChainBuilder builder = new MessageChainBuilder();
        builder.append("探险结束，感谢大家的参与！");
        builder.append("\n");
        builder.append("以下是本次探险的最终结果：");
        HashMap<GamePlayer, Integer> win_map = new HashMap<>();
        // sort in descending order
        Collections.sort(left_players, (o1, o2) -> wealth.get(o2) - wealth.get(o1));
        for (int i = 0; i < left_players.size(); i++) {
            int win = 0;
            int w = wealth.get(left_players.get(i));
            for (int j = i + 1; j < left_players.size(); j++) {
                if (w > wealth.get(left_players.get(j))) {
                    win += w * rate;
                    win_map.put(left_players.get(j), win_map.getOrDefault(left_players.get(j), 0) - w * rate);
                }
            }
            win_map.put(left_players.get(i), win_map.getOrDefault(left_players.get(i), 0) + win);
        }
        for (GamePlayer p : left_players) {
            int exp = ((rate > 0 && state >= 8) ? baseExp : 0) + wealth.getOrDefault(p, 0) * Math.min(20, Math.max(5, rate));
            builder.append("\n");
            builder.append(p.name).append(" 积分:").append(win_map.getOrDefault(p, 0) > 0 ? "+":"").append(String.valueOf(win_map.getOrDefault(p, 0)));
            builder.append(" 经验:+").append(String.valueOf(exp));
            p.data.credit += win_map.get(p);
            p.data.addExp(exp);
        }
        group.sendMessage(builder.build());
        state = -1;
        MiraiGamePlugin.INSTANCE.players_map.get(this.group.getId()).clear();
        MiraiGamePlugin.INSTANCE.games.remove(group.getId());
        try {
            Manager.save();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean allowGroup() {
        return false;
    }

    @Override
    public boolean isWaiting() {
        return state == 0;
    }

    @Override
    public int getMaxPlayer() {
        return 5;
    }

    @Override
    public int getMinPlayer() {
        return 4;
    }

    @Override
    public void addPlayer(GamePlayer activePlayer) {
        players.add(activePlayer);
    }

    @Override
    public void remove(GamePlayer activePlayer) {
        players.remove(activePlayer);
    }

    private BufferedImage getImg(String url) {
        if (cache.containsKey(url)) {
            return cache.get(url);
        }
        if (url.equalsIgnoreCase("http://q1.qlogo.cn/g?b=qq&nk=372542780&s=640")) {
            File f = new File("./resources/saiwei.png");
            if(f.exists()) {
                try {
                    BufferedImage img = ImageIO.read(f);
                    cache.put(url, img);
                    return img;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            URL url1 = new URL(url);
            try (InputStream is = url1.openStream()) {
                BufferedImage img = ImageIO.read(is);
                cache.put(url, img);
                return img;
            }
        } catch (IOException ignored) {

        }
        return null;
    }

    private BufferedImage generateTableImage() throws IOException {
        BufferedImage img = new BufferedImage(800, 100+187*((past.size() - 1) / 7 + 1), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 800, 200+187*(past.size() / 7 + 1));
        g.setColor(Color.BLACK);
        g.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        int playerSize = this.players.size() + left_players.size();
        for (int i = 0; i < this.players.size(); i++) {
            GamePlayer player = this.players.get(i);
            BufferedImage avatar = getImg(player.avatar);
            if (avatar != null) {
                g.drawImage(avatar, 10 + 800 / playerSize * i, 10, 80, 80, null);
            }
            g.drawString("探险中", 90 + 800 / playerSize * i, 60);
            int pt = wealth.getOrDefault(player, 0);
            g.drawString("金币:" + pt, 90 + 800 / playerSize * i, 80);
        }
        for (int i = 0; i < left_players.size(); i++) {
            int j = i + this.players.size();
            GamePlayer player = left_players.get(i);
            BufferedImage avatar = getImg(player.avatar);
            if (avatar != null) {
                g.drawImage(avatar, 10 + 800 / playerSize * j, 10, 80, 80, null);
            }
            g.drawString("已撤离", 90 + 800 / playerSize * j, 60);
            int pt = wealth.getOrDefault(player, 0);
            g.drawString("金币:" + pt, 90 + 800 / playerSize * j, 80);
        }
        for (int i = 0; i < past.size(); i++) {
            int x = i % 7;
            int y = i / 7;
            BufferedImage card = ImageGenerator.generateCardImage(past.get(i));
            g.drawImage(card, 50 + 100 * x + 2, 100 + 187 * y + 1, null);
        }
        g.dispose();
        return img;
    }

    public static class GameThread extends Thread {

        private final TreasureHunter game;

        int count = 50;

        public GameThread(TreasureHunter game) {
            this.game = game;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (game.state == 0) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if (game.state == -1) {
                    return;
                }
                try {
                    if (game.state <= 3) {
                        game.update();
                    } else {
                        if (game.received.size() + game.afk.size() == game.players.size()) {
                            game.update();
                            count = 50;
                        } else {
                            count--;
                            if (count == 30 || count == 20 || count == 10 || count == 5) {
                                MessageChainBuilder builder = new MessageChainBuilder();
                                builder.add("还有" + count + "秒，以下玩家未操作：\n");
                                for (GamePlayer player : game.players) {
                                    if (!game.received.contains(player) && !game.afk.contains(player)) {
                                        builder.append(new At(player.id)).append(" ");
                                    }
                                }
                                if (game.afk.size() > 0) {
                                    builder.append("\n以下玩家挂机中，其它玩家操作完毕后将自动选择继续：\n");
                                    for (GamePlayer player : game.afk) {
                                        builder.append(new At(player.id)).append(" ");
                                    }
                                }
                                game.group.sendMessage(builder.build());
                            }
                            if (count == 0) {
                                for (GamePlayer player : game.players) {
                                    if (!game.received.contains(player)) {
                                        game.afk.add(player);
                                    }
                                }
                                game.update();
                                Thread.sleep(2000);
                                count = 50;
                            }
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
