package tequila.cat.pigpig.client;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import tequila.cat.pigpig.init.ModSounds;

/** 榨汁机运转音的循环管理（每 300 tick 重放一次，方块消失时停止）。 */
public final class JuicerSounds {
    private static final int SOUND_LENGTH_TICK = 300;
    private static final float VOLUME = 0.1F;
    private static final float PITCH = 1.0F;

    private static final Map<Key, Playing> PLAYING = new HashMap<>();

    private JuicerSounds() {
    }

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

    public static void stop(Level level, BlockPos pos) {
        stop(new Key(level.dimension(), pos));
    }

    private static void stop(Key key) {
        Playing playing = PLAYING.remove(key);
        if (playing != null && playing.instance != null) {
            Minecraft.getInstance().getSoundManager().stop(playing.instance);
        }
    }

    private static void play(Key key, Level level, BlockPos pos) {
        SimpleSoundInstance instance = new SimpleSoundInstance(ModSounds.JUICER_WORKING.get(),
                net.minecraft.sounds.SoundSource.BLOCKS, VOLUME, PITCH, RandomSource.create(),
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        Minecraft.getInstance().getSoundManager().play(instance);

        Playing playing = new Playing();
        playing.instance = instance;
        playing.remaining = SOUND_LENGTH_TICK;
        PLAYING.put(key, playing);
    }

    public static void clear() {
        for (Playing playing : PLAYING.values()) {
            if (playing.instance != null) {
                Minecraft.getInstance().getSoundManager().stop(playing.instance);
            }
        }
        PLAYING.clear();
    }

    private record Key(ResourceKey<Level> dimension, BlockPos pos) {
    }

    private static final class Playing {
        private SimpleSoundInstance instance;
        private int remaining = SOUND_LENGTH_TICK;
    }
}
