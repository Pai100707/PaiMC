/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 *  oshi.SystemInfo
 *  oshi.hardware.CentralProcessor
 *  oshi.hardware.CentralProcessor$ProcessorIdentifier
 *  oshi.hardware.GlobalMemory
 *  oshi.hardware.GraphicsCard
 *  oshi.hardware.HardwareAbstractionLayer
 *  oshi.hardware.PhysicalMemory
 *  oshi.hardware.VirtualMemory
 */
package net.minecraft;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.lang.management.ManagementFactory;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PhysicalMemory;
import oshi.hardware.VirtualMemory;

public class SystemReport {
    public static final long BYTES_PER_MEBIBYTE = 0x100000L;
    private static final long ONE_GIGA = 1000000000L;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String OPERATING_SYSTEM = System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version");
    private static final String JAVA_VERSION = System.getProperty("java.version") + ", " + System.getProperty("java.vendor");
    private static final String JAVA_VM_VERSION = System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor");
    private final Map<String, String> entries = Maps.newLinkedHashMap();

    public SystemReport() {
        this.setDetail("Minecraft Version", SharedConstants.getCurrentVersion().name());
        this.setDetail("Minecraft Version ID", SharedConstants.getCurrentVersion().id());
        this.setDetail("Operating System", OPERATING_SYSTEM);
        this.setDetail("Java Version", JAVA_VERSION);
        this.setDetail("Java VM Version", JAVA_VM_VERSION);
        this.setDetail("Memory", () -> {
            Runtime $$0 = Runtime.getRuntime();
            long $$1 = $$0.maxMemory();
            long $$2 = $$0.totalMemory();
            long $$3 = $$0.freeMemory();
            long $$4 = $$1 / 0x100000L;
            long $$5 = $$2 / 0x100000L;
            long $$6 = $$3 / 0x100000L;
            return $$3 + " bytes (" + $$6 + " MiB) / " + $$2 + " bytes (" + $$5 + " MiB) up to " + $$1 + " bytes (" + $$4 + " MiB)";
        });
        this.setDetail("CPUs", () -> String.valueOf(Runtime.getRuntime().availableProcessors()));
        this.ignoreErrors("hardware", () -> this.putHardware(new SystemInfo()));
        this.setDetail("JVM Flags", () -> SystemReport.printJvmFlags($$0 -> $$0.startsWith("-X")));
        this.setDetail("Debug Flags", () -> SystemReport.printJvmFlags($$0 -> $$0.startsWith("-DMC_DEBUG_")));
    }

    private static String printJvmFlags(Predicate<String> $$0) {
        List<String> $$1 = ManagementFactory.getRuntimeMXBean().getInputArguments();
        List<String> $$2 = $$1.stream().filter($$0).toList();
        return String.format(Locale.ROOT, "%d total; %s", $$2.size(), String.join((CharSequence)" ", $$2));
    }

    public void setDetail(String $$0, String $$1) {
        this.entries.put($$0, $$1);
    }

    public void setDetail(String $$0, Supplier<String> $$1) {
        try {
            this.setDetail($$0, $$1.get());
        }
        catch (Exception $$2) {
            LOGGER.warn("Failed to get system info for {}", (Object)$$0, (Object)$$2);
            this.setDetail($$0, "ERR");
        }
    }

    private void putHardware(SystemInfo $$0) {
        HardwareAbstractionLayer $$1 = $$0.getHardware();
        this.ignoreErrors("processor", () -> this.putProcessor($$1.getProcessor()));
        this.ignoreErrors("graphics", () -> this.putGraphics($$1.getGraphicsCards()));
        this.ignoreErrors("memory", () -> this.putMemory($$1.getMemory()));
        this.ignoreErrors("storage", this::putStorage);
    }

    private void ignoreErrors(String $$0, Runnable $$1) {
        try {
            $$1.run();
        }
        catch (Throwable $$2) {
            LOGGER.warn("Failed retrieving info for group {}", (Object)$$0, (Object)$$2);
        }
    }

    public static float sizeInMiB(long $$0) {
        return (float)$$0 / 1048576.0f;
    }

    private void putPhysicalMemory(List<PhysicalMemory> $$0) {
        int $$1 = 0;
        for (PhysicalMemory $$2 : $$0) {
            String $$3 = String.format(Locale.ROOT, "Memory slot #%d ", $$1++);
            this.setDetail($$3 + "capacity (MiB)", () -> String.format(Locale.ROOT, "%.2f", Float.valueOf(SystemReport.sizeInMiB($$2.getCapacity()))));
            this.setDetail($$3 + "clockSpeed (GHz)", () -> String.format(Locale.ROOT, "%.2f", Float.valueOf((float)$$2.getClockSpeed() / 1.0E9f)));
            this.setDetail($$3 + "type", () -> ((PhysicalMemory)$$2).getMemoryType());
        }
    }

