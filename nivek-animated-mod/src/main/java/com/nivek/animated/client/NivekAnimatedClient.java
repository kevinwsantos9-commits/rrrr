package com.nivek.animated.client;

import com.nivek.animated.LegendarySwordItem;
import com.nivek.animated.NivekAnimated;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class NivekAnimatedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> SwingTracker.clear());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null || client.player == null) return;
            SwingTracker.tick(client.world);
            ClientPlayerEntity pl = client.player;
            if (!pl.getMainHandStack().isOf(NivekAnimated.LEGENDARY_SWORD)) return;
            Random r = client.world.random;

            // partículas amarelas leves enquanto segura
            if (pl.age % 3 == 0) {
                Vec3d p = pl.getEyePos().add(pl.getRotationVec(1.0f).multiply(0.9));
                client.world.addParticle(LegendarySwordItem.GOLD_DUST,
                        p.x + (r.nextDouble() - 0.5) * 0.5, p.y - 0.4 + (r.nextDouble() - 0.5) * 0.6,
                        p.z + (r.nextDouble() - 0.5) * 0.5, 0.0, 0.02, 0.0);
            }

            // rastro dourado seguindo o arco do golpe
            float prog = pl.handSwingProgress;
            if (pl.handSwinging && prog > 0.1f && prog < 0.9f) {
                Vec3d look = pl.getRotationVec(1.0f);
                Vec3d side = look.crossProduct(new Vec3d(0, 1, 0)).normalize();
                Vec3d base = pl.getEyePos().add(look.multiply(1.1)).add(0, -0.3, 0);
                float a = (prog - 0.5f) * 2f;
                int combo = SwingTracker.combo(pl.getUuid());
                Vec3d pos;
                if (combo == 0) pos = base.add(side.multiply(-a * 0.9)).add(0, 0.25 * (1 - a * a), 0);
                else if (combo == 1) pos = base.add(0, 0.8 - 1.2 * prog, 0);
                else pos = base.add(look.multiply(0.6 * Math.sin(prog * Math.PI)));
                for (int i = 0; i < 3; i++) {
                    client.world.addParticle(LegendarySwordItem.GOLD_DUST,
                            pos.x + (r.nextDouble() - 0.5) * 0.2, pos.y + (r.nextDouble() - 0.5) * 0.2,
                            pos.z + (r.nextDouble() - 0.5) * 0.2, 0.0, 0.01, 0.0);
                }
            }
        });
    }
}
