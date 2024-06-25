package com.github.starowo.mirai.game.room;

import com.github.starowo.mirai.MSGHandler;
import com.github.starowo.mirai.MiraiGamePlugin;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.MessageChain;

public class GroupRoom implements IRoom {

    public Group group;

    public GroupRoom(Group group) {
        this.group = group;
    }

    @Override
    public void send(String message) {
        group.sendMessage(message);
    }

    @Override
    public void send(MessageChain message) {
        group.sendMessage(message);
    }

    @Override
    public void send(byte[] image) {
        MSGHandler.asyncSendImage(group, image);
    }

    @Override
    public void delete() {
        MiraiGamePlugin.INSTANCE.players_map.get(getId()).clear();
        MiraiGamePlugin.INSTANCE.games.remove(getId());
    }

    @Override
    public long getId() {
        return group.getId();
    }
}
