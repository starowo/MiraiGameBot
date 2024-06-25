package com.github.starowo.mirai.command;

import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;

public interface ICommand {

    String getName();

    String[] getAliases();

    boolean hasPermission(User user);

    String getUsage();

    Message process(User sender, String[] args, MessageChain messages);
}