    private void putVirtualMemory(VirtualMemory $$0) {
        this.setDetail("Virtual memory max (MiB)", () -> String.format(Locale.ROOT, "%.2f", Float.valueOf(SystemReport.sizeInMiB($$0.getVirtualMax()))));
        this.setDetail("Virtual memory used (MiB)", () -> String.format(Locale.ROOT, "%.2f", Float.valueOf(SystemReport.sizeInMiB($$0.getVirtualInUse()))));
        this.setDetail("Swap memory total (MiB)", () -> String.format(Locale.ROOT, "%.2f", Float.valueOf(SystemReport.sizeInMiB($$0.getSwapTotal()))));
        this.setDetail("Swap memory used (MiB)", () -> String.format(Locale.ROOT, "%.2f", Float.valueOf(SystemReport.sizeInMiB($$0.getSwapUsed()))));
    }

    private void putMemory(GlobalMemory $$0) {
        this.ignoreErrors("physical memory", () -> this.putPhysicalMemory($$0.getPhysicalMemory()));
        this.ignoreErrors("virtual memory", () -> this.putVirtualMemory($$0.getVirtualMemory()));
    }

    private void putGraphics(List<GraphicsCard> $$0) {
        int $$1 = 0;
        for (GraphicsCard $$2 : $$0) {
            String $$3 = String.format(Locale.ROOT, "Graphics card #%d ", $$1++);
            this.setDetail($$3 + "name", () -> ((GraphicsCard)$$2).getName());
            this.setDetail($$3 + "vendor", () -> ((GraphicsCard)$$2).getVendor());
            this.setDetail($$3 + "VRAM (MiB)", () -> String.format(Locale.ROOT, "%.2f", Float.valueOf(SystemReport.sizeInMiB($$2.getVRam()))));
            this.setDetail($$3 + "deviceId", () -> ((GraphicsCard)$$2).getDeviceId());
            this.setDetail($$3 + "versionInfo", () -> ((GraphicsCard)$$2).getVersionInfo());
        }
    }

    private void putProcessor(CentralProcessor $$0) {
        CentralProcessor.ProcessorIdentifier $$1 = $$0.getProcessorIdentifier();
        this.setDetail("Processor Vendor", () -> ((CentralProcessor.ProcessorIdentifier)$$1).getVendor());
        this.setDetail("Processor Name", () -> ((CentralProcessor.ProcessorIdentifier)$$1).getName());
        this.setDetail("Identifier", () -> ((CentralProcessor.ProcessorIdentifier)$$1).getIdentifier());
        this.setDetail("Microarchitecture", () -> ((CentralProcessor.ProcessorIdentifier)$$1).getMicroarchitecture());
        this.setDetail("Frequency (GHz)", () -> String.format(Locale.ROOT, "%.2f", Float.valueOf((float)$$1.getVendorFreq() / 1.0E9f)));
        this.setDetail("Number of physical packages", () -> String.valueOf($$0.getPhysicalPackageCount()));
        this.setDetail("Number of physical CPUs", () -> String.valueOf($$0.getPhysicalProcessorCount()));
        this.setDetail("Number of logical CPUs", () -> String.valueOf($$0.getLogicalProcessorCount()));
    }

    private void putStorage() {
        this.putSpaceForProperty("jna.tmpdir");
        this.putSpaceForProperty("org.lwjgl.system.SharedLibraryExtractPath");
        this.putSpaceForProperty("io.netty.native.workdir");
        this.putSpaceForProperty("java.io.tmpdir");
        this.putSpaceForPath("workdir", () -> "");
    }

    private void putSpaceForProperty(String $$0) {
        this.putSpaceForPath($$0, () -> System.getProperty($$0));
    }

    private void putSpaceForPath(String $$0, Supplier<@Nullable String> $$1) {
        String $$2 = "Space in storage for " + $$0 + " (MiB)";
        try {
            String $$3 = $$1.get();
            if ($$3 == null) {
                this.setDetail($$2, "<path not set>");
                return;
            }
            FileStore $$4 = Files.getFileStore(Path.of($$3, new String[0]));
            this.setDetail($$2, String.format(Locale.ROOT, "available: %.2f, total: %.2f", Float.valueOf(SystemReport.sizeInMiB($$4.getUsableSpace())), Float.valueOf(SystemReport.sizeInMiB($$4.getTotalSpace()))));
        }
        catch (InvalidPathException $$5) {
            LOGGER.warn("{} is not a path", (Object)$$0, (Object)$$5);
            this.setDetail($$2, "<invalid path>");
        }
        catch (Exception $$6) {
            LOGGER.warn("Failed retrieving storage space for {}", (Object)$$0, (Object)$$6);
            this.setDetail($$2, "ERR");
        }
    }

    public void appendToCrashReportString(StringBuilder $$0) {
        $$0.append("-- ").append("System Details").append(" --\n");
        $$0.append("Details:");
        this.entries.forEach(($$1, $$2) -> {
            $$0.append("\n\t");
            $$0.append((String)$$1);
            $$0.append(": ");
            $$0.append((String)$$2);
        });
    }

    public String toLineSeparatedString() {
        return this.entries.entrySet().stream().map($$0 -> (String)$$0.getKey() + ": " + (String)$$0.getValue()).collect(Collectors.joining(System.lineSeparator()));
    }
}

