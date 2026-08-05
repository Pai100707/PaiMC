/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.permissions;

import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

public class Permissions {
    public static final Permission COMMANDS_MODERATOR = new Permission.HasCommandLevel(PermissionLevel.MODERATORS);
    public static final Permission COMMANDS_GAMEMASTER = new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS);
    public static final Permission COMMANDS_ADMIN = new Permission.HasCommandLevel(PermissionLevel.ADMINS);
    public static final Permission COMMANDS_OWNER = new Permission.HasCommandLevel(PermissionLevel.OWNERS);
    public static final Permission COMMANDS_ENTITY_SELECTORS = Permission.Atom.create("commands/entity_selectors");
}

