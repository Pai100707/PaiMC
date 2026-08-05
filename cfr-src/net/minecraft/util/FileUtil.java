/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DataResult
 *  org.apache.commons.io.FilenameUtils
 */
package net.minecraft.util;

import com.mojang.serialization.DataResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.SharedConstants;
import org.apache.commons.io.FilenameUtils;

public class FileUtil {
    private static final Pattern COPY_COUNTER_PATTERN = Pattern.compile("(<name>.*) \\((<count>\\d*)\\)", 66);
    private static final int MAX_FILE_NAME = 255;
    private static final Pattern RESERVED_WINDOWS_FILENAMES = Pattern.compile(".*\\.|(?:COM|CLOCK\\$|CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?", 2);
    private static final Pattern STRICT_PATH_SEGMENT_CHECK = Pattern.compile("[-._a-z0-9]+");

    public static String sanitizeName(String $$0) {
        for (char $$1 : SharedConstants.ILLEGAL_FILE_CHARACTERS) {
            $$0 = $$0.replace($$1, '_');
        }
        return $$0.replaceAll("[./\"]", "_");
    }

    public static String findAvailableName(Path $$0, String $$1, String $$2) throws IOException {
        if (!FileUtil.isPathPartPortable((String)($$1 = FileUtil.sanitizeName((String)$$1)))) {
            $$1 = "_" + (String)$$1 + "_";
        }
        Matcher $$3 = COPY_COUNTER_PATTERN.matcher((CharSequence)$$1);
        int $$4 = 0;
        if ($$3.matches()) {
            $$1 = $$3.group("name");
            $$4 = Integer.parseInt($$3.group("count"));
        }
        if (((String)$$1).length() > 255 - $$2.length()) {
            $$1 = ((String)$$1).substring(0, 255 - $$2.length());
        }
        while (true) {
            Object $$5 = $$1;
            if ($$4 != 0) {
                String $$6 = " (" + $$4 + ")";
                int $$7 = 255 - $$6.length();
                if (((String)$$5).length() > $$7) {
                    $$5 = ((String)$$5).substring(0, $$7);
                }
                $$5 = (String)$$5 + $$6;
            }
            $$5 = (String)$$5 + $$2;
            Path $$8 = $$0.resolve((String)$$5);
            try {
                Path $$9 = Files.createDirectory($$8, new FileAttribute[0]);
                Files.deleteIfExists($$9);
                return $$0.relativize($$9).toString();
            }
            catch (FileAlreadyExistsException $$10) {
                ++$$4;
                continue;
            }
            break;
        }
    }

    public static boolean isPathNormalized(Path $$0) {
        Path $$1 = $$0.normalize();
        return $$1.equals($$0);
    }

    public static boolean isPathPortable(Path $$0) {
        for (Path $$1 : $$0) {
            if (FileUtil.isPathPartPortable($$1.toString())) continue;
            return false;
        }
        return true;
    }

    public static boolean isPathPartPortable(String $$0) {
        return !RESERVED_WINDOWS_FILENAMES.matcher($$0).matches();
    }

    public static Path createPathToResource(Path $$0, String $$1, String $$2) {
        String $$3 = $$1 + $$2;
        Path $$4 = Paths.get($$3, new String[0]);
        if ($$4.endsWith($$2)) {
            throw new InvalidPathException($$3, "empty resource name");
        }
        return $$0.resolve($$4);
    }

    public static String getFullResourcePath(String $$0) {
        return FilenameUtils.getFullPath((String)$$0).replace(File.separator, "/");
    }

    public static String normalizeResourcePath(String $$0) {
        return FilenameUtils.normalize((String)$$0).replace(File.separator, "/");
    }

    public static DataResult<List<String>> decomposePath(String $$0) {
        int $$1 = $$0.indexOf(47);
        if ($$1 == -1) {
            return switch ($$0) {
                case "", ".", ".." -> DataResult.error(() -> "Invalid path '" + $$0 + "'");
                default -> !FileUtil.containsAllowedCharactersOnly($$0) ? DataResult.error(() -> "Invalid path '" + $$0 + "'") : DataResult.success(List.of($$0));
            };
        }
        ArrayList<String> $$2 = new ArrayList<String>();
        int $$3 = 0;
        boolean $$4 = false;
        while (true) {
            String $$5;
            switch ($$5 = $$0.substring($$3, $$1)) {
                case "": 
                case ".": 
                case "..": {
                    return DataResult.error(() -> "Invalid segment '" + $$5 + "' in path '" + $$0 + "'");
                }
            }
            if (!FileUtil.containsAllowedCharactersOnly($$5)) {
                return DataResult.error(() -> "Invalid segment '" + $$5 + "' in path '" + $$0 + "'");
            }
            $$2.add($$5);
            if ($$4) {
                return DataResult.success($$2);
            }
            $$3 = $$1 + 1;
            if (($$1 = $$0.indexOf(47, $$3)) != -1) continue;
            $$1 = $$0.length();
            $$4 = true;
        }
    }

    public static Path resolvePath(Path $$0, List<String> $$1) {
        int $$2 = $$1.size();
        return switch ($$2) {
            case 0 -> $$0;
            case 1 -> $$0.resolve($$1.get(0));
            default -> {
                String[] $$3 = new String[$$2 - 1];
                for (int $$4 = 1; $$4 < $$2; ++$$4) {
                    $$3[$$4 - 1] = $$1.get($$4);
                }
                yield $$0.resolve($$0.getFileSystem().getPath($$1.get(0), $$3));
            }
        };
    }

    private static boolean containsAllowedCharactersOnly(String $$0) {
        return STRICT_PATH_SEGMENT_CHECK.matcher($$0).matches();
    }

    public static boolean isValidPathSegment(String $$0) {
        return !$$0.equals("..") && !$$0.equals(".") && FileUtil.containsAllowedCharactersOnly($$0);
    }

    public static void validatePath(String ... $$0) {
        if ($$0.length == 0) {
            throw new IllegalArgumentException("Path must have at least one element");
        }
        for (String $$1 : $$0) {
            if (FileUtil.isValidPathSegment($$1)) continue;
            throw new IllegalArgumentException("Illegal segment " + $$1 + " in path " + Arrays.toString($$0));
        }
    }

    public static void createDirectoriesSafe(Path $$0) throws IOException {
        Files.createDirectories(Files.exists($$0, new LinkOption[0]) ? $$0.toRealPath(new LinkOption[0]) : $$0, new FileAttribute[0]);
    }
}

