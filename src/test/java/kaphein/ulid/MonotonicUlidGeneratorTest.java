package kaphein.ulid;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThrows;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;

public class MonotonicUlidGeneratorTest
{
  @Test
  public void getEpochMilliSupplier()
  {
    final MonotonicUlidGenerator generator = new MonotonicUlidGenerator();

    assertThat(generator.getEpochMilliSupplier(), is(not(nullValue())));
  }

  @Test
  public void getRandomGenerator()
  {
    final MonotonicUlidGenerator generator = new MonotonicUlidGenerator();

    assertThat(generator.getRandomGenerator(), is(not(nullValue())));
  }

  @Test
  public void generateWithOddParameters()
  {
    final MonotonicUlidGenerator generator = new MonotonicUlidGenerator();

    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        generator.generate(-1);
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        generator.generate(-1, 0L);
      }
    });
    assertThat(generator.generate(0), is(empty()));
    assertThat(generator.generate(0, 0L), is(empty()));
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        generator.generate(0, -1L);
      }
    });
    assertThat(generator.generate(0), is(empty()));
    assertThat(generator.generate(0, 0L), is(empty()));
  }

  @Test
  public void generateOneTwoTimes()
  {
    final MonotonicUlidGenerator generator = new MonotonicUlidGenerator();
    final int count = 2000;

    for(int i = 0; i < count; ++i)
    {
      final List<Ulid> first = generator.generate(1);
      final List<Ulid> second = generator.generate(1);
      final Ulid firstUlid = first.get(0);
      final Ulid secondUlid = second.get(0);
      final BigInteger firstRandomness = new BigInteger(
        firstUlid.getRandomnessAsByteArray());
      final BigInteger secondRandomness = new BigInteger(
        secondUlid.getRandomnessAsByteArray());

      if(firstUlid.getTimestamp() == secondUlid.getTimestamp())
      {
        assertThat(secondRandomness, greaterThan(firstRandomness));
        assertThat(
          secondRandomness.subtract(firstRandomness).intValue(),
          is(1));
      }
      else
      {
        assertThat(secondUlid, greaterThan(firstUlid));
      }
    }
  }

  @Test
  public void generateExactTwo()
    throws InterruptedException
  {
    final MonotonicUlidGenerator generator = new MonotonicUlidGenerator();
    final int count = 2000;

    for(int i = 0; i < count; ++i)
    {
      final List<Ulid> twoUlids = generator.generateExact(2);

      assertThat(twoUlids, hasSize(2));

      final Ulid firstUlid = twoUlids.get(0);
      final Ulid secondUlid = twoUlids.get(1);
      final BigInteger firstRandomness = new BigInteger(
        firstUlid.getRandomnessAsByteArray());
      final BigInteger secondRandomness = new BigInteger(
        secondUlid.getRandomnessAsByteArray());

      if(firstUlid.getTimestamp() == secondUlid.getTimestamp())
      {
        assertThat(secondRandomness, greaterThan(firstRandomness));
        assertThat(
          secondRandomness.subtract(firstRandomness).intValue(),
          is(1));
      }
      else
      {
        assertThat(secondUlid, greaterThan(firstUlid));
      }
    }
  }

  @Test
  public void generateWithLoweredTimestamp()
  {
    final MonotonicUlidGenerator generator = new MonotonicUlidGenerator(
      new ControlledEpochMilliSupplier(1000L, 3),
      new PredictableRandom(4));

    final Ulid firstUlid = generator.generate(1).get(0);
    final Ulid secondUlid = generator.generate(1, 900L).get(0);

    assertThat(secondUlid.getTimestamp(), is(firstUlid.getTimestamp()));
    testUlidOrder(Arrays.asList(firstUlid, secondUlid));
  }

  @Test
  public void generateMany()
  {
    final MonotonicUlidGenerator generator = new MonotonicUlidGenerator(
      new ControlledEpochMilliSupplier(0L, 3),
      new PredictableRandom(4));
    final int count = 2000;

    final List<Ulid> ulids = generator.generate(count);

    assertThat(ulids.size(), lessThan(count));
    Long firstTimestamp = null;
    for(final Ulid ulid : ulids)
    {
      if(null == firstTimestamp)
      {
        firstTimestamp = ulid.getTimestamp();
      }
      else
      {
        assertThat(ulid.getTimestamp(), is(firstTimestamp));
      }
    }
    testUlidOrder(ulids);
  }

  @Test
  public void generateManyWhenOverflow()
    throws InterruptedException
  {
    final MonotonicUlidGenerator generator = new MonotonicUlidGenerator(
      new ControlledEpochMilliSupplier(0L, 2),
      new PredictableRandom(3));
    final int count = 5;

    final List<Ulid> ulids = generator.generate(count);

    assertThat(ulids.size(), lessThan(count));
    testUlidOrder(ulids);

    final List<Ulid> ulids2 = generator.generate(count);

    assertThat(ulids2, empty());
    testUlidOrder(ulids2);

    final List<Ulid> ulids3 = generator.generate(count);

    assertThat(ulids, not(empty()));
    testUlidOrder(ulids3);
  }

  @Test
  public void generateExactMany()
    throws InterruptedException
  {
    final MonotonicUlidGenerator generator = new MonotonicUlidGenerator(
      new ControlledEpochMilliSupplier(0L, 1),
      new PredictableRandom(4));
    final int count = 2000;

    final List<Ulid> ulids = generator.generateExact(count);

    assertThat(ulids, hasSize(count));
    testUlidOrder(ulids);
  }

  @Test
  public void generateExactManyWithTimestamp()
    throws InterruptedException
  {
    final MonotonicUlidGenerator generator = new MonotonicUlidGenerator(
      new ControlledEpochMilliSupplier(1000L, 3),
      new PredictableRandom(4));
    final int count = 2000;
    final long initialTimestamp = 900L;

    final List<Ulid> ulids = generator.generateExact(count, initialTimestamp);

    assertThat(ulids, hasSize(count));
    testUlidOrder(ulids);
    for(final Ulid ulid : ulids)
    {
      assertThat(ulid.getTimestamp(), greaterThanOrEqualTo(initialTimestamp));
    }
  }

  private static void testUlidOrder(List<Ulid> ulids)
  {
    Ulid prevUlid = null;
    for(int i = 0; i < ulids.size(); ++i)
    {
      final Ulid ulid = ulids.get(i);

      if(null != prevUlid)
      {
        assertThat(prevUlid, lessThan(ulid));
        assertThat(ulid, greaterThan(prevUlid));

        if(prevUlid.getTimestamp() == ulid.getTimestamp())
        {
          final BigInteger randomness = new BigInteger(
            ulid.getRandomnessAsByteArray());
          final BigInteger prevRandomness = new BigInteger(
            prevUlid.getRandomnessAsByteArray());
          assertThat(randomness, greaterThan(prevRandomness));
          assertThat(randomness.subtract(prevRandomness).intValue(), is(1));

          prevUlid = ulid;
        }
        else
        {
          assertThat(ulid, greaterThan(prevUlid));

          prevUlid = null;
        }
      }
      else
      {
        prevUlid = ulid;
      }
    }
  }
}
