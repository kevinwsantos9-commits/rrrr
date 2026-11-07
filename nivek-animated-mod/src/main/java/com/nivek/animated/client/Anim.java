package com.nivek.animated.client;

import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.MathHelper;

/** Tabelas de keyframes (curvas Catmull-Rom), molas e relógio de golpe. */
public final class Anim {
    private Anim() {}

    // colunas: tempo | pitch do braço | yaw do braço | roll do braço | giro do corpo | inclinação do corpo | avanço (lunge) | braço livre
    public static final float[][] SLASH = {
            {0.00f, -0.30f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f},
            {0.16f, -0.80f, -0.20f, 0.20f, 0.15f, -0.05f, -0.10f, 0.10f},
            {0.32f, -2.60f, -0.90f, 0.65f, -0.35f, -0.12f, -0.20f, 0.25f},
            {0.50f, -1.70f, 0.10f, -0.10f, 0.10f, 0.10f, 0.30f, 0.20f},
            {0.62f, -0.65f, 1.20f, -0.55f, 0.45f, 0.20f, 0.55f, 0.10f},
            {0.80f, -0.85f, 0.95f, -0.40f, 0.35f, 0.12f, 0.40f, 0.05f},
            {1.00f, -0.40f, 0.30f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f}};
    public static final float[][] OVERHEAD = {
            {0.00f, -0.30f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f},
            {0.20f, -1.00f, 0.00f, 0.10f, 0.00f, -0.10f, -0.10f, 0.10f},
            {0.36f, -3.10f, 0.00f, 0.30f, 0.00f, -0.22f, -0.20f, 0.30f},
            {0.52f, -1.40f, 0.00f, -0.05f, 0.00f, 0.25f, 0.30f, 0.15f},
            {0.62f, -0.70f, 0.00f, -0.10f, 0.00f, 0.42f, 0.50f, 0.05f},
            {0.80f, -0.80f, 0.00f, 0.00f, 0.00f, 0.25f, 0.35f, 0.00f},
            {1.00f, -0.40f, 0.00f, 0.00f, 0.00f, 0.05f, 0.00f, 0.00f}};
    public static final float[][] THRUST = {
            {0.00f, -0.30f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f},
            {0.20f, -0.90f, -0.50f, 0.40f, -0.40f, -0.10f, -0.20f, 0.20f},
            {0.38f, -1.10f, -0.70f, 0.45f, -0.45f, -0.10f, -0.25f, 0.25f},
            {0.52f, -1.58f, 0.30f, -0.10f, 0.40f, 0.30f, 0.60f, 0.20f},
            {0.66f, -1.60f, 0.35f, -0.10f, 0.40f, 0.30f, 0.60f, 0.15f},
            {0.85f, -1.00f, 0.10f, 0.00f, 0.10f, 0.10f, 0.20f, 0.00f},
            {1.00f, -0.40f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f}};
    public static final float[][] AXE = {
            {0.00f, -0.30f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f},
            {0.22f, -2.20f, -0.60f, 0.80f, -0.50f, -0.15f, -0.25f, 0.30f},
            {0.40f, -2.40f, -0.70f, 0.85f, -0.55f, -0.18f, -0.28f, 0.32f},
            {0.54f, -1.20f, 0.60f, -0.40f, 0.50f, 0.30f, 0.40f, 0.20f},
            {0.62f, -0.90f, 1.00f, -0.60f, 0.60f, 0.38f, 0.50f, 0.10f},
            {0.82f, -0.95f, 0.85f, -0.45f, 0.45f, 0.25f, 0.30f, 0.05f},
            {1.00f, -0.40f, 0.30f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f}};
    public static final float[][] PICK = {
            {0.00f, -0.30f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f},
            {0.20f, -1.40f, 0.00f, 0.10f, 0.00f, -0.10f, -0.10f, 0.20f},
            {0.38f, -3.00f, 0.00f, 0.25f, 0.00f, -0.25f, -0.20f, 0.30f},
            {0.55f, -1.00f, 0.00f, -0.05f, 0.00f, 0.30f, 0.35f, 0.10f},
            {0.64f, -0.55f, 0.00f, -0.10f, 0.00f, 0.42f, 0.45f, 0.05f},
            {0.82f, -0.90f, 0.00f, 0.00f, 0.00f, 0.20f, 0.20f, 0.00f},
            {1.00f, -0.40f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f}};
    public static final float[][] SHOVEL = {
            {0.00f, -0.30f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f},
            {0.25f, -1.30f, 0.30f, 0.30f, 0.20f, 0.05f, 0.00f, 0.15f},
            {0.50f, -0.40f, 0.10f, 0.00f, 0.00f, 0.40f, 0.40f, 0.10f},
            {0.65f, 0.10f, 0.00f, 0.00f, 0.00f, 0.50f, 0.50f, 0.00f},
            {0.85f, -0.50f, 0.10f, 0.00f, 0.00f, 0.20f, 0.20f, 0.00f},
            {1.00f, -0.40f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f}};
    public static final float[][] HOE = {
            {0.00f, -0.30f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f},
            {0.30f, -1.90f, -0.80f, 0.60f, -0.40f, -0.10f, -0.15f, 0.20f},
            {0.55f, -0.80f, 0.90f, -0.40f, 0.40f, 0.30f, 0.40f, 0.10f},
            {0.70f, -0.70f, 1.10f, -0.50f, 0.50f, 0.35f, 0.45f, 0.05f},
            {1.00f, -0.40f, 0.30f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f}};
    public static final float[][] PUNCH = {
            {0.00f, -0.20f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f},
            {0.25f, -0.50f, -0.20f, 0.20f, -0.20f, 0.00f, -0.05f, 0.10f},
            {0.50f, -1.30f, 0.20f, 0.00f, 0.25f, 0.10f, 0.20f, 0.10f},
            {0.65f, -1.20f, 0.15f, 0.00f, 0.20f, 0.10f, 0.20f, 0.05f},
            {1.00f, -0.20f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f}};

