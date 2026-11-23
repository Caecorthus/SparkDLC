package dev.caecorthus.sparkdlc.gametest;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.scoreboard.ScoreboardComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.scoreboard.ScoreboardComponentInitializer;
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Writes down, in order, what the start-up of the mod registered: the mod's entries of every vanilla registry in
 * raw-id order, the handlers of every Fabric event that has one of ours (in dispatch order, other mods' handlers
 * included, each named by its host class), the component factories in registration order, the payload ids and
 * global receivers, and on a client the modded key bindings. Two dumps from two builds are compared with a plain
 * {@code diff}: that is how a change to the start-up code proves it kept the order. It reads the running game only
 * through reflection and names no class of the start-up code, so the same file works on any revision.
 *
 * <p>Nothing is written unless the run sets {@code -Dsparkdlc.startupOrderDump=<directory>}
 * ({@code ./gradlew runGameTest -PstartupOrderDump=<directory>}); the file is {@code startup-order-<side>.txt}.</p>
 *
 * 按顺序记下模组启动时注册的内容：每个原版注册表中本模组条目的 raw id 顺序、每个含有本模组处理器的 Fabric 事件的处理器
 * （按分发顺序，包含其他模组的处理器，以所在类命名）、组件工厂的注册顺序、数据包 id 与全局接收器，以及客户端上的模组按键绑定。
 * 两个构建各导出一份后直接 {@code diff}：改动启动代码时以此证明顺序未变。它只通过反射读取运行中的游戏，不引用任何启动代码的类，
 * 因此同一个文件可用于任意版本。
 *
 * <p>只有运行时设置了 {@code -Dsparkdlc.startupOrderDump=<目录>}（{@code ./gradlew runGameTest -PstartupOrderDump=<目录>}）
 * 才会写文件，文件名为 {@code startup-order-<side>.txt}。</p>
 */
public final class StartupOrderDump {
    public static final String PROPERTY = "sparkdlc.startupOrderDump";
    private static final String MOD_ID = "sparkdlc";
    private static final String OWN_PACKAGE = "dev.caecorthus.sparkdlc.";
    private static final String GAMETEST_PACKAGE = OWN_PACKAGE + "gametest.";

    private StartupOrderDump() {
    }

    /** Writes the dump when the run asked for it; returns the file, or null. / 运行要求时写出导出文件；返回该文件或 null。 */
    public static Path writeIfRequested() throws ReflectiveOperationException, IOException {
        String directory = System.getProperty(PROPERTY);
        if (directory == null || directory.isBlank()) {
            return null;
        }
        boolean client = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
        Path file = Path.of(directory).resolve("startup-order-" + (client ? "client" : "server") + ".txt");
        Files.createDirectories(file.getParent());
        Files.write(file, lines(), StandardCharsets.UTF_8);
        return file;
    }

    public static List<String> lines() throws ReflectiveOperationException {
        List<String> out = new ArrayList<>();
        registries(out);
        events(out);
        componentFactories().forEach((kind, calls) -> {
            out.add("# component factories: " + kind);
            out.addAll(calls);
        });
        networking(out);
        keyBindings(out);
        return out;
    }

    private static void registries(List<String> out) {
        Map<String, List<String>> byRegistry = new TreeMap<>();
        for (Registry<?> registry : Registries.REGISTRIES) {
            List<String> ids = new ArrayList<>();
            // A registry iterates in raw-id order, which is registration order. / 注册表按 raw id 顺序迭代，即注册顺序。
            registryIdsInOrder(registry, ids);
            if (!ids.isEmpty()) {
                byRegistry.put(registry.getKey().getValue().toString(), ids);
            }
        }
        byRegistry.forEach((name, ids) -> {
            out.add("# registry " + name);
            out.addAll(ids);
        });
    }

    private static <T> void registryIdsInOrder(Registry<T> registry, List<String> ids) {
        for (T entry : registry) {
            Identifier id = registry.getId(entry);
            if (id != null && MOD_ID.equals(id.getNamespace())) {
                ids.add(id.toString());
            }
        }
    }

