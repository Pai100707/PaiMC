package net.minecraft.util;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringEscapeUtils;

public class CsvOutput {
   private static final String LINE_SEPARATOR = "\r\n";
   private static final String FIELD_SEPARATOR = ",";
   private final Writer output;
   private final int columnCount;

   CsvOutput(Writer $$0, List<String> $$1) throws IOException {
      this.output = $$0;
      this.columnCount = $$1.size();
      this.writeLine($$1.stream());
   }

   public static net.minecraft.util.CsvOutput.Builder builder() {
      return new net.minecraft.util.CsvOutput.Builder();
   }

   public void writeRow(Object... $$0) throws IOException {
      if ($$0.length != this.columnCount) {
         throw new IllegalArgumentException("Invalid number of columns, expected " + this.columnCount + ", but got " + $$0.length);
      } else {
         this.writeLine(Stream.of($$0));
      }
   }

   private void writeLine(Stream<? extends Object> $$0) throws IOException {
      this.output.write($$0.map(net.minecraft.util.CsvOutput::getStringValue).collect(Collectors.joining(",")) + "\r\n");
   }

   private static String getStringValue(Object $$0) {
      return StringEscapeUtils.escapeCsv($$0 != null ? $$0.toString() : "[null]");
   }

   public static class Builder {
      private final List<String> headers = Lists.newArrayList();

      public net.minecraft.util.CsvOutput.Builder addColumn(String $$0) {
         this.headers.add($$0);
         return this;
      }

      public net.minecraft.util.CsvOutput build(Writer $$0) throws IOException {
         return new net.minecraft.util.CsvOutput($$0, this.headers);
      }
   }
}
