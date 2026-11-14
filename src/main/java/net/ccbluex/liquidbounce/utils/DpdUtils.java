package net.ccbluex.liquidbounce.utils;

import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

public class DpdUtils {

    public static float maxMemorySize = 0,usedMemorySize = 0;

    /** Refreshes the cached heap usage; called once per tick by {@code DpdHook}. */
    public static void updateMemory() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage();
        maxMemorySize = (float) memoryUsage.getMax()/(1024*1024);
        usedMemorySize = (float) memoryUsage.getUsed()/(1024*1024);
    }

    public static float getMemory() {
        return maxMemorySize/usedMemorySize;
    }

    public static void sendPacketSilent(Packet<?> packet) {
        Minecraft.getMinecraft().getNetHandler().getNetworkManager().sendPacket(packet, null, new GenericFutureListener[0]);
    }
    public static void sendPacketUnlogged(Packet<?> packet) {
        Minecraft.getMinecraft().getNetHandler().getNetworkManager().sendPacket(packet);
    }
}
