package com.github.starowo.mirai.command;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.QuoteReply;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class CommandTime extends CommandBase {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    protected CommandTime() {
        super("time", 0);
    }

    @Override
    public String[] getAliases() {
        return new String[]{"t", "时间"};
    }

    @Override
    public String getUsage() {
        return "时间相关指令";
    }

    @Override
    public Message process(User sender, String[] args, MessageChain message) {
        if(args.length > 0) {
            if(args[0].equalsIgnoreCase("help")) {
                return new MessageChainBuilder()
                        .append("/t help 获取帮助\n")
                        .append("/t star 查看星星的时间\n")
                        .append("/t count <时间> [编号] 开始倒计时\n")
                        .append("倒计时的格式如: 1m30代表90s 2代表2s\n")
                        .append("示例: /t count 1m20 示例倒计时\n")
                        .build();
            }
            if(args[0].equalsIgnoreCase("帮助")) {
                return new MessageChainBuilder()
                        .append("/t 帮助 获取帮助\n")
                        .append("/t 星星 查看星星的时间\n")
                        .append("/t 倒计时 <时间> [编号] 开始倒计时\n")
                        .append("倒计时的格式如: 1m30代表90s 2代表2s\n")
                        .append("示例: /t count 1m20 示例倒计时\n")
                        .build();
            }
            if (args[0].equalsIgnoreCase("star") || args[0].equals("星星")) {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
                int second = calendar.get(Calendar.SECOND);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                String sec = second < 10 ? "0" + second : second + "";
                String hr = hour < 10 ? "0" + hour : hour + "";
                String min = minute < 10 ? "0" + minute : minute + "";
                return new MessageChainBuilder().append("星星现在的时间为：\n").append(String.format("%s:%s:%s", hr, min, sec)).append("\n(太平洋标准时间)").build();
            }
            if(args.length > 1) {
                try {
                    if (args[0].equalsIgnoreCase("count") || args[0].equals("倒计时")) {
                        if (args[1].indexOf("s") == args[1].length() - 1) {
                            args[1] = args[1].replaceAll("[sS]", "");
                        }
                        String[] t = args[1].split("[mM]");
                        if (t.length > 2) {
                            return new MessageChainBuilder().append("倒计时格式错误，使用 /t help或/t 帮助 获取帮助").build();
                        }
                        long time;
                        if(args[1].contains("m")) {
                             time = Long.parseLong(t[0]) * 60L + (t.length > 1 ? Long.parseLong(t[1]) : 0);
                        }else {
                            time = Long.parseLong(t[0]);
                        }
                        time *= 1000L;
                        long pre = 0L;
                        if (time >= 300000) {
                            pre = time - 180000L;
                        } else if (time >= 180000) {
                            pre = time - 120000L;
                        } else if (time >= 90000) {
                            pre = time - 60000L;
                        } else if (time >= 60000) {
                            pre = time - 30000L;
                        } else if (time >= 30000) {
                            pre = time - 10000L;
                        }
                        String name = args.length > 2 ? " " + args[2] + " " : "";
                        long finalPre = pre;
                        long finalTime = time;
                        new Thread(() -> {
                            try {
                                Contact reply = (sender instanceof Member) ? ((Member) sender).getGroup() : sender;
                                if (finalPre > 0) {
                                    Thread.sleep(finalPre);
                                }
                                if (finalTime >= 300000) {
                                    reply.sendMessage(new MessageChainBuilder().append(new QuoteReply(message)).append("您的倒计时").append(name).append("剩余3分钟").build());
                                    Thread.sleep(60000L);
                                }
                                if (finalTime >= 180000) {
                                    reply.sendMessage(new MessageChainBuilder().append(new QuoteReply(message)).append("您的倒计时").append(name).append("剩余2分钟").build());
                                    Thread.sleep(60000L);
                                }
                                if (finalTime >= 90000) {
                                    reply.sendMessage(new MessageChainBuilder().append(new QuoteReply(message)).append("您的倒计时").append(name).append("剩余1分钟").build());
                                    Thread.sleep(30000L);
                                }
                                if (finalTime >= 60000) {
                                    reply.sendMessage(new MessageChainBuilder().append(new QuoteReply(message)).append("您的倒计时").append(name).append("剩余30秒").build());
                                    Thread.sleep(20000L);
                                }
                                if (finalTime >= 30000) {
                                    reply.sendMessage(new MessageChainBuilder().append(new QuoteReply(message)).append("您的倒计时").append(name).append("剩余10秒").build());
                                    Thread.sleep(10000L);
                                }
                                reply.sendMessage(new MessageChainBuilder().append(new QuoteReply(message)).append(name).append("时间到！").build());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }
                    return new MessageChainBuilder().append("倒计时开始").build();
                }catch (NumberFormatException e) {
                    return new MessageChainBuilder().append(args[1]+"不是一个有效的时间").build();
                }
            }
        }
        return new MessageChainBuilder().append("参数错误，使用 /t help或/t 帮助 获取帮助").build();
    }
}
