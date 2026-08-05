/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.kinds.Applicative$Mu
 *  com.mojang.datafixers.kinds.Const$Mu
 *  com.mojang.datafixers.kinds.IdF
 *  com.mojang.datafixers.kinds.IdF$Mu
 *  com.mojang.datafixers.kinds.K1
 *  com.mojang.datafixers.kinds.OptionalBox
 *  com.mojang.datafixers.kinds.OptionalBox$Mu
 *  com.mojang.datafixers.util.Function3
 *  com.mojang.datafixers.util.Function4
 *  com.mojang.datafixers.util.Unit
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.behavior.declarative;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.Const;
import com.mojang.datafixers.kinds.IdF;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.OptionalBox;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Unit;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.behavior.declarative.Trigger;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jspecify.annotations.Nullable;

public class BehaviorBuilder<E extends LivingEntity, M>
implements App<Mu<E>, M> {
    private final TriggerWithResult<E, M> trigger;

    public static <E extends LivingEntity, M> BehaviorBuilder<E, M> unbox(App<Mu<E>, M> $$0) {
        return (BehaviorBuilder)$$0;
    }

    public static <E extends LivingEntity> Instance<E> instance() {
        return new Instance();
    }

    public static <E extends LivingEntity> OneShot<E> create(Function<Instance<E>, ? extends App<Mu<E>, Trigger<E>>> $$0) {
        final TriggerWithResult<E, Trigger<E>> $$1 = BehaviorBuilder.get($$0.apply(BehaviorBuilder.instance()));
        return new OneShot<E>(){

            @Override
            public boolean trigger(ServerLevel $$0, E $$12, long $$2) {
                Trigger $$3 = (Trigger)$$1.tryTrigger($$0, $$12, $$2);
                if ($$3 == null) {
                    return false;
                }
                return $$3.trigger($$0, $$12, $$2);
            }

            @Override
            public String debugString() {
                return "OneShot[" + $$1.debugString() + "]";
            }

            public String toString() {
                return this.debugString();
            }
        };
    }

    public static <E extends LivingEntity> OneShot<E> sequence(Trigger<? super E> $$0, Trigger<? super E> $$1) {
        return BehaviorBuilder.create((Instance<E> $$2) -> $$2.group($$2.ifTriggered($$0)).apply((Applicative)$$2, $$1 -> $$1::trigger));
    }

    public static <E extends LivingEntity> OneShot<E> triggerIf(Predicate<E> $$0, OneShot<? super E> $$1) {
        return BehaviorBuilder.sequence(BehaviorBuilder.triggerIf($$0), $$1);
    }

    public static <E extends LivingEntity> OneShot<E> triggerIf(Predicate<E> $$0) {
        return BehaviorBuilder.create((Instance<E> $$12) -> $$12.point(($$1, $$2, $$3) -> $$0.test($$2)));
    }

    public static <E extends LivingEntity> OneShot<E> triggerIf(BiPredicate<ServerLevel, E> $$0) {
        return BehaviorBuilder.create((Instance<E> $$12) -> $$12.point(($$1, $$2, $$3) -> $$0.test($$1, $$2)));
    }

    static <E extends LivingEntity, M> TriggerWithResult<E, M> get(App<Mu<E>, M> $$0) {
        return BehaviorBuilder.unbox($$0).trigger;
    }

    BehaviorBuilder(TriggerWithResult<E, M> $$0) {
        this.trigger = $$0;
    }

    static <E extends LivingEntity, M> BehaviorBuilder<E, M> create(TriggerWithResult<E, M> $$0) {
        return new BehaviorBuilder<E, M>($$0);
    }

    public static final class Instance<E extends LivingEntity>
    implements Applicative<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, Mu<E>> {
        public <Value> Optional<Value> tryGet(MemoryAccessor<OptionalBox.Mu, Value> $$0) {
            return OptionalBox.unbox($$0.value());
        }

        public <Value> Value get(MemoryAccessor<IdF.Mu, Value> $$0) {
            return (Value)IdF.get($$0.value());
        }

        public <Value> BehaviorBuilder<E, MemoryAccessor<OptionalBox.Mu, Value>> registered(MemoryModuleType<Value> $$0) {
            return new PureMemory(new MemoryCondition.Registered<Value>($$0));
        }

        public <Value> BehaviorBuilder<E, MemoryAccessor<IdF.Mu, Value>> present(MemoryModuleType<Value> $$0) {
            return new PureMemory(new MemoryCondition.Present<Value>($$0));
        }

        public <Value> BehaviorBuilder<E, MemoryAccessor<Const.Mu<Unit>, Value>> absent(MemoryModuleType<Value> $$0) {
            return new PureMemory(new MemoryCondition.Absent<Value>($$0));
        }

        public BehaviorBuilder<E, Unit> ifTriggered(Trigger<? super E> $$0) {
            return new TriggerWrapper<E>($$0);
        }

        public <A> BehaviorBuilder<E, A> point(A $$0) {
            return new Constant($$0);
        }

        public <A> BehaviorBuilder<E, A> point(Supplier<String> $$0, A $$1) {
            return new Constant($$1, $$0);
        }

        public <A, R> Function<App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, A>, App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, R>> lift1(App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, Function<A, R>> $$0) {
            return $$1 -> {
                final TriggerWithResult $$2 = BehaviorBuilder.get($$1);
                final TriggerWithResult $$3 = BehaviorBuilder.get($$0);
                return BehaviorBuilder.create(new TriggerWithResult<E, R>(this){

                    @Override
                    public R tryTrigger(ServerLevel $$0, E $$1, long $$22) {
                        Object $$32 = $$2.tryTrigger($$0, $$1, $$22);
                        if ($$32 == null) {
                            return null;
                        }
                        Function $$4 = (Function)$$3.tryTrigger($$0, $$1, $$22);
                        if ($$4 == null) {
                            return null;
                        }
                        return $$4.apply($$32);
                    }

                    @Override
                    public String debugString() {
                        return $$3.debugString() + " * " + $$2.debugString();
                    }

                    public String toString() {
                        return this.debugString();
                    }
                });
            };
        }

        public <T, R> BehaviorBuilder<E, R> map(final Function<? super T, ? extends R> $$0, App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, T> $$1) {
            final TriggerWithResult<E, T> $$2 = BehaviorBuilder.get($$1);
            return BehaviorBuilder.create(new TriggerWithResult<E, R>(this){

                @Override
                public R tryTrigger(ServerLevel $$02, E $$1, long $$22) {
                    Object $$3 = $$2.tryTrigger($$02, $$1, $$22);
                    if ($$3 == null) {
                        return null;
                    }
                    return $$0.apply($$3);
                }

                @Override
                public String debugString() {
                    return $$2.debugString() + ".map[" + String.valueOf($$0) + "]";
                }

                public String toString() {
                    return this.debugString();
                }
            });
        }

        public <A, B, R> BehaviorBuilder<E, R> ap2(App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, BiFunction<A, B, R>> $$0, App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, A> $$1, App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, B> $$2) {
            final TriggerWithResult<E, A> $$3 = BehaviorBuilder.get($$1);
            final TriggerWithResult<E, B> $$4 = BehaviorBuilder.get($$2);
            final TriggerWithResult<E, BiFunction<A, B, R>> $$5 = BehaviorBuilder.get($$0);
            return BehaviorBuilder.create(new TriggerWithResult<E, R>(this){

                @Override
                public R tryTrigger(ServerLevel $$0, E $$1, long $$2) {
                    Object $$32 = $$3.tryTrigger($$0, $$1, $$2);
                    if ($$32 == null) {
                        return null;
                    }
                    Object $$42 = $$4.tryTrigger($$0, $$1, $$2);
                    if ($$42 == null) {
                        return null;
                    }
                    BiFunction $$52 = (BiFunction)$$5.tryTrigger($$0, $$1, $$2);
                    if ($$52 == null) {
                        return null;
                    }
                    return $$52.apply($$32, $$42);
                }

                @Override
                public String debugString() {
                    return $$5.debugString() + " * " + $$3.debugString() + " * " + $$4.debugString();
                }

                public String toString() {
                    return this.debugString();
                }
            });
        }

        public <T1, T2, T3, R> BehaviorBuilder<E, R> ap3(App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, Function3<T1, T2, T3, R>> $$0, App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, T1> $$1, App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, T2> $$2, App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, T3> $$3) {
            final TriggerWithResult<E, T1> $$4 = BehaviorBuilder.get($$1);
            final TriggerWithResult<E, T2> $$5 = BehaviorBuilder.get($$2);
            final TriggerWithResult<E, T3> $$6 = BehaviorBuilder.get($$3);
            final TriggerWithResult<E, Function3<T1, T2, T3, R>> $$7 = BehaviorBuilder.get($$0);
            return BehaviorBuilder.create(new TriggerWithResult<E, R>(this){

                @Override
                public R tryTrigger(ServerLevel $$0, E $$1, long $$2) {
                    Object $$3 = $$4.tryTrigger($$0, $$1, $$2);
                    if ($$3 == null) {
                        return null;
                    }
                    Object $$42 = $$5.tryTrigger($$0, $$1, $$2);
                    if ($$42 == null) {
                        return null;
                    }
                    Object $$52 = $$6.tryTrigger($$0, $$1, $$2);
                    if ($$52 == null) {
                        return null;
                    }
                    Function3 $$62 = (Function3)$$7.tryTrigger($$0, $$1, $$2);
                    if ($$62 == null) {
                        return null;
                    }
                    return $$62.apply($$3, $$42, $$52);
                }

                @Override
                public String debugString() {
                    return $$7.debugString() + " * " + $$4.debugString() + " * " + $$5.debugString() + " * " + $$6.debugString();
                }

                public String toString() {
                    return this.debugString();
                }
            });
        }

        public <T1, T2, T3, T4, R> BehaviorBuilder<E, R> ap4(App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, Function4<T1, T2, T3, T4, R>> $$0, App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, T1> $$1, App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, T2> $$2, App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, T3> $$3, App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, T4> $$4) {
            final TriggerWithResult<E, T1> $$5 = BehaviorBuilder.get($$1);
            final TriggerWithResult<E, T2> $$6 = BehaviorBuilder.get($$2);
            final TriggerWithResult<E, T3> $$7 = BehaviorBuilder.get($$3);
            final TriggerWithResult<E, T4> $$8 = BehaviorBuilder.get($$4);
            final TriggerWithResult<E, Function4<T1, T2, T3, T4, R>> $$9 = BehaviorBuilder.get($$0);
            return BehaviorBuilder.create(new TriggerWithResult<E, R>(this){

                @Override
                public R tryTrigger(ServerLevel $$0, E $$1, long $$2) {
                    Object $$3 = $$5.tryTrigger($$0, $$1, $$2);
                    if ($$3 == null) {
                        return null;
                    }
                    Object $$4 = $$6.tryTrigger($$0, $$1, $$2);
                    if ($$4 == null) {
                        return null;
                    }
                    Object $$52 = $$7.tryTrigger($$0, $$1, $$2);
                    if ($$52 == null) {
                        return null;
                    }
                    Object $$62 = $$8.tryTrigger($$0, $$1, $$2);
                    if ($$62 == null) {
                        return null;
                    }
                    Function4 $$72 = (Function4)$$9.tryTrigger($$0, $$1, $$2);
                    if ($$72 == null) {
                        return null;
                    }
                    return $$72.apply($$3, $$4, $$52, $$62);
                }

                @Override
                public String debugString() {
                    return $$9.debugString() + " * " + $$5.debugString() + " * " + $$6.debugString() + " * " + $$7.debugString() + " * " + $$8.debugString();
                }

                public String toString() {
                    return this.debugString();
                }
            });
        }

        public /* synthetic */ App ap4(App app, App app2, App app3, App app4, App app5) {
            return this.ap4(app, app2, app3, app4, app5);
        }

        public /* synthetic */ App ap3(App app, App app2, App app3, App app4) {
            return this.ap3(app, app2, app3, app4);
        }

        public /* synthetic */ App ap2(App app, App app2, App app3) {
            return this.ap2(app, app2, app3);
        }

        public /* synthetic */ App point(Object object) {
            return this.point(object);
        }

        public /* synthetic */ App map(Function function, App app) {
            return this.map(function, app);
        }

        static final class Mu<E extends LivingEntity>
        implements Applicative.Mu {
            private Mu() {
            }
        }
    }

    static interface TriggerWithResult<E extends LivingEntity, R> {
        public @Nullable R tryTrigger(ServerLevel var1, E var2, long var3);

        public String debugString();
    }

    static final class TriggerWrapper<E extends LivingEntity>
    extends BehaviorBuilder<E, Unit> {
        TriggerWrapper(final Trigger<? super E> $$0) {
            super(new TriggerWithResult<E, Unit>(){

                @Override
                public @Nullable Unit tryTrigger(ServerLevel $$02, E $$1, long $$2) {
                    return $$0.trigger($$02, $$1, $$2) ? Unit.INSTANCE : null;
                }

                @Override
                public String debugString() {
                    return "T[" + String.valueOf($$0) + "]";
                }

                @Override
                public /* synthetic */ @Nullable Object tryTrigger(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
                    return this.tryTrigger(serverLevel, (Object)livingEntity, l);
                }
            });
        }
    }

    static final class Constant<E extends LivingEntity, A>
    extends BehaviorBuilder<E, A> {
        Constant(A $$0) {
            this($$0, () -> "C[" + String.valueOf($$0) + "]");
        }

        Constant(final A $$0, final Supplier<String> $$1) {
            super(new TriggerWithResult<E, A>(){

                @Override
                public A tryTrigger(ServerLevel $$02, E $$12, long $$2) {
                    return $$0;
                }

                @Override
                public String debugString() {
                    return (String)$$1.get();
                }

                public String toString() {
                    return this.debugString();
                }
            });
        }
    }

    static final class PureMemory<E extends LivingEntity, F extends K1, Value>
    extends BehaviorBuilder<E, MemoryAccessor<F, Value>> {
        PureMemory(final MemoryCondition<F, Value> $$0) {
            super(new TriggerWithResult<E, MemoryAccessor<F, Value>>(){

                @Override
                public @Nullable MemoryAccessor<F, Value> tryTrigger(ServerLevel $$02, E $$1, long $$2) {
                    Brain<?> $$3 = ((LivingEntity)$$1).getBrain();
                    Optional $$4 = $$3.getMemoryInternal($$0.memory());
                    if ($$4 == null) {
                        return null;
                    }
                    return $$0.createAccessor($$3, $$4);
                }

                @Override
                public String debugString() {
                    return "M[" + String.valueOf($$0) + "]";
                }

                public String toString() {
                    return this.debugString();
                }

                @Override
                public /* synthetic */ @Nullable Object tryTrigger(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
                    return this.tryTrigger(serverLevel, livingEntity, l);
                }
            });
        }
    }

    public static final class Mu<E extends LivingEntity>
    implements K1 {
    }
}

