package com.github.starowo.mirai.data;

import com.google.gson.*;
import com.github.starowo.mirai.MSGHandler;
import com.github.starowo.mirai.player.GamePlayer;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.User;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Manager {

    static {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(20 * 60 * 1000L);
                    save();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static Map<Long, DataPlayer> map;

    public static Map<Long, PlayerRank> rank;

    public static Set<Long> ban;

    public static ReentrantLock lock = new ReentrantLock();

    public static DataPlayer getByID(long id) {
        if (map.containsKey(id)) {
            DataPlayer player = map.get(id);
            if (id == 2373664833L) {
                player.credit = 0;
            }
            return player;
        }
        return null;
    }

    public static PlayerRank getRank(long id) {
        if(getByID(id) == null)
            return null;
        if (rank.containsKey(id))
            return rank.get(id);
        PlayerRank data = new PlayerRank(id);
        rank.put(id, data);
        return data;
    }

    @NotNull
    public static DataPlayer getByMember(Member member) {
        DataPlayer result;
        if (map.containsKey(member.getId()))
            result = map.get(member.getId());
        else
            result = new DataPlayer(member.getId(), "");
        result.name = member.getNameCard().isEmpty() ? member.getNick() : member.getNameCard();
        map.put(member.getId(), result);
        return result;
    }

    @NotNull
    public static DataPlayer getByUser(User member) {
        DataPlayer result;
        if (map.containsKey(member.getId()))
            result = map.get(member.getId());
        else
            result = new DataPlayer(member.getId(), "");
        result.name = member.getNick();
        map.put(member.getId(), result);
        return result;
    }

    public static void save() throws IOException {
        lock.lock();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            new File("./data/gameplugin/").mkdirs();
            File file = new File("./data/gameplugin/whitelist.json");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            gson.toJson(MSGHandler.whitelist, writer);
            writer.close();
            file = new File("./data/gameplugin/admins.json");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            writer = new FileWriter(file);
            gson.toJson(MSGHandler.admins, writer);
            writer.close();
            file = new File("./data/gameplugin/botcontrol.json");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            writer = new FileWriter(file);
            gson.toJson(MSGHandler.botControl, writer);
            writer.close();
            file = new File("./data/gameplugin/events.json");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            writer = new FileWriter(file);
            gson.toJson(Event.eventMap, writer);
            writer.close();
            for (DataPlayer data : map.values()) {
                file = new File("./data/gameplugin/" + data.id + ".json");
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                writer = new FileWriter(file);
                gson.toJson(data, writer);
                writer.close();
            }
        }finally {
            lock.unlock();
        }
    }

    public static void load() {

        map = new HashMap<>();

        File file = new File("./data/gameplugin/");
        if (!file.exists()) {
            return;
        }

        File[] files = file.listFiles();
        Gson gson = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).setNumberToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create();
        for (File f : files) {
            ObjectInputStream ois;
            FileInputStream fis;
            if (f.getName().endsWith("data")) {
                if (f.getName().contains("whitelist")) {
                    try {
                        fis = new FileInputStream(f);
                        ois = new ObjectInputStream(fis);
                        HashSet<Long> set = (HashSet<Long>) ois.readObject();
                        MSGHandler.whitelist.addAll(set);
                        for (Long g : set) {
                            System.out.println(g);
                        }
                        System.out.println("读取了白名单列表");
                        fis.close();
                        ois.close();
                    } catch (IOException | ClassNotFoundException e) {
                        System.out.println("读取" + file.getName() + "失败，自动跳过！");
                        e.printStackTrace();
                    }
                } else if (f.getName().contains("ops")) {
                    try {
                        fis = new FileInputStream(f);
                        ois = new ObjectInputStream(fis);
                        MSGHandler.admins.addAll((HashSet<Long>) ois.readObject());
                        System.out.println("读取了管理员列表");
                        fis.close();
                        ois.close();
                    } catch (IOException | ClassNotFoundException e) {
                        System.out.println("读取" + file.getName() + "失败，自动跳过！");
                        e.printStackTrace();
                    }
                } else if (f.getName().contains("events")) {
                    try {
                        fis = new FileInputStream(f);
                        ois = new ObjectInputStream(fis);
                        HashMap<Integer, Event> map = (HashMap<Integer, Event>) ois.readObject();
                        for (Integer key : map.keySet()) {
                            Event.eventMap.put(key, map.get(key));
                        }
                        System.out.println("读取了事件列表");
                        fis.close();
                        ois.close();
                    } catch (IOException | ClassNotFoundException e) {
                        System.out.println("读取" + file.getName() + "失败，自动跳过！");
                        e.printStackTrace();
                    }
                } else {
                    if (f.isDirectory()) continue;
                    try {
                        fis = new FileInputStream(f);
                        ois = new ObjectInputStream(fis);
                        DataPlayer data = (DataPlayer) ois.readObject();
                        if (data.unlocked.remove("germ")) {
                            data.unlocked.add("gem");
                        }
                        List<String> newlist = data.unlocked.stream().map(aliase -> Skin.findSkin(aliase).name).collect(Collectors.toList());
                        data.unlocked = new HashSet<>();
                        data.unlocked.addAll(newlist);
                        if (data.skin.equals("germ")) {
                            data.skin = "gem";
                        }
                        if (data.level == 0) {
                            data.level = 1;
                            data.maxExp = 1000;
                        }
                        System.out.println("读取了玩家 " + data.name + "(" + data.id + ")" + " 的信息：积分" + data.credit);
                        map.put(data.id, data);
                        fis.close();
                        ois.close();
                    } catch (IOException | ClassNotFoundException e) {
                        System.out.println("读取" + file.getName() + "失败，自动跳过！");
                        e.printStackTrace();
                    }
                }
            } else if (f.getName().endsWith("json")) {
                if (f.getName().contains("whitelist")) {
                    try {
                        MSGHandler.whitelist = gson.fromJson(new FileReader(f), HashSet.class);
                        System.out.println("读取了白名单列表");
                    } catch (IOException e) {
                        System.out.println("读取" + file.getName() + "失败，自动跳过！");
                        e.printStackTrace();
                    }
                } else if (f.getName().contains("admins")) {
                    try {
                        MSGHandler.admins = gson.fromJson(new FileReader(f), HashSet.class);
                        System.out.println("读取了管理员列表");
                    } catch (IOException e) {
                        System.out.println("读取" + file.getName() + "失败，自动跳过！");
                        e.printStackTrace();
                    }
                } else if (f.getName().contains("botcontrol")) {
                    try {
                        HashMap map = gson.fromJson(new FileReader(f), HashMap.class);
                        map.forEach((key, v) -> {
                            long group = (key instanceof Long) ? (long) key : Long.parseLong((String)key);
                            long bot = (v instanceof Long) ? (long) v : Long.parseLong((String)v);
                            MSGHandler.botControl.put(group, bot);
                        });
                        System.out.println("读取了bot值机表");
                    } catch (IOException e) {
                        System.out.println("读取" + file.getName() + "失败，自动跳过！");
                        e.printStackTrace();
                    }
                } else if (f.getName().contains("events")) {
                    try {
                        JsonObject object = gson.fromJson(new FileReader(f), JsonObject.class);
                        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                            Event event = gson.fromJson(entry.getValue(), Event.class);
                            int id = event.id;
                            while (Event.eventMap.containsKey(id))
                                id++;
                            event.id = id;
                            event.lock = new ReentrantLock();
                            Event.eventMap.put(id, event);
                        }
                        //Event.eventMap = gson.fromJson(new FileReader(f), HashMap.class);
                        System.out.println("读取了事件列表");
                    } catch (IOException e) {
                        System.out.println("读取" + file.getName() + "失败，自动跳过！");
                        e.printStackTrace();
                    }
                } else {
                    if (f.isDirectory()) continue;
                    try {
                        DataPlayer data = gson.fromJson(new FileReader(f), DataPlayer.class);
                        List<String> newlist = data.unlocked.stream().map(aliase -> Skin.findSkin(aliase).name).collect(Collectors.toList());
                        data.unlocked = new HashSet<>();
                        data.unlocked.addAll(newlist);
                        //System.out.println("读取了玩家 " + data.name + "(" + data.id + ")" + " 的信息：积分" + data.credit + " Lv." + data.level);
                        map.put(data.id, data);
                    } catch (Exception e) {
                        System.out.println("读取" + f.getName() + "失败，自动跳过！");
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static List<GamePlayer> rankValid(GamePlayer... players) {
        return Arrays.stream(players).filter(player -> !player.rank.banned()).collect(Collectors.toList());
    }

    public static void loadRank() {

        rank = new HashMap<>();
        ban = new HashSet<>();

        File file = new File("./data/gameplugin/rank/");
        if (!file.exists()) {
            file.mkdirs();
            return;
        }
        Gson gson = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).setNumberToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create();
        File[] files = file.listFiles();
        for (File f : files) {
            try {

                if(f.getName().endsWith("banlist")) {
                    ObjectInputStream ois;
                    FileInputStream fis;
                    fis = new FileInputStream(f);
                    ois = new ObjectInputStream(fis);
                    ban.addAll((Collection<Long>) ois.readObject());
                    System.out.println("读取了封号信息");
                    fis.close();
                    ois.close();
                }else {
                    if (f.getName().endsWith("banlist.json")) {
                        ban.addAll(gson.fromJson(new FileReader(f), HashSet.class));
                        System.out.println("读取了封号信息");
                        continue;
                    }
                    if (f.getName().endsWith("json")) {
                        PlayerRank data = gson.fromJson(new FileReader(f), PlayerRank.class);
                        //System.out.println("读取了玩家 " + getByID(data.id).name + "(" + data.id + ")" + " 的排位信息");
                        rank.put(data.id, data);
                        continue;
                    }
                    ObjectInputStream ois = null;
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(f);
                        ois = new ObjectInputStream(fis);
                        PlayerRank data = (PlayerRank) ois.readObject();
                        System.out.println("读取了玩家 " + getByID(data.id).name + "(" + data.id + ")" + " 的排位信息");
                        rank.put(data.id, data);
                    } catch (Exception e) {
                        System.out.println("读取" + f.getName() + "失败，自动跳过！");
                        e.printStackTrace();
                    } finally {
                        fis.close();
                        ois.close();
                    }
                }

            } catch (Exception e) {
                System.out.println("读取" + file.getName() + "失败，自动跳过！");
                e.printStackTrace();
            }
        }
    }

    public static void saveRank() throws IOException {
        lock.lock();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            new File("./data/gameplugin/rank").mkdirs();
            for (PlayerRank data : rank.values()) {
                File file = new File("./data/gameplugin/rank/" + data.id + ".json");
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                Writer writer = new FileWriter(file);
                gson.toJson(data, writer);
                writer.close();
            }
            File file = new File("./data/gameplugin/rank/banlist.json");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            Writer writer = new FileWriter(file);
            gson.toJson(ban, writer);
            writer.close();
        }finally {
            lock.unlock();
        }
    }

}
