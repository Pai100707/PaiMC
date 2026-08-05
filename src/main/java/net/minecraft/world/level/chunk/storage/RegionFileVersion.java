package net.minecraft.world.level.chunk.storage;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.minecraft.util.FastBufferedInputStream;
import org.slf4j.Logger;

public class RegionFileVersion {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Int2ObjectMap<RegionFileVersion> VERSIONS = new Int2ObjectOpenHashMap();
   private static final Object2ObjectMap<String, RegionFileVersion> VERSIONS_BY_NAME = new Object2ObjectOpenHashMap();
   public static final RegionFileVersion VERSION_GZIP = register(
      new RegionFileVersion(1, null, $$0 -> new FastBufferedInputStream(new GZIPInputStream($$0)), $$0 -> new BufferedOutputStream(new GZIPOutputStream($$0)))
   );
   public static final RegionFileVersion VERSION_DEFLATE = register(
      new RegionFileVersion(
         2, "deflate", $$0 -> new FastBufferedInputStream(new InflaterInputStream($$0)), $$0 -> new BufferedOutputStream(new DeflaterOutputStream($$0))
      )
   );
   public static final RegionFileVersion VERSION_NONE = register(new RegionFileVersion(3, "none", FastBufferedInputStream::new, BufferedOutputStream::new));
   public static final RegionFileVersion VERSION_LZ4 = register(
      new RegionFileVersion(
         4, "lz4", $$0 -> new FastBufferedInputStream(new LZ4BlockInputStream($$0)), $$0 -> new BufferedOutputStream(new LZ4BlockOutputStream($$0))
      )
   );
   public static final RegionFileVersion VERSION_CUSTOM = register(new RegionFileVersion(127, null, $$0 -> {
      throw new UnsupportedOperationException();
   }, $$0 -> {
      throw new UnsupportedOperationException();
   }));
   public static final RegionFileVersion DEFAULT = VERSION_DEFLATE;
   private static volatile RegionFileVersion selected = DEFAULT;
   private final int id;
   
   private final String optionName;
   private final RegionFileVersion.StreamWrapper<InputStream> inputWrapper;
   private final RegionFileVersion.StreamWrapper<OutputStream> outputWrapper;

   private RegionFileVersion(int $$0, String $$1, RegionFileVersion.StreamWrapper<InputStream> $$2, RegionFileVersion.StreamWrapper<OutputStream> $$3) {
      this.id = $$0;
      this.optionName = $$1;
      this.inputWrapper = $$2;
      this.outputWrapper = $$3;
   }

   private static RegionFileVersion register(RegionFileVersion $$0) {
      VERSIONS.put($$0.id, $$0);
      if ($$0.optionName != null) {
         VERSIONS_BY_NAME.put($$0.optionName, $$0);
      }

      return $$0;
   }

   
   public static RegionFileVersion fromId(int $$0) {
      return (RegionFileVersion)VERSIONS.get($$0);
   }

   public static void configure(String $$0) {
      RegionFileVersion $$1 = (RegionFileVersion)VERSIONS_BY_NAME.get($$0);
      if ($$1 != null) {
         selected = $$1;
      } else {
         LOGGER.error(
            "Invalid `region-file-compression` value `{}` in server.properties. Please use one of: {}", $$0, String.join(", ", VERSIONS_BY_NAME.keySet())
         );
      }
   }

   public static RegionFileVersion getSelected() {
      return selected;
   }

   public static boolean isValidVersion(int $$0) {
      return VERSIONS.containsKey($$0);
   }

   public int getId() {
      return this.id;
   }

   public OutputStream wrap(OutputStream $$0) throws IOException {
      return this.outputWrapper.wrap($$0);
   }

   public InputStream wrap(InputStream $$0) throws IOException {
      return this.inputWrapper.wrap($$0);
   }

   @FunctionalInterface
   interface StreamWrapper<O> {
      O wrap(O var1) throws IOException;
   }
}
