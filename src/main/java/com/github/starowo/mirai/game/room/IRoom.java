package com.github.starowo.mirai.game.room;

import net.mamoe.mirai.message.data.MessageChain;

public interface IRoom {

    void send(String message);

    void send(MessageChain message);

    void send(byte[] image);

    void delete();

    long getId();

}
