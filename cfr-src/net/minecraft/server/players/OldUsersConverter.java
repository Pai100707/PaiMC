/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.io.Files
 *  com.mojang.authlib.ProfileLookupCallback
 *  com.mojang.authlib.yggdrasil.ProfileNotFoundException
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.yggdrasil.ProfileNotFoundException;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.notifications.EmptyNotificationService;
import net.minecraft.server.players.BanListEntry;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ServerOpList;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.server.players.UserWhiteListEntry;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.storage.LevelResource;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class OldUsersConverter {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final File OLD_IPBANLIST = new File("banned-ips.txt");
    public static final File OLD_USERBANLIST = new File("banned-players.txt");
    public static final File OLD_OPLIST = new File("ops.txt");
    public static final File OLD_WHITELIST = new File("white-list.txt");

    static List<String> readOldListFormat(File $$0, Map<String, String[]> $$1) throws IOException {
        List $$2 = Files.readLines((File)$$0, (Charset)StandardCharsets.UTF_8);
        for (String $$3 : $$2) {
            if (($$3 = $$3.trim()).startsWith("#") || $$3.isEmpty()) continue;
            String[] $$4 = $$3.split("\\|");
            $$1.put($$4[0].toLowerCase(Locale.ROOT), $$4);
        }
        return $$2;
    }

    private static void lookupPlayers(MinecraftServer $$02, Collection<String> $$1, ProfileLookupCallback $$2) {
        String[] $$3 = (String[])$$1.stream().filter($$0 -> !StringUtil.isNullOrEmpty($$0)).toArray(String[]::new);
        if ($$02.usesAuthentication()) {
            $$02.services().profileRepository().findProfilesByNames($$3, $$2);
        } else {
            for (String $$4 : $$3) {
                $$2.onProfileLookupSucceeded($$4, UUIDUtil.createOfflinePlayerUUID($$4));
            }
        }
    }

    public static boolean convertUserBanlist(final MinecraftServer $$0) {
        final UserBanList $$1 = new UserBanList(PlayerList.USERBANLIST_FILE, new EmptyNotificationService());
        if (OLD_USERBANLIST.exists() && OLD_USERBANLIST.isFile()) {
            if ($$1.getFile().exists()) {
                try {
                    $$1.load();
                }
                catch (IOException $$2) {
                    LOGGER.warn("Could not load existing file {}", (Object)$$1.getFile().getName(), (Object)$$2);
                }
            }
            try {
                final HashMap $$3 = Maps.newHashMap();
                OldUsersConverter.readOldListFormat(OLD_USERBANLIST, $$3);
                ProfileLookupCallback $$4 = new ProfileLookupCallback(){

                    public void onProfileLookupSucceeded(String $$02, UUID $$12) {
                        NameAndId $$2 = new NameAndId($$12, $$02);
                        $$0.services().nameToIdCache().add($$2);
                        String[] $$32 = (String[])$$3.get($$2.name().toLowerCase(Locale.ROOT));
                        if ($$32 == null) {
                            LOGGER.warn("Could not convert user banlist entry for {}", (Object)$$2.name());
                            throw new ConversionError("Profile not in the conversionlist");
                        }
                        Date $$4 = $$32.length > 1 ? OldUsersConverter.parseDate($$32[1], null) : null;
                        String $$5 = $$32.length > 2 ? $$32[2] : null;
                        Date $$6 = $$32.length > 3 ? OldUsersConverter.parseDate($$32[3], null) : null;
                        String $$7 = $$32.length > 4 ? $$32[4] : null;
                        $$1.add(new UserBanListEntry($$2, $$4, $$5, $$6, $$7));
                    }

                    public void onProfileLookupFailed(String $$02, Exception $$12) {
                        LOGGER.warn("Could not lookup user banlist entry for {}", (Object)$$02, (Object)$$12);
                        if (!($$12 instanceof ProfileNotFoundException)) {
                            throw new ConversionError("Could not request user " + $$02 + " from backend systems", $$12);
                        }
                    }
                };
                OldUsersConverter.lookupPlayers($$0, $$3.keySet(), $$4);
                $$1.save();
                OldUsersConverter.renameOldFile(OLD_USERBANLIST);
            }
            catch (IOException $$5) {
                LOGGER.warn("Could not read old user banlist to convert it!", (Throwable)$$5);
                return false;
            }
            catch (ConversionError $$6) {
                LOGGER.error("Conversion failed, please try again later", (Throwable)$$6);
                return false;
            }
            return true;
        }
        return true;
    }

    public static boolean convertIpBanlist(MinecraftServer $$0) {
        IpBanList $$1 = new IpBanList(PlayerList.IPBANLIST_FILE, new EmptyNotificationService());
        if (OLD_IPBANLIST.exists() && OLD_IPBANLIST.isFile()) {
            if ($$1.getFile().exists()) {
                try {
                    $$1.load();
                }
                catch (IOException $$2) {
                    LOGGER.warn("Could not load existing file {}", (Object)$$1.getFile().getName(), (Object)$$2);
                }
            }
            try {
                HashMap $$3 = Maps.newHashMap();
                OldUsersConverter.readOldListFormat(OLD_IPBANLIST, $$3);
                for (String $$4 : $$3.keySet()) {
                    String[] $$5 = (String[])$$3.get($$4);
                    Date $$6 = $$5.length > 1 ? OldUsersConverter.parseDate($$5[1], null) : null;
                    String $$7 = $$5.length > 2 ? $$5[2] : null;
                    Date $$8 = $$5.length > 3 ? OldUsersConverter.parseDate($$5[3], null) : null;
                    String $$9 = $$5.length > 4 ? $$5[4] : null;
                    $$1.add(new IpBanListEntry($$4, $$6, $$7, $$8, $$9));
                }
                $$1.save();
                OldUsersConverter.renameOldFile(OLD_IPBANLIST);
            }
            catch (IOException $$10) {
                LOGGER.warn("Could not parse old ip banlist to convert it!", (Throwable)$$10);
                return false;
            }
            return true;
        }
        return true;
    }

    public static boolean convertOpsList(final MinecraftServer $$0) {
        final ServerOpList $$1 = new ServerOpList(PlayerList.OPLIST_FILE, new EmptyNotificationService());
        if (OLD_OPLIST.exists() && OLD_OPLIST.isFile()) {
            if ($$1.getFile().exists()) {
                try {
                    $$1.load();
                }
                catch (IOException $$2) {
                    LOGGER.warn("Could not load existing file {}", (Object)$$1.getFile().getName(), (Object)$$2);
                }
            }
            try {
                List $$3 = Files.readLines((File)OLD_OPLIST, (Charset)StandardCharsets.UTF_8);
                ProfileLookupCallback $$4 = new ProfileLookupCallback(){

                    public void onProfileLookupSucceeded(String $$02, UUID $$12) {
                        NameAndId $$2 = new NameAndId($$12, $$02);
                        $$0.services().nameToIdCache().add($$2);
                        $$1.add(new ServerOpListEntry($$2, $$0.operatorUserPermissions(), false));
                    }

                    public void onProfileLookupFailed(String $$02, Exception $$12) {
                        LOGGER.warn("Could not lookup oplist entry for {}", (Object)$$02, (Object)$$12);
                        if (!($$12 instanceof ProfileNotFoundException)) {
                            throw new ConversionError("Could not request user " + $$02 + " from backend systems", $$12);
                        }
                    }
                };
                OldUsersConverter.lookupPlayers($$0, $$3, $$4);
                $$1.save();
                OldUsersConverter.renameOldFile(OLD_OPLIST);
            }
            catch (IOException $$5) {
                LOGGER.warn("Could not read old oplist to convert it!", (Throwable)$$5);
                return false;
            }
            catch (ConversionError $$6) {
                LOGGER.error("Conversion failed, please try again later", (Throwable)$$6);
                return false;
            }
            return true;
        }
        return true;
    }

    public static boolean convertWhiteList(final MinecraftServer $$0) {
        final UserWhiteList $$1 = new UserWhiteList(PlayerList.WHITELIST_FILE, new EmptyNotificationService());
        if (OLD_WHITELIST.exists() && OLD_WHITELIST.isFile()) {
            if ($$1.getFile().exists()) {
                try {
                    $$1.load();
                }
                catch (IOException $$2) {
                    LOGGER.warn("Could not load existing file {}", (Object)$$1.getFile().getName(), (Object)$$2);
                }
            }
            try {
                List $$3 = Files.readLines((File)OLD_WHITELIST, (Charset)StandardCharsets.UTF_8);
                ProfileLookupCallback $$4 = new ProfileLookupCallback(){

                    public void onProfileLookupSucceeded(String $$02, UUID $$12) {
                        NameAndId $$2 = new NameAndId($$12, $$02);
                        $$0.services().nameToIdCache().add($$2);
                        $$1.add(new UserWhiteListEntry($$2));
                    }

                    public void onProfileLookupFailed(String $$02, Exception $$12) {
                        LOGGER.warn("Could not lookup user whitelist entry for {}", (Object)$$02, (Object)$$12);
                        if (!($$12 instanceof ProfileNotFoundException)) {
                            throw new ConversionError("Could not request user " + $$02 + " from backend systems", $$12);
                        }
                    }
                };
                OldUsersConverter.lookupPlayers($$0, $$3, $$4);
                $$1.save();
                OldUsersConverter.renameOldFile(OLD_WHITELIST);
            }
            catch (IOException $$5) {
                LOGGER.warn("Could not read old whitelist to convert it!", (Throwable)$$5);
                return false;
            }
            catch (ConversionError $$6) {
                LOGGER.error("Conversion failed, please try again later", (Throwable)$$6);
                return false;
            }
            return true;
        }
        return true;
    }

    public static @Nullable UUID convertMobOwnerIfNecessary(final MinecraftServer $$0, String $$1) {
        if (StringUtil.isNullOrEmpty($$1) || $$1.length() > 16) {
            try {
                return UUID.fromString($$1);
            }
            catch (IllegalArgumentException $$2) {
                return null;
            }
        }
        Optional<UUID> $$3 = $$0.services().nameToIdCache().get($$1).map(NameAndId::id);
        if ($$3.isPresent()) {
            return $$3.get();
        }
        if ($$0.isSingleplayer() || !$$0.usesAuthentication()) {
            return UUIDUtil.createOfflinePlayerUUID($$1);
        }
        final ArrayList $$4 = new ArrayList();
        ProfileLookupCallback $$5 = new ProfileLookupCallback(){

            public void onProfileLookupSucceeded(String $$02, UUID $$1) {
                NameAndId $$2 = new NameAndId($$1, $$02);
                $$0.services().nameToIdCache().add($$2);
                $$4.add($$2);
            }

            public void onProfileLookupFailed(String $$02, Exception $$1) {
                LOGGER.warn("Could not lookup user whitelist entry for {}", (Object)$$02, (Object)$$1);
            }
        };
        OldUsersConverter.lookupPlayers($$0, Lists.newArrayList((Object[])new String[]{$$1}), $$5);
        if (!$$4.isEmpty()) {
            return ((NameAndId)$$4.getFirst()).id();
        }
        return null;
    }

    public static boolean convertPlayers(final DedicatedServer $$0) {
        final File $$1 = OldUsersConverter.getWorldPlayersDirectory($$0);
        final File $$2 = new File($$1.getParentFile(), "playerdata");
        final File $$3 = new File($$1.getParentFile(), "unknownplayers");
        if (!$$1.exists() || !$$1.isDirectory()) {
            return true;
        }
        File[] $$4 = $$1.listFiles();
        ArrayList $$5 = Lists.newArrayList();
        for (File $$6 : $$4) {
            String $$8;
            String $$7 = $$6.getName();
            if (!$$7.toLowerCase(Locale.ROOT).endsWith(".dat") || ($$8 = $$7.substring(0, $$7.length() - ".dat".length())).isEmpty()) continue;
            $$5.add($$8);
        }
        try {
            Object[] $$9 = $$5.toArray(new String[$$5.size()]);
            ProfileLookupCallback $$10 = new ProfileLookupCallback(){
                final /* synthetic */ String[] val$names;
                {
                    this.val$names = stringArray;
                }

                public void onProfileLookupSucceeded(String $$02, UUID $$12) {
                    NameAndId $$22 = new NameAndId($$12, $$02);
                    $$0.services().nameToIdCache().add($$22);
                    this.movePlayerFile($$2, this.getFileNameForProfile($$02), $$12.toString());
                }

                public void onProfileLookupFailed(String $$02, Exception $$12) {
                    LOGGER.warn("Could not lookup user uuid for {}", (Object)$$02, (Object)$$12);
                    if (!($$12 instanceof ProfileNotFoundException)) {
                        throw new ConversionError("Could not request user " + $$02 + " from backend systems", $$12);
                    }
                    String $$22 = this.getFileNameForProfile($$02);
                    this.movePlayerFile($$3, $$22, $$22);
                }

                private void movePlayerFile(File $$02, String $$12, String $$22) {
                    File $$32 = new File($$1, $$12 + ".dat");
                    File $$4 = new File($$02, $$22 + ".dat");
                    OldUsersConverter.ensureDirectoryExists($$02);
                    if (!$$32.renameTo($$4)) {
                        throw new ConversionError("Could not convert file for " + $$12);
                    }
                }

                private String getFileNameForProfile(String $$02) {
                    String $$12 = null;
                    for (String $$22 : this.val$names) {
                        if ($$22 == null || !$$22.equalsIgnoreCase($$02)) continue;
                        $$12 = $$22;
                        break;
                    }
                    if ($$12 == null) {
                        throw new ConversionError("Could not find the filename for " + $$02 + " anymore");
                    }
                    return $$12;
                }
            };
            OldUsersConverter.lookupPlayers($$0, Lists.newArrayList((Object[])$$9), $$10);
        }
        catch (ConversionError $$11) {
            LOGGER.error("Conversion failed, please try again later", (Throwable)$$11);
            return false;
        }
        return true;
    }

    static void ensureDirectoryExists(File $$0) {
        if ($$0.exists()) {
            if ($$0.isDirectory()) {
                return;
            }
            throw new ConversionError("Can't create directory " + $$0.getName() + " in world save directory.");
        }
        if (!$$0.mkdirs()) {
            throw new ConversionError("Can't create directory " + $$0.getName() + " in world save directory.");
        }
    }

    public static boolean serverReadyAfterUserconversion(MinecraftServer $$0) {
        boolean $$1 = OldUsersConverter.areOldUserlistsRemoved();
        $$1 = $$1 && OldUsersConverter.areOldPlayersConverted($$0);
        return $$1;
    }

    private static boolean areOldUserlistsRemoved() {
        boolean $$0 = false;
        if (OLD_USERBANLIST.exists() && OLD_USERBANLIST.isFile()) {
            $$0 = true;
        }
        boolean $$1 = false;
        if (OLD_IPBANLIST.exists() && OLD_IPBANLIST.isFile()) {
            $$1 = true;
        }
        boolean $$2 = false;
        if (OLD_OPLIST.exists() && OLD_OPLIST.isFile()) {
            $$2 = true;
        }
        boolean $$3 = false;
        if (OLD_WHITELIST.exists() && OLD_WHITELIST.isFile()) {
            $$3 = true;
        }
        if ($$0 || $$1 || $$2 || $$3) {
            LOGGER.warn("**** FAILED TO START THE SERVER AFTER ACCOUNT CONVERSION!");
            LOGGER.warn("** please remove the following files and restart the server:");
            if ($$0) {
                LOGGER.warn("* {}", (Object)OLD_USERBANLIST.getName());
            }
            if ($$1) {
                LOGGER.warn("* {}", (Object)OLD_IPBANLIST.getName());
            }
            if ($$2) {
                LOGGER.warn("* {}", (Object)OLD_OPLIST.getName());
            }
            if ($$3) {
                LOGGER.warn("* {}", (Object)OLD_WHITELIST.getName());
            }
            return false;
        }
        return true;
    }

    private static boolean areOldPlayersConverted(MinecraftServer $$0) {
        File $$1 = OldUsersConverter.getWorldPlayersDirectory($$0);
        if ($$1.exists() && $$1.isDirectory() && ($$1.list().length > 0 || !$$1.delete())) {
            LOGGER.warn("**** DETECTED OLD PLAYER DIRECTORY IN THE WORLD SAVE");
            LOGGER.warn("**** THIS USUALLY HAPPENS WHEN THE AUTOMATIC CONVERSION FAILED IN SOME WAY");
            LOGGER.warn("** please restart the server and if the problem persists, remove the directory '{}'", (Object)$$1.getPath());
            return false;
        }
        return true;
    }

    private static File getWorldPlayersDirectory(MinecraftServer $$0) {
        return $$0.getWorldPath(LevelResource.PLAYER_OLD_DATA_DIR).toFile();
    }

    private static void renameOldFile(File $$0) {
        File $$1 = new File($$0.getName() + ".converted");
        $$0.renameTo($$1);
    }

    static Date parseDate(String $$0, Date $$1) {
        Date $$4;
        try {
            Date $$2 = BanListEntry.DATE_FORMAT.parse($$0);
        }
        catch (ParseException $$3) {
            $$4 = $$1;
        }
        return $$4;
    }

    static class ConversionError
    extends RuntimeException {
        ConversionError(String $$0, Throwable $$1) {
            super($$0, $$1);
        }

        ConversionError(String $$0) {
            super($$0);
        }
    }
}

