package com.github.starowo.mirai.command;

import com.google.common.collect.Lists;
import com.github.starowo.mirai.MSGHandler;
import com.github.starowo.mirai.data.DataPlayer;
import com.github.starowo.mirai.data.Manager;
import com.github.starowo.mirai.data.PlayerRank;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CommandRank extends CommandBase {

    public static final HashSet<String> GAMELIST = new HashSet<>();
    {
        GAMELIST.add("云顶之巢");
        GAMELIST.add("夺宝奇兵");
        GAMELIST.add("24轮盘");
        GAMELIST.add("溜冰棋");
        GAMELIST.add("贪吃棋");
        GAMELIST.add("步步为营");
        GAMELIST.add("圣托里尼");
        GAMELIST.add("终极井字棋");
        GAMELIST.add("陨落双子星");
        GAMELIST.add("双蛇");
    }

    protected CommandRank() {
        super("rank", 0);
    }

    @Override
    public String[] getAliases() {
        return new String[]{"r", "排位"};
    }

    @Override
    public String getUsage() {
        return "排位相关指令";
    }

    @Override
    public Message process(User sender, String[] args, MessageChain msg) {
        if(args.length == 0) {
            PlayerRank data = Manager.getRank(sender.getId());
            if(data == null || data.scores.isEmpty()) {
                return new MessageChainBuilder().append("您在此bot中还没有信息").build();
            }
            if(!data.banned())
                return new MessageChainBuilder().append("您的排位信息如下：\n").append(data.getRanks()).build();
            return new MessageChainBuilder().append("您的排位信息已被封禁").build();
        }
        if(args.length == 1) {
            if (args[0].equalsIgnoreCase("help")) {
                return new MessageChainBuilder()
                        .append("/r 查询自己的rank分\n")
                        .append("/r help 查看帮助\n")
                        //.append("/r <游戏名> 查询自己指定游戏的rank分\n")
                        .append("/r rank <游戏名> 查看指定游戏的排行榜\n")
                        //.append("/r <QQ号/@目标> <游戏名>\n")
                        .build();
            }
            if (args[0].equalsIgnoreCase("帮助")) {
                return new MessageChainBuilder()
                        .append("/r 查询自己的rank分\n")
                        .append("/r 帮助 查看帮助\n")
                        //.append("/r <游戏名> 查询自己指定游戏的rank分\n")
                        .append("/r 排行 <游戏名> 查看指定游戏的排行榜\n")
                        .build();
            }
        }
        if(args.length == 2) {
            if(args[0].equalsIgnoreCase("rank") || args[0].equals("排行")) {
                if(!GAMELIST.contains(args[1]))
                    return new MessageChainBuilder().append("该游戏暂无排行").build();
                PlayerRank data = Manager.getRank(sender.getId());
                List<PlayerRank> list = Lists.newArrayList(Manager.rank.values()).stream().filter(player -> player.scores.containsKey(args[1]) && !player.banned()).collect(Collectors.toList());
                if(list.isEmpty())
                    return new MessageChainBuilder().append("该游戏暂无排行").build();
                list.sort((o1, o2) -> o2.scores.get(args[1]) - o1.scores.get(args[1]));
                MessageChainBuilder builder = new MessageChainBuilder().append(args[1]).append("排行榜：\n");
                for (int i = 0; i < Math.min(10, list.size()); i++) {
                    builder.append(String.valueOf(i + 1)).append(".").append(Manager.getByID(list.get(i).id).name).append("(").append(String.valueOf(list.get(i).id)).append(") - ").append(String.valueOf(list.get(i).scores.get(args[1]))).append("\n");
                }
                builder.append("...\n");
                if(list.contains(data))
                    builder.append("你的排名:").append(String.valueOf(list.indexOf(data) + 1)).append(" (").append(String.valueOf(data.scores.get(args[1]))).append(")");
                return builder.build();
            }
            if(MSGHandler.admins.contains(sender.getId()) || sender.getId() == 1273300377L) {
                if (args[0].equalsIgnoreCase("ban")) {
                    long id = Long.parseLong(args[1].replaceAll("@", ""));
                    DataPlayer data = Manager.getByID(id);
                    if (data == null) {
                        return new MessageChainBuilder().append("该用户不存在").build();
                    }
                    Manager.ban.add(id);
                    try {
                        Manager.saveRank();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return new MessageChainBuilder().append(String.format("已封禁玩家 %1s 的排位信息", data.name)).build();
                }
                if (args[0].equalsIgnoreCase("unban")) {
                    long id = Long.parseLong(args[1].replaceAll("@", ""));
                    DataPlayer data = Manager.getByID(id);
                    if (data == null) {
                        return new MessageChainBuilder().append("该用户不存在").build();
                    }
                    if (Manager.ban.remove(id)) {
                        try {
                            Manager.saveRank();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return new MessageChainBuilder().append(String.format("已解封玩家 %1s 的排位信息", data.name)).build();
                    }
                    return new MessageChainBuilder().append(String.format("玩家 %1s 没有被封禁", data.name)).build();
                }
            }
        }
        if(args.length == 4) {
            if(MSGHandler.admins.contains(sender.getId()) || sender.getId() == 1273300377L) {
                if (args[0].equalsIgnoreCase("add")) {
                    long id = Long.parseLong(args[1].replaceAll("@", ""));
                    String game = args[2];
                    int score = Integer.parseInt(args[3]);
                    PlayerRank data = Manager.getRank(id);
                    if (data == null) {
                        return new MessageChainBuilder().append("该用户不存在").build();
                    }
                    data.scores.put(game, data.scores.getOrDefault(game, 1200) + score);
                    try {
                        Manager.saveRank();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return new MessageChainBuilder().append("修改成功").build();
                }
            }
        }
        return new MessageChainBuilder().append("参数错误，使用 /r help或/r 帮助 获取帮助").build();
    }

}
