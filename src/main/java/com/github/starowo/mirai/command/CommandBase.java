package com.github.starowo.mirai.command;

import com.github.starowo.mirai.MSGHandler;
import com.github.starowo.mirai.PluginConfiguration;
import net.mamoe.mirai.contact.User;

public abstract class CommandBase implements ICommand {

    protected final String name;
    protected final int level;

    protected CommandBase(String name, int level) {
        this.name = name;
        this.level = level;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean hasPermission(User user) {
        if (level == 0) return true;
        if (level == 2)
            return user.getId() == PluginConfiguration.OWNER_ID;
        return MSGHandler.admins.contains(user.getId()) || user.getId() == PluginConfiguration.OWNER_ID;
    }

}
