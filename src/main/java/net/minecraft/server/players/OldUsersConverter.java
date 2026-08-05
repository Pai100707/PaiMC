package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.yggdrasil.ProfileNotFoundException;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.notifications.EmptyNotificationService;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

public class OldUsersConverter {
   static final Logger LOGGER = LogUtils.getLogger();
   public static final File OLD_IPBANLIST = new File("banned-ips.txt");
   public static final File OLD_USERBANLIST = new File("banned-players.txt");
   public static final File OLD_OPLIST = new File("ops.txt");
   public static final File OLD_WHITELIST = new File("white-list.txt");

   static List<String> readOldListFormat(File $$0, Map<String, String[]> $$1) throws IOException {
      List<String> $$2 = Files.readLines($$0, StandardCharsets.UTF_8);

      for (String $$3 : $$2) {
         $$3 = $$3.trim();
         if (!$$3.startsWith("#") && !$$3.isEmpty()) {
            String[] $$4 = $$3.split("\\|");
            $$1.put($$4[0].toLowerCase(Locale.ROOT), $$4);
         }
      }

      return $$2;
   }

   private static void lookupPlayers(net.minecraft.server.MinecraftServer $$0, Collection<String> $$1, ProfileLookupCallback $$2) {
      String[] $$3 = $$1.stream().filter($$0x -> !StringUtil.isNullOrEmpty($$0x)).toArray(String[]::new);
      if ($$0.usesAuthentication()) {
         $$0.services().profileRepository().findProfilesByNames($$3, $$2);
      } else {
         for (String $$4 : $$3) {
            $$2.onProfileLookupSucceeded($$4, UUIDUtil.createOfflinePlayerUUID($$4));
         }
      }
   }

