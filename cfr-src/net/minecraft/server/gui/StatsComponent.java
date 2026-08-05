/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javax.swing.JComponent;
import javax.swing.Timer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.TimeUtil;
import org.jspecify.annotations.Nullable;

public class StatsComponent
extends JComponent {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("########0.000", DecimalFormatSymbols.getInstance(Locale.ROOT));
    private final int[] values = new int[256];
    private int vp;
    private final @Nullable String[] msgs = new String[11];
    private final MinecraftServer server;
    private final Timer timer;

    public StatsComponent(MinecraftServer $$02) {
        this.server = $$02;
        this.setPreferredSize(new Dimension(456, 246));
        this.setMinimumSize(new Dimension(456, 246));
        this.setMaximumSize(new Dimension(456, 246));
        this.timer = new Timer(500, $$0 -> this.tick());
        this.timer.start();
        this.setBackground(Color.BLACK);
    }

    private void tick() {
        long $$0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        this.msgs[0] = "Memory use: " + $$0 / 1024L / 1024L + " mb (" + Runtime.getRuntime().freeMemory() * 100L / Runtime.getRuntime().maxMemory() + "% free)";
        this.msgs[1] = "Avg tick: " + DECIMAL_FORMAT.format((double)this.server.getAverageTickTimeNanos() / (double)TimeUtil.NANOSECONDS_PER_MILLISECOND) + " ms";
        this.values[this.vp++ & 0xFF] = (int)($$0 * 100L / Runtime.getRuntime().maxMemory());
        this.repaint();
    }

    @Override
    public void paint(Graphics $$0) {
        $$0.setColor(new Color(0xFFFFFF));
        $$0.fillRect(0, 0, 456, 246);
        for (int $$1 = 0; $$1 < 256; ++$$1) {
            int $$2 = this.values[$$1 + this.vp & 0xFF];
            $$0.setColor(new Color($$2 + 28 << 16));
            $$0.fillRect($$1, 100 - $$2, 1, $$2);
        }
        $$0.setColor(Color.BLACK);
        for (int $$3 = 0; $$3 < this.msgs.length; ++$$3) {
            String $$4 = this.msgs[$$3];
            if ($$4 == null) continue;
            $$0.drawString($$4, 32, 116 + $$3 * 16);
        }
    }

    public void close() {
        this.timer.stop();
    }
}

