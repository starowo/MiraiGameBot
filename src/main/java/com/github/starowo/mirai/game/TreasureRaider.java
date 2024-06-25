package com.github.starowo.mirai.game;

import com.google.common.collect.Lists;
import com.github.starowo.mirai.MSGHandler;
import com.github.starowo.mirai.MiraiGamePlugin;
import com.github.starowo.mirai.data.Manager;
import com.github.starowo.mirai.player.GamePlayer;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TreasureRaider implements IGame {

    public static List<String> special_msg = Lists.newArrayList(
            "乌拉圭的人口有345.7万，同时，仅澳大利亚就有4700万只袋鼠。 如果袋鼠决定入侵乌拉圭，那么每一个乌拉圭人要打14只袋鼠，你不知道，你不在乎，你只关心你的宝藏。",
            "第17集,霹雳火和猛虎王交手,霹雳火使出雷霆半月斩第一刀,被猛虎王用剑齿挡住,没有造成任何伤害,第二刀和第三刀,都被猛虎王闪开,只有第四刀有效命中,洛洛看了一下扣血10%,此时霹雳火的战斗等级为八级,没有任何属性加成。第47集,对抗暴龙神,霹雳火利用月光回血之后,是出连环合雷霆半月斩,有效命中暴龙神7刀,暴龙神还在空中重重的摔了下来,又是一次伤害,一共八次有效伤害,暴龙神扣血90%,此时霹雳火战斗等级为18级,再加上有月光增强威力,可以看出这暴龙神的防御力简直是恐怖如斯,还有一个地方就是,猛虎王被12级的留影电光闪碰到一下,直接被打成病猫,而暴龙神被18级的天宇屠龙舞,命中后也只掉了一半血量,在风雪之城对超音速放水,超音速大招加导弹攻击只打掉暴龙神五滴血。我是在搞不懂某些无脑黑凭什么黑暴龙神防御力最差。",
            "《原神》是由米哈游自主研发的一款全新开放世界冒险游戏。游戏发生在一个被称作「提瓦特」的幻想世界,在这里,被神选中的人将被授予「神之眼」,导引元素之力。你将扮演一位名为「旅行者」的神秘角色，在自由的旅行中邂逅性格各异、能力独特的同伴们,和他们一起击败强敌,找回失散的亲人——同时,逐步发掘「原神」的真相。",
            "这种情况的前提下，会因为法/韦克内收空间中的幽体辐射下，形成以η/Ð宏粒子射线为场能的波态中子向心力场，然而当前时空的基本实体常量与太核质能固定不变，在其向太核中的吸收过程时，会与当前背景空间之间形成一个类似中心放射状环形空间，从而导致全部基质实体的引力动场能量瞬值反冲效应直接作用于υ太核核中  [ WoM表现为：⊙υ∝◎Ðℓ≈∮↑β√1.44№/ξ¾ⁿ≒§η, ￡≠√ℓº², (Θ┙iGE表格┊F0.7) ]   ，最终体现为其内核子内虚衡空间蓝向展开，当前时空形成时态空洞，而韦克低纬时空凭空出现基质实体。",
            "老干妈的酸辣并不能为广东肠粉提鲜，反而黄焖鸡米饭更让我感觉吃得放心；而实际上俄罗斯的优势在于地广人稀，假设原神的成功可以带领中国单机游戏旭日东升，那么我觉得穿西装的话，还是打领带比较得体一些。我觉得这个观点有点偏激了，首先外星人是紫色的，而且派大星和海绵宝宝一起去抓水母了。当然，太阳从东边升起的时候，也代表她会从西边落下。依古比古的毯子好像是红色的，就算小头爸爸不是大耳朵图图的亲生父亲，我还是认为肖战不应该偷猪猪侠的超级棒棒糖。",
            "我认为老坛酸菜面应该配三鹿奶粉，这样更能净化恒河水，但后果就是导致全球的金融危机，甚至富士山大爆炸，再说根据乘法口诀表推算出核弹中加满牛奶能威力更大，我俗称它大男孩，有人会觉得这观点偏激了，当然从达尔文的进化轮可以得知牛顿被苹果砸中后发现了奶酪要用嘴吃，也可以得出路易十五与奥巴马视频聊天通话中谈到是先有鸡，还是先有蛋的话题，总体来说，这都不影响上帝与夏娃在一起创造银河",
            "我觉得牛头是可以打上单的，开了大招之后比奥恩还能顶，w顶走刺客，q击飞前排就是一个团战搅屎棍，一般来说搅屎棍的材质最好是金属，因为木头可能会残留一些味道，而这些血的味道会引来鲨鱼，所以在大海中千万不要喝海水，海水中盐分太高只会加速你的脱水，如果你在洗衣机里的衣服已经脱水完毕，记得把衣服晾在比较干燥的地方，因为最近天气很冷，所以风湿骨病仅仅靠贴膏药是不足以根治的",
            "我记得我以前小时候春节去老家里，有一颗核弹，我以为是鞭炮，和大地红一起点了，当时噼里啪啦得，然后突然一朵蘑菇云平地而起，当时我就只记得两眼一黑，昏过去了，整个村子没了，幸好我是体育生，身体素质不错，住了几天院就没事了，几个月下来腿脚才利落，现在已经没事了，但是那种钻心的疼还是让我一生难忘。",
            "首先，韩信可以用一技能来加快时间的流速，这也就证明了永动机的存在，所以汽车内燃机的燃料可以选择3060ti，在这样的环境下，燃烧产生的氧气可以加速温室效应，这也就是相对论的基本原理，基于此，我们可以利用丁达尔效应，让我们在床上放养的羊，去吸收肉食动物光合作用产生的伽马射线，再利用光的波粒二象性，就可以在南极引发台风",
            "首先我想大家都明白墨西哥卷饼是个错误，其次，香蕉苹果手机壳这种玩法已经过时，在读初中的汉堡都玩过，现在它已经成为了甘蔗。你无法想象与理解我是如何变成一个触手上长满带着小夫的眼睛的大王酸浆鱿的！就像大王乌贼不被人所熟知，我们不知道为什么那些拿着话筒的胖子在洗车间清洗老坛酸菜，也许类似与加了邓氏鱼的意大利总统候选人？这类似于菊石，但那一只羊被清水冲洗后就被人遗忘了。",
            "这次世界杯，我支持巴萨，因为他们有科比，罚分线起跳三周半入水无水花，面对任何对手都可以发出eis球，状态好的话一杆清台，ko对手不成问题，天气好的话还可以考虑弯道超越，利用二传的巧妙配合击败对手。而且他当ADC是一件很合适的事，他的武器弹道下坠不明显，运气好点可以随机分配到法国方乘坐齐柏林飞艇，打败关底boss后很大几率爆橙装，并且能将新的卡牌加入牌库",
            "我觉得作为五费卡，雷泽最优解是带狼头走上路，一级先出钻石稿和黑曜石，骨灰带仿生泪滴，战灰带老寒腿，第一件神话最好做天顶剑，对面僵王博士上线咱们直接召唤bt-7274，一套E他脸上AWAQE直接带走",
            "毕竟一元一次方程比二元一次方程要更好解，虽然吃薯片多了会口干，但是开车的时候一定要注意安全，这样能够大大缓解温室效应，不过黄瓜+苹果榨成汁其实不好喝，所以上床睡觉之前记得要脱鞋，否则很容易弄脏床单，甚至造成石油短缺，更严重则珠穆朗玛峰会升高两百米，虽然勾股定理证明地壳为什么会活动，加上三角函数也证明得出苹果砸了牛顿之后发明出纳米不冻液用来浇花，倒进锅里小火煮到沸腾还是有可能洗干净衣服，但这目前还没有科学理论为基础，因此达尔文进化论证明苏打水经过一系列反应可以进化成人，而人工智能将来有可能跟海绵宝宝一起抓水母。"
    );

    public Group group;
    public List<GamePlayer> players;
    public HashSet<Integer> treasures = new HashSet<>();
    public HashMap<Integer, Integer> map = new HashMap<>();
    public HashMap<Integer, Integer> getscore = new HashMap<>();
    public HashSet<Integer> available = new HashSet<>();
    public HashMap<GamePlayer, Integer> chosen = new HashMap<>();
    public HashSet<Integer> chosen1 = new HashSet<>();
    public HashSet<GamePlayer> ffed = new HashSet<>();
    public HashSet<GamePlayer> afk = new HashSet<>();
    public HashSet<GamePlayer> received = new HashSet<>();
    public HashMap<GamePlayer, Integer> score = new HashMap<>();
    public int size;
    public int founded;
    public int round = 0;
    public int sec = 120;
    public Thread thread;
    public ReentrantLock lock = new ReentrantLock();
    int count;
    Random rand = new Random();
    long seed = rand.nextLong();
    private boolean waiting = true;

    public TreasureRaider(List<GamePlayer> players, Group group, int special) {
        rand = new Random(seed);
        this.group = group;
        this.players = players;
        size = special;
        init();
    }

    private void init() {
        if (size > 12) {
            count = 60;
        } else if (size > 9) {
            count = 40;
        } else if (size > 6) {
            count = 20;
        } else {
            count = 8;
        }
        for (int i = 0; i < count; i++) {
            while (!treasures.add(zip(rand.nextInt(size), rand.nextInt(size)))) ;
        }
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int pos = zip(i, j);
                available.add(pos);
                if (treasures.contains(pos))
                    continue;
                int num = findSurroundingTreasures(i, j);
                map.put(pos, num);
            }
        }
    }

    private int findSurroundingTreasures(int i, int j) {
        int num = 0;
        for (int x = Math.max(i - 1, 0); x < Math.min(i + 2, size); x++) {
            for (int y = Math.max(j - 1, 0); y < Math.min(j + 2, size); y++) {
                int pos = zip(x, y);
                if (treasures.contains(pos))
                    num++;
            }
        }
        return num;
    }

    private int zip(int x, int y) {
        return x << 4 | y;
    }

    private int[] unzip(int zipped) {
        return new int[]{zipped >> 4, zipped & 15};
    }

    @Override
    public void start() {
        try {
            if (!waiting)
                return;
            for (GamePlayer player : players) {
                score.put(player, 0);
            }
            waiting = false;
            lock.lock();
            update();
            (thread = new ThreadTime(this)).start();
        } finally {
            lock.unlock();
        }
    }

    private boolean update() {
        received.clear();
        received.addAll(ffed);
        updateMap();
        group.sendMessage("第" + round++ + "轮>>\r\n剩余宝藏数量:" + (count - founded));
        for (Map.Entry<GamePlayer, Integer> entry : chosen.entrySet()) {
            if (getscore.containsKey(entry.getValue())) {
                int count = getscore.get(entry.getValue());
                score.put(entry.getKey(), score.getOrDefault(entry.getKey(), 0) + 60 / count);
            }
        }
        getscore.clear();
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
        chosen1.clear();
        chosen.clear();
        sec = 120;
        MessageChainBuilder messages = new MessageChainBuilder();
        if (!score.isEmpty()) {
            messages.append("当前得分>>");
            for (Map.Entry<GamePlayer, Integer> entry : score.entrySet()) {
                messages.append("\r\n")
                        .append(entry.getKey().name)
                        .append(": ")
                        .append(String.valueOf(entry.getValue()));
            }
            group.sendMessage(messages.build());
        }
        return check();
    }

    private void updateMap() {
        for (int pos : chosen1) {
            available.remove(pos);
            if (treasures.contains(pos)) {
                founded++;
            }
        }
    }

    private BufferedImage getMapImage() {
        BufferedImage iBlank = new BufferedImage(size * 64 + 64, size * 64 + 64, 2);
        Graphics2D g = iBlank.createGraphics();
        //g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER));
        g.setColor(Color.white);
        g.fillRect(0, 0, size * 64 + 64, size * 64 + 64);
        Font font = new Font("微软雅黑", Font.BOLD, 32);
        g.setFont(font);
        g.setColor(Color.BLACK);
        char x = 'A';
        for (int i = 0; i < size; i++, x++) {
            int w = g.getFontMetrics().charWidth(x);
            int h = g.getFontMetrics().getHeight();
            int centerX = 64 * (i + 1) + (64 - w) / 2;
            int centerY = (64 - h) / 2 + g.getFontMetrics().getAscent();
            g.drawString(String.valueOf(x), centerX, centerY);
        }
        for (int i = 0; i < size; i++) {
            int w = g.getFontMetrics().stringWidth(String.valueOf(i + 1));
            int h = g.getFontMetrics().getHeight();
            int centerX = (64 - w) / 2;
            int centerY = 64 * (i + 1) + (64 - h) / 2 + g.getFontMetrics().getAscent();
            g.drawString(String.valueOf(i + 1), centerX, centerY);
        }
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int pos = zip(i, j);
                if (available.contains(pos)) {
                    g.fillRect(64 * (i + 1), 64 * (j + 1), 64, 64);
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawRect(64 * (i + 1), 64 * (j + 1), 63, 63);
                    g.setColor(Color.BLACK);
                    continue;
                }
                if (chosen1.contains(pos)) {
                    g.setColor(Color.ORANGE);
                    g.fillRect(64 * (i + 1), 64 * (j + 1), 64, 64);
                    g.setColor(Color.BLACK);
                }
                g.drawRect(64 * (i + 1), 64 * (j + 1), 63, 63);
                if (treasures.contains(pos)) {
                    int w = g.getFontMetrics().charWidth('*');
                    int h = g.getFontMetrics().getHeight();
                    int centerX = 64 * (i + 1) + (64 - w) / 2;
                    int centerY = 64 * (j + 1) + (64 - h) / 2 + g.getFontMetrics().getAscent();
                    g.drawString("*", centerX, centerY);
                    continue;
                }
                int num = map.get(pos);
                int w = g.getFontMetrics().stringWidth(String.valueOf(num));
                int h = g.getFontMetrics().getHeight();
                int centerX = 64 * (i + 1) + (64 - w) / 2;
                int centerY = 64 * (j + 1) + (64 - h) / 2 + g.getFontMetrics().getAscent();
                g.drawString(String.valueOf(num), centerX, centerY);
            }
        }
        g.dispose();
        return iBlank;
    }

    @Override
    public Message input(GamePlayer player, MessageChain msg) {
        lock.lock();
        try {
            String text = msg.contentToString().trim();
            if (text.equals("投降")) {
                ffed.add(player);
                received.add(player);
                return new MessageChainBuilder()
                        .append("你退出了游戏")
                        .build();
            }
            if (!received.contains(player)) {
                String x = text.substring(0, 1).toUpperCase(Locale.ROOT);
                String y = text.substring(1);
            /*if(text.length() != 2) {
                return new MessageChainBuilder()
                        .append("指令错误了呢，好好检查一下吧~")
                        .build();
            }*/
                String regEx = "[^0-9]";
                Pattern p = Pattern.compile(regEx);
                Matcher m = p.matcher(y);
                if (m.find()) {
                    return new MessageChainBuilder()
                            .append("指令错误了呢，好好检查一下吧~")
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
                    case "G":
                        posX = 6;
                        break;
                    case "H":
                        posX = 7;
                        break;
                    case "I":
                        posX = 8;
                        break;
                    case "J":
                        posX = 9;
                        break;
                    case "K":
                        posX = 10;
                        break;
                    case "L":
                        posX = 11;
                        break;
                    case "M":
                        posX = 12;
                        break;
                    case "N":
                        posX = 13;
                        break;
                    case "O":
                        posX = 14;
                        break;
                    case "P":
                        posX = 15;
                        break;
                    default:
                        return new MessageChainBuilder()
                                .append("\"")
                                .append(x)
                                .append("\"不是一个有效的横坐标！")
                                .build();
                }
                if (posX >= size) {
                    return new MessageChainBuilder()
                            .append("\"")
                            .append(x)
                            .append("\"不是一个有效的横坐标！")
                            .build();
                }
                if (posY >= size || posY < 0) {
                    return new MessageChainBuilder()
                            .append("\"")
                            .append(y)
                            .append("\"不是一个有效的纵坐标！")
                            .build();
                }
                int pos = zip(posX, posY);
                if (!available.contains(pos)) {
                    return new MessageChainBuilder()
                            .append("这个位置已经挖掘过了！")
                            .build();
                }
                received.add(player);
                afk.remove(player);
                chosen.put(player, pos);
                chosen1.add(pos);
                if (treasures.contains(pos)) {
                    getscore.put(pos, getscore.getOrDefault(pos, 0) + 1);
                    return new MessageChainBuilder()
                            .append("你挖到一个宝藏！")
                            .build();
                }
                int num = map.get(pos);
                if (num > 0) {
                    if (rand.nextFloat() < 0.009) {
                        return new MessageChainBuilder()
                                .append("你什么也没挖到，但是")
                                .append(special_msg.get(rand.nextInt(special_msg.size())))
                                .build();
                    }
                    return new MessageChainBuilder()
                            .append("你什么也没挖到，但是通过探测器发现附近有")
                            .append(String.valueOf(num))
                            .append("个宝藏")
                            .build();
                }
                return new MessageChainBuilder()
                        .append("你什么也没挖到，而且通过探测器发现附近也没有宝藏")
                        .build();
            }
            return new MessageChainBuilder()
                    .append("你已经执行过本轮的操作了！")
                    .build();
        } catch (Exception E) {
            return new MessageChainBuilder()
                    .append("指令错误了呢~ 好好检查一下吧")
                    .append(new QuoteReply(msg))
                    .build();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean allowGroup() {
        return false;
    }

    @Override
    public boolean isWaiting() {
        return waiting;
    }

    @Override
    public int getMaxPlayer() {
        return size > 12 ? 12 : size > 9 ? 9 : size > 6 ? 6 : 4;
    }

    @Override
    public int getMinPlayer() {
        return 2;
    }

    @Override
    public void addPlayer(GamePlayer activePlayer) {
        players.add(activePlayer);
        if(activePlayer.rank.banned()) {
            group.sendMessage("玩家"+activePlayer.name+"的排位信息被封禁，他的游戏结果不会计入本局的rank分");
        }
    }

    public boolean check() {
        return available.size() <= 0 || founded >= count || ffed.size() == players.size();
    }

    @Override
    public void stop() {
        try {
            players.sort((p1, p2) -> Integer.compare(score.getOrDefault(p2, 0), score.getOrDefault(p1, 0)));
            List<GamePlayer> ranks = Manager.rankValid(players.toArray(new GamePlayer[0]));
            float total = 0f;
            int totalRank = 0;
            float median = 0;
            int playerSize = ranks.size();
            for (int i = 0; i < playerSize; i++) {
                GamePlayer player = ranks.get(i);
                if (i == playerSize / 2) {
                    median = (median + score.getOrDefault(player, 0));
                } else if (playerSize % 2 == 0 && i == (playerSize - 1) / 2) {
                    median = (median + score.getOrDefault(player, 0));
                }
                total += score.getOrDefault(player, 0);
                totalRank += player.rank.scores.getOrDefault("夺宝奇兵", 1200);
            }
            if (playerSize % 2 == 0)
                median /= 2f;
            float avg = total / ranks.size();
            int avgRank = totalRank / ranks.size();
            int highest = score.getOrDefault(ranks.get(0), 0);
            MessageChainBuilder endMsg = new MessageChainBuilder();
            for (int i = 0; i < players.size(); i++) {
                GamePlayer player = players.get(i);
                int sc = score.getOrDefault(player, 0);
                int credits = players.size() * 50;
                float k = 1f;//Math.min(playerSize / 8f, 1f);
                for (int j = 0; j < players.size(); j++) {
                    GamePlayer after = players.get(j);
                    if (score.get(after) <= sc)
                        credits += (int) (score.get(after) * k);
                }
                credits = player.data.reducedCreditEarn(credits);
                int exp = Math.max(Math.min(credits * 3, 2000), 500);
                float k1 = Math.max(-1, (sc - median) / (highest - median)) * Math.min(1.5f, Math.max(1, 1 + 10 * (sc / (playerSize * avg) - 1f / playerSize * 1.17f)));
                if(playerSize >= 2 && !player.rank.banned()) {
                    int rank = player.rank.process("夺宝奇兵", k1, avgRank);
                    endMsg.append(String.valueOf(i + 1))
                            .append(". ")
                            .append(new At(player.id))
                            .append(" 得分：")
                            .append(String.valueOf(sc))
                            .append(" 积分：+")
                            .append(String.valueOf(credits))
                            .append(" 经验：+")
                            .append(String.valueOf(exp))
                            .append(" rank分：").append(rank > 0 ? "+" : "")
                            .append(String.valueOf(rank))
                            .append("\r\n");
                }else {
                    endMsg.append(String.valueOf(i + 1))
                            .append(". ")
                            .append(new At(player.id))
                            .append(" 得分：")
                            .append(String.valueOf(sc))
                            .append(" 积分：+")
                            .append(String.valueOf(credits))
                            .append(" 经验：+")
                            .append(String.valueOf(exp))
                            .append("\r\n");
                }
                player.data.credit += credits;
                player.data.addExp(exp);
            }
            GamePlayer player = players.get(0);
            endMsg.append("\r\n 恭喜胜者——")
                    .append(new At(player.id))
                    .append(" !!!");
            group.sendMessage(endMsg.build());
            group.sendMessage("本局随机数种子：" + seed);
            available.clear();
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
                ExternalResource resource = ExternalResource.create(baImage);
                group.sendMessage(group.uploadImage(resource));
                try {
                    resource.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable e) {
            group.sendMessage("结算失败，游戏中止！");
            group.sendMessage(e + Arrays.toString(e.getStackTrace()));
        }
        MiraiGamePlugin.INSTANCE.players_map.get(this.group.getId()).clear();
        MiraiGamePlugin.INSTANCE.games.remove(group.getId());
        thread.interrupt();
        try {
            Manager.saveRank();
            Manager.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remove(GamePlayer activePlayer) {
        players.remove(activePlayer);
    }

    public static class ThreadTime extends Thread {
        private final TreasureRaider game;

        public ThreadTime(TreasureRaider game) {
            this.game = game;
        }

        public void run() {
            while (true) {

                game.lock.lock();
                try {
                    if (game.received.size() + game.afk.size() >= game.players.size()) {
                        if (game.update()) {
                            game.stop();
                            break;
                        }
                    }
                    if (game.sec == 60)
                        game.group.sendMessage("剩余时间60秒");
                    if (game.sec == 30)
                        game.group.sendMessage("剩余时间30秒");
                    if (game.sec == 15)
                        game.group.sendMessage("剩余时间15秒");
                    if (game.sec == 5)
                        game.group.sendMessage("剩余时间5秒");
                    if (game.sec-- < 0) {
                        for (GamePlayer player : game.players) {
                            if (!game.received.contains(player)) {
                                game.afk.add(player);
                            }
                        }
                        game.received.addAll(game.players);
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
