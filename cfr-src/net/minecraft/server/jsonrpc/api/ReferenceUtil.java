/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.server.jsonrpc.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.net.URI;
import java.net.URISyntaxException;

public class ReferenceUtil {
    public static final Codec<URI> REFERENCE_CODEC = Codec.STRING.comapFlatMap($$0 -> {
        try {
            return DataResult.success((Object)new URI((String)$$0));
        }
        catch (URISyntaxException $$1) {
            return DataResult.error($$1::getMessage);
        }
    }, URI::toString);

    public static URI createLocalReference(String $$0) {
        return URI.create("#/components/schemas/" + $$0);
    }
}

