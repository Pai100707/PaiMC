package net.minecraft.util.parsing.packrat;


public abstract class CachedParseState<S> implements ParseState<S> {
   private CachedParseState.PositionCache[] positionCache = new CachedParseState.PositionCache[256];
   private final ErrorCollector<S> errorCollector;
   private final Scope scope = new Scope();
   private CachedParseState.SimpleControl[] controlCache = new CachedParseState.SimpleControl[16];
   private int nextControlToReturn;
   private final CachedParseState<S>.Silent silent = new CachedParseState.Silent();

   protected CachedParseState(ErrorCollector<S> $$0) {
      this.errorCollector = $$0;
   }

   @Override
   public Scope scope() {
      return this.scope;
   }

   @Override
   public ErrorCollector<S> errorCollector() {
      return this.errorCollector;
   }

   
   @Override
   public <T> T parse(NamedRule<S, T> $$0) {
      int $$1 = this.mark();
      CachedParseState.PositionCache $$2 = this.getCacheForPosition($$1);
      int $$3 = $$2.findKeyIndex($$0.name());
      if ($$3 != -1) {
         CachedParseState.CacheEntry<T> $$4 = $$2.getValue($$3);
         if ($$4 != null) {
            if ($$4 == CachedParseState.CacheEntry.NEGATIVE) {
               return null;
            }

            this.restore($$4.markAfterParse);
            return $$4.value;
         }
      } else {
         $$3 = $$2.allocateNewEntry($$0.name());
      }

      T $$5 = $$0.value().parse(this);
      CachedParseState.CacheEntry<T> $$6;
      if ($$5 == null) {
         $$6 = CachedParseState.CacheEntry.negativeEntry();
      } else {
         int $$7 = this.mark();
         $$6 = new CachedParseState.CacheEntry<>($$5, $$7);
      }

      $$2.setValue($$3, $$6);
      return $$5;
   }

   private CachedParseState.PositionCache getCacheForPosition(int $$0) {
      int $$1 = this.positionCache.length;
      if ($$0 >= $$1) {
         int $$2 = net.minecraft.util.Util.growByHalf($$1, $$0 + 1);
         CachedParseState.PositionCache[] $$3 = new CachedParseState.PositionCache[$$2];
         System.arraycopy(this.positionCache, 0, $$3, 0, $$1);
         this.positionCache = $$3;
      }

      CachedParseState.PositionCache $$4 = this.positionCache[$$0];
      if ($$4 == null) {
         $$4 = new CachedParseState.PositionCache();
         this.positionCache[$$0] = $$4;
      }

      return $$4;
   }

   @Override
   public Control acquireControl() {
      int $$0 = this.controlCache.length;
      if (this.nextControlToReturn >= $$0) {
         int $$1 = net.minecraft.util.Util.growByHalf($$0, this.nextControlToReturn + 1);
         CachedParseState.SimpleControl[] $$2 = new CachedParseState.SimpleControl[$$1];
         System.arraycopy(this.controlCache, 0, $$2, 0, $$0);
         this.controlCache = $$2;
      }

      int $$3 = this.nextControlToReturn++;
      CachedParseState.SimpleControl $$4 = this.controlCache[$$3];
      if ($$4 == null) {
         $$4 = new CachedParseState.SimpleControl();
         this.controlCache[$$3] = $$4;
      } else {
         $$4.reset();
      }

      return $$4;
   }

   @Override
   public void releaseControl() {
      this.nextControlToReturn--;
   }

   @Override
   public ParseState<S> silent() {
      return this.silent;
   }

   record CacheEntry<T>(T value, int markAfterParse) {
      public static final CachedParseState.CacheEntry<?> NEGATIVE = new CachedParseState.CacheEntry(null, -1);

      public static <T> CachedParseState.CacheEntry<T> negativeEntry() {
         return (CachedParseState.CacheEntry<T>)NEGATIVE;
      }
   }

   static class PositionCache {
      public static final int ENTRY_STRIDE = 2;
      private static final int NOT_FOUND = -1;
      private Object[] atomCache = new Object[16];
      private int nextKey;

      public int findKeyIndex(Atom<?> $$0) {
         for (int $$1 = 0; $$1 < this.nextKey; $$1 += 2) {
            if (this.atomCache[$$1] == $$0) {
               return $$1;
            }
         }

         return -1;
      }

      public int allocateNewEntry(Atom<?> $$0) {
         int $$1 = this.nextKey;
         this.nextKey += 2;
         int $$2 = $$1 + 1;
         int $$3 = this.atomCache.length;
         if ($$2 >= $$3) {
            int $$4 = net.minecraft.util.Util.growByHalf($$3, $$2 + 1);
            Object[] $$5 = new Object[$$4];
            System.arraycopy(this.atomCache, 0, $$5, 0, $$3);
            this.atomCache = $$5;
         }

         this.atomCache[$$1] = $$0;
         return $$1;
      }

      
      public <T> CachedParseState.CacheEntry<T> getValue(int $$0) {
         return (CachedParseState.CacheEntry<T>)this.atomCache[$$0 + 1];
      }

      public void setValue(int $$0, CachedParseState.CacheEntry<?> $$1) {
         this.atomCache[$$0 + 1] = $$1;
      }
   }

   class Silent implements ParseState<S> {
      private final ErrorCollector<S> silentCollector = new ErrorCollector.Nop<>();

      @Override
      public ErrorCollector<S> errorCollector() {
         return this.silentCollector;
      }

      @Override
      public Scope scope() {
         return CachedParseState.this.scope();
      }

      
      @Override
      public <T> T parse(NamedRule<S, T> $$0) {
         return CachedParseState.this.parse($$0);
      }

      @Override
      public S input() {
         return CachedParseState.this.input();
      }

      @Override
      public int mark() {
         return CachedParseState.this.mark();
      }

      @Override
      public void restore(int $$0) {
         CachedParseState.this.restore($$0);
      }

      @Override
      public Control acquireControl() {
         return CachedParseState.this.acquireControl();
      }

      @Override
      public void releaseControl() {
         CachedParseState.this.releaseControl();
      }

      @Override
      public ParseState<S> silent() {
         return this;
      }
   }

   static class SimpleControl implements Control {
      private boolean hasCut;

      @Override
      public void cut() {
         this.hasCut = true;
      }

      @Override
      public boolean hasCut() {
         return this.hasCut;
      }

      public void reset() {
         this.hasCut = false;
      }
   }
}
