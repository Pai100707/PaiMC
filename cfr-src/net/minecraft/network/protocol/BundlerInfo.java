/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.BundleDelimiterPacket;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import org.jspecify.annotations.Nullable;

public interface BundlerInfo {
    public static final int BUNDLE_SIZE_LIMIT = 4096;

    public static <T extends PacketListener, P extends BundlePacket<? super T>> BundlerInfo createForPacket(final PacketType<P> $$0, final Function<Iterable<Packet<? super T>>, P> $$1, final BundleDelimiterPacket<? super T> $$2) {
        return new BundlerInfo(){

            @Override
            public void unbundlePacket(Packet<?> $$02, Consumer<Packet<?>> $$12) {
                if ($$02.type() == $$0) {
                    BundlePacket $$22 = (BundlePacket)$$02;
                    $$12.accept($$2);
                    $$22.subPackets().forEach($$12);
                    $$12.accept($$2);
                } else {
                    $$12.accept($$02);
                }
            }

            @Override
            public @Nullable Bundler startPacketBundling(Packet<?> $$02) {
                if ($$02 == $$2) {
                    return new Bundler(){
                        private final List<Packet<? super T>> bundlePackets = new ArrayList();

                        @Override
                        public @Nullable Packet<?> addPacket(Packet<?> $$0) {
                            if ($$0 == $$2) {
                                return (Packet)$$1.apply(this.bundlePackets);
                            }
                            Packet<?> $$1 = $$0;
                            if (this.bundlePackets.size() >= 4096) {
                                throw new IllegalStateException("Too many packets in a bundle");
                            }
                            this.bundlePackets.add($$1);
                            return null;
                        }
                    };
                }
                return null;
            }
        };
    }

    public void unbundlePacket(Packet<?> var1, Consumer<Packet<?>> var2);

    public @Nullable Bundler startPacketBundling(Packet<?> var1);

    public static interface Bundler {
        public @Nullable Packet<?> addPacket(Packet<?> var1);
    }
}