    /**
     * Fabric keeps every array-backed event in one set; an event has no name, so it is named by its callback
     * interface (events sharing an interface are told apart by their handler lists, sorted).
     * Fabric 把所有数组事件放在一个集合里；事件没有名字，因此以回调接口命名（共用接口的事件按处理器列表排序区分）。
     */
    private static void events(List<String> out) throws ReflectiveOperationException {
        eventHandlerHosts().forEach((callback, lists) -> {
            for (List<String> hosts : lists) {
                out.add("# event " + callback);
                out.addAll(hosts);
            }
        });
    }

    /**
     * Callback interface name to the handler hosts of each event with that interface, in dispatch order; only events
     * with a handler of this mod. Listeners the game tests themselves register are left out.
     * 回调接口名到使用该接口的各事件的处理器所在类（按分发顺序）；只含带有本模组处理器的事件。game test 自己注册的监听器不计入。
     */
    public static Map<String, List<List<String>>> eventHandlerHosts() throws ReflectiveOperationException {
        Class<?> factory = Class.forName("net.fabricmc.fabric.impl.base.event.EventFactoryImpl");
        Field all = factory.getDeclaredField("ARRAY_BACKED_EVENTS");
        all.setAccessible(true);
        Field handlersField = Class.forName("net.fabricmc.fabric.impl.base.event.ArrayBackedEvent")
                .getDeclaredField("handlers");
        handlersField.setAccessible(true);
        Map<String, List<List<String>>> byEvent = new TreeMap<>();
        for (Object event : new ArrayList<>((Collection<?>) all.get(null))) {
            Object handlers = handlersField.get(event);
            List<String> hosts = new ArrayList<>();
            boolean own = false;
            for (int index = 0; index < Array.getLength(handlers); index++) {
                String host = host(Array.get(handlers, index));
                if (!host.startsWith(GAMETEST_PACKAGE)) {
                    own |= host.startsWith(OWN_PACKAGE);
                    hosts.add(host);
                }
            }
            if (own) {
                String callback = handlers.getClass().getComponentType().getName();
                byEvent.computeIfAbsent(callback, key -> new ArrayList<>()).add(hosts);
            }
        }
        byEvent.values().forEach(lists -> lists.sort(Comparator.comparing(hosts -> String.join("\n", hosts))));
        return byEvent;
    }

    /** A lambda's hidden class is named after the class that declares it. / lambda 的隐藏类以声明它的类命名。 */
    private static String host(Object handler) {
        String name = handler.getClass().getName();
        int lambda = name.indexOf("$$Lambda");
        return lambda < 0 ? name : name.substring(0, lambda) + " (lambda)";
    }

    /**
     * The component factories every {@code cardinal-components} entrypoint of the mod registers, in call order, taken
     * by handing the entrypoints a recording registry (entity, then world, then scoreboard, as Cardinal Components
     * does). Each line is one call with its component id, target class and respawn strategy.
     * 本模组每个 {@code cardinal-components} 入口注册的组件工厂（按调用顺序）：把一个只做记录的注册表交给入口得到（与 Cardinal
     * Components 一样依次为实体、世界、计分板）。每行是一次调用及其组件 id、目标类与重生策略。
     */
    public static Map<String, List<String>> componentFactories() {
        List<Object> entrypoints = new ArrayList<>();
        for (EntrypointContainer<Object> container
                : FabricLoader.getInstance().getEntrypointContainers("cardinal-components", Object.class)) {
            if (MOD_ID.equals(container.getProvider().getMetadata().getId())) {
                entrypoints.add(container.getEntrypoint());
            }
        }
        Map<String, List<String>> calls = new TreeMap<>();
        List<String> entity = calls.computeIfAbsent("1 entity", key -> new ArrayList<>());
        List<String> world = calls.computeIfAbsent("2 world", key -> new ArrayList<>());
        List<String> scoreboard = calls.computeIfAbsent("3 scoreboard", key -> new ArrayList<>());
        for (Object entrypoint : entrypoints) {
            if (entrypoint instanceof EntityComponentInitializer initializer) {
                initializer.registerEntityComponentFactories(recorder(EntityComponentFactoryRegistry.class, entity));
            }
        }
        for (Object entrypoint : entrypoints) {
            if (entrypoint instanceof WorldComponentInitializer initializer) {
                initializer.registerWorldComponentFactories(recorder(WorldComponentFactoryRegistry.class, world));
            }
        }
        for (Object entrypoint : entrypoints) {
            if (entrypoint instanceof ScoreboardComponentInitializer initializer) {
                initializer.registerScoreboardComponentFactories(
                        recorder(ScoreboardComponentFactoryRegistry.class, scoreboard));
            }
        }
        return calls;
    }

