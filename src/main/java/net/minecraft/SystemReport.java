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
import org.slf4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PhysicalMemory;
import oshi.hardware.VirtualMemory;
import oshi.hardware.CentralProcessor.ProcessorIdentifier;

public class SystemReport {
   public static final long BYTES_PER_MEBIBYTE = 1048576L;
   private static final long ONE_GIGA = 1000000000L;
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String OPERATING_SYSTEM = System.getProperty("os.name")
      + " ("
      + System.getProperty("os.arch")
      + ") version "
      + System.getProperty("os.version");
   private static final String JAVA_VERSION = System.getProperty("java.version") + ", " + System.getProperty("java.vendor");
   private static final String JAVA_VM_VERSION = System.getProperty("java.vm.name")
      + " ("
      + System.getProperty("java.vm.info")
      + "), "
      + System.getProperty("java.vm.vendor");
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
         long $$4 = $$1 / 1048576L;
         long $$5 = $$2 / 1048576L;
         long $$6 = $$3 / 1048576L;
         return $$3 + " bytes (" + $$6 + " MiB) / " + $$2 + " bytes (" + $$5 + " MiB) up to " + $$1 + " bytes (" + $$4 + " MiB)";
      });
      this.setDetail("CPUs", () -> String.valueOf(Runtime.getRuntime().availableProcessors()));
      this.ignoreErrors("hardware", () -> this.putHardware(new SystemInfo()));
      this.setDetail("JVM Flags", () -> printJvmFlags($$0 -> $$0.startsWith("-X")));
      this.setDetail("Debug Flags", () -> printJvmFlags($$0 -> $$0.startsWith("-DMC_DEBUG_")));
   }

   private static String printJvmFlags(Predicate<String> $$0) {
      List<String> $$1 = ManagementFactory.getRuntimeMXBean().getInputArguments();
      List<String> $$2 = $$1.stream().filter($$0).toList();
      return String.format(Locale.ROOT, "%d total; %s", $$2.size(), String.join(" ", $$2));
   }

   public void setDetail(String $$0, String $$1) {
      this.entries.put($$0, $$1);
   }

   public void setDetail(String $$0, Supplier<String> $$1) {
      try {
         this.setDetail($$0, $$1.get());
      } catch (Exception var4) {
         LOGGER.warn("Failed to get system info for {}", $$0, var4);
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
      } catch (Throwable var4) {
         LOGGER.warn("Failed retrieving info for group {}", $$0, var4);
      }
   }

   public static float sizeInMiB(long $$0) {
      return (float)$$0 / 1048576.0F;
   }

   private void putPhysicalMemory(List<PhysicalMemory> $$0) {
      int $$1 = 0;

      for (PhysicalMemory $$2 : $$0) {
         String $$3 = String.format(Locale.ROOT, "Memory slot #%d ", $$1++);
         this.setDetail($$3 + "capacity (MiB)", () -> String.format(Locale.ROOT, "%.2f", sizeInMiB($$2.getCapacity())));
         this.setDetail($$3 + "clockSpeed (GHz)", () -> String.format(Locale.ROOT, "%.2f", (float)$$2.getClockSpeed() / 1.0E9F));
         this.setDetail($$3 + "type", $$2::getMemoryType);
      }
   }

   private void putVirtualMemory(VirtualMemory $$0) {
      this.setDetail("Virtual memory max (MiB)", () -> String.format(Locale.ROOT, "%.2f", sizeInMiB($$0.getVirtualMax())));
      this.setDetail("Virtual memory used (MiB)", () -> String.format(Locale.ROOT, "%.2f", sizeInMiB($$0.getVirtualInUse())));
      this.setDetail("Swap memory total (MiB)", () -> String.format(Locale.ROOT, "%.2f", sizeInMiB($$0.getSwapTotal())));
      this.setDetail("Swap memory used (MiB)", () -> String.format(Locale.ROOT, "%.2f", sizeInMiB($$0.getSwapUsed())));
   }

   private void putMemory(GlobalMemory $$0) {
      this.ignoreErrors("physical memory", () -> this.putPhysicalMemory($$0.getPhysicalMemory()));
      this.ignoreErrors("virtual memory", () -> this.putVirtualMemory($$0.getVirtualMemory()));
   }

   private void putGraphics(List<GraphicsCard> $$0) {
      int $$1 = 0;

      for (GraphicsCard $$2 : $$0) {
         String $$3 = String.format(Locale.ROOT, "Graphics card #%d ", $$1++);
         this.setDetail($$3 + "name", $$2::getName);
         this.setDetail($$3 + "vendor", $$2::getVendor);
         this.setDetail($$3 + "VRAM (MiB)", () -> String.format(Locale.ROOT, "%.2f", sizeInMiB($$2.getVRam())));
         this.setDetail($$3 + "deviceId", $$2::getDeviceId);
         this.setDetail($$3 + "versionInfo", $$2::getVersionInfo);
      }
   }

   private void putProcessor(CentralProcessor $$0) {
      ProcessorIdentifier $$1 = $$0.getProcessorIdentifier();
      this.setDetail("Processor Vendor", $$1::getVendor);
      this.setDetail("Processor Name", $$1::getName);
      this.setDetail("Identifier", $$1::getIdentifier);
      this.setDetail("Microarchitecture", $$1::getMicroarchitecture);
      this.setDetail("Frequency (GHz)", () -> String.format(Locale.ROOT, "%.2f", (float)$$1.getVendorFreq() / 1.0E9F));
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

   private void putSpaceForPath(String $$0, Supplier<String> $$1) {
      String $$2 = "Space in storage for " + $$0 + " (MiB)";

      try {
         String $$3 = $$1.get();
         if ($$3 == null) {
            this.setDetail($$2, "<path not set>");
            return;
         }

         FileStore $$4 = Files.getFileStore(Path.of($$3));
         this.setDetail($$2, String.format(Locale.ROOT, "available: %.2f, total: %.2f", sizeInMiB($$4.getUsableSpace()), sizeInMiB($$4.getTotalSpace())));
      } catch (InvalidPathException var6) {
         LOGGER.warn("{} is not a path", $$0, var6);
         this.setDetail($$2, "<invalid path>");
      } catch (Exception var7) {
         LOGGER.warn("Failed retrieving storage space for {}", $$0, var7);
         this.setDetail($$2, "ERR");
      }
   }

   public void appendToCrashReportString(StringBuilder $$0) {
      $$0.append("-- ").append("System Details").append(" --\n");
      $$0.append("Details:");
      this.entries.forEach(($$1, $$2) -> {
         $$0.append("\n\t");
         $$0.append($$1);
         $$0.append(": ");
         $$0.append($$2);
      });
   }

   public String toLineSeparatedString() {
      return this.entries.entrySet().stream().map($$0 -> $$0.getKey() + ": " + $$0.getValue()).collect(Collectors.joining(System.lineSeparator()));
   }
}
