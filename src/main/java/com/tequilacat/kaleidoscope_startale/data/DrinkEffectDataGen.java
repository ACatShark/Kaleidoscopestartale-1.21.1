package com.tequilacat.kaleidoscope_startale.data;

import com.github.ysbbbbbb.kaleidoscopetavern.datamap.data.DrinkEffectData;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModEffects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.JsonOps;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.init.KSEffects;
import com.tequilacat.kaleidoscope_startale.init.KSItems;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.ToIntFunction;

public class DrinkEffectDataGen implements DataProvider {
    private static final int[] SLIGHTLY_TIPSY_DURATIONS = {45, 30, 30, 20, 10};

    private static final ToIntFunction<String> ORDER_FIELDS = Util.make(new Object2IntOpenHashMap<>(), map -> {
        map.put("item", 0);
        map.defaultReturnValue(1);
    });

    private final Map<String, DrinkEffectData> data = Maps.newLinkedHashMap();
    private final PackOutput output;

    public DrinkEffectDataGen(PackOutput output) {
        this.output = output;
    }

    private void gather() {

        add(KSItems.ANCIENT_FRUIT_WINE,
                List.of(effect(KSEffects.BLUE_HOUR, 80, 0)),
                List.of(effect(KSEffects.BLUE_HOUR, 240, 0)),
                List.of(effect(KSEffects.BLUE_HOUR, 480, 0)),
                List.of(effect(KSEffects.BLUE_HOUR, 960, 0))
        );
        add(KSItems.GRAPE_FLAVORED_WINE,
                List.of(
                        effect(MobEffects.FIRE_RESISTANCE, 60, 0),
                        effect(MobEffects.REGENERATION, 60, 0),
                        effect(ModEffects.LONG_REACH, 60, 0)
                ),
                List.of(
                        effect(MobEffects.FIRE_RESISTANCE, 120, 0),
                        effect(MobEffects.REGENERATION, 120, 0),
                        effect(ModEffects.LONG_REACH, 120, 0)
                ),
                List.of(
                        effect(MobEffects.FIRE_RESISTANCE, 270, 0),
                        effect(MobEffects.REGENERATION, 270, 0),
                        effect(ModEffects.LONG_REACH, 270, 0)
                ),
                List.of(
                        effect(MobEffects.FIRE_RESISTANCE, 540, 0),
                        effect(MobEffects.REGENERATION, 540, 0),
                        effect(ModEffects.LONG_REACH, 540, 0)
                )
        );
        addAllLevels(KSItems.JOJA_COLA,
                List.of(effect(MobEffects.MOVEMENT_SPEED, 5, 4)),
                List.of(effect(MobEffects.MOVEMENT_SPEED, 30, 4)),
                List.of(effect(MobEffects.MOVEMENT_SPEED, 60, 4)),
                List.of(effect(MobEffects.MOVEMENT_SPEED, 120, 4)),
                List.of(effect(MobEffects.MOVEMENT_SPEED, 240, 4)),
                List.of(effect(MobEffects.MOVEMENT_SPEED, 480, 4))
        );
        addAllLevels(KSItems.RED_EYE_WINE,
                List.of(effect(KSEffects.BLOODTHIRST, 60, 0)),
                List.of(effect(KSEffects.BLOODTHIRST, 180, 0)),
                List.of(effect(KSEffects.BLOODTHIRST, 300, 0)),
                List.of(effect(KSEffects.BLOODTHIRST, 420, 0)),
                List.of(effect(KSEffects.BLOODTHIRST, 540, 0)),
                List.of(effect(KSEffects.BLOODTHIRST, 720, 0))
        );
        add(KSItems.YANJING_U7,
                List.of(effect(ModEffects.TOMB_RAIDER, 180, 0)),
                List.of(effect(ModEffects.TOMB_RAIDER, 420, 0)),
                List.of(effect(ModEffects.TOMB_RAIDER, 720, 0)),
                List.of(effect(ModEffects.TOMB_RAIDER, 2160, 0))
        );
        add(KSItems.REFRESHING_BEER,
                List.of(
                        effect(KSEffects.BLOODTHIRST, 45, 0),
                        effect(MobEffects.MOVEMENT_SPEED, 45, 2)
                ),
                List.of(
                        effect(KSEffects.BLOODTHIRST, 90, 0),
                        effect(MobEffects.MOVEMENT_SPEED, 90, 2)
                ),
                List.of(
                        effect(KSEffects.BLOODTHIRST, 180, 0),
                        effect(MobEffects.MOVEMENT_SPEED, 180, 2)
                ),
                List.of(
                        effect(KSEffects.BLOODTHIRST, 360, 0),
                        effect(MobEffects.MOVEMENT_SPEED, 360, 2)
                )
        );

        addCocktail(KSItems.ICE_ELF_COCKTAIL, effect(KSEffects.SMART, 180, 0));
        addCocktail(KSItems.BUBBLE_ECHO_COCKTAIL, effect(KSEffects.INSPIRATION, 180, 0));
        addCocktail(KSItems.SPRING_DRINK_COCKTAIL, effect(KSEffects.ILLUSION, 360, 0));
        addCocktail(KSItems.SPRING_DRINK_1_COCKTAIL, effect(KSEffects.ILLUSION, 360, 0));

    }

