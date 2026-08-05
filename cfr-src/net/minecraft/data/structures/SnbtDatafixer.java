/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 */
package net.minecraft.data.structures;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.data.structures.StructureUpdater;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.Bootstrap;

public class SnbtDatafixer {
    public static void main(String[] $$0) throws IOException {
        SharedConstants.setVersion(DetectedVersion.BUILT_IN);
        Bootstrap.bootStrap();
        for (String $$1 : $$0) {
            SnbtDatafixer.updateInDirectory($$1);
        }
    }

    private static void updateInDirectory(String $$02) throws IOException {
        try (Stream<Path> $$1 = Files.walk(Paths.get($$02, new String[0]), new FileVisitOption[0]);){
            $$1.filter($$0 -> $$0.toString().endsWith(".snbt")).forEach($$0 -> {
                try {
                    String $$1 = Files.readString($$0);
                    CompoundTag $$2 = NbtUtils.snbtToStructure($$1);
                    CompoundTag $$3 = StructureUpdater.update($$0.toString(), $$2);
                    NbtToSnbt.writeSnbt(CachedOutput.NO_CACHE, $$0, NbtUtils.structureToSnbt($$3));
                }
                catch (CommandSyntaxException | IOException $$4) {
                    throw new RuntimeException($$4);
                }
            });
        }
    }
}

