package com.github.starowo.mirai.game;

import com.github.starowo.mirai.MSGHandler;
import com.github.starowo.mirai.MiraiGamePlugin;
import com.github.starowo.mirai.player.GamePlayer;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.*;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sokoban implements IGame {

    private static final BufferedImage target;

    static {
        BufferedImage image = new BufferedImage(64, 64, 2);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.GREEN);
        g.fillArc(0, 0, 64, 64, 0, 360);
        g.dispose();
        target = image;
    }

    public GamePlayer player1;
    public GamePlayer player2;
    public int status = 0;
    public boolean acting = true;
    public HashMap<Integer, Integer> map = new HashMap<>();
    public List<HashMap<Integer, Integer>> history = new ArrayList<>();
    public int pos;
    public List<Integer> history_pos = new ArrayList<>();
    public HashSet<Integer> targets = new HashSet<>();
    public Group group;
    public ReentrantLock lock = new ReentrantLock();
    Random rand = new Random();
    long seed = rand.nextLong();
    private int time = 360;
    private boolean flag = false;

    public Sokoban(Group group) {
        rand = new Random(seed);
        this.group = group;
    }

    private int zip(int x, int y) {
        return x << 3 | y;
    }

    private int[] unzip(int zipped) {
        return new int[]{zipped >> 3, zipped & 7};
    }

    @Override
    public void start() {
        lock.lock();
        status = 1;
        init();
        GamePlayer player = acting ? player1 : player2;
        group.sendMessage(new MessageChainBuilder()
                .append(new At(player.id))
                .append(" 先手！")
                .build());
        update();
        lock.unlock();
    }

    private void update() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] baImage = null;
        try {
            ImageIO.write(getMapImage(), "png", os);
            baImage = os.toByteArray();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (baImage != null) {
            MSGHandler.asyncSendImage(group, baImage);
        }
        GamePlayer player = acting ? player1 : player2;
        if (status == 1) {
            group.sendMessage(new MessageChainBuilder()
                    .append(new At(player.id))
                    .append(" 请放置一个箱子")
                    .build());
            return;
        }
        if (status == 2) {
            group.sendMessage(new MessageChainBuilder()
                    .append(new At(player.id))
                    .append(" 请放置一个目标点")
                    .build());
            return;
        }
        if (status == 3) {
            group.sendMessage(new MessageChainBuilder()
                    .append(new At(player.id))
                    .append(" 请判断当前局面能不能将箱子全部推到目标点上")
                    .build());
            return;
        }
        if (status == 4) {
            if (check()) {
                stop();
            }
        }
    }

    @Override
    public boolean needAt() {
        return false;
    }


    private RenderedImage getMapImage() {
        BufferedImage iBlank = new BufferedImage(6 * 64 + 64, 6 * 64 + 64, 2);
        Graphics2D g = iBlank.createGraphics();
        //g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER));
        g.setColor(Color.white);
        g.fillRect(0, 0, 6 * 64 + 64, 6 * 64 + 64);
        Font font = new Font("微软雅黑", Font.BOLD, 32);
        g.setFont(font);
        g.setColor(Color.BLACK);
        char x = 'A';
        for (int i = 0; i < 6; i++, x++) {
            int w = g.getFontMetrics().charWidth(x);
            int h = g.getFontMetrics().getHeight();
            int centerX = 64 * (i + 1) + (64 - w) / 2;
            int centerY = (64 - h) / 2 + g.getFontMetrics().getAscent();
            g.drawString(String.valueOf(x), centerX, centerY);
        }
        for (int i = 0; i < 6; i++) {
            int w = g.getFontMetrics().stringWidth(String.valueOf(i + 1));
            int h = g.getFontMetrics().getHeight();
            int centerX = (64 - w) / 2;
            int centerY = 64 * (i + 1) + (64 - h) / 2 + g.getFontMetrics().getAscent();
            g.drawString(String.valueOf(i + 1), centerX, centerY);
        }
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                int pos = zip(i, j);
                if (targets.contains(pos)) {
                    g.drawImage(target, 64 * (i + 1), 64 * (j + 1), null);
                }
                g.drawImage(getItemImage(map.get(pos), targets.contains(pos)), 64 * (i + 1), 64 * (j + 1), null);
            }
        }
        g.dispose();
        return iBlank;
    }

    private Image getItemImage(int id, boolean onTarget) {
        if (id == 0) {
            return new BufferedImage(64, 64, 2);
        }
        if (id == 1) {
            BufferedImage image = new BufferedImage(64, 64, 2);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.GRAY);
            g.fillRect(0, 0, 64, 64);
            g.setColor(Color.BLACK);
            g.drawRect(0, 0, 63, 63);
            g.dispose();
            return image;
        }
        if (id == 2) {
            BufferedImage image = new BufferedImage(64, 64, 2);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.BLUE);
            g.fillRect(0, 0, 64, 64);
            g.setColor(Color.BLACK);
            g.drawRect(0, 0, 63, 63);
            g.dispose();
            return image;
        }
        if (id == 3) {
            BufferedImage image = new BufferedImage(64, 64, 2);
            Graphics2D g = image.createGraphics();
            g.setColor(onTarget ? Color.ORANGE : Color.YELLOW);
            g.fillRect(0, 0, 64, 64);
            g.setColor(Color.BLACK);
            g.drawRect(0, 0, 63, 63);
            g.drawLine(0, 0, 63, 63);
            g.drawLine(0, 63, 63, 0);
            g.dispose();
            return image;
        }
        return new BufferedImage(64, 64, 2);
    }

    private void init() {

        acting = rand.nextBoolean();
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                int pos = zip(i, j);
                map.put(pos, 0);
            }
        }
        for (int i = 0; i < 3; i++) {
            int pos = zip(rand.nextInt(6), rand.nextInt(6));
            if (map.get(pos) == 1) {
                i--;
                continue;
            }
            map.put(pos, 1);
        }
        int pos;
        while (map.get((pos = zip(rand.nextInt(6), rand.nextInt(6)))) == 1) ;
        this.pos = pos;
        map.put(pos, 2);
    }

    @Override
    public Message input(GamePlayer player, MessageChain msg) {
        if (isWaiting())
            return null;
        String text = msg.contentToString().replaceAll("@" + group.getBot().getId(), "").trim();
        lock.lock();
        try {
            if (text.equals("投降")) {
                stop();
                return null;
            }
            if ((player.equals(player1) && !acting) || player.equals(player2) && acting) {
                return new MessageChainBuilder()
                        .append("现在不是你的回合哦~")
                        .append(new QuoteReply(msg))
                        .build();
            }
            if (status == 1 || status == 2) {
                String x = text.substring(0, 1).toUpperCase(Locale.ROOT);
                String y = text.substring(1);
                if (text.length() != 2) {
                    return new MessageChainBuilder()
                            .append("指令错误了呢，好好检查一下吧~")
                            .append(new QuoteReply(msg))
                            .build();
                }
                String regEx = "[^0-9]";
                Pattern p = Pattern.compile(regEx);
                Matcher m = p.matcher(y);
                if (m.find()) {
                    return new MessageChainBuilder()
                            .append("指令错误了呢，好好检查一下吧~")
                            .append(new QuoteReply(msg))
                            .build();
                }
                int posX;
                int posY = Integer.parseInt(y) - 1;
                switch (x) {
                    case "A":
                        posX = 0;
                        break;
                    case "B":
                        posX = 1;
                        break;
                    case "C":
                        posX = 2;
                        break;
                    case "D":
                        posX = 3;
                        break;
                    case "E":
                        posX = 4;
                        break;
                    case "F":
                        posX = 5;
                        break;
                    default:
                        return new MessageChainBuilder()
                                .append("\"")
                                .append(x)
                                .append("\"不是一个有效的横坐标！")
                                .append(new QuoteReply(msg))
                                .build();
                }
                if (posY >= 6 || posY < 0) {
                    return new MessageChainBuilder()
                            .append("\"")
                            .append(y)
                            .append("\"不是一个有效的纵坐标！")
                            .append(new QuoteReply(msg))
                            .build();
                }
                int pos = zip(posX, posY);
                if (status == 1 && map.get(pos) != 0) {
                    return new MessageChainBuilder()
                            .append("你不能将箱子放在这个位置！")
                            .append(new QuoteReply(msg))
                            .build();
                }
                if (status == 2 && (map.get(pos) == 1 || targets.contains(pos) || (flag && (map.get(pos) == 3)))) {
                    return new MessageChainBuilder()
                            .append("你不能将目标放在这个位置！")
                            .append(new QuoteReply(msg))
                            .build();
                }
                if (status == 1) {
                    map.put(pos, 3);
                    status = 2;
                    if (targets.contains(pos))
                        flag = true;
                    update();
                    return null;
                }
                if (status == 2) {
                    targets.add(pos);
                    flag = false;
                    status = 3;
                    acting = !acting;
                    update();
                    return null;
                }
            }
            if (status == 3) {
                if (text.equals("能")) {
                    status = 1;
                    update();
                    return null;
                }
                if (text.equals("不能")) {
                    status = 4;
                    acting = !acting;
                    GamePlayer p = acting ? player1 : player2;
                    group.sendMessage(new MessageChainBuilder()
                            .append(new At(p.id))
                            .append(" 请开始推箱子，6分钟内成功则胜利，否则失败！")
                            .build());
                    update();
                    new ThreadTime(this).start();
                    return null;
                }
            }
            if (status == 4) {
                int dir = -1;
                String[] args = text.toUpperCase(Locale.ROOT).split("");
                String regEx3 = "[A-Z]";
                Pattern p3 = Pattern.compile(regEx3);
                Matcher m3 = p3.matcher(args[0]);
                if (args.length == 2 && m3.find()) {
                    String x = args[0];
                    String y = args[1];
                    String regEx = "[^0-9]";
                    Pattern p = Pattern.compile(regEx);
                    Matcher m = p.matcher(y);
                    if (m.find()) {
                        return new MessageChainBuilder()
                                .append("指令错误了呢，好好检查一下吧~")
                                .append(new QuoteReply(msg))
                                .build();
                    }
                    int posX;
                    int posY = Integer.parseInt(y) - 1;
                    switch (x) {
                        case "A":
                            posX = 0;
                            break;
                        case "B":
                            posX = 1;
                            break;
                        case "C":
                            posX = 2;
                            break;
                        case "D":
                            posX = 3;
                            break;
                        case "E":
                            posX = 4;
                            break;
                        case "F":
                            posX = 5;
                            break;
                        default:
                            return null;
                    }
                    if (posY >= 6 || posY < 0) {
                        return new MessageChainBuilder()
                                .append("指令错误了呢，好好检查一下吧~")
                                .append(new QuoteReply(msg))
                                .build();
                    }
                    int pos = zip(posX, posY);
                    if (hasWay(pos)) {
                        moveTo(pos);
                        update();
                        return null;
                    } else {
                        return new MessageChainBuilder()
                                .append("你不能这么移动")
                                .append(new QuoteReply(msg))
                                .build();
                    }
                }
                String regEx = "[^0-9]";
                Pattern p = Pattern.compile(regEx);
                Matcher m = p.matcher(text);
                String cstr = m.replaceAll("");
                if (cstr.isEmpty()) cstr = "1";
                int count = Integer.parseInt(cstr);
                String regEx2 = "[0-9]";
                Pattern p2 = Pattern.compile(regEx2);
                Matcher m2 = p2.matcher(text);
                text = m2.replaceAll("");
                switch (text) {
                    case "上":
                        dir = 0;
                        break;
                    case "下":
                        dir = 1;
                        break;
                    case "左":
                        dir = 2;
                        break;
                    case "右":
                        dir = 3;
                        break;
                }
                if (dir > -1) {
                    boolean b = true;
                    for (int i = 0; i < count; i++) {
                        if (move(dir)) {
                            b = false;
                        } else {
                            break;
                        }
                    }
                    if (b) {
                        return new MessageChainBuilder()
                                .append("你不能这么移动")
                                .append(new QuoteReply(msg))
                                .build();
                    } else {
                        update();
                        return null;
                    }
                }
                if (text.equals("重来")) {
                    if (history.size() > 0) {
                        map = history.get(0);
                        pos = history_pos.get(0);
                        history.clear();
                        history_pos.clear();
                        update();
                        return null;
                    } else {
                        return new MessageChainBuilder()
                                .append("你还没开始呢！")
                                .append(new QuoteReply(msg))
                                .build();
                    }
                }
                if (text.equals("回退")) {
                    boolean b = true;
                    for (int i = 0; i < count; i++) {
                        if (history.size() > 0) {
                            map = history.get(history.size() - 1);
                            pos = history_pos.get(history_pos.size() - 1);
                            history.remove(history.size() - 1);
                            history_pos.remove(history_pos.size() - 1);
                            b = false;
                        } else {
                            break;
                        }
                    }
                    if (b) {
                        return new MessageChainBuilder()
                                .append("你还没开始呢！")
                                .append(new QuoteReply(msg))
                                .build();
                    } else {
                        update();
                        return null;
                    }
                }
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    private void moveTo(int pos) {
        history.add((HashMap<Integer, Integer>) map.clone());
        history_pos.add(this.pos);
        map.put(this.pos, 0);
        this.pos = pos;
        map.put(this.pos, 2);
    }

    private boolean hasWay(int target) {
        if (map.getOrDefault(target, -1) != 0) {
            return false;
        }
        HashMap<Integer, Integer> mapc = (HashMap<Integer, Integer>) map.clone();
        mapc.put(pos, 0);
        return findWay(mapc, pos, target);
    }

    private boolean findWay(HashMap<Integer, Integer> map, int pos, int target) {
        if (pos == target) return true;
        if (map.getOrDefault(pos, 1) == 0) {
            map.put(pos, 2);
            if (findWay(map, pos + (1 << 3), target)) {
                return true;
            }
            if (findWay(map, pos - (1 << 3), target)) {
                return true;
            }
            if (findWay(map, pos + 1, target)) {
                return true;
            }
            if (findWay(map, pos - 1, target)) {
                return true;
            }
            System.out.println(pos + ":false");
            map.put(pos, 1);
            return false;
        }
        System.out.println(pos + ":false");
        return false;
    }

    private boolean move(int dir) {
        int[] pos = unzip(this.pos);
        if (dir == 0) {
            pos[1] = pos[1] - 1;
            if (pos[1] < 0) {
                return false;
            }
            int n = map.get(zip(pos[0], pos[1]));
            if (n == 1) {
                return false;
            }
            if (n == 3) {
                int y = pos[1] - 1;
                if (y < 0)
                    return false;
                int n1 = map.get(zip(pos[0], y));
                if (n1 == 1) {
                    return false;
                }
                history.add((HashMap<Integer, Integer>) map.clone());
                history_pos.add(this.pos);
                map.put(this.pos, 0);
                this.pos = zip(pos[0], pos[1]);
                map.put(zip(pos[0], y), 3);
                map.put(this.pos, 2);
                return true;
            }
            history.add((HashMap<Integer, Integer>) map.clone());
            history_pos.add(this.pos);
            map.put(this.pos, 0);
            this.pos = zip(pos[0], pos[1]);
            map.put(this.pos, 2);
            return true;
        }
        if (dir == 1) {
            pos[1] = pos[1] + 1;
            if (pos[1] > 5) {
                return false;
            }
            int n = map.get(zip(pos[0], pos[1]));
            if (n == 1) {
                return false;
            }
            if (n == 3) {
                int y = pos[1] + 1;
                if (y > 5)
                    return false;
                int n1 = map.get(zip(pos[0], y));
                if (n1 == 1) {
                    return false;
                }
                history.add((HashMap<Integer, Integer>) map.clone());
                history_pos.add(this.pos);
                map.put(this.pos, 0);
                this.pos = zip(pos[0], pos[1]);
                map.put(zip(pos[0], y), 3);
                map.put(this.pos, 2);
                return true;
            }
            history.add((HashMap<Integer, Integer>) map.clone());
            history_pos.add(this.pos);
            map.put(this.pos, 0);
            this.pos = zip(pos[0], pos[1]);
            map.put(this.pos, 2);
            return true;
        }
        if (dir == 2) {
            pos[0] = pos[0] - 1;
            if (pos[0] < 0) {
                return false;
            }
            int n = map.get(zip(pos[0], pos[1]));
            if (n == 1) {
                return false;
            }
            if (n == 3) {
                int x = pos[0] - 1;
                if (x < 0)
                    return false;
                int n1 = map.get(zip(x, pos[1]));
                if (n1 == 1) {
                    return false;
                }
                history.add((HashMap<Integer, Integer>) map.clone());
                history_pos.add(this.pos);
                map.put(this.pos, 0);
                this.pos = zip(pos[0], pos[1]);
                map.put(zip(x, pos[1]), 3);
                map.put(this.pos, 2);
                return true;
            }
            history.add((HashMap<Integer, Integer>) map.clone());
            history_pos.add(this.pos);
            map.put(this.pos, 0);
            this.pos = zip(pos[0], pos[1]);
            map.put(this.pos, 2);
            return true;
        }
        if (dir == 3) {
            pos[0] = pos[0] + 1;
            if (pos[0] > 5) {
                return false;
            }
            int n = map.get(zip(pos[0], pos[1]));
            if (n == 1) {
                return false;
            }
            if (n == 3) {
                int x = pos[0] + 1;
                if (x > 5)
                    return false;
                int n1 = map.get(zip(x, pos[1]));
                if (n1 == 1) {
                    return false;
                }
                history.add((HashMap<Integer, Integer>) map.clone());
                history_pos.add(this.pos);
                map.put(this.pos, 0);
                this.pos = zip(pos[0], pos[1]);
                map.put(zip(x, pos[1]), 3);
                map.put(this.pos, 2);
                return true;
            }
            history.add((HashMap<Integer, Integer>) map.clone());
            history_pos.add(this.pos);
            map.put(this.pos, 0);
            this.pos = zip(pos[0], pos[1]);
            map.put(this.pos, 2);
            return true;
        }
        return false;
    }

    public boolean check() {
        for (int p : targets) {
            if (map.get(p) != 3)
                return false;
        }
        return true;
    }

    @Override
    public void stop() {
        status = 0;
        GamePlayer player;
        if (check()) {
            player = acting ? player1 : player2;
        } else {
            player = acting ? player2 : player1;
        }
        MessageChainBuilder endMsg = new MessageChainBuilder().append("游戏结束！\n");
        endMsg.append("\r\n 恭喜胜者——")
                .append(new At(player.id))
                .append(" !!!");
        group.sendMessage(endMsg.build());
        group.sendMessage("本局随机数种子：" + seed);
        MiraiGamePlugin.INSTANCE.players_map.get(this.group.getId()).clear();
        MiraiGamePlugin.INSTANCE.games.remove(group.getId());
    }

    @Override
    public boolean allowGroup() {
        return true;
    }

    @Override
    public boolean isWaiting() {
        return status == 0;
    }

    @Override
    public int getMaxPlayer() {
        return 2;
    }

    @Override
    public void addPlayer(GamePlayer activePlayer) {
        if (player1 == null)
            player1 = activePlayer;
        else
            player2 = activePlayer;
    }

    @Override
    public void remove(GamePlayer activePlayer) {
        if (player1.equals(activePlayer))
            player1 = null;
        else
            player2 = null;
    }

    public static class ThreadTime extends Thread {
        private final Sokoban game;

        public ThreadTime(Sokoban game) {
            this.game = game;
        }

        @Override
        public void run() {
            while (true) {

                game.lock.lock();
                try {
                    if (game.isWaiting())
                        break;
                    if (game.time == 300)
                        game.group.sendMessage("剩余时间五分钟");
                    if (game.time == 240)
                        game.group.sendMessage("剩余时间四分钟");
                    if (game.time == 180)
                        game.group.sendMessage("剩余时间三分钟");
                    if (game.time == 120)
                        game.group.sendMessage("剩余时间两分钟");
                    if (game.time == 60)
                        game.group.sendMessage("剩余时间60秒");
                    if (game.time == 30)
                        game.group.sendMessage("剩余时间30秒");
                    if (game.time == 20)
                        game.group.sendMessage("剩余时间20秒");
                    if (game.time == 10)
                        game.group.sendMessage("剩余时间10秒");
                    if (game.time == 5)
                        game.group.sendMessage("剩余时间5秒");
                    if (game.time-- <= 0) {
                        game.stop();
                    }
                } finally {
                    game.lock.unlock();
                }
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
