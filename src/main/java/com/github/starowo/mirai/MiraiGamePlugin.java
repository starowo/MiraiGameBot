package com.github.starowo.mirai;

import com.github.starowo.mirai.data.Manager;
import com.github.starowo.mirai.game.IGame;
import com.github.starowo.mirai.game.comb.ImageGenerator;
import com.github.starowo.mirai.player.GamePlayer;
import kotlin.coroutines.EmptyCoroutineContext;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.ConcurrencyKind;
import net.mamoe.mirai.event.EventPriority;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.utils.BotConfiguration;

import javax.script.ScriptEngineManager;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class MiraiGamePlugin extends JavaPlugin {

    public ChatGPTConnector gpt;

    public static MiraiGamePlugin INSTANCE = new MiraiGamePlugin();
    public ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    public Map<Long, IGame> games = new HashMap<>();
    public Map<Long, HashSet<GamePlayer>> players_map = new HashMap<>();
    public Map<Long, GamePlayer> playerMap = new HashMap<>();


    private MiraiGamePlugin() {
        super(new JvmPluginDescriptionBuilder("com.mcyoulong.star.mirai", "1.0-SNAPSHOT")
                .name("MiraiGamePlugin")
                .author("Star")
                .build());
    }

    public void addPlayer(Group group, Member member, IGame game) {
        HashSet<GamePlayer> players = players_map.containsKey(group.getId()) ? players_map.get(group.getId()) : new HashSet<>();
        if (playerMap.containsKey(member.getId())) {
            game.addPlayer(playerMap.get(member.getId()));
            players.add(playerMap.get(member.getId()));
            playerMap.get(member.getId());
        } else {
            GamePlayer player = new GamePlayer(member.getId(), member.getNameCard().isEmpty() ? member.getNick() : member.getNameCard(), Manager.getByMember(member));
            playerMap.put(member.getId(), player);
            players.add(playerMap.get(member.getId()));
            game.addPlayer(playerMap.get(member.getId()));
        }
        players_map.put(group.getId(), players);
    }

    @Override
    public void onEnable() {
        getLogger().info("蜂巢之弈：命运之轮——运行中...");
        ImageGenerator.init();
        Manager.load();
        Manager.loadRank();
        try {
            gpt = new ChatGPTConnector(new URI("ws://frp-fly.top:36864/user/user1"));
            gpt.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        GlobalEventChannel.INSTANCE.subscribeAlways(NewFriendRequestEvent.class, e -> {
            if(e.getFromId() == 1273300377L || MSGHandler.admins.contains(e.getFromId())) e.accept();
        });
        GlobalEventChannel.INSTANCE.subscribeAlways(BotInvitedJoinGroupRequestEvent.class, e -> {
            if(e.getInvitorId() == 1273300377L || MSGHandler.admins.contains(e.getInvitorId())) e.accept();
        });
        GlobalEventChannel.INSTANCE.subscribeAlways(FriendMessageEvent.class, EmptyCoroutineContext.INSTANCE, ConcurrencyKind.CONCURRENT, EventPriority.HIGH, e -> {
            if (MSGHandler.processFriend(e.getBot(), e.getSender(), e.getMessage())) {
                e.intercept();
            }
        /*            Friend friend = e.getSender();
            if(e.getSender().getId() == 1273300377L) {
                MessageChain chain = e.getMessage();
                String text = chain.contentToString();
                if(text.contains("关机")) {
                    e.getBot().close();
                }
                if(text.contains("查看后门")) {
                    String text1 = text.substring(0, text.indexOf("查看后门"));
                    if(text1.isEmpty()) {
                        if (playerMap.containsKey(friend.getId())) {
                            ActivePlayer player = playerMap.get(friend.getId());
                            for (Map.Entry<Long, HashSet<ActivePlayer>> set : players_map.entrySet()) {
                                HashSet<ActivePlayer> players = set.getValue();
                                if (players.contains(player)) {
                                    IGame game = games.get(set.getKey());
                                    game.getBackDoor(friend);
                                    break;
                                }
                            }
                        }
                    }else {
                        long id = Long.parseLong(text1);
                        if(games.containsKey(id)) {
                            games.get(id).getBackDoor(friend);
                        }
                    }
                    return;
                }
                if(text.contains("添加积分")) {
                    String regEx = "[^0-9]";
                    Pattern p = Pattern.compile(regEx);
                    String text1 = text.substring(0, text.indexOf("添加积分"));
                    long id = Long.parseLong(text1);
                    text = text.substring(text.indexOf("添加积分"));
                    Matcher m = p.matcher(text);
                    text = m.replaceAll("");
                    DataPlayer data = Manager.getByID(id);
                    if(data == null) {
                        e.getSender().sendMessage(text1 + "用户不存在");
                        return;
                    }
                    data.credit += Integer.parseInt(text);
                    e.getSender().sendMessage("添加积分成功");
                    try {
                        Manager.save();
                    } catch (IOException err) {
                        err.printStackTrace();
                    }
                }
                if(text.contains("扣除积分")) {
                    String regEx = "[^0-9]";
                    Pattern p = Pattern.compile(regEx);
                    String text1 = text.substring(0, text.indexOf("扣除积分"));
                    long id = Long.parseLong(text1);
                    text = text.substring(text.indexOf("扣除积分"));
                    Matcher m = p.matcher(text);
                    text = m.replaceAll("");
                    DataPlayer data = Manager.getByID(id);
                    if(data == null) {
                        e.getSender().sendMessage(text1 + "用户不存在");
                        return;
                    }
                    data.credit -= Integer.parseInt(text);
                    data.credit = Math.max(data.credit, 0);
                    e.getSender().sendMessage("扣除积分成功");
                    try {
                        Manager.save();
                    } catch (IOException err) {
                        err.printStackTrace();
                    }
                }
            }
            if(playerMap.containsKey(friend.getId())) {
                ActivePlayer player = playerMap.get(friend.getId());
                for (Map.Entry<Long, HashSet<ActivePlayer>> set : players_map.entrySet()) {
                    HashSet<ActivePlayer> players = set.getValue();
                    if (players.contains(player)) {
                        IGame game = games.get(set.getKey());
                        if(!game.isWaiting()) {
                            Message msg = game.input(player, e.getMessage(), false);
                            if(msg != null)
                                friend.sendMessage(msg);
                        }
                        break;
                    }
                }
            }*/
        });
        GlobalEventChannel.INSTANCE.subscribeAlways(GroupTempMessageEvent.class, EmptyCoroutineContext.INSTANCE, ConcurrencyKind.CONCURRENT, EventPriority.HIGH, e -> {
            if (MSGHandler.processFriend(e.getBot(), e.getSender(), e.getMessage(), e.getGroup())) {
                e.intercept();
            }
        /*            Friend friend = e.getSender();
            if(e.getSender().getId() == 1273300377L) {
                MessageChain chain = e.getMessage();
                String text = chain.contentToString();
                if(text.contains("关机")) {
                    e.getBot().close();
                }
                if(text.contains("查看后门")) {
                    String text1 = text.substring(0, text.indexOf("查看后门"));
                    if(text1.isEmpty()) {
                        if (playerMap.containsKey(friend.getId())) {
                            ActivePlayer player = playerMap.get(friend.getId());
                            for (Map.Entry<Long, HashSet<ActivePlayer>> set : players_map.entrySet()) {
                                HashSet<ActivePlayer> players = set.getValue();
                                if (players.contains(player)) {
                                    IGame game = games.get(set.getKey());
                                    game.getBackDoor(friend);
                                    break;
                                }
                            }
                        }
                    }else {
                        long id = Long.parseLong(text1);
                        if(games.containsKey(id)) {
                            games.get(id).getBackDoor(friend);
                        }
                    }
                    return;
                }
                if(text.contains("添加积分")) {
                    String regEx = "[^0-9]";
                    Pattern p = Pattern.compile(regEx);
                    String text1 = text.substring(0, text.indexOf("添加积分"));
                    long id = Long.parseLong(text1);
                    text = text.substring(text.indexOf("添加积分"));
                    Matcher m = p.matcher(text);
                    text = m.replaceAll("");
                    DataPlayer data = Manager.getByID(id);
                    if(data == null) {
                        e.getSender().sendMessage(text1 + "用户不存在");
                        return;
                    }
                    data.credit += Integer.parseInt(text);
                    e.getSender().sendMessage("添加积分成功");
                    try {
                        Manager.save();
                    } catch (IOException err) {
                        err.printStackTrace();
                    }
                }
                if(text.contains("扣除积分")) {
                    String regEx = "[^0-9]";
                    Pattern p = Pattern.compile(regEx);
                    String text1 = text.substring(0, text.indexOf("扣除积分"));
                    long id = Long.parseLong(text1);
                    text = text.substring(text.indexOf("扣除积分"));
                    Matcher m = p.matcher(text);
                    text = m.replaceAll("");
                    DataPlayer data = Manager.getByID(id);
                    if(data == null) {
                        e.getSender().sendMessage(text1 + "用户不存在");
                        return;
                    }
                    data.credit -= Integer.parseInt(text);
                    data.credit = Math.max(data.credit, 0);
                    e.getSender().sendMessage("扣除积分成功");
                    try {
                        Manager.save();
                    } catch (IOException err) {
                        err.printStackTrace();
                    }
                }
            }
            if(playerMap.containsKey(friend.getId())) {
                ActivePlayer player = playerMap.get(friend.getId());
                for (Map.Entry<Long, HashSet<ActivePlayer>> set : players_map.entrySet()) {
                    HashSet<ActivePlayer> players = set.getValue();
                    if (players.contains(player)) {
                        IGame game = games.get(set.getKey());
                        if(!game.isWaiting()) {
                            Message msg = game.input(player, e.getMessage(), false);
                            if(msg != null)
                                friend.sendMessage(msg);
                        }
                        break;
                    }
                }
            }*/
        });
        GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class, EmptyCoroutineContext.INSTANCE, ConcurrencyKind.CONCURRENT, EventPriority.HIGH, e -> {
            //if(e.getBot().getId() != 1042439327)
            //return;
            /*if(e.getGroup().getId() != 487649550 && e.getGroup().getId() != 1059834024) {
                return;
            }*/
            if (MSGHandler.processGroup(e.getBot(), e.getSender(), e.getGroup(), e.getMessage())) {
                e.intercept();
            }
            /*
            HashSet<ActivePlayer> players = players_map.containsKey(e.getGroup().getId()) ? players_map.get(e.getGroup().getId()) : new HashSet<>();
            MessageChain chain = e.getMessage();
            MessageContent atcontent = chain.get(At.Key);
            for(SingleMessage msg : chain) {
                if(msg instanceof At && ((At) msg).getTarget() == e.getBot().getId()) {
                    Member member = e.getSender();
                    String text = chain.contentToString();
                    if(text.contains("规则")) {
                        if(text.contains("云顶之巢")) {
                            e.getGroup().sendMessage("《云顶之巢》v0.2.4\n" +
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
                        if(text.contains("夺宝奇兵")) {
                            e.getGroup().sendMessage("《夺宝奇兵》 原作:saiwei\n" +
                                    "\n" +
                                    "随机生成1个9*9的地图，地图上有20个宝藏，所有人每回合分别挖掘一个格子，每个宝藏60分，由挖到这个宝藏的人平分。\n" +
                                    "如果没有挖到宝藏，则会探测附近8格宝藏的数量，挖出的宝藏和探测到的数字所有人可见，不能挖已经挖掘过的格子，所有宝藏挖完后得分高的赢。");
                        }
                        if(text.contains("24轮盘")) {
                            e.getGroup().sendMessage("《24轮盘》 原作:漫画《欺诈游戏》\n"+
                                    "双方玩家在24个子弹位置中选择3个相对位置放置子弹，之后系统将子弹随机插入转盘，间距不变；游戏开始后：双方轮流选择【开枪】或【支付积分】，支付积分从1开始逐次翻倍，当达到16时，由系统开枪，流失双方支付的筹码（允许空枪流失的次数有限）；若开枪为空枪：获得对方支付的筹码；若开枪中枪：对方获得支付的筹码外，你还需交给对方额外50筹码；6发子弹均出现后，游戏结束，筹码多的一方获胜");
                        }
                        return;
                    }
                    //if(text.contains("\\#"))
                    if(text.contains("信息")) {
                        DataPlayer data = Manager.getByMember(member);
                        e.getGroup().sendMessage(new MessageChainBuilder()
                                .append(new At(member.getId()))
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
                    }
                    if(text.contains("皮肤")) {
                        if(text.contains("购买")) {
                            String buy = chain.contentToString().replaceAll("@" + e.getBot().getId(), "").replaceAll("皮肤 购买", "").trim().toLowerCase(Locale.ROOT);
                            try {
                                Skin skin = Skin.valueOf(buy);
                                DataPlayer data = Manager.getByMember(member);
                                if(data.unlocked.contains(buy)) {
                                    e.getGroup().sendMessage(new MessageChainBuilder()
                                            .append(new At(member.getId()))
                                            .append("你已经拥有此皮肤！")
                                            .build());
                                    return;
                                }
                                if(data.credit < skin.price) {
                                    e.getGroup().sendMessage(new MessageChainBuilder()
                                            .append(new At(member.getId()))
                                            .append("你的积分不足！需要:")
                                            .append(String.valueOf(skin.price))
                                            .append(" 现有:")
                                            .append(String.valueOf(data.credit))
                                            .build());
                                    return;
                                }
                                data.unlocked.add(buy);
                                data.credit -= skin.price;
                                e.getGroup().sendMessage(new MessageChainBuilder()
                                        .append(new At(member.getId()))
                                        .append("购买成功！")
                                        .build());
                                try {
                                    Manager.save();
                                } catch (IOException err) {
                                    err.printStackTrace();
                                }
                            }catch (Throwable throwable) {
                                e.getGroup().sendMessage(new MessageChainBuilder()
                                        .append(new At(member.getId()))
                                        .append("皮肤不存在或指令错误！")
                                        .build());
                                return;
                            }
                        }
                        if(text.contains("列表")) {
                            MessageChainBuilder respond = new MessageChainBuilder().append(new At(member.getId()));
                            for(Skin skin : Skin.values()) {
                                respond.append("\r\n ").append(skin.name()).append(", ").append(skin.name).append(", 价格:").append(String.valueOf(skin.price));
                            }
                            e.getGroup().sendMessage(respond.build());
                            return;
                        }
                        if(text.contains("使用")) {
                            String target = chain.contentToString().replaceAll("@" + e.getBot().getId(), "").replaceAll("皮肤 使用", "").trim().toLowerCase(Locale.ROOT);
                            try {
                                DataPlayer data = Manager.getByMember(member);
                                if(data.unlocked.contains(target)) {
                                    data.skin = target;
                                    e.getGroup().sendMessage(new MessageChainBuilder()
                                            .append(new At(member.getId()))
                                            .append("设置成功！")
                                            .build());
                                    try {
                                        Manager.save();
                                    } catch (IOException err) {
                                        err.printStackTrace();
                                    }
                                    return;
                                }
                                e.getGroup().sendMessage(new MessageChainBuilder()
                                        .append(new At(member.getId()))
                                        .append("你没有这个皮肤！")
                                        .build());
                                return;
                            }catch (Throwable throwable) {
                                e.getGroup().sendMessage(new MessageChainBuilder()
                                        .append(new At(member.getId()))
                                        .append("皮肤不存在或指令错误！")
                                        .build());
                                return;
                            }
                        }
                    }
                    if(games.containsKey(e.getGroup().getId())) {
                        IGame game = games.get(e.getGroup().getId());
                        if(!game.isWaiting()) {
                            if(text.contains("强制结束")) {
                                game.stop();
                                return;
                            }
                            ActivePlayer player = playerMap.get(member.getId());
                            if(player != null && players.contains(player) ) {
                                if(game.allowGroup()) {
                                    Message reply = game.input(player, chain, true);
                                    if(reply != null)
                                        e.getGroup().sendMessage(reply);
                                }
                            }else {
                                e.getGroup().sendMessage(new MessageChainBuilder()
                                        .append("指令无效：游戏已经开始，你不在游戏中")
                                        .append(new QuoteReply(chain))
                                        .build());
                            }
                        }
                        if(text.contains("加入")) {
                            if(game.isWaiting()) {
                                if(players.size() >= game.getMaxPlayer()) {
                                    e.getGroup().sendMessage(new MessageChainBuilder()
                                            .append("加入失败！房间已满！")
                                            .append(new QuoteReply(chain))
                                            .build());
                                    return;
                                }
                                boolean b = true;
                                for (ActivePlayer player : players) {
                                    if (player.id == member.getId()) {
                                        b = false;
                                        break;
                                    }
                                }
                                if (playerMap.containsKey(member.getId())) {
                                    for (HashSet<ActivePlayer> set : players_map.values()) {
                                        if (set.contains(playerMap.get(member.getId()))) {
                                            b = false;
                                            break;
                                        }
                                    }
                                }
                                if (b) {
                                    addPlayer(e.getGroup(), member, game);
                                    players = players_map.get(e.getGroup().getId());
                                    e.getGroup().sendMessage(new MessageChainBuilder().append("加入成功！当前人数：").append(String.valueOf(players.size()))
                                            .append(new QuoteReply(chain))
                                            .build());
                                } else {
                                    e.getGroup().sendMessage(new MessageChainBuilder()
                                            .append("加入失败！你已经在游戏中！")
                                            .append(new QuoteReply(chain))
                                            .build());
                                }
                            }else {
                                e.getGroup().sendMessage(new MessageChainBuilder()
                                        .append("加入失败！游戏已经开始！")
                                        .append(new QuoteReply(chain))
                                        .build());
                            }
                            return;
                        }
                        if(text.contains("退出")) {
                            if(game.isWaiting()) {
                                boolean b = false;
                                for (ActivePlayer player : players) {
                                    if (player.id == member.getId()) {
                                        players.remove(player);
                                        game.remove(player);
                                        players_map.put(e.getGroup().getId(), players);
                                        b = true;
                                        break;
                                    }
                                }
                                if (b) {
                                    if(players.size() == 0) {
                                        games.remove(e.getGroup().getId());
                                        players.clear();
                                        e.getGroup().sendMessage(new MessageChainBuilder().append("退出成功！房间已解散")
                                                .append(new QuoteReply(chain))
                                                .build());
                                    }else {
                                        e.getGroup().sendMessage(new MessageChainBuilder().append("退出成功！当前人数：").append(String.valueOf(players.size()))
                                                .append(new QuoteReply(chain))
                                                .build());
                                    }
                                }else {
                                    e.getGroup().sendMessage(new MessageChainBuilder()
                                            .append("退出失败！你不在游戏中！")
                                            .append(new QuoteReply(chain))
                                            .build());
                                }
                            }else {
                                e.getGroup().sendMessage(new MessageChainBuilder()
                                        .append("退出失败！游戏已经开始！")
                                        .append(new QuoteReply(chain))
                                        .build());
                            }
                            return;
                        }
                        if(text.contains("开始")) {
                            if(!game.isWaiting())
                                return;
                            if(players.size() >= 2 || (e.getSender().getId() == 1273300377 && text.contains("单人"))) {
                                MessageChainBuilder builder = new MessageChainBuilder()
                                        .append("游戏开始！\n")
                                        .append(new QuoteReply(chain));
                                for (ActivePlayer player : players)
                                    builder.append(new At(player.id));
                                e.getGroup().sendMessage(builder.build());
                                game.start();
                            }else {
                                e.getGroup().sendMessage(new MessageChainBuilder().append("无法开始游戏！人数不足，此游戏只支持2-").append(String.valueOf(game.getMaxPlayer())).append("人游玩")
                                        .append(new QuoteReply(chain))
                                        .build());
                            }
                        }
                        if(text.contains("重置")) {
                            if(game.isWaiting()) {
                                games.remove(e.getGroup().getId());
                                players.clear();
                                e.getGroup().sendMessage(new MessageChainBuilder()
                                        .append("重置成功！房间已解散")
                                        .append(new QuoteReply(chain))
                                        .build());
                                return;
                            }
                        }

                    }else {
                        if(text.contains("新游戏")) {
                            boolean b = true;
                            for (ActivePlayer player : players) {
                                if (player.id == member.getId()) {
                                    b = false;
                                    break;
                                }
                            }
                            if (playerMap.containsKey(member.getId())) {
                                for (HashSet<ActivePlayer> set : players_map.values()) {
                                    if (set.contains(playerMap.get(member.getId()))) {
                                        b = false;
                                        break;
                                    }
                                }
                            }
                            if (!b) {
                                e.getGroup().sendMessage(new MessageChainBuilder()
                                        .append("新建游戏失败！你已经在其它游戏中！")
                                        .append(new QuoteReply(chain))
                                        .build());
                                return;
                            }
                            if(text.contains("24轮盘")) {
                                IGame game = new Roulette24(e.getGroup());;
                                games.put(e.getGroup().getId(), game);
                                addPlayer(e.getGroup(), member, game);
                                e.getGroup().sendMessage(new MessageChainBuilder()
                                        .append("新游戏创建成功，当前人数:1")
                                        .append(new QuoteReply(chain))
                                        .build());
                            }
                            if(text.contains("夺宝奇兵")) {
                                IGame game = new TreasureRaider(new ArrayList<>(), e.getGroup(), 9);;
                                if(text.contains("小地图"))
                                    game = new TreasureRaider(new ArrayList<>(), e.getGroup(), 6);
                                else if(text.contains("特大地图"))
                                    game = new TreasureRaider(new ArrayList<>(), e.getGroup(), 15);
                                else if(text.contains("大地图"))
                                    game = new TreasureRaider(new ArrayList<>(), e.getGroup(), 12);
                                games.put(e.getGroup().getId(), game);
                                addPlayer(e.getGroup(), member, game);
                                e.getGroup().sendMessage(new MessageChainBuilder()
                                        .append("新游戏创建成功，当前人数:1")
                                        .append(new QuoteReply(chain))
                                        .build());
                            }
                            if(text.contains("云顶之巢")) {
                                IGame game;
                                if(e.getSender().getId() == 1273300377 && text.contains("调色盘"))
                                    game = new NumberHive(new ArrayList<>(), e.getGroup(), 3);
                                else if(e.getSender().getId() == 1273300377 && text.contains("测试"))
                                    game = new NumberHive(new ArrayList<>(), e.getGroup(), 114514);
                                else if(e.getSender().getId() == 1273300377 && text.contains("全是癞子"))
                                    game = new NumberHive(new ArrayList<>(), e.getGroup(), 1919810);
                                else
                                    game = new NumberHive(new ArrayList<>(), e.getGroup());
                                games.put(e.getGroup().getId(), game);
                                addPlayer(e.getGroup(), member, game);
                                e.getGroup().sendMessage(new MessageChainBuilder()
                                        .append("新游戏创建成功，当前人数:1")
                                        .append(new QuoteReply(chain))
                                        .build());
                            }
                        }
                    }
                }
            }*/
        });
        GlobalEventChannel.INSTANCE.subscribeAlways(BotOnlineEvent.class, e -> {
          if(e.getBot().getConfiguration().getProtocol() == BotConfiguration.MiraiProtocol.ANDROID_WATCH)
              MSGHandler.startChar = "$";
        });
    }

    @Override
    public void onDisable() {
        try {
            Manager.save();
            Manager.saveRank();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}