   public static boolean convertUserBanlist(final net.minecraft.server.MinecraftServer $$0) {
      final UserBanList $$1 = new UserBanList(PlayerList.USERBANLIST_FILE, new EmptyNotificationService());
      if (OLD_USERBANLIST.exists() && OLD_USERBANLIST.isFile()) {
         if ($$1.getFile().exists()) {
            try {
               $$1.load();
            } catch (IOException var6) {
               LOGGER.warn("Could not load existing file {}", $$1.getFile().getName(), var6);
            }
         }

         try {
            final Map<String, String[]> $$3 = Maps.newHashMap();
            readOldListFormat(OLD_USERBANLIST, $$3);
            ProfileLookupCallback $$4 = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(String $$0x, UUID $$1x) {
                  NameAndId $$2 = new NameAndId($$1, $$0);
                  $$0.services().nameToIdCache().add($$2);
                  String[] $$3x = $$3.get($$2.name().toLowerCase(Locale.ROOT));
                  if ($$3x == null) {
                     OldUsersConverter.LOGGER.warn("Could not convert user banlist entry for {}", $$2.name());
                     throw new OldUsersConverter.ConversionError("Profile not in the conversionlist");
                  } else {
                     Date $$4x = $$3x.length > 1 ? OldUsersConverter.parseDate($$3x[1], null) : null;
                     String $$5 = $$3x.length > 2 ? $$3x[2] : null;
                     Date $$6 = $$3x.length > 3 ? OldUsersConverter.parseDate($$3x[3], null) : null;
                     String $$7 = $$3x.length > 4 ? $$3x[4] : null;
                     $$1.add(new UserBanListEntry($$2, $$4x, $$5, $$6, $$7));
                  }
               }

               public void onProfileLookupFailed(String $$0x, Exception $$1x) {
                  OldUsersConverter.LOGGER.warn("Could not lookup user banlist entry for {}", $$0, $$1);
                  if (!($$1 instanceof ProfileNotFoundException)) {
                     throw new OldUsersConverter.ConversionError("Could not request user " + $$0 + " from backend systems", $$1);
                  }
               }
            };
            lookupPlayers($$0, $$3.keySet(), $$4);
            $$1.save();
            renameOldFile(OLD_USERBANLIST);
            return true;
         } catch (IOException var4) {
            LOGGER.warn("Could not read old user banlist to convert it!", var4);
            return false;
         } catch (OldUsersConverter.ConversionError var5) {
            LOGGER.error("Conversion failed, please try again later", var5);
            return false;
         }
      } else {
         return true;
      }
   }

   public static boolean convertIpBanlist(net.minecraft.server.MinecraftServer $$0) {
      IpBanList $$1 = new IpBanList(PlayerList.IPBANLIST_FILE, new EmptyNotificationService());
      if (OLD_IPBANLIST.exists() && OLD_IPBANLIST.isFile()) {
         if ($$1.getFile().exists()) {
            try {
               $$1.load();
            } catch (IOException var11) {
               LOGGER.warn("Could not load existing file {}", $$1.getFile().getName(), var11);
            }
         }

         try {
            Map<String, String[]> $$3 = Maps.newHashMap();
            readOldListFormat(OLD_IPBANLIST, $$3);

            for (String $$4 : $$3.keySet()) {
               String[] $$5 = $$3.get($$4);
               Date $$6 = $$5.length > 1 ? parseDate($$5[1], null) : null;
               String $$7 = $$5.length > 2 ? $$5[2] : null;
               Date $$8 = $$5.length > 3 ? parseDate($$5[3], null) : null;
               String $$9 = $$5.length > 4 ? $$5[4] : null;
               $$1.add(new IpBanListEntry($$4, $$6, $$7, $$8, $$9));
            }

            $$1.save();
            renameOldFile(OLD_IPBANLIST);
            return true;
         } catch (IOException var10) {
            LOGGER.warn("Could not parse old ip banlist to convert it!", var10);
            return false;
         }
      } else {
         return true;
      }
   }

   public static boolean convertOpsList(final net.minecraft.server.MinecraftServer $$0) {
      final ServerOpList $$1 = new ServerOpList(PlayerList.OPLIST_FILE, new EmptyNotificationService());
      if (OLD_OPLIST.exists() && OLD_OPLIST.isFile()) {
         if ($$1.getFile().exists()) {
            try {
               $$1.load();
            } catch (IOException var6) {
               LOGGER.warn("Could not load existing file {}", $$1.getFile().getName(), var6);
            }
         }

         try {
            List<String> $$3 = Files.readLines(OLD_OPLIST, StandardCharsets.UTF_8);
            ProfileLookupCallback $$4 = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(String $$0x, UUID $$1x) {
                  NameAndId $$2 = new NameAndId($$1, $$0);
                  $$0.services().nameToIdCache().add($$2);
                  $$1.add(new ServerOpListEntry($$2, $$0.operatorUserPermissions(), false));
               }

               public void onProfileLookupFailed(String $$0x, Exception $$1x) {
                  OldUsersConverter.LOGGER.warn("Could not lookup oplist entry for {}", $$0, $$1);
                  if (!($$1 instanceof ProfileNotFoundException)) {
                     throw new OldUsersConverter.ConversionError("Could not request user " + $$0 + " from backend systems", $$1);
                  }
               }
            };
            lookupPlayers($$0, $$3, $$4);
            $$1.save();
            renameOldFile(OLD_OPLIST);
            return true;
         } catch (IOException var4) {
            LOGGER.warn("Could not read old oplist to convert it!", var4);
            return false;
         } catch (OldUsersConverter.ConversionError var5) {
            LOGGER.error("Conversion failed, please try again later", var5);
            return false;
         }
      } else {
         return true;
      }
   }

   public static boolean convertWhiteList(final net.minecraft.server.MinecraftServer $$0) {
      final UserWhiteList $$1 = new UserWhiteList(PlayerList.WHITELIST_FILE, new EmptyNotificationService());
      if (OLD_WHITELIST.exists() && OLD_WHITELIST.isFile()) {
         if ($$1.getFile().exists()) {
            try {
               $$1.load();
            } catch (IOException var6) {
               LOGGER.warn("Could not load existing file {}", $$1.getFile().getName(), var6);
            }
         }

         try {
            List<String> $$3 = Files.readLines(OLD_WHITELIST, StandardCharsets.UTF_8);
            ProfileLookupCallback $$4 = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(String $$0x, UUID $$1x) {
                  NameAndId $$2 = new NameAndId($$1, $$0);
                  $$0.services().nameToIdCache().add($$2);
                  $$1.add(new UserWhiteListEntry($$2));
               }

               public void onProfileLookupFailed(String $$0x, Exception $$1x) {
                  OldUsersConverter.LOGGER.warn("Could not lookup user whitelist entry for {}", $$0, $$1);
                  if (!($$1 instanceof ProfileNotFoundException)) {
                     throw new OldUsersConverter.ConversionError("Could not request user " + $$0 + " from backend systems", $$1);
                  }
               }
            };
            lookupPlayers($$0, $$3, $$4);
            $$1.save();
            renameOldFile(OLD_WHITELIST);
            return true;
         } catch (IOException var4) {
            LOGGER.warn("Could not read old whitelist to convert it!", var4);
            return false;
         } catch (OldUsersConverter.ConversionError var5) {
            LOGGER.error("Conversion failed, please try again later", var5);
            return false;
         }
      } else {
         return true;
      }
   }

   
   public static UUID convertMobOwnerIfNecessary(final net.minecraft.server.MinecraftServer $$0, String $$1) {
      if (!StringUtil.isNullOrEmpty($$1) && $$1.length() <= 16) {
         Optional<UUID> $$3 = $$0.services().nameToIdCache().get($$1).map(NameAndId::id);
         if ($$3.isPresent()) {
            return $$3.get();
         } else if (!$$0.isSingleplayer() && $$0.usesAuthentication()) {
            final List<NameAndId> $$4 = new ArrayList<>();
            ProfileLookupCallback $$5 = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(String $$0x, UUID $$1x) {
                  NameAndId $$2 = new NameAndId($$1x, $$0);
                  $$0.services().nameToIdCache().add($$2);
                  $$4.add($$2);
               }

               public void onProfileLookupFailed(String $$0x, Exception $$1x) {
                  OldUsersConverter.LOGGER.warn("Could not lookup user whitelist entry for {}", $$0, $$1x);
               }
            };
            lookupPlayers($$0, Lists.newArrayList(new String[]{$$1}), $$5);
            return !$$4.isEmpty() ? $$4.getFirst().id() : null;
         } else {
            return UUIDUtil.createOfflinePlayerUUID($$1);
         }
      } else {
         try {
            return UUID.fromString($$1);
         } catch (IllegalArgumentException var5) {
            return null;
         }
      }
   }

   public static boolean convertPlayers(final DedicatedServer $$0) {
      final File $$1 = getWorldPlayersDirectory($$0);
      final File $$2 = new File($$1.getParentFile(), "playerdata");
      final File $$3 = new File($$1.getParentFile(), "unknownplayers");
      if ($$1.exists() && $$1.isDirectory()) {
         File[] $$4 = $$1.listFiles();
         List<String> $$5 = Lists.newArrayList();

         for (File $$6 : $$4) {
            String $$7 = $$6.getName();
            if ($$7.toLowerCase(Locale.ROOT).endsWith(".dat")) {
               String $$8 = $$7.substring(0, $$7.length() - ".dat".length());
               if (!$$8.isEmpty()) {
                  $$5.add($$8);
               }
            }
         }

         try {
            final String[] $$9 = $$5.toArray(new String[$$5.size()]);
            ProfileLookupCallback $$10 = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(String $$0x, UUID $$1x) {
                  NameAndId $$2x = new NameAndId($$1, $$0);
                  $$0.services().nameToIdCache().add($$2x);
                  this.movePlayerFile($$2, this.getFileNameForProfile($$0), $$1.toString());
               }

               public void onProfileLookupFailed(String $$0x, Exception $$1x) {
                  OldUsersConverter.LOGGER.warn("Could not lookup user uuid for {}", $$0, $$1);
                  if ($$1 instanceof ProfileNotFoundException) {
                     String $$2x = this.getFileNameForProfile($$0);
                     this.movePlayerFile($$3, $$2x, $$2x);
                  } else {
                     throw new OldUsersConverter.ConversionError("Could not request user " + $$0 + " from backend systems", $$1);
                  }
               }

               private void movePlayerFile(File $$0x, String $$1x, String $$2x) {
                  File $$3x = new File($$1, $$1 + ".dat");
                  File $$4x = new File($$0, $$2 + ".dat");
                  OldUsersConverter.ensureDirectoryExists($$0);
                  if (!$$3x.renameTo($$4x)) {
                     throw new OldUsersConverter.ConversionError("Could not convert file for " + $$1);
                  }
               }

               private String getFileNameForProfile(String $$0x) {
                  String $$1x = null;

                  for (String $$2x : $$9) {
                     if ($$2x != null && $$2x.equalsIgnoreCase($$0)) {
                        $$1x = $$2x;
                        break;
                     }
                  }

                  if ($$1x == null) {
                     throw new OldUsersConverter.ConversionError("Could not find the filename for " + $$0 + " anymore");
                  } else {
                     return $$1x;
                  }
               }
            };
            lookupPlayers($$0, Lists.newArrayList($$9), $$10);
            return true;
         } catch (OldUsersConverter.ConversionError var12) {
            LOGGER.error("Conversion failed, please try again later", var12);
            return false;
         }
      } else {
         return true;
      }
   }

   static void ensureDirectoryExists(File $$0) {
      if ($$0.exists()) {
         if (!$$0.isDirectory()) {
            throw new OldUsersConverter.ConversionError("Can't create directory " + $$0.getName() + " in world save directory.");
         }
      } else if (!$$0.mkdirs()) {
         throw new OldUsersConverter.ConversionError("Can't create directory " + $$0.getName() + " in world save directory.");
      }
   }

   public static boolean serverReadyAfterUserconversion(net.minecraft.server.MinecraftServer $$0) {
      boolean $$1 = areOldUserlistsRemoved();
      return $$1 && areOldPlayersConverted($$0);
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

      if (!$$0 && !$$1 && !$$2 && !$$3) {
         return true;
      } else {
         LOGGER.warn("**** FAILED TO START THE SERVER AFTER ACCOUNT CONVERSION!");
         LOGGER.warn("** please remove the following files and restart the server:");
         if ($$0) {
            LOGGER.warn("* {}", OLD_USERBANLIST.getName());
         }

         if ($$1) {
            LOGGER.warn("* {}", OLD_IPBANLIST.getName());
         }

         if ($$2) {
            LOGGER.warn("* {}", OLD_OPLIST.getName());
         }

         if ($$3) {
            LOGGER.warn("* {}", OLD_WHITELIST.getName());
         }

         return false;
      }
   }

   private static boolean areOldPlayersConverted(net.minecraft.server.MinecraftServer $$0) {
      File $$1 = getWorldPlayersDirectory($$0);
      if (!$$1.exists() || !$$1.isDirectory() || $$1.list().length <= 0 && $$1.delete()) {
         return true;
      } else {
         LOGGER.warn("**** DETECTED OLD PLAYER DIRECTORY IN THE WORLD SAVE");
         LOGGER.warn("**** THIS USUALLY HAPPENS WHEN THE AUTOMATIC CONVERSION FAILED IN SOME WAY");
         LOGGER.warn("** please restart the server and if the problem persists, remove the directory '{}'", $$1.getPath());
         return false;
      }
   }

   private static File getWorldPlayersDirectory(net.minecraft.server.MinecraftServer $$0) {
      return $$0.getWorldPath(LevelResource.PLAYER_OLD_DATA_DIR).toFile();
   }

   private static void renameOldFile(File $$0) {
      File $$1 = new File($$0.getName() + ".converted");
      $$0.renameTo($$1);
   }

   static Date parseDate(String $$0, Date $$1) {
      Date $$2;
      try {
         $$2 = BanListEntry.DATE_FORMAT.parse($$0);
      } catch (ParseException var4) {
         $$2 = $$1;
      }

      return $$2;
   }

   static class ConversionError extends RuntimeException {
      ConversionError(String $$0, Throwable $$1) {
         super($$0, $$1);
      }

      ConversionError(String $$0) {
         super($$0);
      }
   }
}