    /** The component ids of the recorded calls, in order. / 所记录调用中的组件 id，按顺序。 */
    public static List<String> componentIds(Collection<List<String>> calls) {
        List<String> ids = new ArrayList<>();
        for (List<String> kind : calls) {
            for (String call : kind) {
                int key = call.indexOf("key=");
                if (key >= 0) {
                    int end = call.indexOf(' ', key);
                    ids.add(call.substring(key + 4, end < 0 ? call.length() : end));
                }
            }
        }
        return ids;
    }

    @SuppressWarnings("unchecked")
    private static <T> T recorder(Class<T> type, List<String> calls) {
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> type.getSimpleName() + " recorder";
                };
            }
            StringBuilder call = new StringBuilder(method.getName());
            if (args != null) {
                for (Object arg : args) {
                    if (arg instanceof ComponentKey<?> key) {
                        call.append(" key=").append(key.getId());
                    } else if (arg instanceof Class<?> target) {
                        call.append(" class=").append(target.getName());
                    } else if (arg instanceof RespawnCopyStrategy<?> strategy) {
                        call.append(" respawn=").append(respawnStrategyName(strategy));
                    }
                }
            }
            calls.add(call.toString());
            Class<?> result = method.getReturnType();
            // The fluent registration builder is recorded in the same list. / 链式注册构建器记录在同一个列表里。
            return result.isInterface() ? recorder(result, calls) : null;
        };
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler);
    }

    private static String respawnStrategyName(RespawnCopyStrategy<?> strategy) throws IllegalAccessException {
        for (Field field : RespawnCopyStrategy.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.get(null) == strategy) {
                return field.getName();
            }
        }
        return "custom";
    }

    /** Payload types and receivers live in maps: only the sets are observable. / 数据包类型与接收器存于 map：只有集合可观察。 */
    private static void networking(List<String> out) throws ReflectiveOperationException {
        Class<?> payloadTypes = Class.forName("net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl");
        Field packetTypes = payloadTypes.getDeclaredField("packetTypes");
        packetTypes.setAccessible(true);
        for (String side : List.of("CONFIGURATION_C2S", "CONFIGURATION_S2C", "PLAY_C2S", "PLAY_S2C")) {
            out.add("# payload types " + side + " (sorted)");
            out.addAll(ownIds(((Map<?, ?>) packetTypes.get(payloadTypes.getField(side).get(null))).keySet()));
        }
        receivers(out, "net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl", "server");
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            receivers(out, "net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl", "client");
        }
    }

    private static void receivers(List<String> out, String owner, String side) throws ReflectiveOperationException {
        Class<?> networking = Class.forName(owner);
        for (String phase : List.of("LOGIN", "CONFIGURATION", "PLAY")) {
            Object registry = networking.getField(phase).get(null);
            Collection<?> channels = (Collection<?>) registry.getClass().getMethod("getChannels").invoke(registry);
            out.add("# global receivers " + side + " " + phase + " (sorted)");
            out.addAll(ownIds(channels));
        }
    }

    private static Collection<String> ownIds(Collection<?> ids) {
        TreeSet<String> own = new TreeSet<>();
        for (Object id : ids) {
            if (id instanceof Identifier identifier && MOD_ID.equals(identifier.getNamespace())) {
                own.add(identifier.toString());
            }
        }
        return own;
    }

    /** Client only: Fabric's list of modded key bindings, in registration order. / 仅客户端：Fabric 的模组按键绑定列表，按注册顺序。 */
    private static void keyBindings(List<String> out) throws ReflectiveOperationException {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
            return;
        }
        Class<?> registry = Class.forName("net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl");
        Field bindings = registry.getDeclaredField("MODDED_KEY_BINDINGS");
        bindings.setAccessible(true);
        out.add("# modded key bindings (all mods)");
        for (Object binding : (Collection<?>) bindings.get(null)) {
            // KeyBinding#getTranslationKey, looked up by name so this class also loads on a server.
            // 按名字查找 KeyBinding#getTranslationKey，使本类在服务端也能加载。
            out.add(String.valueOf(binding.getClass().getMethod("getTranslationKey").invoke(binding)));
        }
    }
}
