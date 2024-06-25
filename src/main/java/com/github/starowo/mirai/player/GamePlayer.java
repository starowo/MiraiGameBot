package com.github.starowo.mirai.player;

import com.github.starowo.mirai.data.DataPlayer;
import com.github.starowo.mirai.data.Manager;
import com.github.starowo.mirai.data.PlayerRank;

import java.util.Objects;

public class GamePlayer {

    public long id;
    public String name;
    public DataPlayer data;
    public PlayerRank rank;
    public String avatar;

    public GamePlayer(long id, String name, DataPlayer data) {
        this.id = id;
        this.name = name;
        this.data = data;
        this.rank = Manager.getRank(id);
        this.avatar = "http://q1.qlogo.cn/g?b=qq&nk=" + id + "&s=640";
    }

    public GamePlayer(long id, String name, DataPlayer data, String avatar) {
        this.id = id;
        this.name = name;
        this.data = data;
        this.rank = Manager.getRank(id);
        this.avatar = avatar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GamePlayer player = (GamePlayer) o;
        return id == player.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
