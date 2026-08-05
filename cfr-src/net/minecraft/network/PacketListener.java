/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketUtils;

public interface PacketListener {
    public PacketFlow flow();

    public ConnectionProtocol protocol();

    public void onDisconnect(DisconnectionDetails var1);

    default public void onPacketError(Packet $$0, Exception $$1) throws ReportedException {
        throw PacketUtils.makeReportedException($$1, $$0, this);
    }

    default public DisconnectionDetails createDisconnectionInfo(Component $$0, Throwable $$1) {
        return new DisconnectionDetails($$0);
    }

    public boolean isAcceptingMessages();

    default public boolean shouldHandleMessage(Packet<?> $$0) {
        return this.isAcceptingMessages();
    }

    default public void fillCrashReport(CrashReport $$0) {
        CrashReportCategory $$1 = $$0.addCategory("Connection");
        $$1.setDetail("Protocol", () -> this.protocol().id());
        $$1.setDetail("Flow", () -> this.flow().toString());
        this.fillListenerSpecificCrashDetails($$0, $$1);
    }

    default public void fillListenerSpecificCrashDetails(CrashReport $$0, CrashReportCategory $$1) {
    }
}

