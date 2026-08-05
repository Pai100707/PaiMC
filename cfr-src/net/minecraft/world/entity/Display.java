/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity;

import com.mojang.logging.LogUtils;
import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.util.ARGB;
import net.minecraft.util.Brightness;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class Display
extends Entity {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final int NO_BRIGHTNESS_OVERRIDE = -1;
    private static final EntityDataAccessor<Integer> DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_POS_ROT_INTERPOLATION_DURATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Vector3fc> DATA_TRANSLATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Vector3fc> DATA_SCALE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Quaternionfc> DATA_LEFT_ROTATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.QUATERNION);
    private static final EntityDataAccessor<Quaternionfc> DATA_RIGHT_ROTATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.QUATERNION);
    private static final EntityDataAccessor<Byte> DATA_BILLBOARD_RENDER_CONSTRAINTS_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_BRIGHTNESS_OVERRIDE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_VIEW_RANGE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_SHADOW_RADIUS_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_SHADOW_STRENGTH_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_WIDTH_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_HEIGHT_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_GLOW_COLOR_OVERRIDE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
    private static final IntSet RENDER_STATE_IDS = IntSet.of((int[])new int[]{DATA_TRANSLATION_ID.id(), DATA_SCALE_ID.id(), DATA_LEFT_ROTATION_ID.id(), DATA_RIGHT_ROTATION_ID.id(), DATA_BILLBOARD_RENDER_CONSTRAINTS_ID.id(), DATA_BRIGHTNESS_OVERRIDE_ID.id(), DATA_SHADOW_RADIUS_ID.id(), DATA_SHADOW_STRENGTH_ID.id()});
    private static final int INITIAL_TRANSFORMATION_INTERPOLATION_DURATION = 0;
    private static final int INITIAL_TRANSFORMATION_START_INTERPOLATION = 0;
    private static final int INITIAL_POS_ROT_INTERPOLATION_DURATION = 0;
    private static final float INITIAL_SHADOW_RADIUS = 0.0f;
    private static final float INITIAL_SHADOW_STRENGTH = 1.0f;
    private static final float INITIAL_VIEW_RANGE = 1.0f;
    private static final float INITIAL_WIDTH = 0.0f;
    private static final float INITIAL_HEIGHT = 0.0f;
    private static final int NO_GLOW_COLOR_OVERRIDE = -1;
    public static final String TAG_POS_ROT_INTERPOLATION_DURATION = "teleport_duration";
    public static final String TAG_TRANSFORMATION_INTERPOLATION_DURATION = "interpolation_duration";
    public static final String TAG_TRANSFORMATION_START_INTERPOLATION = "start_interpolation";
    public static final String TAG_TRANSFORMATION = "transformation";
    public static final String TAG_BILLBOARD = "billboard";
    public static final String TAG_BRIGHTNESS = "brightness";
    public static final String TAG_VIEW_RANGE = "view_range";
    public static final String TAG_SHADOW_RADIUS = "shadow_radius";
    public static final String TAG_SHADOW_STRENGTH = "shadow_strength";
    public static final String TAG_WIDTH = "width";
    public static final String TAG_HEIGHT = "height";
    public static final String TAG_GLOW_COLOR_OVERRIDE = "glow_color_override";
    private long interpolationStartClientTick = Integer.MIN_VALUE;
    private int interpolationDuration;
    private float lastProgress;
    private AABB cullingBoundingBox;
    private boolean noCulling = true;
    protected boolean updateRenderState;
    private boolean updateStartTick;
    private boolean updateInterpolationDuration;
    private @Nullable RenderState renderState;
    private final InterpolationHandler interpolation = new InterpolationHandler((Entity)this, 0);

    public Display(EntityType<?> $$0, Level $$1) {
        super($$0, $$1);
        this.noPhysics = true;
        this.cullingBoundingBox = this.getBoundingBox();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
        super.onSyncedDataUpdated($$0);
        if (DATA_HEIGHT_ID.equals($$0) || DATA_WIDTH_ID.equals($$0)) {
            this.updateCulling();
        }
        if (DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID.equals($$0)) {
            this.updateStartTick = true;
        }
        if (DATA_POS_ROT_INTERPOLATION_DURATION_ID.equals($$0)) {
            this.interpolation.setInterpolationLength(this.getPosRotInterpolationDuration());
        }
        if (DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID.equals($$0)) {
            this.updateInterpolationDuration = true;
        }
        if (RENDER_STATE_IDS.contains($$0.id())) {
            this.updateRenderState = true;
        }
    }

    @Override
    public final boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        return false;
    }

    private static Transformation createTransformation(SynchedEntityData $$0) {
        Vector3fc $$1 = $$0.get(DATA_TRANSLATION_ID);
        Quaternionfc $$2 = $$0.get(DATA_LEFT_ROTATION_ID);
        Vector3fc $$3 = $$0.get(DATA_SCALE_ID);
        Quaternionfc $$4 = $$0.get(DATA_RIGHT_ROTATION_ID);
        return new Transformation($$1, $$2, $$3, $$4);
    }

    @Override
    public void tick() {
        Entity $$0 = this.getVehicle();
        if ($$0 != null && $$0.isRemoved()) {
            this.stopRiding();
        }
        if (this.level().isClientSide()) {
            if (this.updateStartTick) {
                this.updateStartTick = false;
                int $$1 = this.getTransformationInterpolationDelay();
                this.interpolationStartClientTick = this.tickCount + $$1;
            }
            if (this.updateInterpolationDuration) {
                this.updateInterpolationDuration = false;
                this.interpolationDuration = this.getTransformationInterpolationDuration();
            }
            if (this.updateRenderState) {
                this.updateRenderState = false;
                boolean $$2 = this.interpolationDuration != 0;
                this.renderState = $$2 && this.renderState != null ? this.createInterpolatedRenderState(this.renderState, this.lastProgress) : this.createFreshRenderState();
                this.updateRenderSubState($$2, this.lastProgress);
            }
            this.interpolation.interpolate();
        }
    }

    @Override
    public InterpolationHandler getInterpolation() {
        return this.interpolation;
    }

    protected abstract void updateRenderSubState(boolean var1, float var2);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        $$0.define(DATA_POS_ROT_INTERPOLATION_DURATION_ID, 0);
        $$0.define(DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID, 0);
        $$0.define(DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID, 0);
        $$0.define(DATA_TRANSLATION_ID, new Vector3f());
        $$0.define(DATA_SCALE_ID, new Vector3f(1.0f, 1.0f, 1.0f));
        $$0.define(DATA_RIGHT_ROTATION_ID, new Quaternionf());
        $$0.define(DATA_LEFT_ROTATION_ID, new Quaternionf());
        $$0.define(DATA_BILLBOARD_RENDER_CONSTRAINTS_ID, BillboardConstraints.FIXED.getId());
        $$0.define(DATA_BRIGHTNESS_OVERRIDE_ID, -1);
        $$0.define(DATA_VIEW_RANGE_ID, Float.valueOf(1.0f));
        $$0.define(DATA_SHADOW_RADIUS_ID, Float.valueOf(0.0f));
        $$0.define(DATA_SHADOW_STRENGTH_ID, Float.valueOf(1.0f));
        $$0.define(DATA_WIDTH_ID, Float.valueOf(0.0f));
        $$0.define(DATA_HEIGHT_ID, Float.valueOf(0.0f));
        $$0.define(DATA_GLOW_COLOR_OVERRIDE_ID, -1);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        this.setTransformation($$0.read(TAG_TRANSFORMATION, Transformation.EXTENDED_CODEC).orElse(Transformation.identity()));
        this.setTransformationInterpolationDuration($$0.getIntOr(TAG_TRANSFORMATION_INTERPOLATION_DURATION, 0));
        this.setTransformationInterpolationDelay($$0.getIntOr(TAG_TRANSFORMATION_START_INTERPOLATION, 0));
        int $$1 = $$0.getIntOr(TAG_POS_ROT_INTERPOLATION_DURATION, 0);
        this.setPosRotInterpolationDuration(Mth.clamp($$1, 0, 59));
        this.setBillboardConstraints($$0.read(TAG_BILLBOARD, BillboardConstraints.CODEC).orElse(BillboardConstraints.FIXED));
        this.setViewRange($$0.getFloatOr(TAG_VIEW_RANGE, 1.0f));
        this.setShadowRadius($$0.getFloatOr(TAG_SHADOW_RADIUS, 0.0f));
        this.setShadowStrength($$0.getFloatOr(TAG_SHADOW_STRENGTH, 1.0f));
        this.setWidth($$0.getFloatOr(TAG_WIDTH, 0.0f));
        this.setHeight($$0.getFloatOr(TAG_HEIGHT, 0.0f));
        this.setGlowColorOverride($$0.getIntOr(TAG_GLOW_COLOR_OVERRIDE, -1));
        this.setBrightnessOverride($$0.read(TAG_BRIGHTNESS, Brightness.CODEC).orElse(null));
    }

    private void setTransformation(Transformation $$0) {
        this.entityData.set(DATA_TRANSLATION_ID, $$0.getTranslation());
        this.entityData.set(DATA_LEFT_ROTATION_ID, $$0.getLeftRotation());
        this.entityData.set(DATA_SCALE_ID, $$0.getScale());
        this.entityData.set(DATA_RIGHT_ROTATION_ID, $$0.getRightRotation());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        $$0.store(TAG_TRANSFORMATION, Transformation.EXTENDED_CODEC, Display.createTransformation(this.entityData));
        $$0.store(TAG_BILLBOARD, BillboardConstraints.CODEC, this.getBillboardConstraints());
        $$0.putInt(TAG_TRANSFORMATION_INTERPOLATION_DURATION, this.getTransformationInterpolationDuration());
        $$0.putInt(TAG_POS_ROT_INTERPOLATION_DURATION, this.getPosRotInterpolationDuration());
        $$0.putFloat(TAG_VIEW_RANGE, this.getViewRange());
        $$0.putFloat(TAG_SHADOW_RADIUS, this.getShadowRadius());
        $$0.putFloat(TAG_SHADOW_STRENGTH, this.getShadowStrength());
        $$0.putFloat(TAG_WIDTH, this.getWidth());
        $$0.putFloat(TAG_HEIGHT, this.getHeight());
        $$0.putInt(TAG_GLOW_COLOR_OVERRIDE, this.getGlowColorOverride());
        $$0.storeNullable(TAG_BRIGHTNESS, Brightness.CODEC, this.getBrightnessOverride());
    }

    public AABB getBoundingBoxForCulling() {
        return this.cullingBoundingBox;
    }

    public boolean affectedByCulling() {
        return !this.noCulling;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    public @Nullable RenderState renderState() {
        return this.renderState;
    }

    private void setTransformationInterpolationDuration(int $$0) {
        this.entityData.set(DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID, $$0);
    }

    private int getTransformationInterpolationDuration() {
        return this.entityData.get(DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID);
    }

    private void setTransformationInterpolationDelay(int $$0) {
        this.entityData.set(DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID, $$0, true);
    }

    private int getTransformationInterpolationDelay() {
        return this.entityData.get(DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID);
    }

    private void setPosRotInterpolationDuration(int $$0) {
        this.entityData.set(DATA_POS_ROT_INTERPOLATION_DURATION_ID, $$0);
    }

    private int getPosRotInterpolationDuration() {
        return this.entityData.get(DATA_POS_ROT_INTERPOLATION_DURATION_ID);
    }

    private void setBillboardConstraints(BillboardConstraints $$0) {
        this.entityData.set(DATA_BILLBOARD_RENDER_CONSTRAINTS_ID, $$0.getId());
    }

    private BillboardConstraints getBillboardConstraints() {
        return BillboardConstraints.BY_ID.apply(this.entityData.get(DATA_BILLBOARD_RENDER_CONSTRAINTS_ID).byteValue());
    }

    private void setBrightnessOverride(@Nullable Brightness $$0) {
        this.entityData.set(DATA_BRIGHTNESS_OVERRIDE_ID, $$0 != null ? $$0.pack() : -1);
    }

    private @Nullable Brightness getBrightnessOverride() {
        int $$0 = this.entityData.get(DATA_BRIGHTNESS_OVERRIDE_ID);
        return $$0 != -1 ? Brightness.unpack($$0) : null;
    }

    private int getPackedBrightnessOverride() {
        return this.entityData.get(DATA_BRIGHTNESS_OVERRIDE_ID);
    }

    private void setViewRange(float $$0) {
        this.entityData.set(DATA_VIEW_RANGE_ID, Float.valueOf($$0));
    }

    private float getViewRange() {
        return this.entityData.get(DATA_VIEW_RANGE_ID).floatValue();
    }

    private void setShadowRadius(float $$0) {
        this.entityData.set(DATA_SHADOW_RADIUS_ID, Float.valueOf($$0));
    }

    private float getShadowRadius() {
        return this.entityData.get(DATA_SHADOW_RADIUS_ID).floatValue();
    }

    private void setShadowStrength(float $$0) {
        this.entityData.set(DATA_SHADOW_STRENGTH_ID, Float.valueOf($$0));
    }

    private float getShadowStrength() {
        return this.entityData.get(DATA_SHADOW_STRENGTH_ID).floatValue();
    }

    private void setWidth(float $$0) {
        this.entityData.set(DATA_WIDTH_ID, Float.valueOf($$0));
    }

    private float getWidth() {
        return this.entityData.get(DATA_WIDTH_ID).floatValue();
    }

    private void setHeight(float $$0) {
        this.entityData.set(DATA_HEIGHT_ID, Float.valueOf($$0));
    }

    private int getGlowColorOverride() {
        return this.entityData.get(DATA_GLOW_COLOR_OVERRIDE_ID);
    }

    private void setGlowColorOverride(int $$0) {
        this.entityData.set(DATA_GLOW_COLOR_OVERRIDE_ID, $$0);
    }

    public float calculateInterpolationProgress(float $$0) {
        float $$4;
        int $$1 = this.interpolationDuration;
        if ($$1 <= 0) {
            return 1.0f;
        }
        float $$2 = (long)this.tickCount - this.interpolationStartClientTick;
        float $$3 = $$2 + $$0;
        this.lastProgress = $$4 = Mth.clamp(Mth.inverseLerp($$3, 0.0f, $$1), 0.0f, 1.0f);
        return $$4;
    }

    private float getHeight() {
        return this.entityData.get(DATA_HEIGHT_ID).floatValue();
    }

    @Override
    public void setPos(double $$0, double $$1, double $$2) {
        super.setPos($$0, $$1, $$2);
        this.updateCulling();
    }

    private void updateCulling() {
        float $$0 = this.getWidth();
        float $$1 = this.getHeight();
        this.noCulling = $$0 == 0.0f || $$1 == 0.0f;
        float $$2 = $$0 / 2.0f;
        double $$3 = this.getX();
        double $$4 = this.getY();
        double $$5 = this.getZ();
        this.cullingBoundingBox = new AABB($$3 - (double)$$2, $$4, $$5 - (double)$$2, $$3 + (double)$$2, $$4 + (double)$$1, $$5 + (double)$$2);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double $$0) {
        return $$0 < Mth.square((double)this.getViewRange() * 64.0 * Display.getViewScale());
    }

    @Override
    public int getTeamColor() {
        int $$0 = this.getGlowColorOverride();
        return $$0 != -1 ? $$0 : super.getTeamColor();
    }

    private RenderState createFreshRenderState() {
        return new RenderState(GenericInterpolator.constant(Display.createTransformation(this.entityData)), this.getBillboardConstraints(), this.getPackedBrightnessOverride(), FloatInterpolator.constant(this.getShadowRadius()), FloatInterpolator.constant(this.getShadowStrength()), this.getGlowColorOverride());
    }

    private RenderState createInterpolatedRenderState(RenderState $$0, float $$1) {
        Transformation $$2 = $$0.transformation.get($$1);
        float $$3 = $$0.shadowRadius.get($$1);
        float $$4 = $$0.shadowStrength.get($$1);
        return new RenderState(new TransformationInterpolator($$2, Display.createTransformation(this.entityData)), this.getBillboardConstraints(), this.getPackedBrightnessOverride(), new LinearFloatInterpolator($$3, this.getShadowRadius()), new LinearFloatInterpolator($$4, this.getShadowStrength()), this.getGlowColorOverride());
    }

    public static final class RenderState
    extends Record {
        final GenericInterpolator<Transformation> transformation;
        private final BillboardConstraints billboardConstraints;
        private final int brightnessOverride;
        final FloatInterpolator shadowRadius;
        final FloatInterpolator shadowStrength;
        private final int glowColorOverride;

        public RenderState(GenericInterpolator<Transformation> $$0, BillboardConstraints $$1, int $$2, FloatInterpolator $$3, FloatInterpolator $$4, int $$5) {
            this.transformation = $$0;
            this.billboardConstraints = $$1;
            this.brightnessOverride = $$2;
            this.shadowRadius = $$3;
            this.shadowStrength = $$4;
            this.glowColorOverride = $$5;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{RenderState.class, "transformation;billboardConstraints;brightnessOverride;shadowRadius;shadowStrength;glowColorOverride", "transformation", "billboardConstraints", "brightnessOverride", "shadowRadius", "shadowStrength", "glowColorOverride"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RenderState.class, "transformation;billboardConstraints;brightnessOverride;shadowRadius;shadowStrength;glowColorOverride", "transformation", "billboardConstraints", "brightnessOverride", "shadowRadius", "shadowStrength", "glowColorOverride"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RenderState.class, "transformation;billboardConstraints;brightnessOverride;shadowRadius;shadowStrength;glowColorOverride", "transformation", "billboardConstraints", "brightnessOverride", "shadowRadius", "shadowStrength", "glowColorOverride"}, this, $$0);
        }

        public GenericInterpolator<Transformation> transformation() {
            return this.transformation;
        }

        public BillboardConstraints billboardConstraints() {
            return this.billboardConstraints;
        }

        public int brightnessOverride() {
            return this.brightnessOverride;
        }

        public FloatInterpolator shadowRadius() {
            return this.shadowRadius;
        }

        public FloatInterpolator shadowStrength() {
            return this.shadowStrength;
        }

        public int glowColorOverride() {
            return this.glowColorOverride;
        }
    }

    public static enum BillboardConstraints implements StringRepresentable
    {
        FIXED(0, "fixed"),
        VERTICAL(1, "vertical"),
        HORIZONTAL(2, "horizontal"),
        CENTER(3, "center");

        public static final Codec<BillboardConstraints> CODEC;
        public static final IntFunction<BillboardConstraints> BY_ID;
        private final byte id;
        private final String name;

        private BillboardConstraints(byte $$0, String $$1) {
            this.name = $$1;
            this.id = $$0;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        byte getId() {
            return this.id;
        }

        static {
            CODEC = StringRepresentable.fromEnum(BillboardConstraints::values);
            BY_ID = ByIdMap.continuous(BillboardConstraints::getId, BillboardConstraints.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        }
    }

    @FunctionalInterface
    public static interface GenericInterpolator<T> {
        public static <T> GenericInterpolator<T> constant(T $$0) {
            return $$1 -> $$0;
        }

        public T get(float var1);
    }

    @FunctionalInterface
    public static interface FloatInterpolator {
        public static FloatInterpolator constant(float $$0) {
            return $$1 -> $$0;
        }

        public float get(float var1);
    }

    record TransformationInterpolator(Transformation previous, Transformation current) implements GenericInterpolator<Transformation>
    {
        @Override
        public Transformation get(float $$0) {
            if ((double)$$0 >= 1.0) {
                return this.current;
            }
            return this.previous.slerp(this.current, $$0);
        }

        @Override
        public /* synthetic */ Object get(float f) {
            return this.get(f);
        }
    }

    record LinearFloatInterpolator(float previous, float current) implements FloatInterpolator
    {
        @Override
        public float get(float $$0) {
            return Mth.lerp($$0, this.previous, this.current);
        }
    }

    record ColorInterpolator(int previous, int current) implements IntInterpolator
    {
        @Override
        public int get(float $$0) {
            return ARGB.srgbLerp($$0, this.previous, this.current);
        }
    }

    record LinearIntInterpolator(int previous, int current) implements IntInterpolator
    {
        @Override
        public int get(float $$0) {
            return Mth.lerpInt($$0, this.previous, this.current);
        }
    }

    @FunctionalInterface
    public static interface IntInterpolator {
        public static IntInterpolator constant(int $$0) {
            return $$1 -> $$0;
        }

        public int get(float var1);
    }

    public static class TextDisplay
    extends Display {
        public static final String TAG_TEXT = "text";
        private static final String TAG_LINE_WIDTH = "line_width";
        private static final String TAG_TEXT_OPACITY = "text_opacity";
        private static final String TAG_BACKGROUND_COLOR = "background";
        private static final String TAG_SHADOW = "shadow";
        private static final String TAG_SEE_THROUGH = "see_through";
        private static final String TAG_USE_DEFAULT_BACKGROUND = "default_background";
        private static final String TAG_ALIGNMENT = "alignment";
        public static final byte FLAG_SHADOW = 1;
        public static final byte FLAG_SEE_THROUGH = 2;
        public static final byte FLAG_USE_DEFAULT_BACKGROUND = 4;
        public static final byte FLAG_ALIGN_LEFT = 8;
        public static final byte FLAG_ALIGN_RIGHT = 16;
        private static final byte INITIAL_TEXT_OPACITY = -1;
        public static final int INITIAL_BACKGROUND = 0x40000000;
        private static final int INITIAL_LINE_WIDTH = 200;
        private static final EntityDataAccessor<Component> DATA_TEXT_ID = SynchedEntityData.defineId(TextDisplay.class, EntityDataSerializers.COMPONENT);
        private static final EntityDataAccessor<Integer> DATA_LINE_WIDTH_ID = SynchedEntityData.defineId(TextDisplay.class, EntityDataSerializers.INT);
        private static final EntityDataAccessor<Integer> DATA_BACKGROUND_COLOR_ID = SynchedEntityData.defineId(TextDisplay.class, EntityDataSerializers.INT);
        private static final EntityDataAccessor<Byte> DATA_TEXT_OPACITY_ID = SynchedEntityData.defineId(TextDisplay.class, EntityDataSerializers.BYTE);
        private static final EntityDataAccessor<Byte> DATA_STYLE_FLAGS_ID = SynchedEntityData.defineId(TextDisplay.class, EntityDataSerializers.BYTE);
        private static final IntSet TEXT_RENDER_STATE_IDS = IntSet.of((int[])new int[]{DATA_TEXT_ID.id(), DATA_LINE_WIDTH_ID.id(), DATA_BACKGROUND_COLOR_ID.id(), DATA_TEXT_OPACITY_ID.id(), DATA_STYLE_FLAGS_ID.id()});
        private @Nullable CachedInfo clientDisplayCache;
        private @Nullable TextRenderState textRenderState;

        public TextDisplay(EntityType<?> $$0, Level $$1) {
            super($$0, $$1);
        }

        @Override
        protected void defineSynchedData(SynchedEntityData.Builder $$0) {
            super.defineSynchedData($$0);
            $$0.define(DATA_TEXT_ID, Component.empty());
            $$0.define(DATA_LINE_WIDTH_ID, 200);
            $$0.define(DATA_BACKGROUND_COLOR_ID, 0x40000000);
            $$0.define(DATA_TEXT_OPACITY_ID, (byte)-1);
            $$0.define(DATA_STYLE_FLAGS_ID, (byte)0);
        }

        @Override
        public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
            super.onSyncedDataUpdated($$0);
            if (TEXT_RENDER_STATE_IDS.contains($$0.id())) {
                this.updateRenderState = true;
            }
        }

        private Component getText() {
            return this.entityData.get(DATA_TEXT_ID);
        }

        private void setText(Component $$0) {
            this.entityData.set(DATA_TEXT_ID, $$0);
        }

        private int getLineWidth() {
            return this.entityData.get(DATA_LINE_WIDTH_ID);
        }

        private void setLineWidth(int $$0) {
            this.entityData.set(DATA_LINE_WIDTH_ID, $$0);
        }

        private byte getTextOpacity() {
            return this.entityData.get(DATA_TEXT_OPACITY_ID);
        }

        private void setTextOpacity(byte $$0) {
            this.entityData.set(DATA_TEXT_OPACITY_ID, $$0);
        }

        private int getBackgroundColor() {
            return this.entityData.get(DATA_BACKGROUND_COLOR_ID);
        }

        private void setBackgroundColor(int $$0) {
            this.entityData.set(DATA_BACKGROUND_COLOR_ID, $$0);
        }

        private byte getFlags() {
            return this.entityData.get(DATA_STYLE_FLAGS_ID);
        }

        private void setFlags(byte $$0) {
            this.entityData.set(DATA_STYLE_FLAGS_ID, $$0);
        }

        private static byte loadFlag(byte $$0, ValueInput $$1, String $$2, byte $$3) {
            if ($$1.getBooleanOr($$2, false)) {
                return (byte)($$0 | $$3);
            }
            return $$0;
        }

        @Override
        protected void readAdditionalSaveData(ValueInput $$0) {
            super.readAdditionalSaveData($$0);
            this.setLineWidth($$0.getIntOr(TAG_LINE_WIDTH, 200));
            this.setTextOpacity($$0.getByteOr(TAG_TEXT_OPACITY, (byte)-1));
            this.setBackgroundColor($$0.getIntOr(TAG_BACKGROUND_COLOR, 0x40000000));
            byte $$1 = TextDisplay.loadFlag((byte)0, $$0, TAG_SHADOW, (byte)1);
            $$1 = TextDisplay.loadFlag($$1, $$0, TAG_SEE_THROUGH, (byte)2);
            $$1 = TextDisplay.loadFlag($$1, $$0, TAG_USE_DEFAULT_BACKGROUND, (byte)4);
            Optional<Align> $$2 = $$0.read(TAG_ALIGNMENT, Align.CODEC);
            if ($$2.isPresent()) {
                $$1 = switch ($$2.get().ordinal()) {
                    default -> throw new MatchException(null, null);
                    case 0 -> $$1;
                    case 1 -> (byte)($$1 | 8);
                    case 2 -> (byte)($$1 | 0x10);
                };
            }
            this.setFlags($$1);
            Optional<Component> $$3 = $$0.read(TAG_TEXT, ComponentSerialization.CODEC);
            if ($$3.isPresent()) {
                try {
                    Level level = this.level();
                    if (level instanceof ServerLevel) {
                        ServerLevel $$4 = (ServerLevel)level;
                        CommandSourceStack $$5 = this.createCommandSourceStackForNameResolution($$4).withPermission(LevelBasedPermissionSet.GAMEMASTER);
                        MutableComponent $$6 = ComponentUtils.updateForEntity($$5, $$3.get(), (Entity)this, 0);
                        this.setText($$6);
                    } else {
                        this.setText(Component.empty());
                    }
                }
                catch (Exception $$7) {
                    LOGGER.warn("Failed to parse display entity text {}", $$3, (Object)$$7);
                }
            }
        }

        private static void storeFlag(byte $$0, ValueOutput $$1, String $$2, byte $$3) {
            $$1.putBoolean($$2, ($$0 & $$3) != 0);
        }

        @Override
        protected void addAdditionalSaveData(ValueOutput $$0) {
            super.addAdditionalSaveData($$0);
            $$0.store(TAG_TEXT, ComponentSerialization.CODEC, this.getText());
            $$0.putInt(TAG_LINE_WIDTH, this.getLineWidth());
            $$0.putInt(TAG_BACKGROUND_COLOR, this.getBackgroundColor());
            $$0.putByte(TAG_TEXT_OPACITY, this.getTextOpacity());
            byte $$1 = this.getFlags();
            TextDisplay.storeFlag($$1, $$0, TAG_SHADOW, (byte)1);
            TextDisplay.storeFlag($$1, $$0, TAG_SEE_THROUGH, (byte)2);
            TextDisplay.storeFlag($$1, $$0, TAG_USE_DEFAULT_BACKGROUND, (byte)4);
            $$0.store(TAG_ALIGNMENT, Align.CODEC, TextDisplay.getAlign($$1));
        }

        @Override
        protected void updateRenderSubState(boolean $$0, float $$1) {
            this.textRenderState = $$0 && this.textRenderState != null ? this.createInterpolatedTextRenderState(this.textRenderState, $$1) : this.createFreshTextRenderState();
            this.clientDisplayCache = null;
        }

        public @Nullable TextRenderState textRenderState() {
            return this.textRenderState;
        }

        private TextRenderState createFreshTextRenderState() {
            return new TextRenderState(this.getText(), this.getLineWidth(), IntInterpolator.constant(this.getTextOpacity()), IntInterpolator.constant(this.getBackgroundColor()), this.getFlags());
        }

        private TextRenderState createInterpolatedTextRenderState(TextRenderState $$0, float $$1) {
            int $$2 = $$0.backgroundColor.get($$1);
            int $$3 = $$0.textOpacity.get($$1);
            return new TextRenderState(this.getText(), this.getLineWidth(), new LinearIntInterpolator($$3, this.getTextOpacity()), new ColorInterpolator($$2, this.getBackgroundColor()), this.getFlags());
        }

        public CachedInfo cacheDisplay(LineSplitter $$0) {
            if (this.clientDisplayCache == null) {
                this.clientDisplayCache = this.textRenderState != null ? $$0.split(this.textRenderState.text(), this.textRenderState.lineWidth()) : new CachedInfo(List.of(), 0);
            }
            return this.clientDisplayCache;
        }

        public static Align getAlign(byte $$0) {
            if (($$0 & 8) != 0) {
                return Align.LEFT;
            }
            if (($$0 & 0x10) != 0) {
                return Align.RIGHT;
            }
            return Align.CENTER;
        }

        public static enum Align implements StringRepresentable
        {
            CENTER("center"),
            LEFT("left"),
            RIGHT("right");

            public static final Codec<Align> CODEC;
            private final String name;

            private Align(String $$0) {
                this.name = $$0;
            }

            @Override
            public String getSerializedName() {
                return this.name;
            }

            static {
                CODEC = StringRepresentable.fromEnum(Align::values);
            }
        }

        public static final class TextRenderState
        extends Record {
            private final Component text;
            private final int lineWidth;
            final IntInterpolator textOpacity;
            final IntInterpolator backgroundColor;
            private final byte flags;

            public TextRenderState(Component $$0, int $$1, IntInterpolator $$2, IntInterpolator $$3, byte $$4) {
                this.text = $$0;
                this.lineWidth = $$1;
                this.textOpacity = $$2;
                this.backgroundColor = $$3;
                this.flags = $$4;
            }

            @Override
            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{TextRenderState.class, "text;lineWidth;textOpacity;backgroundColor;flags", "text", "lineWidth", "textOpacity", "backgroundColor", "flags"}, this);
            }

            @Override
            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{TextRenderState.class, "text;lineWidth;textOpacity;backgroundColor;flags", "text", "lineWidth", "textOpacity", "backgroundColor", "flags"}, this);
            }

            @Override
            public final boolean equals(Object $$0) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{TextRenderState.class, "text;lineWidth;textOpacity;backgroundColor;flags", "text", "lineWidth", "textOpacity", "backgroundColor", "flags"}, this, $$0);
            }

            public Component text() {
                return this.text;
            }

            public int lineWidth() {
                return this.lineWidth;
            }

            public IntInterpolator textOpacity() {
                return this.textOpacity;
            }

            public IntInterpolator backgroundColor() {
                return this.backgroundColor;
            }

            public byte flags() {
                return this.flags;
            }
        }

        public record CachedInfo(List<CachedLine> lines, int width) {
        }

        @FunctionalInterface
        public static interface LineSplitter {
            public CachedInfo split(Component var1, int var2);
        }

        public record CachedLine(FormattedCharSequence contents, int width) {
        }
    }

    public static class BlockDisplay
    extends Display {
        public static final String TAG_BLOCK_STATE = "block_state";
        private static final EntityDataAccessor<BlockState> DATA_BLOCK_STATE_ID = SynchedEntityData.defineId(BlockDisplay.class, EntityDataSerializers.BLOCK_STATE);
        private @Nullable BlockRenderState blockRenderState;

        public BlockDisplay(EntityType<?> $$0, Level $$1) {
            super($$0, $$1);
        }

        @Override
        protected void defineSynchedData(SynchedEntityData.Builder $$0) {
            super.defineSynchedData($$0);
            $$0.define(DATA_BLOCK_STATE_ID, Blocks.AIR.defaultBlockState());
        }

        @Override
        public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
            super.onSyncedDataUpdated($$0);
            if ($$0.equals(DATA_BLOCK_STATE_ID)) {
                this.updateRenderState = true;
            }
        }

        private BlockState getBlockState() {
            return this.entityData.get(DATA_BLOCK_STATE_ID);
        }

        private void setBlockState(BlockState $$0) {
            this.entityData.set(DATA_BLOCK_STATE_ID, $$0);
        }

        @Override
        protected void readAdditionalSaveData(ValueInput $$0) {
            super.readAdditionalSaveData($$0);
            this.setBlockState($$0.read(TAG_BLOCK_STATE, BlockState.CODEC).orElse(Blocks.AIR.defaultBlockState()));
        }

        @Override
        protected void addAdditionalSaveData(ValueOutput $$0) {
            super.addAdditionalSaveData($$0);
            $$0.store(TAG_BLOCK_STATE, BlockState.CODEC, this.getBlockState());
        }

        public @Nullable BlockRenderState blockRenderState() {
            return this.blockRenderState;
        }

        @Override
        protected void updateRenderSubState(boolean $$0, float $$1) {
            this.blockRenderState = new BlockRenderState(this.getBlockState());
        }

        public record BlockRenderState(BlockState blockState) {
        }
    }

    public static class ItemDisplay
    extends Display {
        private static final String TAG_ITEM = "item";
        private static final String TAG_ITEM_DISPLAY = "item_display";
        private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK_ID = SynchedEntityData.defineId(ItemDisplay.class, EntityDataSerializers.ITEM_STACK);
        private static final EntityDataAccessor<Byte> DATA_ITEM_DISPLAY_ID = SynchedEntityData.defineId(ItemDisplay.class, EntityDataSerializers.BYTE);
        private final SlotAccess slot = SlotAccess.of(this::getItemStack, this::setItemStack);
        private @Nullable ItemRenderState itemRenderState;

        public ItemDisplay(EntityType<?> $$0, Level $$1) {
            super($$0, $$1);
        }

        @Override
        protected void defineSynchedData(SynchedEntityData.Builder $$0) {
            super.defineSynchedData($$0);
            $$0.define(DATA_ITEM_STACK_ID, ItemStack.EMPTY);
            $$0.define(DATA_ITEM_DISPLAY_ID, ItemDisplayContext.NONE.getId());
        }

        @Override
        public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
            super.onSyncedDataUpdated($$0);
            if (DATA_ITEM_STACK_ID.equals($$0) || DATA_ITEM_DISPLAY_ID.equals($$0)) {
                this.updateRenderState = true;
            }
        }

        private ItemStack getItemStack() {
            return this.entityData.get(DATA_ITEM_STACK_ID);
        }

        private void setItemStack(ItemStack $$0) {
            this.entityData.set(DATA_ITEM_STACK_ID, $$0);
        }

        private void setItemTransform(ItemDisplayContext $$0) {
            this.entityData.set(DATA_ITEM_DISPLAY_ID, $$0.getId());
        }

        private ItemDisplayContext getItemTransform() {
            return ItemDisplayContext.BY_ID.apply(this.entityData.get(DATA_ITEM_DISPLAY_ID).byteValue());
        }

        @Override
        protected void readAdditionalSaveData(ValueInput $$0) {
            super.readAdditionalSaveData($$0);
            this.setItemStack($$0.read(TAG_ITEM, ItemStack.CODEC).orElse(ItemStack.EMPTY));
            this.setItemTransform($$0.read(TAG_ITEM_DISPLAY, ItemDisplayContext.CODEC).orElse(ItemDisplayContext.NONE));
        }

        @Override
        protected void addAdditionalSaveData(ValueOutput $$0) {
            super.addAdditionalSaveData($$0);
            ItemStack $$1 = this.getItemStack();
            if (!$$1.isEmpty()) {
                $$0.store(TAG_ITEM, ItemStack.CODEC, $$1);
            }
            $$0.store(TAG_ITEM_DISPLAY, ItemDisplayContext.CODEC, this.getItemTransform());
        }

        @Override
        public @Nullable SlotAccess getSlot(int $$0) {
            if ($$0 == 0) {
                return this.slot;
            }
            return null;
        }

        public @Nullable ItemRenderState itemRenderState() {
            return this.itemRenderState;
        }

        @Override
        protected void updateRenderSubState(boolean $$0, float $$1) {
            ItemStack $$2 = this.getItemStack();
            $$2.setEntityRepresentation(this);
            this.itemRenderState = new ItemRenderState($$2, this.getItemTransform());
        }

        public record ItemRenderState(ItemStack itemStack, ItemDisplayContext itemTransform) {
        }
    }
}

