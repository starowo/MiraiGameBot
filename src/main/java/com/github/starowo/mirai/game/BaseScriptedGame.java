package com.github.starowo.mirai.game;

import com.github.starowo.mirai.MiraiGamePlugin;
import com.github.starowo.mirai.data.DataPlayer;
import com.github.starowo.mirai.data.Manager;
import com.github.starowo.mirai.game.room.IRoom;
import com.github.starowo.mirai.player.GamePlayer;
import com.github.starowo.core.util.FileUtils;
import com.github.starowo.core.util.ImageBuilder;
import com.github.starowo.core.util.ScriptedTimer;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.io.File;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BaseScriptedGame implements IGame {

    public static final String BASE_PATH = "./resources/script/game/";
    public static final Lock GLOBAL_SCRIPT_LOCK = new ReentrantLock();

    Context graal;
    protected String scriptPath;

    protected IRoom room;
    protected String[] args;

    public BaseScriptedGame(String scriptPath, String[] args) {
        this.scriptPath = scriptPath;
        File scriptFile = new File(BASE_PATH + scriptPath);
        this.args = args;
        String script = FileUtils.readFromFile(scriptFile);
        GLOBAL_SCRIPT_LOCK.lock();
        try {
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            graal = Context.newBuilder("js").allowAllAccess(true).allowHostClassLookup(clazz -> true).build();
            graal.eval("js", script);
            setupScriptEnvironment();
            Thread.currentThread().setContextClassLoader(old);
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            GLOBAL_SCRIPT_LOCK.unlock();
        }
    }

    public BaseScriptedGame(String scriptPath) {
        this(scriptPath, new String[0]);
    }

    protected void setupScriptEnvironment() {
        graal.getBindings("js").putMember("HashMap", java.util.HashMap.class);
        graal.getBindings("js").putMember("ArrayList", java.util.ArrayList.class);
        graal.getBindings("js").putMember("HashSet", java.util.HashSet.class);
        graal.getBindings("js").putMember("Timer", ScriptedTimer.class);
        graal.getBindings("js").putMember("Random", java.util.Random.class);
        graal.getBindings("js").putMember("MessageChainBuilder", MessageChainBuilder.class);
        graal.getBindings("js").putMember("At", net.mamoe.mirai.message.data.At.class);
        graal.getBindings("js").putMember("Manager", Manager.class);
        graal.getBindings("js").putMember("ImageBuilder", ImageBuilder.class);
        // image utils
        try {
            graal.getBindings("js").putMember("BufferedImage", Class.forName("java.awt.image.BufferedImage"));
            graal.getBindings("js").putMember("Graphics2D", Class.forName("java.awt.Graphics2D"));
            graal.getBindings("js").putMember("Color", Class.forName("java.awt.Color"));
            graal.getBindings("js").putMember("Image", Class.forName("java.awt.Image"));
            graal.getBindings("js").putMember("ImageIO", Class.forName("javax.imageio.ImageIO"));
            graal.getBindings("js").putMember("Font", Class.forName("java.awt.Font"));
            graal.getBindings("js").putMember("ByteArrayOutputStream", Class.forName("java.io.ByteArrayOutputStream"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        graal.eval("js", "function print(msg) { Java.type('java.lang.System').out.println(msg); }");
    }

    public void init(IRoom room) {
        this.room = room;
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        graal.getBindings("js").putMember("room", room);
        graal.getBindings("js").putMember("game", this);
        graal.getBindings("js").putMember("plugin", MiraiGamePlugin.INSTANCE);
        Thread.currentThread().setContextClassLoader(old);
    }

    public void start() {
        GLOBAL_SCRIPT_LOCK.lock();
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            graal.getBindings("js").getMember("start").execute();
        } catch (Exception e) {
            e.printStackTrace();
            room.send("发生错误，请联系星星修复。");
        } finally {
            GLOBAL_SCRIPT_LOCK.unlock();
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    @Override
    public String getName() {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        GLOBAL_SCRIPT_LOCK.lock();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            return graal.getBindings("js").getMember("NAME").asString();
        } catch (Exception e) {
            e.printStackTrace();
            room.send("发生错误，请联系星星修复。");
        } finally {
            GLOBAL_SCRIPT_LOCK.unlock();
            Thread.currentThread().setContextClassLoader(old);
        }
        return "";
    }

    @Override
    public String getRule() {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        GLOBAL_SCRIPT_LOCK.lock();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            return graal.getBindings("js").getMember("RULE").asString();
        } catch (Exception e) {
            e.printStackTrace();
            room.send("发生错误，请联系星星修复。");
        } finally {
            GLOBAL_SCRIPT_LOCK.unlock();
            Thread.currentThread().setContextClassLoader(old);
        }
        return "";
    }

    public void stop() {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        GLOBAL_SCRIPT_LOCK.lock();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            graal.getBindings("js").getMember("stop").execute();
        } catch (Exception e) {
            e.printStackTrace();
            room.send("发生错误，请联系星星修复。");
        } finally {
            GLOBAL_SCRIPT_LOCK.unlock();
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public boolean allowGroup() {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        GLOBAL_SCRIPT_LOCK.lock();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            return graal.getBindings("js").getMember("ALLOW_GROUP").asBoolean();
        } catch (Exception e) {
            e.printStackTrace();
            room.send("发生错误，请联系星星修复。");
        } finally {
            GLOBAL_SCRIPT_LOCK.unlock();
            Thread.currentThread().setContextClassLoader(old);
        }
        return false;
    }

    public boolean isWaiting() {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        GLOBAL_SCRIPT_LOCK.lock();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            return graal.getBindings("js").getMember("isWaiting").execute().asBoolean();
        } catch (Exception e) {
            e.printStackTrace();
            room.send("发生错误，请联系星星修复。");
        } finally {
            GLOBAL_SCRIPT_LOCK.unlock();
            Thread.currentThread().setContextClassLoader(old);
        }
        return true;
    }

    public int getMaxPlayer() {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        GLOBAL_SCRIPT_LOCK.lock();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            return graal.getBindings("js").getMember("MAX_PLAYER").asInt();
        } catch (Exception e) {
            e.printStackTrace();
            room.send("发生错误，请联系星星修复。");
        } finally {
            GLOBAL_SCRIPT_LOCK.unlock();
            Thread.currentThread().setContextClassLoader(old);
        }
        return 2;
    }

    public void addPlayer(GamePlayer activePlayer) {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        GLOBAL_SCRIPT_LOCK.lock();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            graal.getBindings("js").getMember("addPlayer").execute(activePlayer);
        } catch (Exception e) {
            e.printStackTrace();
            room.send("发生错误，请联系星星修复。");
        } finally {
            GLOBAL_SCRIPT_LOCK.unlock();
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public void remove(GamePlayer activePlayer) {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        GLOBAL_SCRIPT_LOCK.lock();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            graal.getBindings("js").getMember("remove").execute(activePlayer);
        } catch (Exception e) {
            e.printStackTrace();
            room.send("发生错误，请联系星星修复。");
        } finally {
            GLOBAL_SCRIPT_LOCK.unlock();
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    @Override
    public Message input(GamePlayer player, MessageChain msg, boolean group, boolean at) {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        GLOBAL_SCRIPT_LOCK.lock();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            return graal.getBindings("js").getMember("input").execute(player, msg, group, at).as(Message.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new MessageChainBuilder().append("发生错误，请联系星星修复。").build();
        } finally {
            GLOBAL_SCRIPT_LOCK.unlock();
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public boolean canJoin(DataPlayer player) {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        GLOBAL_SCRIPT_LOCK.lock();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            return graal.getBindings("js").getMember("canJoin").execute(player).asBoolean();
        } catch (Exception e) {
            e.printStackTrace();
            room.send("发生错误，请联系星星修复。");
        } finally {
            GLOBAL_SCRIPT_LOCK.unlock();
            Thread.currentThread().setContextClassLoader(old);
        }
        return false;
    }

    @Override
    public boolean needAt() {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        GLOBAL_SCRIPT_LOCK.lock();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            Value member = graal.getBindings("js").getMember("NEED_AT");
            if (member.isNull()) {
                return true;
            } else {
                return member.asBoolean();
            }
        } catch (Exception e) {
            e.printStackTrace();
            room.send("发生错误，请联系星星修复。");
        } finally {
            GLOBAL_SCRIPT_LOCK.unlock();
            Thread.currentThread().setContextClassLoader(old);
        }
        return false;
    }
}
