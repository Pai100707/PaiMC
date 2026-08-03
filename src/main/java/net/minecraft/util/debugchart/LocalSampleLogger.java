package net.minecraft.util.debugchart;

public class LocalSampleLogger extends AbstractSampleLogger implements SampleStorage {
   public static final int CAPACITY = 240;
   private final long[][] samples;
   private int start;
   private int size;

   public LocalSampleLogger(int $$0) {
      this($$0, new long[$$0]);
   }

   public LocalSampleLogger(int $$0, long[] $$1) {
      super($$0, $$1);
      this.samples = new long[240][$$0];
   }

   @Override
   protected void useSample() {
      int $$0 = this.wrapIndex(this.start + this.size);
      System.arraycopy(this.sample, 0, this.samples[$$0], 0, this.sample.length);
      if (this.size < 240) {
         this.size++;
      } else {
         this.start = this.wrapIndex(this.start + 1);
      }
   }

   @Override
   public int capacity() {
      return this.samples.length;
   }

   @Override
   public int size() {
      return this.size;
   }

   @Override
   public long get(int $$0) {
      return this.get($$0, 0);
   }

   @Override
   public long get(int $$0, int $$1) {
      if ($$0 >= 0 && $$0 < this.size) {
         long[] $$2 = this.samples[this.wrapIndex(this.start + $$0)];
         if ($$1 >= 0 && $$1 < $$2.length) {
            return $$2[$$1];
         } else {
            throw new IndexOutOfBoundsException($$1 + " out of bounds for dimensions " + $$2.length);
         }
      } else {
         throw new IndexOutOfBoundsException($$0 + " out of bounds for length " + this.size);
      }
   }

   private int wrapIndex(int $$0) {
      return $$0 % 240;
   }

   @Override
   public void reset() {
      this.start = 0;
      this.size = 0;
   }
}
