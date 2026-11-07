package com.nivek.animated.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

/** Animação da mão em primeira pessoa: balanço, corrida, inércia do pulo, golpes por keyframes e tranco de impacto. */
public final class FirstPersonSword {
    private static final Anim.Spring LAG = new Anim.Spring();
    private static final Anim.SwingClock CLOCK = new Anim.SwingClock();
    private static float sprint, kick, kickT, lastP;
    private static long last;

    private FirstPersonSword() {}

    private static void rot(MatrixStack mx, RotationAxis axis, float deg, float px, float py, float pz) {
        mx.translate(px, py, pz);
        mx.multiply(axis.rotationDegrees(deg));
        mx.translate(-px, -py, -pz);
    }

    public static void apply(AbstractClientPlayerEntity player, float tickDelta, float swingProgress, ItemStack stack, MatrixStack mx) {
        long now = System.nanoTime();
        float dt = last == 0 ? 0.016f : Math.min((now - last) / 1.0e9f, 0.05f);
        last = now;

        float side = player.getMainArm() == Arm.RIGHT ? 1f : -1f;
        float px = 0.56f * side, py = -0.52f, pz = -0.72f;     // posição aproximada da mão
        float age = player.age + tickDelta;

        // inércia vertical (pulo e queda)
        float vy = (float) player.getVelocity().y;
        float lag = LAG.step(MathHelper.clamp(vy * 0.35f, -0.14f, 0.14f), dt, 90f, 0.55f);
        mx.translate(0f, -lag, 0f);

        // balanço ao andar
        float sp = player.limbAnimator.getSpeed(tickDelta);
        float pos = player.limbAnimator.getPos(tickDelta) * 0.6662f;
        mx.translate(MathHelper.sin(pos) * 0.012f * sp, -Math.abs(MathHelper.cos(pos)) * 0.02f * sp, 0f);
        rot(mx, RotationAxis.POSITIVE_Z, MathHelper.cos(pos) * 1.2f * sp * side, px, py, pz);

        // respiração parado
        rot(mx, RotationAxis.POSITIVE_Z, MathHelper.sin(age * 0.06f) * 0.6f * side, px, py, pz);
        rot(mx, RotationAxis.POSITIVE_X, MathHelper.cos(age * 0.045f) * 0.5f, px, py, pz);

        // corrida: ferramenta inclina e abaixa
        sprint += ((player.isSprinting() ? 1f : 0f) - sprint) * (1f - (float) Math.exp(-8f * dt));
        rot(mx, RotationAxis.POSITIVE_X, -9f * sprint, px, py, pz);
        rot(mx, RotationAxis.POSITIVE_Y, 7f * sprint * side, px, py, pz);
        mx.translate(0f, -0.03f * sprint, 0f);

        // golpe
        int cat = Anim.category(stack);
        MinecraftClient mc = MinecraftClient.getInstance();
        boolean mining = mc.interactionManager != null && mc.interactionManager.isBreakingBlock() && cat >= 1 && cat <= 4;
        float p = CLOCK.map(swingProgress, mining);
        if (p > 0f && p < 1f) {
            int table = Anim.table(cat, SwingTracker.combo(player.getUuid()));
            float[] c = Anim.sample(p, Anim.TABLES[table]);
            float w = MathHelper.clamp(p / 0.10f, 0f, 1f) * MathHelper.clamp((1f - p) / 0.22f, 0f, 1f);
            rot(mx, RotationAxis.POSITIVE_X, (c[0] + 0.4f) * 14f * w, px, py, pz);
            rot(mx, RotationAxis.POSITIVE_Y, c[1] * 16f * side * w, px, py, pz);
            rot(mx, RotationAxis.POSITIVE_Z, c[2] * 22f * side * w, px, py, pz);
            mx.translate(0f, -c[4] * 0.08f * w, -c[5] * 0.22f * w);
            if (lastP < Anim.HIT[table] && p >= Anim.HIT[table]) { kick = 1f; kickT = 0f; }
        }
        lastP = p;

        // tranco do impacto
        if (kick > 0f) {
            kickT += dt;
            kick = Math.max(0f, kick - dt * 5f);
            float k = kick * MathHelper.cos(kickT * 34f);
            mx.translate(0f, -0.04f * k, 0f);
            rot(mx, RotationAxis.POSITIVE_X, 5f * k, px, py, pz);
        }
    }
}
