package com.github.starowo.mirai.command;

import com.google.common.collect.Lists;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class CommandRandom extends CommandBase {

    public static final Random rd = new Random();

    protected CommandRandom() {
        super("random", 0);
    }

    @Override
    public String[] getAliases() {
        return new String[]{"rand", "rd", "随机"};
    }

    @Override
    public String getUsage() {
        return "生成随机结果";
    }

    @Override
    public Message process(User sender, String[] args, MessageChain messages) {
        if (args.length == 0)
            args = new String[]{"i", "100"};
        if (args[0].equalsIgnoreCase("help")) {
            return new MessageChainBuilder()
                    .append("/rd 从1-100中抽取一个整数\n")
                    .append("/rd c <n> <元素列表> 抽取n个不重复的元素\n")
                    .append("/rd r <n> <元素列表> 抽取n个可重复的元素\n")
                    .append("/rd rn <n> <元素列表> 抽取n个可重复的元素，简化输出\n")
                    .append("/rd i [下界] <上界> 随机生成1个整数\n")
                    .append("/rd ic <n> [下界] <上界> 随机生成n个不重复的整数\n")
                    .append("/rd ir <n> [下界] <上界> 随机生成n个可重复的整数\n")
                    .append("/rd f <n> <下界> <上界> [小数位] 随机生成n个范围内的浮点数，默认3位小数\n")
                    .append("为防止刷屏或机器人卡顿，群聊生成时n不得超过20，私聊生成时n不得超过100。\n")
                    .append("rn指令除外，rn指令n不得超过100000。")
                    .build();
        }
        if (args[0].equalsIgnoreCase("i") && (args.length == 3 || args.length == 2)) {
            int min;
            int max;
            if (args.length == 3) {
                try {
                    min = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    return new MessageChainBuilder().append("错误: ").append(args[2]).append(" 不是一个有效的数字").build();
                }
            } else {
                min = 1;
            }
            try {
                if (args.length == 3) {
                    max = Integer.parseInt(args[2]);
                } else {
                    max = Integer.parseInt(args[1]);
                }
            } catch (NumberFormatException e) {
                return new MessageChainBuilder().append("错误: ").append(args[3]).append(" 不是一个有效的数字").build();
            }
            if (max <= min) {
                return new MessageChainBuilder().append("错误: 上界应大于下界").build();
            }

            MessageChainBuilder builder = new MessageChainBuilder().append("抽取结果:");
            builder.append("\n").append(String.valueOf(rd.nextInt(max - min + 1) + min));
            return builder.build();
        }
        if (args.length > 2) {
            int n;
            try {
                n = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                return new MessageChainBuilder().append("错误: ").append(args[1]).append(" 不是一个有效的数字").build();
            }
            if (n < 1) {
                return new MessageChainBuilder().append("错误: ").append(args[1]).append(" 是否有点太小了").build();
            }
            if (args[0].equalsIgnoreCase("rn")) {
                if (n > 100000) {
                    return new MessageChainBuilder().append("错误: n不得超过100000").build();
                }
            } else if (n > 20 && sender instanceof Member) {
                return new MessageChainBuilder().append("错误: n不得超过20").build();
            } else if (n > 100 && sender instanceof Friend) {
                return new MessageChainBuilder().append("错误: n不得超过50").build();
            }
            if (args[0].equalsIgnoreCase("c")) {
                List<String> list = Lists.newArrayList(args);
                list.remove(0);
                list.remove(0);
                if (n > list.size()) {
                    return new MessageChainBuilder().append("错误: 元素数量不足，无法抽取").build();
                }
                MessageChainBuilder builder = new MessageChainBuilder().append("抽取结果:");
                for (int i = 0; i < n; i++) {
                    builder.append("\n").append(list.remove(rd.nextInt(list.size())));
                }
                return builder.build();
            }
            if (args[0].equalsIgnoreCase("r")) {
                List<String> list = Lists.newArrayList(args);
                list.remove(0);
                list.remove(0);
                MessageChainBuilder builder = new MessageChainBuilder().append("抽取结果:");
                for (int i = 0; i < n; i++) {
                    builder.append("\n").append(list.get(rd.nextInt(list.size())));
                }
                return builder.build();
            }
            if (args[0].equalsIgnoreCase("rn")) {
                List<String> list = Lists.newArrayList(args);
                list.remove(0);
                list.remove(0);
                HashMap<String, Integer> map = new HashMap<>();
                for (int i = 0; i < n; i++) {
                    String s = list.get(rd.nextInt(list.size()));
                    map.put(s, map.getOrDefault(s, 0) + 1);
                }
                MessageChainBuilder builder = new MessageChainBuilder().append("抽取结果:");
                map.forEach((k, v) -> builder.append("\n").append(k).append(" x").append(String.valueOf(v)));
                return builder.build();
            }
            if (args.length == 4 || args.length == 3) {
                int min;
                int max;
                if (args.length == 4) {
                    try {
                        min = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        return new MessageChainBuilder().append("错误: ").append(args[2]).append(" 不是一个有效的数字").build();
                    }
                } else {
                    min = 1;
                }
                try {
                    if (args.length == 4) {
                        max = Integer.parseInt(args[3]);
                    } else {
                        max = Integer.parseInt(args[2]);
                    }
                } catch (NumberFormatException e) {
                    return new MessageChainBuilder().append("错误: ").append(args[3]).append(" 不是一个有效的数字").build();
                }
                if (max <= min) {
                    return new MessageChainBuilder().append("错误: 上界应大于下界").build();
                }
                if (args[0].equalsIgnoreCase("ic")) {
                    if (n > max - min) {
                        return new MessageChainBuilder().append("错误: 范围内整数不足，无法抽取").build();
                    }
                    HashSet<Integer> chosen = new HashSet<>();
                    MessageChainBuilder builder = new MessageChainBuilder().append("抽取结果:");
                    while (chosen.size() < n) {
                        int r = rd.nextInt(max - min + 1) + min;
                        if (chosen.add(r)) {
                            builder.append("\n").append(String.valueOf(r));
                        }
                    }
                    return builder.build();
                }
                if (args[0].equalsIgnoreCase("ir")) {
                    MessageChainBuilder builder = new MessageChainBuilder().append("抽取结果:");
                    for (int i = 0; i < n; i++) {
                        builder.append("\n").append(String.valueOf(rd.nextInt(max - min + 1) + min));
                    }
                    return builder.build();
                }
                if (args[0].equalsIgnoreCase("f")) {

                    MessageChainBuilder builder = new MessageChainBuilder().append("抽取结果:");
                    for (int i = 0; i < n; i++) {
                        builder.append(String.format("\n%.3f", rd.nextDouble() * (max - min) + min));
                    }
                    return builder.build();
                }
            }
            if (args.length == 5 && args[0].equalsIgnoreCase("f")) {
                int min;
                int max;
                try {
                    min = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    return new MessageChainBuilder().append("错误: ").append(args[2]).append(" 不是一个有效的数字").build();
                }
                try {
                    max = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    return new MessageChainBuilder().append("错误: ").append(args[3]).append(" 不是一个有效的数字").build();
                }
                try {
                    int p = Integer.parseInt(args[4]);
                    if (p < 1 || p > 8) {
                        return new MessageChainBuilder().append("错误: 仅可保留1-8位小数").build();
                    }
                } catch (NumberFormatException e) {
                    return new MessageChainBuilder().append("错误: ").append(args[4]).append(" 不是一个有效的数字").build();
                }
                MessageChainBuilder builder = new MessageChainBuilder().append("抽取结果:");
                for (int i = 0; i < n; i++) {
                    builder.append(String.format("\n%." + args[4] + "f", rd.nextDouble() * (max - min) + min));
                }
                return builder.build();
            }
        }
        return new MessageChainBuilder().append("参数错误，使用/rd help获取帮助").build();
    }
}
