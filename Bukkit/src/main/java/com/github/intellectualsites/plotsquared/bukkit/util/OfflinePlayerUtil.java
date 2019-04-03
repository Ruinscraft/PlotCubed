package com.github.intellectualsites.plotsquared.bukkit.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.UUID;

import static com.github.intellectualsites.plotsquared.plot.util.ReflectionUtils.*;

public class OfflinePlayerUtil {

    public static Player loadPlayer(String name) {
        return loadPlayer(Bukkit.getOfflinePlayer(name));
    }

    public static Player loadPlayer(UUID id) {
        return loadPlayer(Bukkit.getOfflinePlayer(id));
    }

    public static Player loadPlayer(OfflinePlayer player) {
        if (player == null) {
            return null;
        }
        if (player instanceof Player) {
            return (Player) player;
        }
        return loadPlayer(player.getUniqueId(), player.getName());
    }

    private static Player loadPlayer(UUID id, String name) {
        Object server = getMinecraftServer();
        Object interactManager = newPlayerInteractManager();
        Object worldServer = getWorldServer();
        Object profile = newGameProfile(id, name);
        Class<?> entityPlayerClass = getNmsClass("EntityPlayer");
        Constructor entityPlayerConstructor =
            makeConstructor(entityPlayerClass, getNmsClass("MinecraftServer"),
                getNmsClass("WorldServer"), getUtilClass("com.mojang.authlib.GameProfile"),
                getNmsClass("PlayerInteractManager"));
        Object entityPlayer =
            callConstructor(entityPlayerConstructor, server, worldServer, profile, interactManager);
        return (Player) getBukkitEntity(entityPlayer);
    }

    private static Object newGameProfile(UUID id, String name) {
        Class<?> gameProfileClass = getUtilClass("com.mojang.authlib.GameProfile");
        if (gameProfileClass == null) { //Before uuids
            return name;
        }
        Constructor gameProfileConstructor =
            makeConstructor(gameProfileClass, UUID.class, String.class);
        if (gameProfileConstructor == null) { //Version has string constructor
            gameProfileConstructor = makeConstructor(gameProfileClass, String.class, String.class);
            return callConstructor(gameProfileConstructor, id.toString(), name);
        } else { //Version has uuid constructor
            return callConstructor(gameProfileConstructor, id, name);
        }
    }

    private static Object newPlayerInteractManager() {
        Object worldServer = getWorldServer();
        Class<?> playerInteractClass = getNmsClass("PlayerInteractManager");
        Class<?> worldClass = getNmsClass("World");
        Constructor c = makeConstructor(playerInteractClass, worldClass);
        return callConstructor(c, worldServer);
    }

    private static Object getWorldServer() {
        Object server = getMinecraftServer();
        Class<?> minecraftServerClass = getNmsClass("MinecraftServer");
        Method getWorldServer = makeMethod(minecraftServerClass, "getWorldServer", int.class);
        return callMethod(getWorldServer, server, 0);
    }

    //NMS Utils

    private static Object getMinecraftServer() {
        return callMethod(makeMethod(getCbClass("CraftServer"), "getServer"), Bukkit.getServer());
    }

    private static Entity getBukkitEntity(Object o) {
        Method getBukkitEntity = makeMethod(o.getClass(), "getBukkitEntity");
        return callMethod(getBukkitEntity, o);
    }
}
