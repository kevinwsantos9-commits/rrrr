package com.nivek.animated.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Motor de animação do NIVEK ANIMATED (sem dependências extras).
 * Pose alvo = vanilla + camadas procedurais + golpe por keyframes; o resultado é seguido por molas
 * amortecidas, o que dá antecipação, impacto, sobra de movimento e transições suaves.
 */
public final class PoseSmoother {
    private static final float WEIGHT_RATE = 9f;
    private static final Map<UUID, State> STATES = new HashMap<>();

    private static final class State {
        final Anim.Spring[] sp = new Anim.Spring[15];
        final Anim.Spring hy = new Anim.Spring(), hp = new Anim.Spring();
        final Anim.SwingClock clock = new Anim.SwingClock();
        final float[] w = new float[4];   // corrida, subida, queda, pose de ferramenta
        long last;
        boolean wasGround = true;
        float lastVy, land, impact, lastP, kick, kickT;

        State() { for (int i = 0; i < sp.length; i++) sp[i] = new Anim.Spring(); }
    }

    private PoseSmoother() {}

    private static float smooth(float x) { x = MathHelper.clamp(x, 0f, 1f); return x * x * (3f - 2f * x); }

    private static void layers(LivingEntity e, PlayerEntityModel<?> m, State s, float limbAngle, float limbDistance, float t) {
        if (e.getPose() != EntityPose.STANDING || e.hasVehicle() || e.isFallFlying()) return;
        float ph = limbAngle * 0.6662f;
        float mv = Math.min(limbDistance, 1f);
        float pi = (float) Math.PI;

        // respiração e transferência de peso parado
        float idle = MathHelper.clamp(1f - mv * 6f, 0f, 1f);
        float br = MathHelper.sin(t * 0.07f) * idle;
        m.body.pitch += br * 0.012f;
        m.body.roll += MathHelper.sin(t * 0.035f) * idle * 0.015f;
        m.rightArm.roll += br * 0.03f;
        m.leftArm.roll -= br * 0.03f;

        // quadril e quique do tronco ao andar
        float twist = MathHelper.cos(ph) * mv * 0.10f;
        m.body.yaw += twist;
        m.body.roll += MathHelper.sin(ph) * mv * 0.025f;
        m.head.yaw -= twist;
        float bob = mv * (0.5f - 0.5f * MathHelper.cos(2f * ph)) * (0.6f + 0.5f * s.w[0]);
        m.head.pivotY += bob;
        m.body.pivotY += bob;
        m.rightArm.pivotY += bob;
        m.leftArm.pivotY += bob;

        // corrida
        float run = s.w[0];
        m.body.pitch += 0.20f * run;
        m.head.pitch -= 0.12f * run;
        m.rightArm.pitch += (-0.45f + MathHelper.cos(ph + pi) * mv * 0.5f) * run;
        m.leftArm.pitch += (-0.45f + MathHelper.cos(ph) * mv * 0.5f) * run;
        m.rightLeg.pitch += MathHelper.cos(ph) * mv * 0.35f * run;
        m.leftLeg.pitch += MathHelper.cos(ph + pi) * mv * 0.35f * run;

        // pulo (subida)
        float rise = s.w[1];
        m.rightArm.pitch -= 0.7f * rise;
        m.leftArm.pitch -= 0.7f * rise;
        m.rightLeg.pitch -= 0.5f * rise;
        m.leftLeg.pitch += 0.25f * rise;

        // queda
        float fall = s.w[2];
        m.rightArm.roll += 0.8f * fall;
        m.leftArm.roll -= 0.8f * fall;
        m.rightArm.pitch -= 0.25f * fall;
        m.leftArm.pitch -= 0.25f * fall;
        m.body.pitch -= 0.06f * fall;
        m.rightLeg.roll += 0.12f * fall;
        m.leftLeg.roll -= 0.12f * fall;

        // pouso com impacto
        float q = s.land * s.land * (3f - 2f * s.land) * s.impact;
        m.body.pitch += 0.28f * q;
        m.head.pitch -= 0.15f * q;
        m.rightArm.pitch -= 0.35f * q;
        m.leftArm.pitch -= 0.35f * q;
        m.rightArm.roll += 0.2f * q;
        m.leftArm.roll -= 0.2f * q;
        m.rightLeg.pitch -= 0.3f * q;
        m.leftLeg.pitch -= 0.3f * q;
        float shift = 2.0f * q;
        m.head.pivotY += shift;
        m.body.pivotY += shift;
        m.rightArm.pivotY += shift;
        m.leftArm.pivotY += shift;

        // postura de prontidão com ferramenta/espada
        ModelPart main = e.getMainArm() == Arm.RIGHT ? m.rightArm : m.leftArm;
        main.pitch -= 0.25f * s.w[3];
    }

    private static void swing(LivingEntity e, PlayerEntityModel<?> m, float p, int table) {
        boolean right = (e.preferredHand == Hand.MAIN_HAND) == (e.getMainArm() == Arm.RIGHT);
        ModelPart arm = right ? m.rightArm : m.leftArm;
        ModelPart other = right ? m.leftArm : m.rightArm;
        float side = right ? 1f : -1f;
        float w = smooth(p / 0.10f) * smooth((1f - p) / 0.22f);
        float[] c = Anim.sample(p, Anim.TABLES[table]);
        arm.pitch = MathHelper.lerp(w, arm.pitch, c[0]);
        arm.yaw = MathHelper.lerp(w, arm.yaw, c[1] * side);
        arm.roll = MathHelper.lerp(w, arm.roll, c[2] * side);
        float twist = c[3] * side * w;
        m.body.yaw += twist;
        m.head.yaw -= twist * 0.8f;
        m.body.pitch += c[4] * w;
        m.head.pitch -= c[4] * w * 0.5f;
        other.pitch += c[6] * w;
        float lunge = c[5] * w;
        m.rightLeg.pitch += -lunge * side * 0.8f;
        m.leftLeg.pitch += lunge * side * 0.8f;
    }

