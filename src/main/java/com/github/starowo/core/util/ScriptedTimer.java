package com.github.starowo.core.util;

import com.github.starowo.mirai.game.BaseScriptedGame;
import org.graalvm.polyglot.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class ScriptedTimer extends Thread {

    public ReentrantLock lock = new ReentrantLock();
    public int count = 0;
    public Map<Integer, Value> script = new HashMap<>();
    public Value shouldStop;

    public ScriptedTimer(int count, Value shouldStop) {
        this.count = count;
        this.shouldStop = shouldStop;
    }

    @Override
    public void run() {
        while (true) {
            BaseScriptedGame.GLOBAL_SCRIPT_LOCK.lock();
            lock.lock();
            try {
                if (shouldStop.execute(count).asBoolean()) {
                    lock.unlock();
                    break;
                }
                count--;
                if (script.containsKey(count)) {
                    script.get(count).execute(count);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                BaseScriptedGame.GLOBAL_SCRIPT_LOCK.unlock();
            }
            lock.unlock();
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void on(int count, Value script, boolean sync) {
        if (sync) {
            this.script.put(count, script);
        } else {
            lock.lock();
            this.script.put(count, script);
            lock.unlock();
        }
    }

    public void off(int count, boolean sync) {
        if (sync) {
            this.script.remove(count);
        } else {
            lock.lock();
            this.script.remove(count);
            lock.unlock();
        }
    }

    public void reset(int count, boolean sync) {
        if (sync) {
            this.count = count;
        } else {
            lock.lock();
            this.count = count;
            lock.unlock();
        }
    }

    public void add(int count, boolean sync) {
        if (sync) {
            this.count += count;
        } else {
            lock.lock();
            this.count += count;
            lock.unlock();
        }
    }

}