    public static final float[][][] TABLES = {SLASH, OVERHEAD, THRUST, AXE, PICK, SHOVEL, HOE, PUNCH};
    /** momento do impacto em cada tabela (dispara o tranco). */
    public static final float[] HIT = {0.60f, 0.60f, 0.52f, 0.58f, 0.55f, 0.62f, 0.60f, 0.50f};

    /** 0 espada, 1 machado, 2 picareta, 3 pá, 4 enxada, 5 outro. */
    public static int category(ItemStack s) {
        Item it = s.getItem();
        if (it instanceof SwordItem) return 0;
        if (it instanceof AxeItem) return 1;
        if (it instanceof PickaxeItem) return 2;
        if (it instanceof ShovelItem) return 3;
        if (it instanceof HoeItem) return 4;
        return 5;
    }

    public static int table(int cat, int combo) {
        if (cat == 0) return combo;
        if (cat == 5) return 7;
        return cat + 2;
    }

    /** Spline cúbica de Hermite (Catmull-Rom) por todas as chaves: velocidade contínua, sem "quebras". */
    public static float[] sample(float p, float[][] k) {
        int n = k.length;
        int i = 0;
        while (i < n - 2 && p > k[i + 1][0]) i++;
        float t0 = k[i][0], t1 = k[i + 1][0], h = t1 - t0;
        float u = MathHelper.clamp((p - t0) / h, 0f, 1f);
        float u2 = u * u, u3 = u2 * u;
        float h00 = 2 * u3 - 3 * u2 + 1, h10 = u3 - 2 * u2 + u, h01 = -2 * u3 + 3 * u2, h11 = u3 - u2;
        float[] r = new float[7];
        for (int c = 1; c <= 7; c++) {
            float y0 = k[i][c], y1 = k[i + 1][c];
            float m0 = i > 0 ? (y1 - k[i - 1][c]) / (t1 - k[i - 1][0]) : (y1 - y0) / h;
            float m1 = i + 2 < n ? (k[i + 2][c] - y0) / (k[i + 2][0] - t0) : (y1 - y0) / h;
            r[c - 1] = h00 * y0 + h10 * h * m0 + h01 * y1 + h11 * h * m1;
        }
        return r;
    }

    /** Mola amortecida: segue o alvo com leve "overshoot" (movimento com peso e vida). */
    public static final class Spring {
        public float x, v;
        private boolean init;

        public float step(float target, float dt, float k, float zeta) {
            if (!init) { x = target; v = 0f; init = true; return x; }
            dt = Math.min(dt, 0.05f);
            float c = 2f * zeta * (float) Math.sqrt(k);
            int n = Math.max(1, (int) Math.ceil(dt / 0.008f));
            float h = dt / n;
            for (int i = 0; i < n; i++) {
                v += (k * (target - x) - c * v) * h;
                x += v * h;
            }
            return x;
        }

        public void snap(float value) { x = value; v = 0f; init = true; }
    }

    /**
     * O jogo reinicia o golpe na metade enquanto se minera (progresso só vai de 0 a 0,5).
     * Este relógio detecta isso e estica o progresso para 0..1, para a animação completa tocar.
     */
    public static final class SwingClock {
        private float last;
        private boolean fast;

        public float map(float p, boolean forceFast) {
            if (p <= 0.0001f) { last = 0f; fast = false; return 0f; }
            if (p < last - 0.05f) fast = last < 0.8f;
            else if (p > 0.85f) fast = false;
            last = p;
            return (fast || forceFast) ? Math.min(p * 2f, 1f) : p;
        }
    }
}
