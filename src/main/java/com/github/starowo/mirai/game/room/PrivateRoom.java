package com.github.starowo.mirai.game.room;

import com.github.starowo.mirai.MSGHandler;
import com.github.starowo.mirai.MiraiGamePlugin;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.HashSet;

public class PrivateRoom implements IRoom {

    public HashSet<User> players = new HashSet<>();
    public final long id;

    public PrivateRoom(long id) {
        this.id = id;
    }

    @Override
    public void send(String message) {
        players.forEach(player -> player.sendMessage(message));
    }

    @Override
    public void send(MessageChain message) {
        players.forEach(player -> player.sendMessage(message));
    }

    @Override
    public void send(byte[] image) {
        players.forEach(player -> MSGHandler.asyncSendImage(player, image));
    }

    @Override
    public void delete() {
        MiraiGamePlugin.INSTANCE.players_map.get(getId()).clear();
        MiraiGamePlugin.INSTANCE.games.remove(getId());
    }

    @Override
    public long getId() {
        return id;
    }

}
