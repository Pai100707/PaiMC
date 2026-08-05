/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import net.minecraft.util.FileUtil;

public class DirectoryLock
implements AutoCloseable {
    public static final String LOCK_FILE = "session.lock";
    private final FileChannel lockFile;
    private final FileLock lock;
    private static final ByteBuffer DUMMY;

    public static DirectoryLock create(Path $$0) throws IOException {
        Path $$1 = $$0.resolve(LOCK_FILE);
        FileUtil.createDirectoriesSafe($$0);
        FileChannel $$2 = FileChannel.open($$1, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        try {
            $$2.write(DUMMY.duplicate());
            $$2.force(true);
            FileLock $$3 = $$2.tryLock();
            if ($$3 == null) {
                throw LockException.alreadyLocked($$1);
            }
            return new DirectoryLock($$2, $$3);
        }
        catch (IOException $$4) {
            try {
                $$2.close();
            }
            catch (IOException $$5) {
                $$4.addSuppressed($$5);
            }
            throw $$4;
        }
    }

    private DirectoryLock(FileChannel $$0, FileLock $$1) {
        this.lockFile = $$0;
        this.lock = $$1;
    }

    @Override
    public void close() throws IOException {
        try {
            if (this.lock.isValid()) {
                this.lock.release();
            }
        }
        finally {
            if (this.lockFile.isOpen()) {
                this.lockFile.close();
            }
        }
    }

    public boolean isValid() {
        return this.lock.isValid();
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static boolean isLocked(Path $$0) throws IOException {
        Path $$1 = $$0.resolve(LOCK_FILE);
        try (FileChannel $$2 = FileChannel.open($$1, StandardOpenOption.WRITE);){
            boolean bl;
            block15: {
                FileLock $$3 = $$2.tryLock();
                try {
                    boolean bl2 = bl = $$3 == null;
                    if ($$3 == null) break block15;
                }
                catch (Throwable throwable) {
                    if ($$3 != null) {
                        try {
                            $$3.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                $$3.close();
            }
            return bl;
        }
        catch (AccessDeniedException $$4) {
            return true;
        }
        catch (NoSuchFileException $$5) {
            return false;
        }
    }

    static {
        byte[] $$0 = "\u2603".getBytes(StandardCharsets.UTF_8);
        DUMMY = ByteBuffer.allocateDirect($$0.length);
        DUMMY.put($$0);
        DUMMY.flip();
    }

    public static class LockException
    extends IOException {
        private LockException(Path $$0, String $$1) {
            super(String.valueOf($$0.toAbsolutePath()) + ": " + $$1);
        }

        public static LockException alreadyLocked(Path $$0) {
            return new LockException($$0, "already locked (possibly by other Minecraft instance?)");
        }
    }
}

