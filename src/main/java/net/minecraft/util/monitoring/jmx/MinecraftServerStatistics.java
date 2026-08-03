package net.minecraft.util.monitoring.jmx;

import com.mojang.logging.LogUtils;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import net.minecraft.server.MinecraftServer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public final class MinecraftServerStatistics implements DynamicMBean {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final MinecraftServer server;
   private final MBeanInfo mBeanInfo;
   private final Map<String, MinecraftServerStatistics.AttributeDescription> attributeDescriptionByName = Stream.of(
         new MinecraftServerStatistics.AttributeDescription("tickTimes", this::getTickTimes, "Historical tick times (ms)", long[].class),
         new MinecraftServerStatistics.AttributeDescription("averageTickTime", this::getAverageTickTime, "Current average tick time (ms)", long.class)
      )
      .collect(Collectors.toMap($$0x -> $$0x.name, Function.identity()));

   private MinecraftServerStatistics(MinecraftServer $$0) {
      this.server = $$0;
      MBeanAttributeInfo[] $$1 = this.attributeDescriptionByName
         .values()
         .stream()
         .map(MinecraftServerStatistics.AttributeDescription::asMBeanAttributeInfo)
         .toArray(MBeanAttributeInfo[]::new);
      this.mBeanInfo = new MBeanInfo(
         MinecraftServerStatistics.class.getSimpleName(), "metrics for dedicated server", $$1, null, null, new MBeanNotificationInfo[0]
      );
   }

   public static void registerJmxMonitoring(MinecraftServer $$0) {
      try {
         ManagementFactory.getPlatformMBeanServer().registerMBean(new MinecraftServerStatistics($$0), new ObjectName("net.minecraft.server:type=Server"));
      } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException | MalformedObjectNameException var2) {
         LOGGER.warn("Failed to initialise server as JMX bean", var2);
      }
   }

   private float getAverageTickTime() {
      return this.server.getCurrentSmoothedTickTime();
   }

   private long[] getTickTimes() {
      return this.server.getTickTimesNanos();
   }

   @Nullable
   @Override
   public Object getAttribute(String $$0) {
      MinecraftServerStatistics.AttributeDescription $$1 = this.attributeDescriptionByName.get($$0);
      return $$1 == null ? null : $$1.getter.get();
   }

   @Override
   public void setAttribute(Attribute $$0) {
   }

   @Override
   public AttributeList getAttributes(String[] $$0) {
      List<Attribute> $$1 = Arrays.stream($$0)
         .map(this.attributeDescriptionByName::get)
         .filter(Objects::nonNull)
         .map($$0x -> new Attribute($$0x.name, $$0x.getter.get()))
         .collect(Collectors.toList());
      return new AttributeList($$1);
   }

   @Override
   public AttributeList setAttributes(AttributeList $$0) {
      return new AttributeList();
   }

   @Nullable
   @Override
   public Object invoke(String $$0, Object[] $$1, String[] $$2) {
      return null;
   }

   @Override
   public MBeanInfo getMBeanInfo() {
      return this.mBeanInfo;
   }

   static final class AttributeDescription {
      final String name;
      final Supplier<Object> getter;
      private final String description;
      private final Class<?> type;

      AttributeDescription(String $$0, Supplier<Object> $$1, String $$2, Class<?> $$3) {
         this.name = $$0;
         this.getter = $$1;
         this.description = $$2;
         this.type = $$3;
      }

      private MBeanAttributeInfo asMBeanAttributeInfo() {
         return new MBeanAttributeInfo(this.name, this.type.getSimpleName(), this.description, true, false, false);
      }
   }
}
