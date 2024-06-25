package com.github.starowo.mirai.game;

import com.github.starowo.mirai.player.GamePlayer;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;

public interface IGame {

    void start();

    default Message input(GamePlayer player, MessageChain msg) {
        return null;
    }

    default Message input(GamePlayer player, MessageChain msg, boolean group) {
        return input(player, msg);
    }

    default Message input(GamePlayer player, MessageChain msg, boolean group, boolean at) {
        return input(player, msg, group);
    }

    default String getName() {
        return "";
    }

    default String getRule() {
        return "";
    }

    void stop();

    boolean allowGroup();

    boolean isWaiting();

    int getMaxPlayer();

    void addPlayer(GamePlayer activePlayer);

    void remove(GamePlayer activePlayer);

    default boolean needAt() {
        return true;
    }

    default int getMinPlayer() {
        return 2;
    }
}
