package net.minecraft.locale;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringDecomposer;
import org.slf4j.Logger;

public abstract class Language {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Gson GSON = new Gson();
   private static final Pattern UNSUPPORTED_FORMAT_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");
   public static final String DEFAULT = "en_us";
   private static volatile net.minecraft.locale.Language instance = loadDefault();

   private static net.minecraft.locale.Language loadDefault() {
      net.minecraft.locale.DeprecatedTranslationsInfo $$0 = net.minecraft.locale.DeprecatedTranslationsInfo.loadFromDefaultResource();
      Map<String, String> $$1 = new HashMap<>();
      BiConsumer<String, String> $$2 = $$1::put;
      parseTranslations($$2, "/assets/minecraft/lang/en_us.json");
      $$0.applyToMap($$1);
      final Map<String, String> $$3 = Map.copyOf($$1);
      return new net.minecraft.locale.Language() {
         @Override
         public String getOrDefault(String $$0, String $$1x) {
            return $$3.getOrDefault($$0, $$1x);
         }

         @Override
         public boolean has(String $$0) {
            return $$3.containsKey($$0);
         }

         @Override
         public boolean isDefaultRightToLeft() {
            return false;
         }

         @Override
         public FormattedCharSequence getVisualOrder(FormattedText $$0) {
            return $$1x -> $$0.visit(
                  ($$1xx, $$2x) -> StringDecomposer.iterateFormatted($$2x, $$1xx, $$1) ? Optional.empty() : FormattedText.STOP_ITERATION, Style.EMPTY
               )
               .isPresent();
         }
      };
   }

   private static void parseTranslations(BiConsumer<String, String> $$0, String $$1) {
      try (InputStream $$2 = net.minecraft.locale.Language.class.getResourceAsStream($$1)) {
         loadFromJson($$2, $$0);
      } catch (JsonParseException | IOException var7) {
         LOGGER.error("Couldn't read strings from {}", $$1, var7);
      }
   }

   public static void loadFromJson(InputStream $$0, BiConsumer<String, String> $$1) {
      JsonObject $$2 = (JsonObject)GSON.fromJson(new InputStreamReader($$0, StandardCharsets.UTF_8), JsonObject.class);

      for (Entry<String, JsonElement> $$3 : $$2.entrySet()) {
         String $$4 = UNSUPPORTED_FORMAT_PATTERN.matcher(GsonHelper.convertToString($$3.getValue(), $$3.getKey())).replaceAll("%$1s");
         $$1.accept($$3.getKey(), $$4);
      }
   }

   public static net.minecraft.locale.Language getInstance() {
      return instance;
   }

   public static void inject(net.minecraft.locale.Language $$0) {
      instance = $$0;
   }

   public String getOrDefault(String $$0) {
      return this.getOrDefault($$0, $$0);
   }

   public abstract String getOrDefault(String var1, String var2);

   public abstract boolean has(String var1);

   public abstract boolean isDefaultRightToLeft();

   public abstract FormattedCharSequence getVisualOrder(FormattedText var1);

   public List<FormattedCharSequence> getVisualOrder(List<FormattedText> $$0) {
      return $$0.stream().map(this::getVisualOrder).collect(ImmutableList.toImmutableList());
   }
}
