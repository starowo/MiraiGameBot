package com.github.starowo.mirai.command;

import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;

public class CommandLottery extends CommandBase {

    protected CommandLottery() {
        super("lottery", 0);
    }

    @Override
    public String[] getAliases() {
        return new String[]{"l", "抽奖"};
    }

    @Override
    public String getUsage() {
        return "积分抽奖相关指令";
    }

    @Override
    public Message process(User sender, String[] args, MessageChain messages) {
        return null;
    }
}
