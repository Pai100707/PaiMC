package net.minecraft.world.entity.ai.behavior.declarative;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
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
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class BehaviorBuilder<E extends net.minecraft.world.entity.LivingEntity, M> implements App<BehaviorBuilder.Mu<E>, M> {
   private final BehaviorBuilder.TriggerWithResult<E, M> trigger;

   public static <E extends net.minecraft.world.entity.LivingEntity, M> BehaviorBuilder<E, M> unbox(App<BehaviorBuilder.Mu<E>, M> $$0) {
      return (BehaviorBuilder<E, M>)$$0;
   }

   public static <E extends net.minecraft.world.entity.LivingEntity> BehaviorBuilder.Instance<E> instance() {
      return new BehaviorBuilder.Instance<>();
   }

   public static <E extends net.minecraft.world.entity.LivingEntity> OneShot<E> create(
      Function<BehaviorBuilder.Instance<E>, ? extends App<BehaviorBuilder.Mu<E>, Trigger<E>>> $$0
   ) {
      final BehaviorBuilder.TriggerWithResult<E, Trigger<E>> $$1 = get((App<BehaviorBuilder.Mu<E>, Trigger<E>>)$$0.apply(instance()));
      return new OneShot<E>() {
         @Override
         public boolean trigger(ServerLevel $$0, E $$1x, long $$2) {
            Trigger<E> $$3 = $$1.tryTrigger($$0, $$1, $$2);
            return $$3 == null ? false : $$3.trigger($$0, $$1, $$2);
         }

         @Override
         public String debugString() {
            return "OneShot[" + $$1.debugString() + "]";
         }

         @Override
         public String toString() {
            return this.debugString();
         }
      };
   }

   public static <E extends net.minecraft.world.entity.LivingEntity> OneShot<E> sequence(Trigger<? super E> $$0, Trigger<? super E> $$1) {
      return create($$2 -> $$2.group($$2.ifTriggered($$0)).apply($$2, $$1xx -> $$1::trigger));
   }

   public static <E extends net.minecraft.world.entity.LivingEntity> OneShot<E> triggerIf(Predicate<E> $$0, OneShot<? super E> $$1) {
      return sequence(triggerIf($$0), $$1);
   }

   public static <E extends net.minecraft.world.entity.LivingEntity> OneShot<E> triggerIf(Predicate<E> $$0) {
      return create($$1 -> $$1.point(($$1x, $$2, $$3) -> $$0.test($$2)));
   }

   public static <E extends net.minecraft.world.entity.LivingEntity> OneShot<E> triggerIf(BiPredicate<ServerLevel, E> $$0) {
      return create($$1 -> $$1.point(($$1x, $$2, $$3) -> $$0.test($$1x, $$2)));
   }

   static <E extends net.minecraft.world.entity.LivingEntity, M> BehaviorBuilder.TriggerWithResult<E, M> get(App<BehaviorBuilder.Mu<E>, M> $$0) {
      return unbox($$0).trigger;
   }

   BehaviorBuilder(BehaviorBuilder.TriggerWithResult<E, M> $$0) {
      this.trigger = $$0;
   }

   static <E extends net.minecraft.world.entity.LivingEntity, M> BehaviorBuilder<E, M> create(BehaviorBuilder.TriggerWithResult<E, M> $$0) {
      return new BehaviorBuilder<>($$0);
   }

   static final class Constant<E extends net.minecraft.world.entity.LivingEntity, A> extends BehaviorBuilder<E, A> {
      Constant(A $$0) {
         this($$0, () -> "C[" + $$0 + "]");
      }

      Constant(final A $$0, final Supplier<String> $$1) {
         super(new BehaviorBuilder.TriggerWithResult<E, A>() {
            @Override
            public A tryTrigger(ServerLevel $$0x, E $$1x, long $$2) {
               return $$0;
            }

            @Override
            public String debugString() {
               return $$1.get();
            }

            @Override
            public String toString() {
               return this.debugString();
            }
         });
      }
   }

   public static final class Instance<E extends net.minecraft.world.entity.LivingEntity>
      implements Applicative<BehaviorBuilder.Mu<E>, BehaviorBuilder.Instance.Mu<E>> {
      public <Value> Optional<Value> tryGet(MemoryAccessor<com.mojang.datafixers.kinds.OptionalBox.Mu, Value> $$0) {
         return OptionalBox.unbox($$0.value());
      }

      public <Value> Value get(MemoryAccessor<com.mojang.datafixers.kinds.IdF.Mu, Value> $$0) {
         return (Value)IdF.get($$0.value());
      }

      public <Value> BehaviorBuilder<E, MemoryAccessor<com.mojang.datafixers.kinds.OptionalBox.Mu, Value>> registered(MemoryModuleType<Value> $$0) {
         return new BehaviorBuilder.PureMemory<>(new MemoryCondition.Registered<>($$0));
      }

      public <Value> BehaviorBuilder<E, MemoryAccessor<com.mojang.datafixers.kinds.IdF.Mu, Value>> present(MemoryModuleType<Value> $$0) {
         return new BehaviorBuilder.PureMemory<>(new MemoryCondition.Present<>($$0));
      }

      public <Value> BehaviorBuilder<E, MemoryAccessor<com.mojang.datafixers.kinds.Const.Mu<Unit>, Value>> absent(MemoryModuleType<Value> $$0) {
         return new BehaviorBuilder.PureMemory<>(new MemoryCondition.Absent<>($$0));
      }

      public BehaviorBuilder<E, Unit> ifTriggered(Trigger<? super E> $$0) {
         return new BehaviorBuilder.TriggerWrapper<>($$0);
      }

      public <A> BehaviorBuilder<E, A> point(A $$0) {
         return new BehaviorBuilder.Constant<>($$0);
      }

      public <A> BehaviorBuilder<E, A> point(Supplier<String> $$0, A $$1) {
         return new BehaviorBuilder.Constant<>($$1, $$0);
      }

      public <A, R> Function<App<BehaviorBuilder.Mu<E>, A>, App<BehaviorBuilder.Mu<E>, R>> lift1(App<BehaviorBuilder.Mu<E>, Function<A, R>> $$0) {
         return $$1 -> {
            final BehaviorBuilder.TriggerWithResult<E, A> $$2 = (BehaviorBuilder.TriggerWithResult<E, A>)BehaviorBuilder.get((App<BehaviorBuilder.Mu<E>, M>)$$1);
            final BehaviorBuilder.TriggerWithResult<E, Function<A, R>> $$3 = BehaviorBuilder.get($$0);
            return BehaviorBuilder.create(new BehaviorBuilder.TriggerWithResult<E, R>() {
               @Override
               public R tryTrigger(ServerLevel $$0, E $$1x, long $$2x) {
                  A $$3x = (A)$$2.tryTrigger($$0, $$1x, $$2);
                  if ($$3x == null) {
                     return null;
                  } else {
                     Function<A, R> $$4 = (Function<A, R>)$$3.tryTrigger($$0, $$1x, $$2);
                     return (R)($$4 == null ? null : $$4.apply($$3x));
                  }
               }

               @Override
               public String debugString() {
                  return $$3.debugString() + " * " + $$2.debugString();
               }

               @Override
               public String toString() {
                  return this.debugString();
               }
            });
         };
      }

      public <T, R> BehaviorBuilder<E, R> map(final Function<? super T, ? extends R> $$0, App<BehaviorBuilder.Mu<E>, T> $$1) {
         final BehaviorBuilder.TriggerWithResult<E, T> $$2 = (BehaviorBuilder.TriggerWithResult<E, T>)BehaviorBuilder.get((App<BehaviorBuilder.Mu<E>, M>)$$1);
         return BehaviorBuilder.create(new BehaviorBuilder.TriggerWithResult<E, R>() {
            @Override
            public R tryTrigger(ServerLevel $$0x, E $$1x, long $$2x) {
               T $$3 = $$2.tryTrigger($$0, $$1x, $$2);
               return (R)($$3 == null ? null : $$0.apply($$3));
            }

            @Override
            public String debugString() {
               return $$2.debugString() + ".map[" + $$0 + "]";
            }

            @Override
            public String toString() {
               return this.debugString();
            }
         });
      }

      public <A, B, R> BehaviorBuilder<E, R> ap2(
         App<BehaviorBuilder.Mu<E>, BiFunction<A, B, R>> $$0, App<BehaviorBuilder.Mu<E>, A> $$1, App<BehaviorBuilder.Mu<E>, B> $$2
      ) {
         final BehaviorBuilder.TriggerWithResult<E, A> $$3 = (BehaviorBuilder.TriggerWithResult<E, A>)BehaviorBuilder.get((App<BehaviorBuilder.Mu<E>, M>)$$1);
         final BehaviorBuilder.TriggerWithResult<E, B> $$4 = (BehaviorBuilder.TriggerWithResult<E, B>)BehaviorBuilder.get((App<BehaviorBuilder.Mu<E>, M>)$$2);
         final BehaviorBuilder.TriggerWithResult<E, BiFunction<A, B, R>> $$5 = BehaviorBuilder.get($$0);
         return BehaviorBuilder.create(new BehaviorBuilder.TriggerWithResult<E, R>() {
            @Override
            public R tryTrigger(ServerLevel $$0, E $$1x, long $$2x) {
               A $$3x = $$3.tryTrigger($$0, $$1x, $$2x);
               if ($$3x == null) {
                  return null;
               } else {
                  B $$4x = $$4.tryTrigger($$0, $$1x, $$2x);
                  if ($$4x == null) {
                     return null;
                  } else {
                     BiFunction<A, B, R> $$5x = $$5.tryTrigger($$0, $$1x, $$2x);
                     return $$5x == null ? null : $$5x.apply($$3x, $$4x);
                  }
               }
            }

            @Override
            public String debugString() {
               return $$5.debugString() + " * " + $$3.debugString() + " * " + $$4.debugString();
            }

            @Override
            public String toString() {
               return this.debugString();
            }
         });
      }

      public <T1, T2, T3, R> BehaviorBuilder<E, R> ap3(
         App<BehaviorBuilder.Mu<E>, Function3<T1, T2, T3, R>> $$0,
         App<BehaviorBuilder.Mu<E>, T1> $$1,
         App<BehaviorBuilder.Mu<E>, T2> $$2,
         App<BehaviorBuilder.Mu<E>, T3> $$3
      ) {
         final BehaviorBuilder.TriggerWithResult<E, T1> $$4 = (BehaviorBuilder.TriggerWithResult<E, T1>)BehaviorBuilder.get((App<BehaviorBuilder.Mu<E>, M>)$$1);
         final BehaviorBuilder.TriggerWithResult<E, T2> $$5 = (BehaviorBuilder.TriggerWithResult<E, T2>)BehaviorBuilder.get((App<BehaviorBuilder.Mu<E>, M>)$$2);
         final BehaviorBuilder.TriggerWithResult<E, T3> $$6 = (BehaviorBuilder.TriggerWithResult<E, T3>)BehaviorBuilder.get((App<BehaviorBuilder.Mu<E>, M>)$$3);
         final BehaviorBuilder.TriggerWithResult<E, Function3<T1, T2, T3, R>> $$7 = BehaviorBuilder.get($$0);
         return BehaviorBuilder.create(new BehaviorBuilder.TriggerWithResult<E, R>() {
            @Override
            public R tryTrigger(ServerLevel $$0, E $$1x, long $$2x) {
               T1 $$3x = $$4.tryTrigger($$0, $$1x, $$2x);
               if ($$3x == null) {
                  return null;
               } else {
                  T2 $$4x = $$5.tryTrigger($$0, $$1x, $$2x);
                  if ($$4x == null) {
                     return null;
                  } else {
                     T3 $$5x = $$6.tryTrigger($$0, $$1x, $$2x);
                     if ($$5x == null) {
                        return null;
                     } else {
                        Function3<T1, T2, T3, R> $$6x = $$7.tryTrigger($$0, $$1x, $$2x);
                        return (R)($$6x == null ? null : $$6x.apply($$3x, $$4x, $$5x));
                     }
                  }
               }
            }

            @Override
            public String debugString() {
               return $$7.debugString() + " * " + $$4.debugString() + " * " + $$5.debugString() + " * " + $$6.debugString();
            }

            @Override
            public String toString() {
               return this.debugString();
            }
         });
      }

      public <T1, T2, T3, T4, R> BehaviorBuilder<E, R> ap4(
         App<BehaviorBuilder.Mu<E>, Function4<T1, T2, T3, T4, R>> $$0,
         App<BehaviorBuilder.Mu<E>, T1> $$1,
         App<BehaviorBuilder.Mu<E>, T2> $$2,
         App<BehaviorBuilder.Mu<E>, T3> $$3,
         App<BehaviorBuilder.Mu<E>, T4> $$4
      ) {
         final BehaviorBuilder.TriggerWithResult<E, T1> $$5 = (BehaviorBuilder.TriggerWithResult<E, T1>)BehaviorBuilder.get((App<BehaviorBuilder.Mu<E>, M>)$$1);
         final BehaviorBuilder.TriggerWithResult<E, T2> $$6 = (BehaviorBuilder.TriggerWithResult<E, T2>)BehaviorBuilder.get((App<BehaviorBuilder.Mu<E>, M>)$$2);
         final BehaviorBuilder.TriggerWithResult<E, T3> $$7 = (BehaviorBuilder.TriggerWithResult<E, T3>)BehaviorBuilder.get((App<BehaviorBuilder.Mu<E>, M>)$$3);
         final BehaviorBuilder.TriggerWithResult<E, T4> $$8 = (BehaviorBuilder.TriggerWithResult<E, T4>)BehaviorBuilder.get((App<BehaviorBuilder.Mu<E>, M>)$$4);
         final BehaviorBuilder.TriggerWithResult<E, Function4<T1, T2, T3, T4, R>> $$9 = BehaviorBuilder.get($$0);
         return BehaviorBuilder.create(new BehaviorBuilder.TriggerWithResult<E, R>() {
            @Override
            public R tryTrigger(ServerLevel $$0, E $$1x, long $$2x) {
               T1 $$3x = $$5.tryTrigger($$0, $$1x, $$2x);
               if ($$3x == null) {
                  return null;
               } else {
                  T2 $$4x = $$6.tryTrigger($$0, $$1x, $$2x);
                  if ($$4x == null) {
                     return null;
                  } else {
                     T3 $$5x = $$7.tryTrigger($$0, $$1x, $$2x);
                     if ($$5x == null) {
                        return null;
                     } else {
                        T4 $$6x = $$8.tryTrigger($$0, $$1x, $$2x);
                        if ($$6x == null) {
                           return null;
                        } else {
                           Function4<T1, T2, T3, T4, R> $$7x = $$9.tryTrigger($$0, $$1x, $$2x);
                           return (R)($$7x == null ? null : $$7x.apply($$3x, $$4x, $$5x, $$6x));
                        }
                     }
                  }
               }
            }

            @Override
            public String debugString() {
               return $$9.debugString() + " * " + $$5.debugString() + " * " + $$6.debugString() + " * " + $$7.debugString() + " * " + $$8.debugString();
            }

            @Override
            public String toString() {
               return this.debugString();
            }
         });
      }

      static final class Mu<E extends net.minecraft.world.entity.LivingEntity> implements com.mojang.datafixers.kinds.Applicative.Mu {
         private Mu() {
         }
      }
   }

   public static final class Mu<E extends net.minecraft.world.entity.LivingEntity> implements K1 {
   }

   static final class PureMemory<E extends net.minecraft.world.entity.LivingEntity, F extends K1, Value> extends BehaviorBuilder<E, MemoryAccessor<F, Value>> {
      PureMemory(final MemoryCondition<F, Value> $$0) {
         super(new BehaviorBuilder.TriggerWithResult<E, MemoryAccessor<F, Value>>() {
            
            public MemoryAccessor<F, Value> tryTrigger(ServerLevel $$0x, E $$1, long $$2) {
               Brain<?> $$3 = $$1.getBrain();
               Optional<Value> $$4 = $$3.getMemoryInternal($$0.memory());
               return $$4 == null ? null : $$0.createAccessor($$3, $$4);
            }

            @Override
            public String debugString() {
               return "M[" + $$0 + "]";
            }

            @Override
            public String toString() {
               return this.debugString();
            }
         });
      }
   }

   interface TriggerWithResult<E extends net.minecraft.world.entity.LivingEntity, R> {
      
      R tryTrigger(ServerLevel var1, E var2, long var3);

      String debugString();
   }

   static final class TriggerWrapper<E extends net.minecraft.world.entity.LivingEntity> extends BehaviorBuilder<E, Unit> {
      TriggerWrapper(final Trigger<? super E> $$0) {
         super(new BehaviorBuilder.TriggerWithResult<E, Unit>() {
            
            public Unit tryTrigger(ServerLevel $$0x, E $$1, long $$2) {
               return $$0.trigger($$0, $$1, $$2) ? Unit.INSTANCE : null;
            }

            @Override
            public String debugString() {
               return "T[" + $$0 + "]";
            }
         });
      }
   }
}
