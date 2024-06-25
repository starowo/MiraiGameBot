package com.github.starowo.mirai.command;

import com.google.common.collect.Lists;
import com.github.starowo.mirai.command.cipher.CommandCipher;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.HashMap;
import java.util.List;

public class Commands {

    public static final HashMap<String, ICommand> REGISTRY = new HashMap<>();
    public static final HashMap<String, ICommand> aliasesMap = new HashMap<>();

    static {
        register(new CommandBase("help", 0) {
            @Override
            public String getUsage() {
                return "获取帮助";
            }

            @Override
            public String[] getAliases() {
                return new String[]{"帮助"};
            }

            @Override
            public Message process(User sender, String[] args, MessageChain messages) {
                return new MessageChainBuilder().append(getHelp(sender)).build();
            }
        });
        register(new CommandGame());
        register(new CommandPoints());
        register(new CommandSkin());
        register(new CommandRank());
        register(new CommandRandom());
        register(new CommandTime());
        register(new CommandAdmin());
        register(new CommandEvent());
        register(new CommandCipher());
    }

    public static void register(ICommand command) {
        REGISTRY.put(command.getName(), command);
        for (String alias : command.getAliases()) {
            aliasesMap.put(alias, command);
        }
    }

    public static String getHelp(User sender) {
        StringBuilder s = new StringBuilder("可用的指令列表如下:");
        for (ICommand command : REGISTRY.values()) {
            if (command.hasPermission(sender))
                s.append("\n/").append(command.getName()).append(" ").append(command.getUsage());
        }
        return s.toString();
    }

    public static Message process(User user, String text, MessageChain message) {
        try {
            int end = text.indexOf(" ");
            String name;
            if (end == -1) {
                name = text.substring(1);
            } else {
                name = text.substring(1, end);
            }
            ICommand command = REGISTRY.get(name);
            command = command == null ? aliasesMap.get(name) : command;
            if (command == null) {
                //return text.startsWith(MSGHandler.startChar) ? new MessageChainBuilder().append("未知的指令，请尝试使用/help获取帮助").build() : null;
                return null;
            }
            if (!command.hasPermission(user)) {
                return new MessageChainBuilder().append("你没有使用此指令的权限").build();
            }
            if (end == -1) {
                return command.process(user, new String[0], message);
            } else {
                String arg = text.substring(end + 1);
                String[] args = arg.split(" ");
                List<String> list = Lists.newArrayList(args);
                list.removeIf(String::isEmpty);
                args = list.toArray(new String[0]);
                return command.process(user, args, message);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return new MessageChainBuilder().append("发生了未知的错误").build();
        }
    }

}
