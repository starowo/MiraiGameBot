package com.github.starowo.mirai.command;

import com.github.starowo.mirai.MSGHandler;
import com.github.starowo.mirai.data.DataPlayer;
import com.github.starowo.mirai.data.Manager;
import com.github.starowo.mirai.data.Skin;
import com.github.starowo.mirai.game.comb.ImageGenerator;
import com.github.starowo.mirai.game.comb.NumberHive;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.ExternalResource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

public class CommandSkin extends CommandBase {

    protected CommandSkin() {
        super("skin", 0);
    }

    @Override
    public String[] getAliases() {
        return new String[]{"皮肤"};
    }

    @Override
    public String getUsage() {
        return "皮肤相关指令";
    }

    @Override
    public Message process(User user, String[] args, MessageChain message) {
        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("reload")) {
                ImageGenerator.init();
                return new MessageChainBuilder().append("重载完毕").build();
            }
            if(args[0].equalsIgnoreCase("help")) {
                return new MessageChainBuilder()
                        .append("/skin help 获取帮助\n")
                        .append("/skin list 获取支持的游戏列表\n")
                        .append("/skin list <游戏> 获取游戏的皮肤列表\n")
                        .append("/skin preview <游戏> <皮肤> 预览皮肤\n")
                        .append("/skin buy <游戏> <皮肤> 购买皮肤\n")
                        .append("/skin use <游戏> <皮肤> 使用皮肤\n")
                        .build();
            }
            if(args[0].equalsIgnoreCase("帮助")) {
                return new MessageChainBuilder()
                        .append("/skin 帮助 获取帮助\n")
                        .append("/skin 列表 获取支持的游戏列表\n")
                        .append("/skin 列表 <游戏> 获取游戏的皮肤列表\n")
                        .append("/skin 预览 <游戏> <皮肤> 预览皮肤\n")
                        .append("/skin 购买 <游戏> <皮肤> 购买皮肤\n")
                        .append("/skin 使用 <游戏> <皮肤> 使用皮肤\n")
                        .build();
            }
            if(args[0].equalsIgnoreCase("list") || args[0].equals("列表")) {
                return new MessageChainBuilder().append("当前支持皮肤的游戏列表：\n云顶之巢").build();
            }
        }
        if(args.length == 2) {
            if(args[0].equalsIgnoreCase("list") || args[0].equals("列表")) {
                if(args[1].equals("云顶之巢")) {
                    MessageChainBuilder respond = new MessageChainBuilder().append("云顶之巢皮肤商城:");
                    for (Skin skin : Skin.values()) {
                        if(skin == Skin.test) continue;
                        respond.append("\r\n ").append(skin.name()).append(", ").append(skin.name).append(skin.level > 0 ? " (Lv."+skin.level+")" : "").append(", 价格:").append(String.valueOf(skin.price));
                    }
                    return respond.build();
                }else {
                    return new MessageChainBuilder().append("不支持的游戏").build();
                }
            }
        }
        if(args.length == 3) {
            if (args[0].equalsIgnoreCase("buy") || args[0].equals("购买")) {
                if (args[1].equals("云顶之巢")) {
                    String buy = args[2].toLowerCase(Locale.ROOT);
                    try {
                        Skin skin = Skin.findSkin(buy);
                        if(skin == Skin.test && !MSGHandler.admins.contains(user.getId())) {
                            return new MessageChainBuilder()
                                    .append("皮肤不存在或指令错误！")
                                    .build();
                        }
                        DataPlayer data = Manager.getByUser(user);
                        if (data.unlocked.contains(skin.name)) {
                            return new MessageChainBuilder()
                                    .append("你已经拥有此皮肤！")
                                    .build();
                        }
                        for (String p : skin.pre) {
                            if (!data.unlocked.contains(p) && !data.unlocked.contains(Skin.findSkin(p).name)) {
                                return new MessageChainBuilder()
                                        .append("缺少前置皮肤！需要:")
                                        .append(Skin.findSkin(p).name)
                                        .build();
                            }
                        }
                        if (data.level < skin.level) {
                            return new MessageChainBuilder()
                                    .append("等级不足，你需要至少达到 Lv.")
                                    .append(String.valueOf(skin.level))
                                    .append(" 才能购买此皮肤。")
                                    .build();
                        }
                        if (data.credit < skin.price) {
                            return new MessageChainBuilder()
                                    .append("你的积分不足！需要:")
                                    .append(String.valueOf(skin.price))
                                    .append(" 现有:")
                                    .append(String.valueOf(data.credit))
                                    .build();
                        }
                        data.unlocked.add(skin.name);
                        data.credit -= skin.price;
                        try {
                            Manager.save();
                        } catch (IOException err) {
                            err.printStackTrace();
                        }
                        return new MessageChainBuilder()
                                .append("购买成功！")
                                .build();
                    } catch (Throwable throwable) {
                        if(throwable instanceof IllegalArgumentException) {
                            return new MessageChainBuilder()
                                    .append("皮肤不存在！")
                                    .build();
                        }else {
                            throwable.printStackTrace();
                            return new MessageChainBuilder().append("发生错误:").append(throwable.getMessage()).append("\n请呼叫星星查看后台")
                                    .build();
                        }
                    }
                }else {
                    return new MessageChainBuilder().append("不支持的游戏").build();
                }
            }
            if (args[0].equalsIgnoreCase("use") || args[0].equals("使用")) {
                if (args[1].equals("云顶之巢")) {
                    String target = args[2].toLowerCase(Locale.ROOT);
                    try {
                        DataPlayer data = Manager.getByUser(user);
                        Skin skin = Skin.findSkin(target);
                        if (data.unlocked.contains(skin.name)) {
                            data.skin = skin.name;
                            try {
                                Manager.save();
                            } catch (IOException err) {
                                err.printStackTrace();
                            }
                            return new MessageChainBuilder()
                                    .append("设置成功！")
                                    .build();
                        }
                        return new MessageChainBuilder()
                                .append("你没有这个皮肤！")
                                .build();
                    } catch (Throwable throwable) {
                        return new MessageChainBuilder()
                                .append("皮肤不存在或指令错误！")
                                .build();
                    }
                } else {
                    return new MessageChainBuilder().append("不支持的游戏").build();
                }
            }
            if (args[0].equalsIgnoreCase("preview") || args[0].equals("预览")) {
                if (args[1].equals("云顶之巢")) {
                    String target = args[2].toLowerCase(Locale.ROOT);
                    MessageChainBuilder builder = new MessageChainBuilder();
                    try {
                        Skin skin = Skin.findSkin(target);
                        if(skin == Skin.test && !MSGHandler.admins.contains(user.getId())) {
                            return new MessageChainBuilder()
                                    .append("皮肤不存在！")
                                    .build();
                        }
                        BufferedImage preview = NumberHive.getSkinPreview(skin);
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        byte[] baImage = null;
                        try {
                            ImageIO.write(preview, "png", os);
                            baImage = os.toByteArray();
                            os.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (baImage != null) {
                            ExternalResource resource = ExternalResource.create(baImage);
                            builder.append(user.uploadImage(resource));
                            try {
                                return builder.build();
                            } finally {
                                try {
                                    resource.close();
                                }catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (Throwable throwable) {
                        if(throwable instanceof IllegalArgumentException) {
                            return new MessageChainBuilder()
                                    .append("皮肤不存在！")
                                    .build();
                        }else {
                            throwable.printStackTrace();
                            return new MessageChainBuilder().append("发生错误:").append(throwable.getMessage()).append("\n请呼叫星星查看后台")
                                    .build();
                        }
                    }
                } else {
                    return new MessageChainBuilder().append("不支持的游戏").build();
                }
            }
        }
        if (args.length >= 4 && args[0].equalsIgnoreCase("give") && user.getId() == 1273300377L) {
            if (args[1].equals("云顶之巢")) {
                String target = args[2].toLowerCase(Locale.ROOT);
                try {
                    String targetID = args[3].replaceAll("[^0-9]", "");
                    DataPlayer data = Manager.getByID(Long.parseLong(targetID));
                    if (data == null) {
                        return new MessageChainBuilder()
                                .append("玩家不存在！")
                                .build();
                    }
                    Skin skin = Skin.findSkin(target);
                    if (data.unlocked.contains(skin.name)) {
                        return new MessageChainBuilder()
                                .append("目标玩家已经拥有此皮肤！")
                                .build();
                    }
                    data.unlocked.add(skin.name);
                    try {
                        Manager.save();
                    } catch (IOException err) {
                        err.printStackTrace();
                    }
                    return new MessageChainBuilder()
                            .append("赠送成功！")
                            .build();
                } catch (Throwable throwable) {
                    return new MessageChainBuilder()
                            .append("皮肤不存在或指令错误！")
                            .build();
                }
            } else {
                return new MessageChainBuilder().append("不支持的游戏").build();
            }
        }
        return new MessageChainBuilder().append("参数错误，使用 /skin help或/skin 帮助 获取帮助").build();
    }
}
