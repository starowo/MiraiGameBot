package com.github.starowo.mirai.command;

import com.github.starowo.mirai.MSGHandler;
import com.github.starowo.mirai.PluginConfiguration;
import com.github.starowo.mirai.data.DataPlayer;
import com.github.starowo.mirai.data.Event;
import com.github.starowo.mirai.data.Manager;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.AtAll;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;

public class CommandEvent extends CommandBase {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    protected CommandEvent() {
        super("event", 0);
    }

    @Override
    public String[] getAliases() {
        return new String[]{"e", "活动"};
    }

    @Override
    public String getUsage() {
        return "活动/事件相关指令";
    }

    @Override
    public Message process(User sender, String[] args, MessageChain message) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("help")) {
                MessageChainBuilder builder = new MessageChainBuilder()
                        .append("/e list 查看活动列表\n")
                        .append("/e mine 查看自己报名的活动列表\n")
                        .append("/e create <标题> 创建一个活动\n")
                        .append("/e info <id> 查看活动当前信息\n")
                        .append("/e sign <id> 报名活动\n")
                        .append("/e sub <id> 作为替补报名活动\n")
                        .append("/e quit <id> 退出活动\n")
                        .append("/e delete <id> 删除活动\n")
                        .append("/e title <id> <标题> 更改活动的标题\n")
                        .append("/e desc <id> <描述> 更改活动的描述\n")
                        .append("/e max <id> <人数> 更改活动的最大人数\n")
                        .append("/e announce <id> 在群里公开通告活动\n")
                        .append("/e notice <id> <消息> 向报名了活动的所有人发布一条通知\n");
                return builder.build();
            }
            if (args[0].equalsIgnoreCase("帮助")) {
                MessageChainBuilder builder = new MessageChainBuilder()
                        .append("/e 列表 查看活动列表\n")
                        .append("/e 我的 查看自己报名的活动列表\n")
                        .append("/e 创建 <标题> 创建一个活动\n")
                        .append("/e 信息 <id> 查看活动当前信息\n")
                        .append("/e 报名 <id> 报名活动\n")
                        .append("/e 替补 <id> 作为替补报名活动\n")
                        .append("/e 退出 <id> 退出活动\n")
                        .append("/e 删除 <id> 删除活动\n")
                        .append("/e 标题 <id> <标题> 更改活动的标题\n")
                        .append("/e 描述 <id> <描述> 更改活动的描述\n")
                        .append("/e 人数 <id> <人数> 更改活动的最大人数\n")
                        .append("/e 公告 <id> 在群里公开通告活动\n")
                        .append("/e 通知 <id> <消息> 向报名了活动的所有人发布一条通知\n");
                return builder.build();
            }
            if (args[0].equalsIgnoreCase("list") || args[0].equals("列表")) {
                MessageChainBuilder builder = new MessageChainBuilder().append("当前活动列表:");
                for (Map.Entry<Integer, Event> entry : Event.eventMap.entrySet()) {
                    DataPlayer dataPlayer = Manager.getByID(entry.getValue().owner);
                    assert dataPlayer != null;
                    builder.append("\n").append(String.valueOf(entry.getKey())).append(". ").append(dataPlayer.name).append(" (").append(String.valueOf(dataPlayer.id)).append(") - ").append(entry.getValue().title);
                }
                return builder.build();
            }
            if (args[0].equalsIgnoreCase("mine") || args[0].equals("我的")) {
                MessageChainBuilder builder = new MessageChainBuilder().append("您报名的活动:");
                for (Map.Entry<Integer, Event> entry : Event.eventMap.entrySet()) {
                    DataPlayer dataPlayer = Manager.getByID(entry.getValue().owner);
                    assert dataPlayer != null;
                    if (entry.getValue().enrolled.contains(sender.getId()))
                        builder.append("\n").append(String.valueOf(entry.getKey())).append(" - ").append(dataPlayer.name).append(" (").append(String.valueOf(dataPlayer.id)).append(") - ").append(entry.getValue().title);
                }
                builder.append("\n您替补的活动:");
                for (Map.Entry<Integer, Event> entry : Event.eventMap.entrySet()) {
                    DataPlayer dataPlayer = Manager.getByID(entry.getValue().owner);
                    assert dataPlayer != null;
                    if (entry.getValue().sub.contains(sender.getId()))
                        builder.append("\n").append(String.valueOf(entry.getKey())).append(" - ").append(dataPlayer.name).append(" (").append(String.valueOf(dataPlayer.id)).append(") - ").append(entry.getValue().title);
                }
                builder.append("\n您创建的活动:");
                for (Map.Entry<Integer, Event> entry : Event.eventMap.entrySet()) {
                    DataPlayer dataPlayer = Manager.getByID(entry.getValue().owner);
                    assert dataPlayer != null;
                    if (entry.getValue().owner == sender.getId())
                        builder.append("\n").append(String.valueOf(entry.getKey())).append(" - ").append(entry.getValue().title);
                }
                return builder.build();
            }
        }
        if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("create") || args[0].equals("创建")) {
                StringBuilder title = new StringBuilder(args[1]);
                for (int i = 2; i < args.length; i++) {
                    title.append(" ").append(args[i]);
                }
                Event event = Event.newEvent(title.toString(), sender.getId());
                if(sender instanceof Member) {
                    Manager.getByMember((Member) sender);
                }else {
                    Manager.getByUser(sender);
                }
                try {
                    Manager.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return new MessageChainBuilder().append("活动创建完毕，id:").append(String.valueOf(event.id)).build();
            }
        }
        if (args.length >= 3) {
            if (args[0].equalsIgnoreCase("title") || args[0].equals("标题")) {
                int id;
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    return new MessageChainBuilder().append(args[1]).append("不是一个有效的数字").build();
                }
                Event event = Event.getEvent(id);
                if (event == null) {
                    return new MessageChainBuilder().append("活动").append(args[1]).append("不存在").build();
                }
                if(!hasPermission(event, sender)) {
                    return new MessageChainBuilder().append("您没有操作此活动的权限").build();
                }
                StringBuilder title = new StringBuilder(args[2]);
                for (int i = 3; i < args.length; i++) {
                    title.append(" ").append(args[i]);
                }
                event.title = title.toString();
                try {
                    Manager.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return new MessageChainBuilder().append("活动标题已更改为: ").append(event.title).build();
            }
            if (args[0].equalsIgnoreCase("desc") || args[0].equals("描述")) {
                int id;
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    return new MessageChainBuilder().append(args[1]).append("不是一个有效的数字").build();
                }
                Event event = Event.getEvent(id);
                if (event == null) {
                    return new MessageChainBuilder().append("活动").append(args[1]).append("不存在").build();
                }
                if(!hasPermission(event, sender)) {
                    return new MessageChainBuilder().append("您没有操作此活动的权限").build();
                }
                StringBuilder desc = new StringBuilder(args[2]);
                for (int i = 3; i < args.length; i++) {
                    desc.append(" ").append(args[i]);
                }
                event.desc = desc.toString();
                try {
                    Manager.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return new MessageChainBuilder().append("活动描述已更改为: ").append(event.desc).build();
            }
            if (args[0].equalsIgnoreCase("notice") || args[0].equals("通知")) {
                int id;
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    return new MessageChainBuilder().append(args[1]).append("不是一个有效的数字").build();
                }
                Event event = Event.getEvent(id);
                if (event == null) {
                    return new MessageChainBuilder().append("活动").append(args[1]).append("不存在").build();
                }
                if(!hasPermission(event, sender)) {
                    return new MessageChainBuilder().append("您没有操作此活动的权限").build();
                }
                StringBuilder msg = new StringBuilder(args[2]);
                for (int i = 3; i < args.length; i++) {
                    msg.append(" ").append(args[i]);
                }
                event.notice(sender.getBot(), msg.toString());
                return new MessageChainBuilder().append("通知已发送").build();
            }
        }
        if(args.length == 2) {
            if (args[0].equalsIgnoreCase("info") || args[0].equals("信息")) {
                int id;
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    return new MessageChainBuilder().append(args[1]).append("不是一个有效的数字").build();
                }
                Event event = Event.getEvent(id);
                if (event == null) {
                    return new MessageChainBuilder().append("活动").append(args[1]).append("不存在").build();
                }
                DataPlayer dataPlayer = Manager.getByID(event.owner);
                MessageChainBuilder builder = new MessageChainBuilder().append("活动当前信息如下:");
                builder.append("\nid: ").append(String.valueOf(event.id));
                builder.append("\n发起人: ").append(dataPlayer.name).append(" (").append(String.valueOf(event.owner)).append(")");
                builder.append("\n标题: ").append(event.title);
                builder.append("\n描述: ").append(event.desc);
                builder.append("\n报名 ").append(String.valueOf(event.enrolled.size())).append("/").append(String.valueOf(event.max)).append(":");
                for (Long qq : event.enrolled) {
                    DataPlayer data = Manager.getByID(qq);
                    builder.append("\n").append(data.name).append(" (").append(qq.toString()).append(")");
                }
                builder.append("\n替补: ");
                for (Long qq : event.sub) {
                    DataPlayer data = Manager.getByID(qq);
                    builder.append("\n").append(data.name).append(" (").append(qq.toString()).append(")");
                }
                return builder.build();
            }
            if (args[0].equalsIgnoreCase("announce") || args[0].equals("公告")) {
                if(!(sender instanceof Member)) {
                    return new MessageChainBuilder().append("请在群聊使用此指令").build();
                }
                int id;
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    return new MessageChainBuilder().append(args[1]).append("不是一个有效的数字").build();
                }
                Event event = Event.getEvent(id);
                if (event == null) {
                    return new MessageChainBuilder().append("活动").append(args[1]).append("不存在").build();
                }
                if(!hasPermission(event, sender)) {
                    return new MessageChainBuilder().append("您没有操作此活动的权限").build();
                }
                MessageChainBuilder builder = new MessageChainBuilder();
                if(((Member) sender).getGroup().getBotPermission().getLevel() > 0) {
                    builder.append(AtAll.INSTANCE);
                }
                builder.append(event.title);
                if(!event.desc.isEmpty())
                    builder.append("\n\n").append(event.desc);
                builder.append("\n\n报名 ").append(String.valueOf(event.enrolled.size())).append("/").append(String.valueOf(event.max));
                if(event.enrolled.size() > 0) {
                    builder.append(":");
                    for (Long qq : event.enrolled) {
                        DataPlayer data = Manager.getByID(qq);
                        builder.append("\n").append(data.name).append(" (").append(qq.toString()).append(")");
                    }
                }
                if(event.sub.size() > 0) {
                    builder.append("\n替补: ");
                    for (Long qq : event.sub) {
                        DataPlayer data = Manager.getByID(qq);
                        builder.append("\n").append(data.name).append(" (").append(qq.toString()).append(")");
                    }
                }
                builder.append("\n\n发送/e sign ").append(args[1]).append("即可报名参加活动");
                ((Member) sender).getGroup().sendMessage(builder.build());
                return null;
            }
            if (args[0].equalsIgnoreCase("sign") || args[0].equals("报名")) {
                int id;
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    return new MessageChainBuilder().append(args[1]).append("不是一个有效的数字").build();
                }
                Event event = Event.getEvent(id);
                if (event == null) {
                    return new MessageChainBuilder().append("活动").append(args[1]).append("不存在").build();
                }
                try {
                    event.lock.lock();
                    if (event.enrolled.contains(sender.getId())) {
                        return new MessageChainBuilder().append("您已经在此活动的报名列表中了").build();
                    }
                    if (event.enrolled.size() == event.max) {
                        return new MessageChainBuilder().append("活动太火爆了，报名列表已满，您可以使用/e sub ").append(args[1]).append("作为替补报名").build();
                    }
                    if(sender instanceof Member) {
                        Manager.getByMember((Member) sender);
                    }else {
                        Manager.getByUser(sender);
                    }
                    event.enrolled.add(sender.getId());
                    event.sub.remove(sender.getId());
                    try {
                        Manager.save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return new MessageChainBuilder().append("报名成功！当前人数:").append(String.valueOf(event.enrolled.size())).append("/").append(String.valueOf(event.max)).append("\n建议添加机器人好友以接收活动相关通知").build();
                }finally {
                    event.lock.unlock();
                }
            }
            if (args[0].equalsIgnoreCase("quit") || args[0].equals("退出")) {
                int id;
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    return new MessageChainBuilder().append(args[1]).append("不是一个有效的数字").build();
                }
                Event event = Event.getEvent(id);
                if (event == null) {
                    return new MessageChainBuilder().append("活动").append(args[1]).append("不存在").build();
                }
                try {
                    event.lock.lock();
                    if (!event.enrolled.contains(sender.getId()) && !event.sub.contains(sender.getId())) {
                        return new MessageChainBuilder().append("您不在此活动的报名或替补列表中").build();
                    }
                    event.enrolled.remove(sender.getId());
                    event.sub.remove(sender.getId());
                    try {
                        Manager.save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return new MessageChainBuilder().append("退出成功！当前人数:").append(String.valueOf(event.enrolled.size())).append("/").append(String.valueOf(event.max)).build();
                }finally {
                    event.lock.unlock();
                }
            }
            if (args[0].equalsIgnoreCase("sub") || args[0].equals("替补")) {
                int id;
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    return new MessageChainBuilder().append(args[1]).append("不是一个有效的数字").build();
                }
                Event event = Event.getEvent(id);
                if (event == null) {
                    return new MessageChainBuilder().append("活动").append(args[1]).append("不存在").build();
                }
                try {
                    event.lock.lock();
                    if (event.enrolled.contains(sender.getId()) || event.sub.contains(sender.getId())) {
                        return new MessageChainBuilder().append("您已经在此活动的报名/替补列表中了").build();
                    }
                    if(sender instanceof Member) {
                        Manager.getByMember((Member) sender);
                    }else {
                        Manager.getByUser(sender);
                    }
                    event.sub.add(sender.getId());
                    try {
                        Manager.save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return new MessageChainBuilder().append("报名替补成功！").append("\n建议添加机器人好友以接收活动相关通知").build();
                }finally {
                    event.lock.unlock();
                }
            }
            if (args[0].equalsIgnoreCase("delete") || args[0].equals("删除")) {
                int id;
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    return new MessageChainBuilder().append(args[1]).append("不是一个有效的数字").build();
                }
                Event event = Event.getEvent(id);
                if (event == null) {
                    return new MessageChainBuilder().append("活动").append(args[1]).append("不存在").build();
                }
                if(!hasPermission(event, sender)) {
                    return new MessageChainBuilder().append("您没有操作此活动的权限").build();
                }
                event.notice(sender.getBot(), "活动房间已解散");
                event.enrolled.clear();
                event.sub.clear();
                Event.eventMap.remove(event.id);
                try {
                    Manager.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return new MessageChainBuilder().append("成功删除此活动").build();
            }
        }
        if(args.length == 3) {
            if(args[0].equalsIgnoreCase("max") || args[0].equals("人数")) {
                int id;
                int max;
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    return new MessageChainBuilder().append(args[1]).append("不是一个有效的数字").build();
                }
                try {
                    max = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    return new MessageChainBuilder().append(args[2]).append("不是一个有效的数字").build();
                }
                Event event = Event.getEvent(id);
                if (event == null) {
                    return new MessageChainBuilder().append("活动").append(args[1]).append("不存在").build();
                }
                if(!hasPermission(event, sender)) {
                    return new MessageChainBuilder().append("您没有操作此活动的权限").build();
                }
                event.max = max;
                try {
                    Manager.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return new MessageChainBuilder().append("活动最大人数已更改为: ").append(args[2]).build();
            }
        }
        return new MessageChainBuilder().append("参数错误，使用 /e help或/e 帮助 获取帮助").build();
    }

    private boolean hasPermission(Event event, User sender) {
        return event.owner == sender.getId() || MSGHandler.admins.contains(sender.getId()) || sender.getId() == PluginConfiguration.OWNER_ID;
    }

}
