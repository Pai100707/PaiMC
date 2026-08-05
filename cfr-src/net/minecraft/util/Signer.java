/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.security.PrivateKey;
import java.security.Signature;
import net.minecraft.util.SignatureUpdater;
import org.slf4j.Logger;

public interface Signer {
    public static final Logger LOGGER = LogUtils.getLogger();

    public byte[] sign(SignatureUpdater var1);

    default public byte[] sign(byte[] $$0) {
        return this.sign($$1 -> $$1.update($$0));
    }

    public static Signer from(PrivateKey $$0, String $$1) {
        return $$2 -> {
            try {
                Signature $$3 = Signature.getInstance($$1);
                $$3.initSign($$0);
                $$2.update($$3::update);
                return $$3.sign();
            }
            catch (Exception $$4) {
                throw new IllegalStateException("Failed to sign message", $$4);
            }
        };
    }
}

