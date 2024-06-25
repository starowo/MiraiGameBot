package com.github.starowo.mirai.command;

import com.google.common.collect.Lists;
import com.github.starowo.mirai.MiraiGamePlugin;
import com.github.starowo.mirai.data.DataPlayer;
import com.github.starowo.mirai.data.Manager;
import com.github.starowo.mirai.player.GamePlayer;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.io.IOException;
import java.util.*;

public class CommandPoints extends CommandBase {

    protected CommandPoints() {
        super("points", 0);
    }

    @Override
    public String[] getAliases() {
        return new String[]{"point", "pt", "积分"};
    }

    @Override
    public String getUsage() {
        return "积分相关指令";
    }

    @Override
    public Message process(User sender, String[] args, MessageChain msg) {
        if(args.length == 1) {
            if (args[0].equalsIgnoreCase("help")) {
                return new MessageChainBuilder()
                        .append("/pt help 查看帮助\n")
                        .append("/pt balance 查询自己的积分余额\n")
                        .append("/pt balance <QQ号/@目标> 查询指定目标的积分余额\n")
                        .append("/pt rank 查看积分排行榜\n")
                        .append("/pt transfer <QQ号/@目标> <数额> 向指定目标转账\n\n")
                        .build();
            }
            if (args[0].equalsIgnoreCase("帮助")) {
                return new MessageChainBuilder()
                        .append("/pt 帮助 查看帮助\n")
                        .append("/pt 余额 查询自己的积分余额\n")
                        .append("/pt 余额 <QQ号/@目标> 查询指定目标的积分余额\n")
                        .append("/pt 排行 查看积分排行榜\n")
                        .append("/pt 转账 <QQ号/@目标> <数额> 向指定目标转账\n\n")
                        .build();
            }
            if(args[0].equalsIgnoreCase("balance") || args[0].equals("余额")) {
                DataPlayer data;
                if (sender instanceof Member) {
                    data = Manager.getByMember((Member) sender);
                } else {
                    data = Manager.getByUser(sender);
                }
                return new MessageChainBuilder().append("您当前的积分余额为：").append(String.valueOf(data.credit)).build();
            }
            if(args[0].equalsIgnoreCase("rank") || args[0].equals("排行")) {
                DataPlayer data;
                if (sender instanceof Member) {
                    data = Manager.getByMember((Member) sender);
                } else {
                    data = Manager.getByUser(sender);
                }
                ArrayList<DataPlayer> list = Lists.newArrayList(Manager.map.values());
                list.sort((o1, o2) -> o2.credit - o1.credit);
                MessageChainBuilder builder = new MessageChainBuilder().append("积分排行榜：\n");
                for (int i = 0; i < 10; i++) {
                    builder.append(String.valueOf(i + 1)).append(".").append(list.get(i).name).append("(").append(String.valueOf(list.get(i).id)).append(") - ").append(String.valueOf(list.get(i).credit)).append("\n");
                }
                builder.append("...\n");
                builder.append("你的排名:").append(String.valueOf(list.indexOf(data) + 1)).append(" (").append(String.valueOf(data.credit)).append(")");
                return builder.build();
            }
        }
        if(args.length == 2) {
            if(args[0].equalsIgnoreCase("balance") || args[0].equals("余额")) {
                try {
                    long id = Long.parseLong(args[1].replaceAll("@", ""));
                    DataPlayer data = Manager.getByID(id);
                    if(data == null) {
                        return new MessageChainBuilder().append("该用户不存在").build();
                    }
                    return new MessageChainBuilder().append(data.name).append("当前的积分余额为：").append(String.valueOf(data.credit)).build();
                }catch (NumberFormatException exception) {
                    return new MessageChainBuilder().append("无效的目标").build();
                }
            }
        }
        if(args.length == 3) {
            if(args[0].equalsIgnoreCase("transfer") || args[0].equals("转账")) {
                try {
                    DataPlayer data0;
                    if (sender instanceof Member) {
                        data0 = Manager.getByMember((Member) sender);
                    } else {
                        data0 = Manager.getByUser(sender);
                    }
                    long id = Long.parseLong(args[1].replaceAll("@", ""));
                    DataPlayer data = Manager.getByID(id);
                    if(data == null) {
                        return new MessageChainBuilder().append("该用户不存在").build();
                    }
                    if(data == data0) {
                        return new MessageChainBuilder().append("不能给自己转账喔").build();
                    }
                    if (data0.level < 10 && data.level < 20) {
                        return new MessageChainBuilder().append("权限不足，转账需要您达到10级或转账目标达到20级").build();
                    }
                    int credits = Integer.parseInt(args[2]);
                    if(credits > data0.credit && data0.id != 2373664833L) {
                        return new MessageChainBuilder().append("你没有足够的积分").build();
                    }
                    if(credits < 0) {
                        data0.credit -= 500;
                        Manager.save();
                        return new MessageChainBuilder().append("抢劫是犯法的，罚你500积分").build();
                    }
                    if (credits == 0) {
                        return new MessageChainBuilder().append("不要转空气....").build();
                    }
                    if (MiraiGamePlugin.INSTANCE.playerMap.containsKey(sender.getId())) {
                        for (Map.Entry<Long, HashSet<GamePlayer>> entry : MiraiGamePlugin.INSTANCE.players_map.entrySet()) {
                            int limit = data0.creditInGame;
                            if(credits > data0.credit - limit) {
                                return new MessageChainBuilder().append(String.format("您已将%1s积分带入游戏，剩余积分不足以支付转账", limit)).build();
                            }
                        }
                    }
                    data0.credit -= credits;
                    data.credit += credits;
                    Manager.save();
                    return new MessageChainBuilder().append("成功向").append(data.name).append("转账").append(String.valueOf(credits)).append("积分").build();
                }catch (NumberFormatException exception) {
                    return new MessageChainBuilder().append("无效的目标").build();
                } catch (IOException e) {
                    return new MessageChainBuilder().append("系统繁忙，转账失败").build();
                }
            }
        }
        return new MessageChainBuilder().append("参数错误，使用 /pt help或/pt 帮助 获取帮助").build();
    }
}
