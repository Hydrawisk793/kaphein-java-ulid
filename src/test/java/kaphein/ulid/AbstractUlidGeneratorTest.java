package kaphein.ulid;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;

public class AbstractUlidGeneratorTest
{
  @Test
  public void generateRandomness()
  {
    final AbstractUlidGeneratorStub stub = new AbstractUlidGeneratorStub();

    final long[] randomness = stub.generateRandomness();

    assertThat(randomness.length, is(2));
  }

  @Test
  public void addRandomness()
  {
    final AbstractUlidGeneratorStub stub = new AbstractUlidGeneratorStub(
      null,
      new PredictableRandom(4));

    assertThrows(NullPointerException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        stub.addRandomness(null, 0);
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        stub.addRandomness(new long[] {0L}, 0);
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        stub.addRandomness(new long[] {0L, 0L}, -1);
      }
    });

    final long[] randomness = stub.generateRandomness();
    final boolean overflowed = stub.addRandomness(randomness, 4);

    assertThat(randomness[0], is(Ulid.RANDOMNESS_MAX_VALUE_MS_BITS));
    assertThat(randomness[1], is(Ulid.RANDOMNESS_MAX_VALUE_LS_BITS));
    assertThat(overflowed, is(false));
  }

  @Test
  public void addRandomnessOverflowed()
  {
    final AbstractUlidGeneratorStub stub = new AbstractUlidGeneratorStub(
      null,
      new PredictableRandom(0));

    final long[] randomness = stub.generateRandomness();
    final boolean overflowed = stub.addRandomness(randomness, 1);

    assertThat(overflowed, is(true));
  }

  @Test
  public void incrementRandomness()
  {
    final AbstractUlidGeneratorStub stub = new AbstractUlidGeneratorStub(
      null,
      new PredictableRandom(1));

    assertThrows(NullPointerException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        stub.incrementRandomness(null);
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        stub.incrementRandomness(new long[] {0L});
      }
    });

    final long[] randomness = stub.generateRandomness();
    final boolean overflowed = stub.incrementRandomness(randomness);

    assertThat(randomness[0], is(Ulid.RANDOMNESS_MAX_VALUE_MS_BITS));
    assertThat(randomness[1], is(Ulid.RANDOMNESS_MAX_VALUE_LS_BITS));
    assertThat(overflowed, is(false));
  }

  @Test
  public void incrementRandomnessOveflowed()
  {
    final AbstractUlidGeneratorStub stub = new AbstractUlidGeneratorStub(
      null,
      new PredictableRandom(0));

    final long[] randomness = stub.generateRandomness();
    final boolean overflowed = stub.incrementRandomness(randomness);

    assertThat(overflowed, is(true));
  }

  private static class AbstractUlidGeneratorStub extends AbstractUlidGenerator
  {
    public AbstractUlidGeneratorStub()
    {
      this(null, null);
    }

    public AbstractUlidGeneratorStub(
      EpochMilliSupplier epochMilliSupplier,
      Random rng
    )
    {
      super(epochMilliSupplier, rng);
    }

    @Override
    public List<Ulid> generate(int count)
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<Ulid> generate(int count, long timestamp)
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<Ulid> generateExact(int count)
      throws InterruptedException
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<Ulid> generateExact(int count, long timestamp)
      throws InterruptedException
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public long[] generateRandomness()
    {
      return super.generateRandomness();
    }

    @Override
    public boolean addRandomness(long[] randomnessInOut, int addend)
    {
      return super.addRandomness(randomnessInOut, addend);
    }

    @Override
    public boolean incrementRandomness(long[] randomnessInOut)
    {
      return super.incrementRandomness(randomnessInOut);
    }
  }
}
