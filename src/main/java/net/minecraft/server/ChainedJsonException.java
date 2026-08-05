package net.minecraft.server;

import com.google.common.collect.Lists;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class ChainedJsonException extends IOException {
   private final List<net.minecraft.server.ChainedJsonException.Entry> entries = Lists.newArrayList();
   private final String message;

   public ChainedJsonException(String $$0) {
      this.entries.add(new net.minecraft.server.ChainedJsonException.Entry());
      this.message = $$0;
   }

   public ChainedJsonException(String $$0, Throwable $$1) {
      super($$1);
      this.entries.add(new net.minecraft.server.ChainedJsonException.Entry());
      this.message = $$0;
   }

   public void prependJsonKey(String $$0) {
      this.entries.get(0).addJsonKey($$0);
   }

   public void setFilenameAndFlush(String $$0) {
      this.entries.get(0).filename = $$0;
      this.entries.add(0, new net.minecraft.server.ChainedJsonException.Entry());
   }

   @Override
   public String getMessage() {
      return "Invalid " + this.entries.get(this.entries.size() - 1) + ": " + this.message;
   }

   public static net.minecraft.server.ChainedJsonException forException(Exception $$0) {
      if ($$0 instanceof net.minecraft.server.ChainedJsonException) {
         return (net.minecraft.server.ChainedJsonException)$$0;
      } else {
         String $$1 = $$0.getMessage();
         if ($$0 instanceof FileNotFoundException) {
            $$1 = "File not found";
         }

         return new net.minecraft.server.ChainedJsonException($$1, $$0);
      }
   }

   public static class Entry {
      
      String filename;
      private final List<String> jsonKeys = Lists.newArrayList();

      Entry() {
      }

      void addJsonKey(String $$0) {
         this.jsonKeys.add(0, $$0);
      }

      
      public String getFilename() {
         return this.filename;
      }

      public String getJsonKeys() {
         return StringUtils.join(this.jsonKeys, "->");
      }

      @Override
      public String toString() {
         if (this.filename != null) {
            return this.jsonKeys.isEmpty() ? this.filename : this.filename + " " + this.getJsonKeys();
         } else {
            return this.jsonKeys.isEmpty() ? "(Unknown file)" : "(Unknown file) " + this.getJsonKeys();
         }
      }
   }
}
