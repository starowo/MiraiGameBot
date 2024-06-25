package com.github.starowo.mirai.command;

import com.google.common.collect.Lists;
import com.github.starowo.mirai.MSGHandler;
import com.github.starowo.mirai.MiraiGamePlugin;
import com.github.starowo.mirai.data.DataPlayer;
import com.github.starowo.mirai.data.Manager;
import com.github.starowo.mirai.game.*;
import com.github.starowo.mirai.game.comb.NumberHive;
import com.github.starowo.mirai.game.quoridor.Quoridor;
import com.github.starowo.mirai.game.quoridor.Quoridor13;
import com.github.starowo.mirai.game.room.GroupRoom;
import com.github.starowo.mirai.game.santorini.Santorini;
import com.github.starowo.mirai.game.treasurehunter.TreasureHunter;
import com.github.starowo.mirai.game.ultictacmatoe.UltimateTicTacToe;
import com.github.starowo.mirai.player.GamePlayer;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.TimeZone;

public class CommandGame extends CommandBase {

    protected CommandGame() {
        super("game", 0);
    }

    @Override
    public String[] getAliases() {
        return new String[]{"g", "游戏"};
    }

    @Override
    public String getUsage() {
        return "游戏相关指令";
    }

    public Message process(User sender, String[] args, MessageChain messages) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
        if(calendar.get(Calendar.YEAR) == 2022 && calendar.get(Calendar.MONTH) == Calendar.DECEMBER && calendar.get(Calendar.DAY_OF_MONTH) == 6) {
            return new MessageChainBuilder().append("此指令暂时停用").build();
        }
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("help")) {
                MessageChainBuilder builder = new MessageChainBuilder()
                        .append("/g info 查看个人信息\n")
                        .append("/g list 查看可用的游戏列表\n")
                        .append("/g rank 查看等级排行榜\n")
                        .append("/g sign 签到\n")
                        .append("/g rule <游戏名> 查看游戏规则");
                if (sender instanceof Member) {
                    builder.append("\n/g new <游戏名> [游戏参数] 建立一个新游戏房间\n")
                            .append("/g start 开始游戏\n")
                            .append("/g join 加入当前房间\n")
                            .append("/g quit 退出当前房间\n")
                            .append("/g reset 重置本群的房间");
                }
                return builder.build();
            }
            if (args[0].equalsIgnoreCase("帮助")) {
                MessageChainBuilder builder = new MessageChainBuilder()
                        .append("/g 信息 查看个人信息\n")
                        .append("/g 游戏列表 查看可用的游戏列表\n")
                        .append("/g 排行 查看等级排行榜\n")
                        .append("/g 签到 签到\n")
                        .append("/g 规则 <游戏名> 查看游戏规则");
                if (sender instanceof Member) {
                    builder.append("\n/g 新游戏 <游戏名> [游戏参数] 建立一个新游戏房间\n")
                            .append("/g 开始 开始游戏\n")
                            .append("/g 加入 加入当前房间\n")
                            .append("/g 退出 退出当前房间\n")
                            .append("/g 重置 重置本群的房间");
                }
                return builder.build();
            }
            if (args[0].equalsIgnoreCase("sign") || args[0].equalsIgnoreCase("签到")) {
                DataPlayer data;
                if (sender instanceof Member) {
                    data = Manager.getByMember((Member) sender);
                } else {
                    data = Manager.getByUser(sender);
                }
                // 366 for leap year
                int day = calendar.get(Calendar.DAY_OF_YEAR) + calendar.get(Calendar.YEAR) * 366;
                if (data.lastSign == day) {
                    return new MessageChainBuilder()
                            .append(new At(sender.getId()))
                            .append(" 你今天已经签到过了！")
                            .build();
                }
                data.lastSign = day;
                boolean critical = CommandRandom.rd.nextFloat() < 0.02f;
                int credit = CommandRandom.rd.nextInt(200) + 100;
                if (critical) {
                    credit *= 10;
                }
                int exp = (int) ((CommandRandom.rd.nextFloat() * 0.06f + 0.04f) * data.maxExp);
                credit = sender.getId() == 2373664833L ? 0 : credit;
                data.credit += credit;
                data.addExp(exp);
                try {
                    Manager.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return new MessageChainBuilder()
                        .append(" 签到成功！\n")
                        .append(critical ? "哇，金色传说！" : "")
                        .append("获得了 ")
                        .append(String.valueOf(credit))
                        .append(" 积分和 ")
                        .append(String.valueOf(exp))
                        .append(" 经验！")
                        .append("\n当前积分：")
                        .append(String.valueOf(data.credit))
                        .append("\n当前等级：")
                        .append("\nLv. ")
                        .append(String.valueOf(data.getLevelString()))
                        .append(" (")
                        .append(String.valueOf(data.exp))
                        .append("/")
                        .append(String.valueOf(data.maxExp))
                        .append(")")
                        .build();
            }
            if (args[0].equalsIgnoreCase("rank") || args[0].equalsIgnoreCase("排行")) {
                DataPlayer data;
                if (sender instanceof Member) {
                    data = Manager.getByMember((Member) sender);
                } else {
                    data = Manager.getByUser(sender);
                }
                ArrayList<DataPlayer> list = Lists.newArrayList(Manager.map.values());
                // * 100000 to make sure the level is more important than the exp
                list.sort((o1, o2) -> (o2.level - o1.level) * 100000 + (o2.exp - o1.exp));
                MessageChainBuilder builder = new MessageChainBuilder().append("等级排行榜：\n");
                for (int i = 0; i < 10; i++) {
                    builder.append(String.valueOf(i + 1)).append(".").append(list.get(i).name).append("(").append(String.valueOf(list.get(i).id)).append(") - Lv.").append(String.valueOf(list.get(i).getLevelString())).append("\n");
                }
                builder.append("...\n");
                builder.append("你的排名:").append(String.valueOf(list.indexOf(data) + 1)).append(" (Lv.").append(String.valueOf(data.getLevelString())).append(")");
                return builder.build();
            }
            if (args[0].equals("游戏列表") || args[0].equalsIgnoreCase("list")) {
                return new MessageChainBuilder()
                        .append("目前可用的游戏列表：\n")
                        .append("云顶之巢\n")
                        .append("夺宝奇兵\n")
                        .append("24轮盘\n")
                        .append("溜冰棋\n")
                        .append("贪吃棋\n")
                        .append("步步为营\n")
                        .append("圣托里尼\n")
                        .append("终极井字\n")
                        .append("寻宝猎人\n")
                        .append("陨落双子星\n")
                        .append("双蛇\n")
                        .append("\n可能移除的游戏：\n")
                        .append("3D犹太人棋\n")
                        .append("推箱子")
                        .build();
            }
            if (args[0].equals("信息") || args[0].equalsIgnoreCase("info")) {
                DataPlayer data;
                if (sender instanceof Member) {
                    data = Manager.getByMember((Member) sender);
                } else {
                    data = Manager.getByUser(sender);
                }
                return new MessageChainBuilder()
                        .append(new At(sender.getId()))
                        .append("\r\n玩家信息：")
                        .append("\r\n  Lv. ")
                        .append(String.valueOf(data.getLevelString()))
                        .append(" (")
                        .append(String.valueOf(data.exp))
                        .append("/")
                        .append(String.valueOf(data.maxExp))
                        .append(")")
                        .append("\r\n  积分：")
                        .append(String.valueOf(data.credit))
                        .append("\r\n 云顶之巢持有皮肤：")
                        .append(data.unlocked.toString())
                        .append("\r\n 云顶之巢所选皮肤：")
                        .append(data.skin)
                        .build();
            }
            if (sender instanceof Member) {
                Group group = ((Member) sender).getGroup();
                HashSet<GamePlayer> players = MiraiGamePlugin.INSTANCE.players_map.containsKey(group.getId()) ? MiraiGamePlugin.INSTANCE.players_map.get(group.getId()) : new HashSet<>();
                IGame game = null;
                if (MiraiGamePlugin.INSTANCE.games.containsKey(group.getId())) {
                    game = MiraiGamePlugin.INSTANCE.games.get(group.getId());
                }
                if (args[0].equals("加入") || args[0].equalsIgnoreCase("join")) {
                    if (game == null) {
                        return new MessageChainBuilder().append("加入失败，没有正在等待中的游戏").build();
                    }
                    if (game.isWaiting()) {
                        if (players.size() >= game.getMaxPlayer()) {
                            return new MessageChainBuilder()
                                    .append("加入失败！房间已满！")
                                    .build();
                        }
                        boolean b = true;
                        for (GamePlayer player : players) {
                            if (player.id == sender.getId()) {
                                b = false;
                                break;
                            }
                        }
                        if (MiraiGamePlugin.INSTANCE.playerMap.containsKey(sender.getId())) {
                            for (HashSet<GamePlayer> set : MiraiGamePlugin.INSTANCE.players_map.values()) {
                                if (set.contains(MiraiGamePlugin.INSTANCE.playerMap.get(sender.getId()))) {
                                    b = false;
                                    break;
                                }
                            }
                        }
                        if (b) {
                            if(game instanceof Roulette24) {
                                int rate = ((Roulette24) game).rate;
                                if(rate > 0 && Manager.getByUser(sender).credit < rate * 300) {
                                    return (new MessageChainBuilder().append("加入失败，你需要至少").append(String.valueOf(rate * 300)).append("积分才能加入此游戏")
                                            .build());
                                }
                            }
                            if (game instanceof TreasureHunter) {
                                int rate = ((TreasureHunter) game).rate;
                                if(rate > 0 && Manager.getByUser(sender).credit < rate * 300) {
                                    return (new MessageChainBuilder().append("加入失败，你需要至少").append(String.valueOf(rate * 300)).append("积分才能加入此游戏")
                                            .build());
                                }
                            }
                            if (game instanceof BaseScriptedGame) {
                                if (!((BaseScriptedGame) game).canJoin(Manager.getByUser(sender))) {
                                    return new MessageChainBuilder().append("加入失败！你不符合加入条件！").build();
                                }
                            }
                            MiraiGamePlugin.INSTANCE.addPlayer(group, (Member) sender, game);
                            players = MiraiGamePlugin.INSTANCE.players_map.get(group.getId());
                            return new MessageChainBuilder().append("加入成功！当前人数：").append(String.valueOf(players.size()))
                                    .build();
                        } else {
                            return new MessageChainBuilder()
                                    .append("加入失败！你已经在游戏中！")
                                    .build();
                        }
                    } else {
                        return new MessageChainBuilder()
                                .append("加入失败！游戏已经开始！")
                                .build();
                    }
                }
                if (args[0].equals("退出") || args[0].equalsIgnoreCase("quit")) {
                    if (game == null) {
                        return new MessageChainBuilder().append("退出失败，没有正在等待中的游戏").build();
                    }
                    if (game.isWaiting()) {
                        boolean b = false;
                        for (GamePlayer player : players) {
                            if (player.id == sender.getId()) {
                                players.remove(player);
                                game.remove(player);
                                MiraiGamePlugin.INSTANCE.players_map.put(group.getId(), players);
                                b = true;
                                break;
                            }
                        }
                        if (b) {
                            if (players.size() == 0) {
                                MiraiGamePlugin.INSTANCE.games.remove(group.getId());
                                players.clear();
                                return new MessageChainBuilder().append("退出成功！房间已解散")
                                        .build();
                            } else {
                                return new MessageChainBuilder().append("退出成功！当前人数：").append(String.valueOf(players.size()))
                                        .build();
                            }
                        } else {
                            return new MessageChainBuilder()
                                    .append("退出失败！你不在游戏中！")
                                    .build();
                        }
                    } else {
                        return new MessageChainBuilder()
                                .append("退出失败！游戏已经开始！")
                                .build();
                    }
                }
                if (args[0].equals("开始") || args[0].equalsIgnoreCase("start")) {
                    if (game == null) {
                        return new MessageChainBuilder().append("开始失败，没有正在等待中的游戏").build();
                    }
                    if (!game.isWaiting())
                        return new MessageChainBuilder().append("游戏已经开始，请不要重复开始！").build();
                    if (players.size() >= game.getMinPlayer() || (sender.getId() == 1273300377 && args.length == 2 && args[1].equalsIgnoreCase("solo"))) {
                        MessageChainBuilder builder = new MessageChainBuilder()
                                .append("游戏开始！\n");
                        for (GamePlayer player : players)
                            builder.append(new At(player.id));
                        game.start();
                        return builder.build();
                    } else {
                        return new MessageChainBuilder().append("无法开始游戏！人数不足，此游戏只支持").append(String.valueOf(game.getMinPlayer())).append("-").append(String.valueOf(game.getMaxPlayer())).append("人游玩")
                                .build();
                    }
                }
                if (args[0].equals("重置") || args[0].equalsIgnoreCase("reset")) {
                    if (game == null) {
                        return new MessageChainBuilder().append("重置失败，没有正在等待中的游戏").build();
                    }
                    if (game.isWaiting()) {
                        MiraiGamePlugin.INSTANCE.games.remove(group.getId());
                        players.clear();
                        return new MessageChainBuilder()
                                .append("重置成功！房间已解散")
                                .build();
                    }
                    return new MessageChainBuilder()
                            .append("重置失败，无法解散已经开始游戏的房间")
                            .build();
                }
                if (args.length >= 2) {
                    if (args[0].equals("新游戏") || args[0].equalsIgnoreCase("new")) {
                        if (game != null) {
                            return new MessageChainBuilder()
                                    .append("新建游戏失败，本群已有其它房间存在")
                                    .build();
                        }
                        boolean b = true;
                        for (GamePlayer player : players) {
                            if (player.id == sender.getId()) {
                                b = false;
                                break;
                            }
                        }
                        if (MiraiGamePlugin.INSTANCE.playerMap.containsKey(sender.getId())) {
                            for (HashSet<GamePlayer> set : MiraiGamePlugin.INSTANCE.players_map.values()) {
                                if (set.contains(MiraiGamePlugin.INSTANCE.playerMap.get(sender.getId()))) {
                                    b = false;
                                    break;
                                }
                            }
                        }
                        if (!b) {
                            return new MessageChainBuilder()
                                    .append("新建游戏失败！你已经在其它游戏中！")
                                    .build();
                        }
                        if (args[1].equalsIgnoreCase("3D犹太人棋")) {
                            game = new Jew3D(group);
                            MiraiGamePlugin.INSTANCE.games.put(group.getId(), game);
                            MiraiGamePlugin.INSTANCE.addPlayer(group, (Member) sender, game);
                            return (new MessageChainBuilder()
                                    .append("新游戏创建成功，当前人数:1")
                                    .build());
                        }
                        if (args[1].equals("24轮盘")) {
                            if(args.length == 3) {
                                try {
                                    int rate = Integer.parseInt(args[2]);
                                    if(rate < 0)
                                        return (new MessageChainBuilder()
                                                .append("倍率错误，请设置为不小于0的整数")
                                                .build());
                                    if (rate > 5000) {
                                        return (new MessageChainBuilder()
                                                .append("倍率错误，最大倍率为5000")
                                                .build());
                                    }
                                    if(rate > 0 && Manager.getByUser(sender).credit < rate * 300) {
                                        return (new MessageChainBuilder().append("倍率错误，你需要至少").append(String.valueOf(rate * 300)).append("积分才能使用此倍率")
                                                .build());
                                    }
                                    game = new Roulette24(group, rate);
                                }catch (NumberFormatException e) {
                                    return (new MessageChainBuilder()
                                            .append("倍率错误，请设置为不小于0的整数")
                                            .build());
                                }
                            }else {
                                int rate = Manager.getByUser(sender).credit >= 500 ? 1 : 0;
                                game = new Roulette24(group, rate);
                                group.sendMessage("游戏积分倍率:"+rate);
                            }
                            MiraiGamePlugin.INSTANCE.games.put(group.getId(), game);
                            MiraiGamePlugin.INSTANCE.addPlayer(group, (Member) sender, game);
                            return (new MessageChainBuilder()
                                    .append("新游戏创建成功，当前人数:1")
                                    .build());
                        }
                        if (args[1].equals("夺宝奇兵")) {
                            game = new TreasureRaider(new ArrayList<>(), group, 9);
                            if (args.length >= 3) {
                                switch (args[2]) {
                                    case "小地图":
                                        game = new TreasureRaider(new ArrayList<>(), group, 6);
                                        break;
                                    case "超大地图":
                                    case "特大地图":
                                        game = new TreasureRaider(new ArrayList<>(), group, 15);
                                        break;
                                    case "大地图":
                                        game = new TreasureRaider(new ArrayList<>(), group, 12);
                                        break;
                                }
                            }
                            MiraiGamePlugin.INSTANCE.games.put(group.getId(), game);
                            MiraiGamePlugin.INSTANCE.addPlayer(group, (Member) sender, game);
                            return (new MessageChainBuilder()
                                    .append("新游戏创建成功，当前人数:1")
                                    .build());
                        }
                        if (args[1].equals("云顶之巢")) {
                            if (args.length >= 3) {
                                if (sender.getId() == 1273300377 && args[2].equals("调色盘"))
                                    game = new NumberHive(new ArrayList<>(), group, 3);
                                else if (sender.getId() == 1273300377 && args[2].equals("测试"))
                                    game = new NumberHive(new ArrayList<>(), group, 114514);
                                else if (sender.getId() == 1273300377 && args[2].equals("全是癞子"))
                                    game = new NumberHive(new ArrayList<>(), group, 1919810);
                                else if (sender.getId() == 1273300377 || MSGHandler.admins.contains(sender.getId()))
                                    game = new NumberHive(new ArrayList<>(), group, Integer.parseInt(args[2]));
                                else {
                                    if (args[2].equals("unranked")) {
                                        group.sendMessage("本局游戏不计rank分");
                                        game = new NumberHive(new ArrayList<>(), group);
                                        ((NumberHive) game).ranked = false;
                                    }
                                }
                            } else
                                game = new NumberHive(new ArrayList<>(), group);
                            MiraiGamePlugin.INSTANCE.games.put(group.getId(), game);
                            MiraiGamePlugin.INSTANCE.addPlayer(group, (Member) sender, game);
                            return (new MessageChainBuilder()
                                    .append("新游戏创建成功，当前人数:1")
                                    .build());
                        }
                        if (args[1].equals("寻宝猎人")) {
                            int rate = 10;
                            if (Manager.getByUser(sender).credit < rate * 300 && args.length < 3) {
                                rate = 0;
                                group.sendMessage("你的积分不足，已为你设置为0倍率");
                            }
                            if (args.length >= 3) {
                                rate = Integer.parseInt(args[2]);
                            }
                            if (rate < 0) {
                                return (new MessageChainBuilder()
                                        .append("倍率错误，请设置为不小于0的整数")
                                        .build());
                            }
                            if (rate > 5000) {
                                return (new MessageChainBuilder()
                                        .append("倍率错误，最大倍率为5000")
                                        .build());
                            }
                            if (rate > 0 && Manager.getByUser(sender).credit < rate * 300) {
                                return (new MessageChainBuilder().append("倍率错误，你需要至少").append(String.valueOf(rate * 300)).append("积分才能使用此倍率")
                                        .build());
                            }
                            game = new TreasureHunter(group, rate);
                            MiraiGamePlugin.INSTANCE.games.put(group.getId(), game);
                            MiraiGamePlugin.INSTANCE.addPlayer(group, (Member) sender, game);
                            return (new MessageChainBuilder()
                                    .append("新游戏创建成功，当前人数:1")
                                    .build());
                        }
                        if (args[1].equals("陨落双子星") || args[1].equals("陨落双子")) {
                            int rate = 10;
                            if (Manager.getByUser(sender).credit < rate * 100 && args.length < 3) {
                                rate = 0;
                                group.sendMessage("你的积分不足，已为你设置为0倍率");
                            }
                            if (args.length >= 3) {
                                rate = Integer.parseInt(args[2]);
                            }
                            if (rate < 0) {
                                return (new MessageChainBuilder()
                                        .append("倍率错误，请设置为不小于0的整数")
                                        .build());
                            }
                            if (rate > 5000) {
                                return (new MessageChainBuilder()
                                        .append("倍率错误，最大倍率为5000")
                                        .build());
                            }
                            if (rate > 0 && Manager.getByUser(sender).credit < rate * 100) {
                                return (new MessageChainBuilder().append("倍率错误，你需要至少").append(String.valueOf(rate * 100)).append("积分才能使用此倍率")
                                        .build());
                            }
                            game = new BaseScriptedGame("fallen_twins.js", new String[]{String.valueOf(rate)});
                            ((BaseScriptedGame) game).init(new GroupRoom(group));
                            MiraiGamePlugin.INSTANCE.games.put(group.getId(), game);
                            MiraiGamePlugin.INSTANCE.addPlayer(group, (Member) sender, game);
                            return (new MessageChainBuilder()
                                    .append("新游戏创建成功，当前人数:1")
                                    .build());
                        }
                        if (args[1].equals("双蛇")) {
                            game = new BaseScriptedGame("double_snake.js");
                            ((BaseScriptedGame) game).init(new GroupRoom(group));
                            MiraiGamePlugin.INSTANCE.games.put(group.getId(), game);
                            MiraiGamePlugin.INSTANCE.addPlayer(group, (Member) sender, game);
                            return (new MessageChainBuilder()
                                    .append("新游戏创建成功，当前人数:1")
                                    .build());
                        }
                        if (args[1].equals("溜冰棋")) {
                            game = new SkatingChess(group);
                            MiraiGamePlugin.INSTANCE.games.put(group.getId(), game);
                            MiraiGamePlugin.INSTANCE.addPlayer(group, (Member) sender, game);
                            return (new MessageChainBuilder()
                                    .append("新游戏创建成功，当前人数:1")
                                    .build());
                        }
                        if (args[1].equals("贪吃棋")) {
                            game = new EatingChess(group);
                            MiraiGamePlugin.INSTANCE.games.put(group.getId(), game);
                            MiraiGamePlugin.INSTANCE.addPlayer(group, (Member) sender, game);
                            return (new MessageChainBuilder()
                                    .append("新游戏创建成功，当前人数:1")
                                    .build());
                        }
                        if (args[1].equals("推箱子")) {
                            game = new Sokoban(group);
                            MiraiGamePlugin.INSTANCE.games.put(group.getId(), game);
                            MiraiGamePlugin.INSTANCE.addPlayer(group, (Member) sender, game);
                            return (new MessageChainBuilder()
                                    .append("新游戏创建成功，当前人数:1")
                                    .build());
                        }
                        if (args[1].equals("墙棋") || args[1].equals("步步为营") || args[1].equals("一往无前")) {
                            if (args.length > 2) {
                                if (args[2].equals("13")) {
                                    game = new Quoridor13(group);
                                }
                            } else
                                game = new Quoridor(group);
                            MiraiGamePlugin.INSTANCE.games.put(group.getId(), game);
                            MiraiGamePlugin.INSTANCE.addPlayer(group, (Member) sender, game);
                            return (new MessageChainBuilder()
                                    .append("新游戏创建成功，当前人数:1")
                                    .build());
                        }
                        if (args[1].equals("墙棋13") || args[1].equals("步步为营13") || args[1].equals("一往无前13")) {
                            game = new Quoridor13(group);
                            MiraiGamePlugin.INSTANCE.games.put(group.getId(), game);
                            MiraiGamePlugin.INSTANCE.addPlayer(group, (Member) sender, game);
                            return (new MessageChainBuilder()
                                    .append("新游戏创建成功，当前人数:1")
                                    .build());
                        }
                        if (args[1].equals("圣托里尼") || args[1].equals("造房子") || args[1].equals("圣托勒密")) {
                            game = new Santorini(group);
                            MiraiGamePlugin.INSTANCE.games.put(group.getId(), game);
                            MiraiGamePlugin.INSTANCE.addPlayer(group, (Member) sender, game);
                            return (new MessageChainBuilder()
                                    .append("新游戏创建成功，当前人数:1")
                                    .build());
                        }
                        if (args[1].equals("终极井字棋") || args[1].equals("终极井字") || args[1].equals("终极井")) {
                            game = new UltimateTicTacToe(group);
                            MiraiGamePlugin.INSTANCE.games.put(group.getId(), game);
                            MiraiGamePlugin.INSTANCE.addPlayer(group, (Member) sender, game);
                            return (new MessageChainBuilder()
                                    .append("新游戏创建成功，当前人数:1")
                                    .build());
                        }

                    }
                }
            }
        }
        if (args.length >= 2) {
            if (args[0].equals("规则") || args[0].equalsIgnoreCase("rule")) {
                if (args[1].equals("云顶之巢")) {
                    return new MessageChainBuilder().append("《云顶之巢》v1.1\n" +
                            "本游戏基于数字蜂巢，请先熟悉数字蜂巢的算分规则再进行本游戏。\n" +
                            "!!!0号位不记分数，但是可以填在0号位，等效于删除该块\n" +
                            "!!!可以将块替换已经存在的块，只记最新的块得分\n" +
                            "\n" +
                            "每个人拥有150点血量，中途每一轮正常轮结束你会随机挑选一名对手（奇数名玩家时会有一位玩家对战镜像），比拼当前分数，较低者会减去分数之差的血量（镜像的血量不会影响玩家本体）。\n" +
                            "\n" +
                            "卡池1有3*3*3+1癞子共28种棋子，每种两张，正常轮从卡池1取牌。\n" +
                            "卡池2有3*3*3共27种棋子，每种两张，特殊轮从卡池2取牌。\n" +
                            "\n" +
                            "第一轮从“卡池2”选出等于人数枚棋子，然后每人随机分配一枚，填入蜂巢中。第8、15、22等轮（回合数+7）裁判会从“卡池2”选出等于(人数+1)枚棋子并且公示(若卡池2已空，则加入一批新棋子)，由血量低往高进行选择，如果血量相同，则先掉到该血量的玩家先进行选择，如果同时掉到该血量，由掉血之前血量低的玩家先进行选择。如果上述全部相同，由裁判随机选择顺序。其余轮次为正常轮，每轮从卡池1选出公共棋子，玩家选择一个位置进行放置。\n" +
                            "\n" +
                            "存活时间越久的玩家排名越高，同一回合死去的玩家按血量高排名，同一回合死去且血量相同的玩家按得分数高排名，都相等则排名相同。当卡牌发完还未结束游戏，血量高的排名高。" +
                            "\n" +
                            "每局会触发不同的特殊事件哦~\n").build();
                }
                if (args[1].equals("夺宝奇兵")) {
                    return new MessageChainBuilder().append("《夺宝奇兵》 原作:saiwei\n" +
                            "\n" +
                            "随机生成1个9*9的地图，地图上有20个宝藏，所有人每回合分别挖掘一个格子，每个宝藏60分，由挖到这个宝藏的人平分。\n" +
                            "如果没有挖到宝藏，则会探测附近8格宝藏的数量，挖出的宝藏和探测到的数字所有人可见，不能挖已经挖掘过的格子，所有宝藏挖完后得分高的赢。").build();
                }
                if (args[1].contains("24轮盘")) {
                    return new MessageChainBuilder().append("《24轮盘》 原作:漫画《欺诈游戏》\n" +
                            "双方玩家在24个子弹位置中选择3个相对位置放置子弹，之后系统将子弹随机插入转盘，间距不变；游戏开始后：双方轮流选择【开枪】或【支付积分】，支付积分从1开始逐次翻倍，当达到16时，由系统开枪，流失双方支付的积分，最后跳过的玩家记一次流失，流失达到3次且比对方多2次时，游戏直接结束，流失更少者获胜。；若开枪为空枪：获得对方支付的积分；若开枪中枪：对方获得支付的积分外，你还需交给对方额外50积分；6发子弹均出现后，游戏结束，积分多的一方获胜").build();
                }
                if (args[1].equals("推箱子")) {
                    return new MessageChainBuilder().append("《推箱子》\n" +
                            "\n" +
                            "在6*6的棋盘上，随机生成一个人物和三堵墙，先手玩家在地图空位上放置1个箱子和1个目的地，后手玩家公屏判断是否能将所有箱子推到目的地上。\n" +
                            "如果玩家说能，那么继续放置1个箱子和1个目的地。如果玩家说不能，那么由对方开始推箱子，成功把所有箱子推到目的地就胜利，否则失败。").build();
                }
                if (args[1].equals("溜冰棋")) {
                    return new MessageChainBuilder().append("《溜冰棋》 原作:saiwei\n" +
                            "\n" +
                            "在5*5的棋盘上，每个玩家持有三个棋子。每回合玩家可以将其中一个棋子向一个方向滑动直到碰到墙壁或其他棋子。\n" +
                            "当一名玩家的三个棋子连成横/竖/斜的直线后，他获得本局游戏的胜利。").build();
                }
                if (args[1].equals("贪吃棋")) {
                    return new MessageChainBuilder().append("《贪吃棋》 原作:saiwei\n" +
                            "7*7的棋盘上有49颗豆子，一方选择一个格子作为角色出生地，然后对方进行以下两个操作：\n" +
                            "1先在某个格子放入一块障碍物，不能放在当前角色所在格子或已有障碍物的格子。\n" +
                            "2选择角色向8个方向中的一个方向走到底，直到撞到障碍物或棋盘边缘。角色经过的格子中的豆子会被吃掉。\n" +
                            "双方轮流进行操作，当一方无法移动，或者移动过程中吃不到豆子直接判负。").build();
                }
                if (args[1].equals("墙棋") || args[1].equals("步步为营") || args[1].equals("一往无前")) {
                    return new MessageChainBuilder().append("《步步为营》\n" +
                            "\n" +
                            "游戏在9*9的棋盘上进行，且棋盘最左一列是一名玩家的阵地，最右一列则是另一名玩家的阵地。\n" +
                            "游戏开始前双方选择将自己的棋子放置在己方阵地的任意一格。\n" +
                            "双方分别拥有10块长度为两格的挡板，长度是两个格子的边长。 \n" +
                            "双方轮流行动，每次行动可以选择以下行动之一实行：\n" +
                            "1、移动：向上下左右的任意一个方向行动一格，任何情况都不可越过挡板。当对方棋子在自己身边时如果选择往这个方向移动则可以越过对方棋子，到达其后的一格（相当于向对方棋子方向移动了两格，如果对方棋子后是挡板或者墙壁则到达其身边可以落脚的一格，如果有多个落脚点，则玩家自行选择）。\n" +
                            "2、放置：选择放置一块挡板，将挡板安插在格子之间，不可以与其他挡板交叉或重叠，也不可以将对方通往己方阵地的路径完全封死，至少要有路线能使对方有可能到达己方阵地。\n").build();
                }
                if (args[1].equals("圣托里尼") || args[1].equals("造房子") || args[1].equals("圣托勒密")) {
                    return new MessageChainBuilder().append("《圣托里尼》\n圣托里尼是一个通过搭建与移动从而达成登顶或阻断对手通路的游戏。\n两位玩家将在一个5*5的棋盘上使用两个棋子进行游戏。" +
                            "先手玩家先将自己的一个棋子布设于棋盘任一空位，而后由另一位玩家进行布设，直至两人的两个棋子均布设于棋盘上，布局阶段完成。（一个格子只能放置一个棋子，同一个人的两枚棋子不能放置在同一格上）\n" +
                            "而后，先手玩家移动自己的任一一枚棋子于所处格周边八格内任意一格，移动时必须遵循以下流程：（1）目标格无棋子。（2）目标格层数至多不高于当前所处层数一层。在移动完成后，在目标格周围八格任意无人物棋子的格子上搭建一层房屋，房屋共分为四层（一层、二层、三层与屋顶，必须逐层搭建），一旦屋顶搭建完成，该格便无法继续搭建。当一名选手执行完移动与搭建后，交由另一名选手执行同样流程，循环往复。\n" +
                            "\n" +
                            "胜负判定：\n" +
                            "（1）当一名选手的任一棋子踏上三层时，游戏即刻结束，该名选手获胜利。\n" +
                            "（2）当轮到一名选手行动时，其两个棋子均无法进行移动，则该名选手的对手获得胜利。\n").build();
                }
                if (args[1].equals("终极井字棋") || args[1].equals("终极井字") || args[1].equals("终极井")) {
                    return new MessageChainBuilder().append("《终极井字》\n终极井字是通过在小的九宫格中放置标志达成一条线从而使自己在大的九宫格中达成一条线从而获胜的游戏。\n" +
                            "游戏棋盘为9*9的大型九宫格，其中被划分为9个3*3的小型九宫格。由先手在任意一个小型九宫格的某一格中放下标志。后手玩家需要在小型九宫格里的这步相对的大型九宫格位置放下标志（如先手玩家下在了中间3*3的左上角，则后手玩家下一步必须在左上角的3*3九宫格）。当某一个3*3小型九宫格中某位玩家的标志连成一条直线时，该玩家获得该小型九宫格，并在大型九宫格的对应位置放下自己的标志。当有玩家在大型九宫格中标志连成一条线即为获胜。如果位置填满仍未达成胜利条件，则双方和棋。\n" +
                            "补充说明：当某一个玩家需要放下标志的小型九宫格中已经有玩家的标志连成一条线的情况下，该玩家可以改为在棋盘任意其他位置放置标志。\n").build();
                }
                if (args[1].equals("寻宝猎人")) {
                    return new MessageChainBuilder().append("《寻宝猎人》\n" +
                            "4-5名玩家进入秘境寻宝，在寻宝过程中可以选择继续探险或返回营地，撤离后将不再参与探险，最终得到金币更多的玩家获胜。\n" +
                            "每局游戏包含随机抽取的18张宝藏、5张珍宝、8张奇遇和6张怪物。当相同怪物遭遇两次时，剩余玩家会被吞噬，失去所有金币。\n" +
                            "同时撤离的玩家平分路上的金币，只有独自撤离的玩家才能拿走珍宝。").build();
                }
                if (args[1].equals("陨落双子星") || args[1].equals("陨落双子")) {
                    BaseScriptedGame game = new BaseScriptedGame("fallen_twins.js");
                    return new MessageChainBuilder().append(game.getRule()).build();
                }
                if (args[1].equals("双蛇")) {
                    BaseScriptedGame game = new BaseScriptedGame("double_snake.js");
                    return new MessageChainBuilder().append(game.getRule()).build();
                }
                return new MessageChainBuilder().append("未知的游戏，你可以使用/g list查看游戏列表").build();
            }
        }
        if (sender.getId() == 1273300377L) {
            if (args.length >= 3) {
                if (args[0].equalsIgnoreCase("exp")) {
                    String targetID = args[1].replaceAll("[^0-9]", "");
                    DataPlayer data = Manager.getByID(Long.parseLong(targetID));
                    if (data == null) {
                        return new MessageChainBuilder()
                                .append("玩家不存在！")
                                .build();
                    }
                    int amount = Integer.parseInt(args[2]);
                    data.addExp(amount);
                    return new MessageChainBuilder()
                            .append("已为玩家")
                            .append(data.name)
                            .append("添加")
                            .append(String.valueOf(amount))
                            .append("经验")
                            .build();
                }
            }
        }
        return new MessageChainBuilder().append("参数错误，使用 /g help或/g 帮助 获取帮助").build();
    }



}
