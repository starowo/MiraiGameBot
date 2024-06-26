package com.github.starowo.mirai.command;

import com.github.starowo.mirai.PluginConfiguration;
import com.google.common.collect.Lists;
import com.github.starowo.mirai.MSGHandler;
import com.github.starowo.mirai.data.DataPlayer;
import com.github.starowo.mirai.data.Manager;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.io.IOException;
import java.util.ArrayList;

public class CommandAdmin extends CommandBase {

    protected CommandAdmin() {
        super("admin", 1);
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public String getUsage() {
        return "管理员指令";
    }

    @Override
    public Message process(User sender, String[] args, MessageChain messages) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("pointsMonitor")) {
                ArrayList<DataPlayer> list = Lists.newArrayList(Manager.map.values());
                list.sort((o1, o2) -> o2.credit - o1.credit);
                int sum10 = 0;
                for (int i = 0; i < 10; i++) {
                    sum10 += list.get(i).credit;
                }
                int sum20 = sum10;
                for (int i = 10; i < 20; i++) {
                    sum20 += list.get(i).credit;
                }
                MessageChainBuilder builder = new MessageChainBuilder();
                builder.append("top10总积分：").append(String.valueOf(sum10)).append("\n");
                builder.append("top20总积分：").append(String.valueOf(sum20)).append("\n");
                builder.append("人均积分 - top10: ").append(String.valueOf(sum10 / 10)).append("\n");
                builder.append("人均积分 - top20: ").append(String.valueOf(sum20 / 20)).append("\n");
                return builder.build();
            }
            if (args[0].equalsIgnoreCase("addWhiteList")) {
                if (args.length == 1) {
                    MSGHandler.whitelist.add(((Member) sender).getGroup().getId());
                    try {
                        Manager.save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return new MessageChainBuilder().append("已将本群添加到白名单").build();
                } else {
                    MSGHandler.whitelist.add(Long.parseLong(args[1]));
                    try {
                        Manager.save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return new MessageChainBuilder().append("已将").append(args[1]).append("添加到白名单").build();
                }
            }
            if (args[0].equalsIgnoreCase("delWhiteList")) {
                if (args.length == 1) {
                    MSGHandler.whitelist.remove(((Member) sender).getGroup().getId());
                    try {
                        Manager.save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return new MessageChainBuilder().append("已将本群移除白名单").build();
                } else {
                    MSGHandler.whitelist.remove(Long.parseLong(args[1]));
                    try {
                        Manager.save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return new MessageChainBuilder().append("已将").append(args[1]).append("移除白名单").build();
                }
            }
            if (args[0].equalsIgnoreCase("whitelist")) {
                MessageChainBuilder builder = new MessageChainBuilder();
                builder.append("白名单群列表:");
                for (Long l : MSGHandler.whitelist) {
                    builder.append("\n").append(String.valueOf(l));
                }
                return builder.build();
            }
            if (args[0].equalsIgnoreCase("shutdown")) {
                new Thread(() -> {
                    try {
                        Thread.sleep(3000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    sender.getBot().close();
                }).start();
                return new MessageChainBuilder().append("机器人将在三秒后关机").build();
            }
            if (args[0].equalsIgnoreCase("op")) {
                if (sender.getId() == PluginConfiguration.OWNER_ID) {
                    MSGHandler.admins.add(Long.parseLong(args[1].replaceAll("@", "")));
                    try {
                        Manager.save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return new MessageChainBuilder().append("已将").append(args[1]).append("添加为管理员").build();
                }
            }
            if (args[0].equalsIgnoreCase("deop")) {
                if (sender.getId() == PluginConfiguration.OWNER_ID) {
                    MSGHandler.admins.remove(Long.parseLong(args[1].replaceAll("@", "")));
                    try {
                        Manager.save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return new MessageChainBuilder().append("已取消").append(args[1]).append("的管理员权限").build();
                }
            }
            if (args[0].equalsIgnoreCase("oplist")) {
                MessageChainBuilder builder = new MessageChainBuilder();
                builder.append("主人:1273300377\n管理员列表:");
                for (Long l : MSGHandler.admins) {
                    builder.append("\n").append(String.valueOf(l));
                }
                return builder.build();
            }
            if (args[0].equalsIgnoreCase("botControl")) {
                long groupid = Long.parseLong(args[1]);
                long botId = sender.getBot().getId();
                MSGHandler.botControl.put(groupid, botId);
                try {
                    Manager.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return new MessageChainBuilder().append("已重载数据").build();
            }
        }
        return new MessageChainBuilder().append("无效的操作").build();
    }

}
