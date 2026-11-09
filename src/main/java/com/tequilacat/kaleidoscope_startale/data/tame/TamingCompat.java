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
 * 探测第三方生物自带的驯养体系。
 * hasForeignTaming 用来判断通用饲料要不要回避，回避时既不驯服也不加好感度；
 * isForeignTamed 用来读出生物自己的驯养状态，让拍拍器和好感度在别的模组驯服之后照常工作。
 * 探测结果按类缓存：扫到候选方法就算存在（私有方法也算），其中真正调得到的才保留下来，
 * 反射只扫一次。原版 OwnableEntity 由 MobTaming 先按原版逻辑处理，
 * 女仆模组由 MobTaming.isForeignMaid 排除，这两种情况都不依赖本类。
 */
public final class TamingCompat {
    /** 返回是否已驯养、是否有主的无参方法。 */
    private static final Set<String> TAMING_FLAGS = Set.of(
            "isTamed", "isTame", "isDomesticated", "isTrusted", "isBonded", "hasBeenTamed",
            "isOwned", "hasOwner");

    /** 返回主人实体、主人 UUID 或主人名字的方法。 */
    private static final Set<String> OWNER_GETTERS = Set.of(
            "getOwner", "getOwningPlayer", "getTameOwner", "getTamer", "getMaster",
            "getOwnerUUID", "getOwnerId", "getOwnerUniqueId");

    /** 一个类上真正调得动的驯养方法：布尔标志与主人读取。 */
    private record Probe(List<Method> flags, List<Method> owners) {
    }

    private static final ClassValue<Probe> PROBE = new ClassValue<>() {
        @Override
        protected Probe computeValue(Class<?> type) {
            return detect(type);
        }
    };

    /**
     * 生物是否已经被它自己的驯养体系收编，用于让通用饲料回避。
     * 只看能不能读到一个真实的主人：第三方生物哪怕继承了 TamableAnimal、
     * 身上带着 isTame()，只要还没有主人，就照常允许通用饲料去驯服。
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
     * 读取第三方生物自己的驯养状态，读不到或调用失败一律当作未驯养。
     * 先看布尔标志，任意一个返回 true 就算已驯养；
     * 标志全为 false 或一个都调不通时，再看有没有主人。
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

    private static boolean isThirdParty(LivingEntity entity) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (id == null) {
            return false;
        }
        String namespace = id.getNamespace();
        return !"minecraft".equals(namespace) && !KaleidoscopeStartale.MODID.equals(namespace);
    }

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

    private static void collect(Candidate candidate, Method method, List<Method> flags, List<Method> owners) {
        if (candidate == Candidate.FLAG) {
            flags.add(method);
        } else {
            owners.add(method);
        }
    }

    private enum Candidate {
        NONE,
        FLAG,
        OWNER
    }

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

    /** 主人字段的常见形态：实体本身、UUID、字符串、Optional 包装。 */
    private static boolean isOwnerType(Class<?> returnType) {
        return Entity.class.isAssignableFrom(returnType)
                || returnType == UUID.class
                || returnType == Optional.class
                || CharSequence.class.isAssignableFrom(returnType);
    }

    private static Object invoke(LivingEntity entity, Method method) {
        try {
            return method.invoke(entity);
        } catch (Throwable ignored) {
            // 调用被拒绝或方法内部抛错，当作读不到，继续试下一个
            return null;
        }
    }

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
