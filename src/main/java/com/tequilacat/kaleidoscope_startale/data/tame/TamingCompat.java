package com.tequilacat.kaleidoscope_startale.data.tame;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;

/**
 * 第三方驯养体系探测工具类。
 * <p>
 * 通过反射识别并缓存第三方生物自带的驯养方法：
 * 一组返回布尔值的驯养标志方法与一组返回主人的获取方法。
 * 探测结果按类缓存，私有与受保护方法也会被纳入，
 * 但只有真正可访问的方法才会保留下来，反射只扫描一次。
 */
public final class TamingCompat {

    /**
     * 表示驯养状态的布尔方法名集合。
     */
    private static final Set<String> TAMING_FLAGS = Set.of(
            "isTamed", "isTame", "isDomesticated", "isTrusted", "isBonded", "hasBeenTamed",
            "isOwned", "hasOwner");

    /**
     * 获取主人的方法名集合。
     */
    private static final Set<String> OWNER_GETTERS = Set.of(
            "getOwner", "getOwningPlayer", "getTameOwner", "getTamer", "getMaster",
            "getOwnerUUID", "getOwnerId", "getOwnerUniqueId");

    /**
     * 探测结果记录，分别保存可调用的驯养标志方法与主人获取方法。
     *
     * @param flags  驯养标志方法列表
     * @param owners 主人获取方法列表
     */
    private record Probe(List<Method> flags, List<Method> owners) {
    }

    /**
     * 按类缓存探测结果，每个类只扫描一次。
     */
    private static final ClassValue<Probe> PROBE = new ClassValue<>() {
        @Override
        protected Probe computeValue(Class<?> type) {
            return detect(type);
        }
    };