    public static void apply(LivingEntity entity, PlayerEntityModel<?> m, float limbAngle, float limbDistance, float t) {
        if (!(entity instanceof PlayerEntity)) return;

        if (STATES.size() > 128) STATES.clear();
        long now = System.nanoTime();
        State s = STATES.get(entity.getUuid());
        float dt = s == null ? 1f : (now - s.last) / 1.0e9f;
        boolean snap = s == null || dt > 0.25f;
        if (s == null) { s = new State(); STATES.put(entity.getUuid(), s); }
        s.last = now;
        float dtc = Math.min(dt, 0.05f);

        float hy0 = m.head.yaw, hp0 = m.head.pitch;

        boolean ground = entity.isOnGround();
        boolean free = !entity.isTouchingWater() && !entity.isClimbing() && !entity.hasVehicle();
        float vy = (float) entity.getVelocity().y;
        if (!snap) {
            if (ground && !s.wasGround && s.lastVy < -0.25f) { s.land = 1f; s.impact = MathHelper.clamp(-s.lastVy * 1.2f, 0.25f, 1f); }
            s.land = Math.max(0f, s.land - dtc * 3.2f);
        }
        s.wasGround = ground;
        if (!ground) s.lastVy = vy;

        Hand hand = entity.preferredHand == null ? Hand.MAIN_HAND : entity.preferredHand;
        ItemStack held = entity.getStackInHand(hand);
        int cat = Anim.category(entity.getMainHandStack());
        float[] target = {
                ground && entity.isSprinting() ? 1f : 0f,
                !ground && free ? MathHelper.clamp(vy * 4f, 0f, 1f) : 0f,
                !ground && free ? MathHelper.clamp(-vy * 2.5f, 0f, 1f) : 0f,
                cat <= 4 ? 1f : 0f};
        float wa = snap ? 1f : 1f - (float) Math.exp(-WEIGHT_RATE * dtc);
        for (int i = 0; i < 4; i++) s.w[i] += (target[i] - s.w[i]) * wa;

        layers(entity, m, s, limbAngle, limbDistance, t);

        // golpe (espada, machado, picareta, pá, enxada ou mão)
        if (entity.handSwinging) {
            float raw = entity.getHandSwingProgress(t - entity.age);
            int hc = Anim.category(held);
            MinecraftClient mc = MinecraftClient.getInstance();
            boolean mining = entity == mc.player && mc.interactionManager != null
                    && mc.interactionManager.isBreakingBlock() && hc >= 1 && hc <= 4;
            float p = s.clock.map(raw, mining);
            if (p > 0f && p < 1f) {
                int table = Anim.table(hc, SwingTracker.combo(entity.getUuid()));
                if (s.lastP < Anim.HIT[table] && p >= Anim.HIT[table]) { s.kick = 1f; s.kickT = 0f; }
                swing(entity, m, p, table);
            }
            s.lastP = p;
        } else {
            s.clock.map(0f, false);
            s.lastP = 0f;
        }

        // tranco do impacto (vibra e some)
        if (s.kick > 0f) {
            s.kickT += dtc;
            s.kick = Math.max(0f, s.kick - dtc * 4.5f);
            float k = s.kick * MathHelper.cos(s.kickT * 32f);
            ModelPart main = entity.getMainArm() == Arm.RIGHT ? m.rightArm : m.leftArm;
            ModelPart off = entity.getMainArm() == Arm.RIGHT ? m.leftArm : m.rightArm;
            m.body.pitch += 0.09f * k;
            main.pitch += 0.16f * k;
            off.pitch += 0.05f * k;
            m.head.pitch -= 0.04f * k;
        }

        // molas: cada parte persegue a pose alvo com leve sobra
        float kS = entity.handSwinging ? 520f : 280f;
        float zeta = entity.handSwinging ? 0.70f : 0.80f;
        ModelPart[] parts = {m.rightArm, m.leftArm, m.rightLeg, m.leftLeg, m.body};
        for (int i = 0; i < parts.length; i++) {
            ModelPart p = parts[i];
            int o = i * 3;
            if (snap) { s.sp[o].snap(p.pitch); s.sp[o + 1].snap(p.yaw); s.sp[o + 2].snap(p.roll); }
            else {
                p.pitch = s.sp[o].step(p.pitch, dt, kS, zeta);
                p.yaw = s.sp[o + 1].step(p.yaw, dt, kS, zeta);
                p.roll = s.sp[o + 2].step(p.roll, dt, kS, zeta);
            }
        }
        // cabeça: só o ajuste das animações é suavizado (a câmera continua sem atraso)
        float dy = m.head.yaw - hy0, dp = m.head.pitch - hp0;
        if (snap) { s.hy.snap(dy); s.hp.snap(dp); }
        m.head.yaw = hy0 + s.hy.step(dy, dt, 220f, 0.9f);
        m.head.pitch = hp0 + s.hp.step(dp, dt, 220f, 0.9f);

        m.leftSleeve.copyTransform(m.leftArm);
        m.rightSleeve.copyTransform(m.rightArm);
        m.leftPants.copyTransform(m.leftLeg);
        m.rightPants.copyTransform(m.rightLeg);
        m.jacket.copyTransform(m.body);
        m.hat.copyTransform(m.head);
    }
}
