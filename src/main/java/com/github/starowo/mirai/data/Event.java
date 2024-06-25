package com.github.starowo.mirai.data;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

public class Event implements Serializable {

    public static HashMap<Integer, Event> eventMap = new HashMap<>();

    public int id;

    public transient ReentrantLock lock = new ReentrantLock();
    public String title;
    public String desc;
    public int max;
    public long owner;
    public HashSet<Long> enrolled;
    public HashSet<Long> sub;

    private Event(int id, String title, long owner) {
        this.id = id;
        this.title = title;
        this.owner = owner;
        this.enrolled = new HashSet<>();
        this.sub = new HashSet<>();
        desc = "";
        max = 0;
    }

    public static Event newEvent(String title, long owner) {
        int id = 0;
        while (eventMap.containsKey(id))
            id++;
        Event event = new Event(id, title, owner);
        eventMap.put(id, event);
        return event;
    }

    public static Event getEvent(int id) {
        return eventMap.getOrDefault(id, null);
    }

    public void notice(Bot bot, String message) {
        for (Long id : enrolled) {
            Friend friend = bot.getFriend(id);
            if(friend != null) {
                friend.sendMessage(new MessageChainBuilder().append("你报名的活动 ").append(title).append(" 的发起者向你发送了一条通知：\n").append(message).build());
            }
        }
        for (Long id : sub) {
            Friend friend = bot.getFriend(id);
            if(friend != null) {
                friend.sendMessage(new MessageChainBuilder().append("你替补的活动 ").append(title).append(" 的发起者向你发送了一条通知：\n").append(message).build());
            }
        }
    }

}
