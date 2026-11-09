package com.tequilacat.kaleidoscope_startale.client.renderer.block;

import com.tequilacat.kaleidoscope_startale.init.KSSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * 榨汁机音效管理工具类。
 * <p>
 * 以「维度 + 坐标」为键管理正在播放的榨汁机工作音效：
 * 制作中时循环续播，停止制作时结束音效，并支持统一清除所有音效。
 */
public class JuicerSounds {

    /** 单次音效时长（刻）。 */
    private static final int SOUND_LENGTH_TICK = 300;

    /** 音效音量。 */
    private static final float VOLUME = 0.1F;

    /** 音效音调。 */
    private static final float PITCH = 1.0F;

    /** 正在播放的音效映射。 */
    private static final Map<Key, Playing> PLAYING = new HashMap<>();

    /**
     * 每刻更新榨汁机音效。
     * <p>
     * 未在制作时停止对应音效；制作中且音效尚未结束时递减剩余时长，
     * 音效已结束或不存在时重新播放。
     *
     * @param level    世界
     * @param pos      方块位置
     * @param crafting 是否正在制作
     */
    public static void tick(Level level, BlockPos pos, boolean crafting) {
        Key key = new Key(level.dimension(), pos);
        if (!crafting) {
            stop(key);
            return;
        }

        Playing playing = PLAYING.get(key);
        if (playing != null && playing.remaining > 0) {
            --playing.remaining;
            return;
        }
        if (playing != null) {
            stop(key);
        }
        play(key, level, pos);
    }

    /**
     * 停止指定位置的音效。
     *
     * @param level 世界
     * @param pos   方块位置
     */
    public static void stop(Level level, BlockPos pos) {
        stop(new Key(level.dimension(), pos));
    }

    /**
     * 停止指定键对应的音效并移除记录。
     *
     * @param key 音效键
     */
    private static void stop(Key key) {
        Playing playing = PLAYING.remove(key);
        if (playing != null && playing.instance != null) {
            Minecraft.getInstance().getSoundManager().stop(playing.instance);
        }
    }

    /**
     * 播放指定位置的榨汁机工作音效并记录。
     * <p>
     * 以方块中心为声源位置播放，并记录实例与剩余时长。
     *
     * @param key   音效键
     * @param level 世界
     * @param pos   方块位置
     */
    private static void play(Key key, Level level, BlockPos pos) {
        SimpleSoundInstance instance = new SimpleSoundInstance(KSSounds.JUICER_WORKING.get(),
                SoundSource.BLOCKS, VOLUME, PITCH, RandomSource.create(),
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        Minecraft.getInstance().getSoundManager().play(instance);

        Playing playing = new Playing();
        playing.instance = instance;
        playing.remaining = SOUND_LENGTH_TICK;
        PLAYING.put(key, playing);
    }

    /**
     * 清除所有正在播放的榨汁机音效。
     */
    public static void clear() {
        for (Playing playing : PLAYING.values()) {
            if (playing.instance != null) {
                Minecraft.getInstance().getSoundManager().stop(playing.instance);
            }
        }
        PLAYING.clear();
    }

    /**
     * 音效键，由维度与方块位置组成。
     *
     * @param dimension 维度
     * @param pos       方块位置
     */
    private record Key(ResourceKey<Level> dimension, BlockPos pos) {
    }

    /**
     * 正在播放的音效状态。
     */
    private static final class Playing {
        private SimpleSoundInstance instance;
        private int remaining = SOUND_LENGTH_TICK;
    }
}