/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 */
package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ClientInformation;

public record CommonListenerCookie(GameProfile gameProfile, int latency, ClientInformation clientInformation, boolean transferred) {
    public static CommonListenerCookie createInitial(GameProfile $$0, boolean $$1) {
        return new CommonListenerCookie($$0, 0, ClientInformation.createDefault(), $$1);
    }
}