    /**
     * 判断生物是否已被它自己的驯养体系收编，用于让通用饲料回避。
     * <p>
     * 只看能不能读到一个真实的主人；第三方生物哪怕继承了可驯养类、
     * 身上带着驯养标志，只要还没有主人，就照常允许通用饲料去驯服。
     *
     * @param entity 目标生物
     * @return 是否存在第三方驯养体系
     */
    public static boolean hasForeignTaming(LivingEntity entity) {
        if (!isThirdParty(entity)) {
            return false;
        }
        Probe probe = PROBE.get(entity.getClass());
        for (Method owner : probe.owners()) {
            if (hasOwnerValue(invoke(entity, owner))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 读取第三方生物自己的驯养状态。
     * <p>
     * 读不到或调用失败一律当作未驯养。
     * 先看布尔标志，任意一个返回 true 就算已驯养；
     * 标志全为 false 或一个都调不通时，再看有没有主人。
     *
     * @param entity 目标生物
     * @return 是否已驯养
     */
    public static boolean isForeignTamed(LivingEntity entity) {
        if (!isThirdParty(entity)) {
            return false;
        }

        Probe probe = PROBE.get(entity.getClass());

        for (Method flag : probe.flags()) {
            if (invoke(entity, flag) instanceof Boolean tamed && tamed) {
                return true;
            }
        }
        for (Method owner : probe.owners()) {
            if (hasOwnerValue(invoke(entity, owner))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断生物是否来自第三方模组。
     * <p>
     * 实体类型 ID 为空或命名空间既非 minecraft 也非本模组时视为第三方。
     *
     * @param entity 目标生物
     * @return 是否为第三方生物
     */
    private static boolean isThirdParty(LivingEntity entity) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (id == null) {
            return false;
        }
        String namespace = id.getNamespace();
        return !"minecraft".equals(namespace) && !KaleidoscopeStartale.MODID.equals(namespace);
    }

    /**
     * 扫描指定类的驯养相关方法。
     * <p>
     * 沿继承链取声明方法（含私有与受保护），再补充接口默认方法，
     * 只保留可访问且符合候选条件的方法；出现异常时返回空结果。
     *
     * @param type 目标类
     * @return 探测结果
     */
    private static Probe detect(Class<?> type) {
        List<Method> flags = new ArrayList<>();
        List<Method> owners = new ArrayList<>();

        try {
            // 沿继承链取声明方法，私有和受保护的写法也能读到
            for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
                for (Method method : current.getDeclaredMethods()) {
                    Candidate candidate = classify(method);
                    // 拿不到访问权限的方法调不动，直接跳过
                    if (candidate == Candidate.NONE || !method.trySetAccessible()) {
                        continue;
                    }
                    collect(candidate, method, flags, owners);
                }
            }

            // 接口的 default 方法不在 getDeclaredMethods 里，单独补一轮
            for (Method method : type.getMethods()) {
                Candidate candidate = classify(method);
                if (candidate == Candidate.NONE || flags.contains(method) || owners.contains(method)) {
                    continue;
                }
                if (method.trySetAccessible()) {
                    collect(candidate, method, flags, owners);
                }
            }
        } catch (Throwable ignored) {
            return new Probe(List.of(), List.of());
        }

        return new Probe(List.copyOf(flags), List.copyOf(owners));
    }

    /**
     * 将候选方法归类收集到对应列表。
     *
     * @param candidate 候选类型
     * @param method    方法
     * @param flags     驯养标志方法列表
     * @param owners    主人获取方法列表
     */
    private static void collect(Candidate candidate, Method method, List<Method> flags, List<Method> owners) {
        if (candidate == Candidate.FLAG) {
            flags.add(method);
        } else {
            owners.add(method);
        }
    }

    /**
     * 候选方法类型枚举。
     */
    private enum Candidate {
        NONE,
        FLAG,
        OWNER
    }

    /**
     * 判断方法属于哪类候选。
     * <p>
     * 排除带参数、静态、合成与桥接方法；
     * 布尔返回值的驯养标志方法归为 FLAG，返回主人类型的主人获取方法归为 OWNER。
     *
     * @param method 方法
     * @return 候选类型
     */
    private static Candidate classify(Method method) {
        if (method.getParameterCount() != 0
                || Modifier.isStatic(method.getModifiers())
                || method.isSynthetic()
                || method.isBridge()) {
            return Candidate.NONE;
        }

        String name = method.getName();
        if (TAMING_FLAGS.contains(name)) {
            Class<?> returnType = method.getReturnType();
            if (returnType == boolean.class || returnType == Boolean.class) {
                return Candidate.FLAG;
            }
        }
        if (OWNER_GETTERS.contains(name) && isOwnerType(method.getReturnType())) {
            return Candidate.OWNER;
        }
        return Candidate.NONE;
    }

    /**
     * 判断返回类型是否为可表示主人的类型。
     * <p>
     * 实体、UUID、Optional 与字符序列类型均视为可表示主人。
     *
     * @param returnType 返回类型
     * @return 是否为可表示主人的类型
     */
    private static boolean isOwnerType(Class<?> returnType) {
        return Entity.class.isAssignableFrom(returnType)
                || returnType == UUID.class
                || returnType == Optional.class
                || CharSequence.class.isAssignableFrom(returnType);
    }

    /**
     * 反射调用方法，调用失败返回 null。
     *
     * @param entity 目标生物
     * @param method 方法
     * @return 调用结果，失败时为 null
     */
    private static Object invoke(LivingEntity entity, Method method) {
        try {
            return method.invoke(entity);
        } catch (Throwable ignored) {
            // 调用被拒绝或方法内部抛错，当作读不到，继续试下一个
            return null;
        }
    }

    /**
     * 判断返回值是否表示存在真实主人。
     * <p>
     * null、空 Optional、全 0 的 UUID、空白字符序列均视为无主人；
     * 返回实体对象则视为有主人。
     *
     * @param value 调用返回值
     * @return 是否表示存在主人
     */
    private static boolean hasOwnerValue(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Optional<?> optional) {
            return optional.isPresent();
        }
        if (value instanceof UUID uuid) {
            // 部分模组用全 0 的 UUID 表示没有主人
            return uuid.getMostSignificantBits() != 0L || uuid.getLeastSignificantBits() != 0L;
        }
        if (value instanceof CharSequence text) {
            return !text.toString().isBlank();
        }
        // 能返回实体对象，说明主人字段被填过
        return true;
    }

    private TamingCompat() {
    }
}