    @SafeVarargs
    public final void add(DeferredItem<? extends Item> key, List<DrinkEffectData.Entry>... levelAbove2) {
        var itemKey = BuiltInRegistries.ITEM.getKey(key.value());
        this.add(itemKey.getPath(), key, levelAbove2);
    }

    private DrinkEffectData.Entry effect(Holder<MobEffect> effect, int duration, int amplifier) {
        return new DrinkEffectData.Entry(effect, duration, amplifier, 1f);
    }

    private void addCocktail(DeferredItem<? extends Item> key, DrinkEffectData.Entry... entries) {
        var itemKey = BuiltInRegistries.ITEM.getKey(key.get());
        this.add(itemKey.getPath(), new DrinkEffectData(key, List.of(List.of(entries))));
    }

    @SafeVarargs
    public final void add(String fileName, DeferredItem<? extends Item> key, List<DrinkEffectData.Entry>... levelAbove2) {
        int length = levelAbove2.length;
        if (length == 0) {
            throw new IllegalArgumentException("At least one level above 2 must be provided");
        }
        this.addWithLevel2Effects(fileName, key,
                List.of(),
                levelAbove2[0],
                levelAbove2[Math.min(1, length - 1)],
                levelAbove2[Math.min(2, length - 1)],
                levelAbove2[Math.min(3, length - 1)]
        );
    }

    @SafeVarargs
    private void addWithLevel2Effects(DeferredItem<? extends Item> key, List<DrinkEffectData.Entry>... levelAbove1) {
        var itemKey = BuiltInRegistries.ITEM.getKey(key.get());
        this.addWithLevel2Effects(itemKey.getPath(), key, levelAbove1);
    }

    @SafeVarargs
    private void addWithLevel2Effects(String fileName, DeferredItem<? extends Item> key,
                                      List<DrinkEffectData.Entry>... levelAbove1) {
        int length = levelAbove1.length;
        if (length == 0) {
            throw new IllegalArgumentException("At least one level above 1 must be provided");
        }

        var effects = Lists.<List<DrinkEffectData.Entry>>newArrayListWithExpectedSize(SLIGHTLY_TIPSY_DURATIONS.length + 1);
        // 等级 1，固定为反胃 30s
        effects.add(List.of(new DrinkEffectData.Entry(MobEffects.CONFUSION, 30, 0, 1f)));
        // 等级 2-6，除难以下咽外，都会额外附带对应时长的微醺效果
        for (int i = 0; i < SLIGHTLY_TIPSY_DURATIONS.length; i++) {
            effects.add(withSlightlyTipsy(SLIGHTLY_TIPSY_DURATIONS[i], levelAbove1[Math.min(i, length - 1)]));
        }

        this.add(fileName, new DrinkEffectData(key, effects));
    }

    public void addAllLevels(DeferredItem<? extends Item> key,
                             List<DrinkEffectData.Entry> level1,
                             List<DrinkEffectData.Entry> level2,
                             List<DrinkEffectData.Entry> level3,
                             List<DrinkEffectData.Entry> level4,
                             List<DrinkEffectData.Entry> level5,
                             List<DrinkEffectData.Entry> level6) {
        var itemKey = BuiltInRegistries.ITEM.getKey(key.get());
        var effects = Lists.newArrayList(level1, level2, level3, level4, level5, level6);
        this.add(itemKey.getPath(), new DrinkEffectData(key, effects));
    }

    private List<DrinkEffectData.Entry> withSlightlyTipsy(int duration, List<DrinkEffectData.Entry> entries) {
        var result = Lists.<DrinkEffectData.Entry>newArrayListWithExpectedSize(entries.size() + 1);
        result.add(effect(ModEffects.SLIGHTLY_TIPSY, duration, 0));
        result.addAll(entries);
        return result;
    }

    public void add(String fileName, DrinkEffectData value) {
        this.data.put(fileName, value);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        this.gather();

        List<CompletableFuture<?>> futures = Lists.newArrayList();
        var pathProvider = this.output.createPathProvider(PackOutput.Target.DATA_PACK, "datamap/drink_effect");

        for (var entry : data.entrySet()) {
            DrinkEffectData.CODEC
                    .encodeStart(JsonOps.INSTANCE, entry.getValue())
                    .resultOrPartial(KaleidoscopeStartale.LOGGER::error)
                    .ifPresent(json -> {
                        var filePath = pathProvider.json(KaleidoscopeStartale.id(entry.getKey()));
                        var future = this.saveStable(cache, json, filePath);
                        futures.add(future);
                    });
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @SuppressWarnings("all")
    private CompletableFuture<?> saveStable(CachedOutput output, JsonElement json, Path path) {
        return CompletableFuture.runAsync(() -> {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                HashingOutputStream hashing = new HashingOutputStream(Hashing.sha1(), stream);

                try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(hashing, StandardCharsets.UTF_8))) {
                    writer.setSerializeNulls(false);
                    writer.setIndent("  ");
                    GsonHelper.writeValue(writer, json, Comparator.comparingInt(ORDER_FIELDS));
                }

                output.writeIfNeeded(path, stream.toByteArray(), hashing.hash());
            } catch (IOException ioexception) {
                KaleidoscopeStartale.LOGGER.error("Failed to save file to {}", path, ioexception);
            }
        }, Util.backgroundExecutor());
    }

    @Override
    public String getName() {
        return "Drink Effect Data";
    }
}