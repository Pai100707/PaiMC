/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Properties;
import net.minecraft.SharedConstants;
import net.minecraft.util.CommonLinks;
import org.slf4j.Logger;

public class Eula {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Path file;
    private final boolean agreed;

    public Eula(Path $$0) {
        this.file = $$0;
        this.agreed = SharedConstants.IS_RUNNING_IN_IDE || this.readFile();
    }

    private boolean readFile() {
        boolean bl;
        block8: {
            InputStream $$0 = Files.newInputStream(this.file, new OpenOption[0]);
            try {
                Properties $$1 = new Properties();
                $$1.load($$0);
                bl = Boolean.parseBoolean($$1.getProperty("eula", "false"));
                if ($$0 == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if ($$0 != null) {
                        try {
                            $$0.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Exception $$2) {
                    LOGGER.warn("Failed to load {}", (Object)this.file);
                    this.saveDefaults();
                    return false;
                }
            }
            $$0.close();
        }
        return bl;
    }

    public boolean hasAgreedToEULA() {
        return this.agreed;
    }

    private void saveDefaults() {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            return;
        }
        try (OutputStream $$0 = Files.newOutputStream(this.file, new OpenOption[0]);){
            Properties $$1 = new Properties();
            $$1.setProperty("eula", "false");
            $$1.store($$0, "By changing the setting below to TRUE you are indicating your agreement to our EULA (" + String.valueOf(CommonLinks.EULA) + ").");
        }
        catch (Exception $$2) {
            LOGGER.warn("Failed to save {}", (Object)this.file, (Object)$$2);
        }
    }
}

