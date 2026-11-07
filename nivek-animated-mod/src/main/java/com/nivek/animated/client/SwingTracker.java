package com.nivek.animated.client;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Detecta o início de cada golpe e alterna entre 3 combos (corte, golpe vertical, estocada). */
public final class SwingTracker {
    private static final Map<UUID, int[]> DATA = new HashMap<>(); // [combo, estavaGolpeando]

    private SwingTracker() {}

    public static void tick(ClientWorld world) {
        for (AbstractClientPlayerEntity p : world.getPlayers()) {
            int[] d = DATA.computeIfAbsent(p.getUuid(), k -> new int[2]);
            boolean sw = p.handSwinging;
            if (sw && d[1] == 0) d[0] = (d[0] + 1) % 3;
            d[1] = sw ? 1 : 0;
        }
    }

    public static int combo(UUID id) {
        int[] d = DATA.get(id);
        return d == null ? 0 : d[0];
    }

    public static void clear() { DATA.clear(); }
}
