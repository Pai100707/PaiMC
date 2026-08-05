/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogQueues
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.gui;

import com.google.common.collect.Lists;
import com.mojang.logging.LogQueues;
import com.mojang.logging.LogUtils;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.gui.PlayerListComponent;
import net.minecraft.server.gui.StatsComponent;
import org.slf4j.Logger;

public class MinecraftServerGui
extends JComponent {
    private static final Font MONOSPACED = new Font("Monospaced", 0, 12);
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TITLE = "Minecraft server";
    private static final String SHUTDOWN_TITLE = "Minecraft server - shutting down!";
    private final DedicatedServer server;
    private Thread logAppenderThread;
    private final Collection<Runnable> finalizers = Lists.newArrayList();
    final AtomicBoolean isClosing = new AtomicBoolean();

    public static MinecraftServerGui showFrameFor(final DedicatedServer $$0) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception exception) {
            // empty catch block
        }
        final JFrame $$1 = new JFrame(TITLE);
        final MinecraftServerGui $$2 = new MinecraftServerGui($$0);
        $$1.setDefaultCloseOperation(2);
        $$1.add($$2);
        $$1.pack();
        $$1.setLocationRelativeTo(null);
        $$1.setVisible(true);
        $$1.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent $$02) {
                if (!$$2.isClosing.getAndSet(true)) {
                    $$1.setTitle(MinecraftServerGui.SHUTDOWN_TITLE);
                    $$0.halt(true);
                    $$2.runFinalizers();
                }
            }
        });
        $$2.addFinalizer($$1::dispose);
        $$2.start();
        return $$2;
    }

    private MinecraftServerGui(DedicatedServer $$0) {
        this.server = $$0;
        this.setPreferredSize(new Dimension(854, 480));
        this.setLayout(new BorderLayout());
        try {
            this.add((Component)this.buildChatPanel(), "Center");
            this.add((Component)this.buildInfoPanel(), "West");
        }
        catch (Exception $$1) {
            LOGGER.error("Couldn't build server GUI", (Throwable)$$1);
        }
    }

    public void addFinalizer(Runnable $$0) {
        this.finalizers.add($$0);
    }

    private JComponent buildInfoPanel() {
        JPanel $$0 = new JPanel(new BorderLayout());
        StatsComponent $$1 = new StatsComponent(this.server);
        this.finalizers.add($$1::close);
        $$0.add((Component)$$1, "North");
        $$0.add((Component)this.buildPlayerPanel(), "Center");
        $$0.setBorder(new TitledBorder(new EtchedBorder(), "Stats"));
        return $$0;
    }

    private JComponent buildPlayerPanel() {
        PlayerListComponent $$0 = new PlayerListComponent(this.server);
        JScrollPane $$1 = new JScrollPane($$0, 22, 30);
        $$1.setBorder(new TitledBorder(new EtchedBorder(), "Players"));
        return $$1;
    }

    private JComponent buildChatPanel() {
        JPanel $$0 = new JPanel(new BorderLayout());
        JTextArea $$12 = new JTextArea();
        JScrollPane $$2 = new JScrollPane($$12, 22, 30);
        $$12.setEditable(false);
        $$12.setFont(MONOSPACED);
        JTextField $$3 = new JTextField();
        $$3.addActionListener($$1 -> {
            String $$2 = $$3.getText().trim();
            if (!$$2.isEmpty()) {
                this.server.handleConsoleInput($$2, this.server.createCommandSourceStack());
            }
            $$3.setText("");
        });
        $$12.addFocusListener(new FocusAdapter(this){

            @Override
            public void focusGained(FocusEvent $$0) {
            }
        });
        $$0.add((Component)$$2, "Center");
        $$0.add((Component)$$3, "South");
        $$0.setBorder(new TitledBorder(new EtchedBorder(), "Log and chat"));
        this.logAppenderThread = new Thread(() -> {
            String $$2;
            while (($$2 = LogQueues.getNextLogEvent((String)"ServerGuiConsole")) != null) {
                this.print($$12, $$2, $$2);
            }
        });
        this.logAppenderThread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        this.logAppenderThread.setDaemon(true);
        return $$0;
    }

    public void start() {
        this.logAppenderThread.start();
    }

    public void close() {
        if (!this.isClosing.getAndSet(true)) {
            this.runFinalizers();
        }
    }

    void runFinalizers() {
        this.finalizers.forEach(Runnable::run);
    }

    public void print(JTextArea $$0, JScrollPane $$1, String $$2) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> this.print($$0, $$1, $$2));
            return;
        }
        Document $$3 = $$0.getDocument();
        JScrollBar $$4 = $$1.getVerticalScrollBar();
        boolean $$5 = false;
        if ($$1.getViewport().getView() == $$0) {
            $$5 = (double)$$4.getValue() + $$4.getSize().getHeight() + (double)(MONOSPACED.getSize() * 4) > (double)$$4.getMaximum();
        }
        try {
            $$3.insertString($$3.getLength(), $$2, null);
        }
        catch (BadLocationException badLocationException) {
            // empty catch block
        }
        if ($$5) {
            $$4.setValue(Integer.MAX_VALUE);
        }
    }
}

