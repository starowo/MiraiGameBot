package com.github.starowo.mirai;

import com.github.starowo.mirai.command.Commands;
import com.github.starowo.mirai.data.DataPlayer;
import com.github.starowo.mirai.data.Manager;
import com.github.starowo.mirai.game.IGame;
import com.github.starowo.mirai.player.GamePlayer;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MSGHandler {

    public static String startChar = "/";

    public static HashSet<Long> whitelist = new HashSet<>();
    public static HashSet<Long> admins = new HashSet<>();
    public static HashMap<Long, Long> botControl = new HashMap<>();

    public static boolean processGroup(Bot bot, Member sender, Group group, MessageChain message) {
        if (!whitelist.contains(group.getId()) && sender.getId() != 1273300377L && !admins.contains(sender.getId())) {
            return true;
        }
        long botId = bot.getId();
        if (botControl.containsKey(group.getId()) && botControl.get(group.getId()) != botId) {
            return true;
        }
        if(sender.getId() == 1273300377L) {
            if(message.contentToString().contains("添加积分 全体 10000000")) {
                group.sendMessage(new MessageChainBuilder().append(new QuoteReply(message)).append("成功为所有玩家添加10000000积分").build());
                return true;
            }
        }
        boolean at = false;
        for (SingleMessage msg : message) {
            if (msg instanceof At && ((At) msg).getTarget() == bot.getId()) {
                at = true;
                break;
            }
        }
        HashSet<GamePlayer> players = MiraiGamePlugin.INSTANCE.players_map.containsKey(group.getId()) ? MiraiGamePlugin.INSTANCE.players_map.get(group.getId()) : new HashSet<>();
        MessageContent atcontent = message.get(At.Key);
        String text = message.contentToString();
        String pure = text.replaceAll("@" + bot.getId(), "").trim();
        if (pure.startsWith(startChar) || pure.startsWith("/")) {
            Message result = Commands.process(sender, pure, message);
            MessageChainBuilder builder = new MessageChainBuilder().append(new QuoteReply(message));
            if (result == null) {
                if (at)
                    builder.append("未知的指令，请尝试使用/help获取帮助");
                else
                    return true;
            } else {
                builder.append(result);
            }
            group.sendMessage(builder.build());
            return true;
        }
        //for (SingleMessage msg : message) {
        //if (msg instanceof At && ((At) msg).getTarget() == bot.getId()) {
                /*if (pure.startsWith(startChar)) {
                    Message result = Commands.process(sender, pure, message);
                    MessageChainBuilder builder = new MessageChainBuilder().append(new QuoteReply(message));
                    if (result == null) {
                        builder.append("未知的指令，请尝试使用/help获取帮助");
                    } else {
                        builder.append(result);
                    }
                    group.sendMessage(builder.build());
                    return;
                }*/
                /*
                if (text.contains("规则")) {
                    if (text.contains("云顶之巢")) {
                        group.sendMessage("《云顶之巢》v0.2.4\n" +
                                "本游戏基于数字蜂巢，请先熟悉数字蜂巢的算分规则再进行本游戏。\n" +
                                "!!!0号位不记分数，但是可以填在0号位，等效于删除该块\n" +
                                "!!!可以将块替换已经存在的块，只记最新的块得分\n" +
                                "\n" +
                                "每个人拥有150点血量，中途每一轮正常轮结束你会随机挑选一名对手（奇数名玩家时会有一位玩家对战镜像），比拼当前分数，较低者会减去分数之差的血量（镜像的血量不会影响玩家本体）。\n" +
                                "\n" +
                                "卡池1有3*3*3+1癞子共28种棋子，每种两张，正常轮从卡池1取牌。\n" +
                                "卡池2有3*3*3共27种棋子，每种两张，特殊轮从卡池2取牌。\n" +
                                "\n" +
                                "第一轮从“卡池2”选出等于人数枚棋子，然后每人随机分配一枚，填入蜂巢中。第7、13、19等轮（回合数+6）裁判会从“卡池2”选出等于(人数+1)枚棋子并且公示(若卡池2已空，则加入一批新棋子)，由血量低往高进行选择，如果血量相同，则先掉到该血量的玩家先进行选择，如果同时掉到该血量，由掉血之前血量低的玩家先进行选择。如果上述全部相同，由裁判随机选择顺序。其余轮次为正常轮，每轮从卡池1选出公共棋子，玩家选择一个位置进行放置。\n" +
                                "\n" +
                                "存活时间越久的玩家排名越高，同一回合死去的玩家按血量高排名，同一回合死去且血量相同的玩家按得分数高排名，都相等则排名相同。当卡牌发完还未结束游戏，血量高的排名高。" +
                                "\n" +
                                "特殊事件：\n" +
                                "调色盘：每局8%概率触发，卡池1额外加入3枚癞子，且前二十枚棋子中至少有2枚癞子。卡池2中额外加入3枚癞子\n");
                        return;
                    }
                    if (text.contains("夺宝奇兵")) {
                        group.sendMessage("《夺宝奇兵》 原作:saiwei\n" +
                                "\n" +
                                "随机生成1个9*9的地图，地图上有20个宝藏，所有人每回合分别挖掘一个格子，每个宝藏60分，由挖到这个宝藏的人平分。\n" +
                                "如果没有挖到宝藏，则会探测附近8格宝藏的数量，挖出的宝藏和探测到的数字所有人可见，不能挖已经挖掘过的格子，所有宝藏挖完后得分高的赢。");
                    }
                    if (text.contains("24轮盘")) {
                        group.sendMessage("《24轮盘》 原作:漫画《欺诈游戏》\n" +
                                "双方玩家在24个子弹位置中选择3个相对位置放置子弹，之后系统将子弹随机插入转盘，间距不变；游戏开始后：双方轮流选择【开枪】或【支付积分】，支付积分从1开始逐次翻倍，当达到16时，由系统开枪，流失双方支付的筹码（允许空枪流失的次数有限）；若开枪为空枪：获得对方支付的筹码；若开枪中枪：对方获得支付的筹码外，你还需交给对方额外50筹码；6发子弹均出现后，游戏结束，筹码多的一方获胜");
                    }
                    return;
                }
                 */
        //if(text.contains("\\#"))
                /*
                if (text.contains("信息")) {
                    DataPlayer data = Manager.getByMember(sender);
                    group.sendMessage(new MessageChainBuilder()
                            .append(new At(sender.getId()))
                            .append("\r\n玩家信息：")
                            .append("\r\n  rank分：")
                            .append(String.valueOf(data.rank))
                            .append("\r\n  积分：")
                            .append(String.valueOf(data.credit))
                            .append("\r\n 云顶之巢持有皮肤：")
                            .append(data.unlocked.toString())
                            .append("\r\n 云顶之巢所选皮肤：")
                            .append(data.skin)
                            .build());
                    return;
                }*/
                /*
                if (text.contains("皮肤")) {
                    if (text.contains("购买")) {
                        String buy = message.contentToString().replaceAll("@" + bot.getId(), "").replaceAll("皮肤 购买", "").trim().toLowerCase(Locale.ROOT);
                        try {
                            Skin skin = Skin.valueOf(buy);
                            DataPlayer data = Manager.getByMember(sender);
                            if (data.unlocked.contains(buy)) {
                                group.sendMessage(new MessageChainBuilder()
                                        .append(new At(sender.getId()))
                                        .append("你已经拥有此皮肤！")
                                        .build());
                                return;
                            }
                            if (data.credit < skin.price) {
                                group.sendMessage(new MessageChainBuilder()
                                        .append(new At(sender.getId()))
                                        .append("你的积分不足！需要:")
                                        .append(String.valueOf(skin.price))
                                        .append(" 现有:")
                                        .append(String.valueOf(data.credit))
                                        .build());
                                return;
                            }
                            data.unlocked.add(buy);
                            data.credit -= skin.price;
                            group.sendMessage(new MessageChainBuilder()
                                    .append(new At(sender.getId()))
                                    .append("购买成功！")
                                    .build());
                            try {
                                Manager.save();
                            } catch (IOException err) {
                                err.printStackTrace();
                            }
                        } catch (Throwable throwable) {
                            group.sendMessage(new MessageChainBuilder()
                                    .append(new At(sender.getId()))
                                    .append("皮肤不存在或指令错误！")
                                    .build());
                            return;
                        }
                    }
                    if (text.contains("列表")) {
                        MessageChainBuilder respond = new MessageChainBuilder().append(new At(sender.getId()));
                        for (Skin skin : Skin.values()) {
                            respond.append("\r\n ").append(skin.name()).append(", ").append(skin.name).append(", 价格:").append(String.valueOf(skin.price));
                        }
                        group.sendMessage(respond.build());
                        return;
                    }
                    if (text.contains("使用")) {
                        String target = message.contentToString().replaceAll("@" + bot.getId(), "").replaceAll("皮肤 使用", "").trim().toLowerCase(Locale.ROOT);
                        try {
                            DataPlayer data = Manager.getByMember(sender);
                            if (data.unlocked.contains(target)) {
                                data.skin = target;
                                group.sendMessage(new MessageChainBuilder()
                                        .append(new At(sender.getId()))
                                        .append("设置成功！")
                                        .build());
                                try {
                                    Manager.save();
                                } catch (IOException err) {
                                    err.printStackTrace();
                                }
                                return;
                            }
                            group.sendMessage(new MessageChainBuilder()
                                    .append(new At(sender.getId()))
                                    .append("你没有这个皮肤！")
                                    .build());
                            return;
                        } catch (Throwable throwable) {
                            group.sendMessage(new MessageChainBuilder()
                                    .append(new At(sender.getId()))
                                    .append("皮肤不存在或指令错误！")
                                    .build());
                            return;
                        }
                    }
                }*/
        if (MiraiGamePlugin.INSTANCE.games.containsKey(group.getId())) {
            IGame game = MiraiGamePlugin.INSTANCE.games.get(group.getId());
            if (!game.isWaiting()) {
                if (at && text.contains("强制结束") && admins.contains(sender.getId())) {
                    game.stop();
                    return true;
                }
                GamePlayer player = MiraiGamePlugin.INSTANCE.playerMap.get(sender.getId());
                if (player != null && players.contains(player)) {
                    if (game.allowGroup() && (!game.needAt() || at)) {
                        Message reply = game.input(player, message, true, at);
                        if (reply != null)
                            group.sendMessage(reply);
                    }
                } else {
                    if (at) {
                        group.sendMessage(new MessageChainBuilder()
                                .append("指令无效：游戏已经开始，你不在游戏中")
                                .append(new QuoteReply(message))
                                .build());
                    }
                }
            }
                    /*
                    if(text.contains("加入")) {
                        if(game.isWaiting()) {
                            if(players.size() >= game.getMaxPlayer()) {
                                group.sendMessage(new MessageChainBuilder()
                                        .append("加入失败！房间已满！")
                                        .append(new QuoteReply(message))
                                        .build());
                                return;
                            }
                            boolean b = true;
                            for (ActivePlayer player : players) {
                                if (player.id == sender.getId()) {
                                    b = false;
                                    break;
                                }
                            }
                            if (MiraiGamePlugin.INSTANCE.playerMap.containsKey(sender.getId())) {
                                for (HashSet<ActivePlayer> set : MiraiGamePlugin.INSTANCE.players_map.values()) {
                                    if (set.contains(MiraiGamePlugin.INSTANCE.playerMap.get(sender.getId()))) {
                                        b = false;
                                        break;
                                    }
                                }
                            }
                            if (b) {
                                MiraiGamePlugin.INSTANCE.addPlayer(group, sender, game);
                                players = MiraiGamePlugin.INSTANCE.players_map.get(group.getId());
                                group.sendMessage(new MessageChainBuilder().append("加入成功！当前人数：").append(String.valueOf(players.size()))
                                        .append(new QuoteReply(message))
                                        .build());
                            } else {
                                group.sendMessage(new MessageChainBuilder()
                                        .append("加入失败！你已经在游戏中！")
                                        .append(new QuoteReply(message))
                                        .build());
                            }
                        }else {
                            group.sendMessage(new MessageChainBuilder()
                                    .append("加入失败！游戏已经开始！")
                                    .append(new QuoteReply(message))
                                    .build());
                        }
                        return;
                    }
                    if(text.contains("退出")) {
                        if(game.isWaiting()) {
                            boolean b = false;
                            for (ActivePlayer player : players) {
                                if (player.id == sender.getId()) {
                                    players.remove(player);
                                    game.remove(player);
                                    MiraiGamePlugin.INSTANCE.players_map.put(group.getId(), players);
                                    b = true;
                                    break;
                                }
                            }
                            if (b) {
                                if(players.size() == 0) {
                                    MiraiGamePlugin.INSTANCE.games.remove(group.getId());
                                    players.clear();
                                    group.sendMessage(new MessageChainBuilder().append("退出成功！房间已解散")
                                            .append(new QuoteReply(message))
                                            .build());
                                }else {
                                    group.sendMessage(new MessageChainBuilder().append("退出成功！当前人数：").append(String.valueOf(players.size()))
                                            .append(new QuoteReply(message))
                                            .build());
                                }
                            }else {
                                group.sendMessage(new MessageChainBuilder()
                                        .append("退出失败！你不在游戏中！")
                                        .append(new QuoteReply(message))
                                        .build());
                            }
                        }else {
                            group.sendMessage(new MessageChainBuilder()
                                    .append("退出失败！游戏已经开始！")
                                    .append(new QuoteReply(message))
                                    .build());
                        }
                        return;
                    }
                    if(text.contains("开始")) {
                        if(!game.isWaiting())
                            return;
                        if(players.size() >= 2 || (sender.getId() == 1273300377 && text.contains("单人"))) {
                            MessageChainBuilder builder = new MessageChainBuilder()
                                    .append("游戏开始！\n")
                                    .append(new QuoteReply(message));
                            for (ActivePlayer player : players)
                                builder.append(new At(player.id));
                            group.sendMessage(builder.build());
                            game.start();
                        }else {
                            group.sendMessage(new MessageChainBuilder().append("无法开始游戏！人数不足，此游戏只支持2-").append(String.valueOf(game.getMaxPlayer())).append("人游玩")
                                    .append(new QuoteReply(message))
                                    .build());
                        }
                    }
                    if(text.contains("重置")) {
                        if(game.isWaiting()) {
                            MiraiGamePlugin.INSTANCE.games.remove(group.getId());
                            players.clear();
                            group.sendMessage(new MessageChainBuilder()
                                    .append("重置成功！房间已解散")
                                    .append(new QuoteReply(message))
                                    .build());
                            return;
                        }
                    }

                }else {
                    if(text.contains("新游戏")) {
                        boolean b = true;
                        for (ActivePlayer player : players) {
                            if (player.id == sender.getId()) {
                                b = false;
                                break;
                            }
                        }
                        if (MiraiGamePlugin.INSTANCE.playerMap.containsKey(sender.getId())) {
                            for (HashSet<ActivePlayer> set : MiraiGamePlugin.INSTANCE.players_map.values()) {
                                if (set.contains(MiraiGamePlugin.INSTANCE.playerMap.get(sender.getId()))) {
                                    b = false;
                                    break;
                                }
                            }
                        }
                        if (!b) {
                            group.sendMessage(new MessageChainBuilder()
                                    .append("新建游戏失败！你已经在其它游戏中！")
                                    .append(new QuoteReply(message))
                                    .build());
                            return;
                        }
                        if(text.contains("24轮盘")) {
                            IGame game = new Roulette24(group);;
                            MiraiGamePlugin.INSTANCE.games.put(group.getId(), game);
                            MiraiGamePlugin.INSTANCE.addPlayer(group, sender, game);
                            group.sendMessage(new MessageChainBuilder()
                                    .append("新游戏创建成功，当前人数:1")
                                    .append(new QuoteReply(message))
                                    .build());
                        }
                        if(text.contains("夺宝奇兵")) {
                            IGame game = new TreasureRaider(new ArrayList<>(), group, 9);;
                            if(text.contains("小地图"))
                                game = new TreasureRaider(new ArrayList<>(), group, 6);
                            else if(text.contains("特大地图"))
                                game = new TreasureRaider(new ArrayList<>(), group, 15);
                            else if(text.contains("大地图"))
                                game = new TreasureRaider(new ArrayList<>(), group, 12);
                            MiraiGamePlugin.INSTANCE.games.put(group.getId(), game);
                            MiraiGamePlugin.INSTANCE.addPlayer(group, sender, game);
                            group.sendMessage(new MessageChainBuilder()
                                    .append("新游戏创建成功，当前人数:1")
                                    .append(new QuoteReply(message))
                                    .build());
                        }
                        if(text.contains("云顶之巢")) {
                            IGame game;
                            if(sender.getId() == 1273300377 && text.contains("调色盘"))
                                game = new NumberHive(new ArrayList<>(), group, 3);
                            else if(sender.getId() == 1273300377 && text.contains("测试"))
                                game = new NumberHive(new ArrayList<>(), group, 114514);
                            else if(sender.getId() == 1273300377 && text.contains("全是癞子"))
                                game = new NumberHive(new ArrayList<>(), group, 1919810);
                            else
                                game = new NumberHive(new ArrayList<>(), group);
                            MiraiGamePlugin.INSTANCE.games.put(group.getId(), game);
                            MiraiGamePlugin.INSTANCE.addPlayer(group, sender, game);
                            group.sendMessage(new MessageChainBuilder()
                                    .append("新游戏创建成功，当前人数:1")
                                    .append(new QuoteReply(message))
                                    .build());
                        }
                    }*/
        }else if (at) {

                    String name = sender.getNameCard();
                    if(name.isEmpty()) name = sender.getNick();
                    MiraiGamePlugin.INSTANCE.gpt.request(group, message, name + ":" + pure);


        }
        //    break;
        //}
        // }
        return false;
    }

    public static boolean processFriend(Bot bot, User friend, MessageChain message) {
        //if(!admins.contains(friend.getId()))
        //    return;
        String text = message.contentToString();
        if (friend.getId() == 1273300377L) {
            if (text.contains("清理gpt")) {
                MiraiGamePlugin.INSTANCE.gpt.request = null;
                MiraiGamePlugin.INSTANCE.gpt.open = false;
                friend.sendMessage("已清理");
            }

            if (text.contains("添加积分")) {
                String regEx = "[^0-9]";
                Pattern p = Pattern.compile(regEx);
                String text1 = text.substring(0, text.indexOf("添加积分"));
                long id = Long.parseLong(text1);
                text = text.substring(text.indexOf("添加积分"));
                Matcher m = p.matcher(text);
                text = m.replaceAll("");
                DataPlayer data = Manager.getByID(id);
                if (data == null) {
                    friend.sendMessage(text1 + "用户不存在");
                    return true;
                }
                data.credit += Integer.parseInt(text);
                friend.sendMessage("添加积分成功");
                try {
                    Manager.save();
                } catch (IOException err) {
                    err.printStackTrace();
                }
            }
            if (text.contains("扣除积分")) {
                String regEx = "[^0-9]";
                Pattern p = Pattern.compile(regEx);
                String text1 = text.substring(0, text.indexOf("扣除积分"));
                long id = Long.parseLong(text1);
                text = text.substring(text.indexOf("扣除积分"));
                Matcher m = p.matcher(text);
                text = m.replaceAll("");
                DataPlayer data = Manager.getByID(id);
                if (data == null) {
                    friend.sendMessage(text1 + "用户不存在");
                    return true;
                }
                data.credit -= Integer.parseInt(text);
                data.credit = Math.max(data.credit, 0);
                friend.sendMessage("扣除积分成功");
                try {
                    Manager.save();
                } catch (IOException err) {
                    err.printStackTrace();
                }
            }
        }
        if ((text.startsWith(startChar) || text.startsWith("/"))) {
            if(!(friend instanceof Friend)) {
                return true;
            }
            Message result = Commands.process(friend, text, message);
            MessageChainBuilder builder = new MessageChainBuilder().append(new QuoteReply(message));
            if (result == null) {
                builder.append("未知的指令，请尝试使用/help获取帮助");
            } else {
                builder.append(result);
            }
            friend.sendMessage(builder.build());
            return true;
        }
        if (MiraiGamePlugin.INSTANCE.playerMap.containsKey(friend.getId())) {
            GamePlayer player = MiraiGamePlugin.INSTANCE.playerMap.get(friend.getId());
            for (Map.Entry<Long, HashSet<GamePlayer>> set : MiraiGamePlugin.INSTANCE.players_map.entrySet()) {
                HashSet<GamePlayer> players = set.getValue();
                if (players.contains(player)) {
                    IGame game = MiraiGamePlugin.INSTANCE.games.get(set.getKey());
                    if (!game.isWaiting()) {
                        Message msg = game.input(player, message, false, true);
                        if (msg != null) {
                            if(friend instanceof Friend) {
                                friend.sendMessage(msg);
                            }
                        }
                    }
                    return true;
                }
            }
        }
        return !(friend instanceof Friend);
    }

    public static boolean processFriend(Bot bot, User friend, MessageChain message, Group group) {
        //if(!admins.contains(friend.getId()))
        //    return;
        String text = message.contentToString();
        if (friend.getId() == 1273300377L) {
            if (text.contains("清理gpt")) {
                MiraiGamePlugin.INSTANCE.gpt.request = null;
                MiraiGamePlugin.INSTANCE.gpt.open = false;
                friend.sendMessage("已清理");
            }
            if (text.contains("添加积分")) {
                String regEx = "[^0-9]";
                Pattern p = Pattern.compile(regEx);
                String text1 = text.substring(0, text.indexOf("添加积分"));
                long id = Long.parseLong(text1);
                text = text.substring(text.indexOf("添加积分"));
                Matcher m = p.matcher(text);
                text = m.replaceAll("");
                DataPlayer data = Manager.getByID(id);
                if (data == null) {
                    friend.sendMessage(text1 + "用户不存在");
                    return true;
                }
                data.credit += Integer.parseInt(text);
                friend.sendMessage("添加积分成功");
                try {
                    Manager.save();
                } catch (IOException err) {
                    err.printStackTrace();
                }
            }
            if (text.contains("扣除积分")) {
                String regEx = "[^0-9]";
                Pattern p = Pattern.compile(regEx);
                String text1 = text.substring(0, text.indexOf("扣除积分"));
                long id = Long.parseLong(text1);
                text = text.substring(text.indexOf("扣除积分"));
                Matcher m = p.matcher(text);
                text = m.replaceAll("");
                DataPlayer data = Manager.getByID(id);
                if (data == null) {
                    friend.sendMessage(text1 + "用户不存在");
                    return true;
                }
                data.credit -= Integer.parseInt(text);
                data.credit = Math.max(data.credit, 0);
                friend.sendMessage("扣除积分成功");
                try {
                    Manager.save();
                } catch (IOException err) {
                    err.printStackTrace();
                }
            }
        }
        if ((text.startsWith(startChar) || text.startsWith("/"))) {
            if(!(group.getId() == 601623338L)) {
                return true;
            }
            Message result = Commands.process(friend, text, message);
            MessageChainBuilder builder = new MessageChainBuilder().append(new QuoteReply(message));
            if (result == null) {
                builder.append("未知的指令，请尝试使用/help获取帮助");
            } else {
                builder.append(result);
            }
            friend.sendMessage(builder.build());
            return true;
        }
        if (MiraiGamePlugin.INSTANCE.playerMap.containsKey(friend.getId())) {
            GamePlayer player = MiraiGamePlugin.INSTANCE.playerMap.get(friend.getId());
            for (Map.Entry<Long, HashSet<GamePlayer>> set : MiraiGamePlugin.INSTANCE.players_map.entrySet()) {
                HashSet<GamePlayer> players = set.getValue();
                if (players.contains(player)) {
                    IGame game = MiraiGamePlugin.INSTANCE.games.get(set.getKey());
                    if (!game.isWaiting()) {
                        Message msg = game.input(player, message, false, true);
                        if (msg != null) {
                            if(group.getId() == 601623338L) {
                                friend.sendMessage(msg);
                            }
                        }
                    }
                    return true;
                }
            }
        }
        return !(friend instanceof Friend);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static void asyncSendImage(Contact contact, byte[] baImage) {
        new Thread(() -> {
            ExternalResource resource = ExternalResource.create(baImage).toAutoCloseable();
            Contact.sendImage(contact, resource);
            //contact.sendMessage(contact.uploadImage(resource));
        }).start();
    }

}
