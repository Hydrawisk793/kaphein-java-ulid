package kaphein.ulid;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThrows;

import java.util.List;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;

public class SimpleUlidGeneratorTest
{
  @Test
  public void getEpochMilliSupplier()
  {
    final SimpleUlidGenerator generator = new SimpleUlidGenerator();

    assertThat(generator.getEpochMilliSupplier(), is(not(nullValue())));
  }

  @Test
  public void getRandomGenerator()
  {
    final SimpleUlidGenerator generator = new SimpleUlidGenerator();

    assertThat(generator.getRandomGenerator(), is(not(nullValue())));
  }

  @Test
  public void generateWithOddParameters()
  {
    final SimpleUlidGenerator generator = new SimpleUlidGenerator();

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
  public void generateMany()
  {
    final int count = 20;
    final SimpleUlidGenerator generator = new SimpleUlidGenerator(
      new ControlledEpochMilliSupplier(0L, 4),
      new PredictableRandom(4));

    final List<Ulid> ulids = generator.generate(count);

    assertThat(ulids.size(), lessThanOrEqualTo(count));
  }

  @Test
  public void generateWithInitialTimestamp()
  {
    final SimpleUlidGenerator generator = new SimpleUlidGenerator();
    final int count = 2000;

    final long initialTimestamp = 2000L;
    final List<Ulid> ulids = generator.generate(count, initialTimestamp);

    assertThat(ulids.size(), lessThanOrEqualTo(count));
    assertThat(ulids, is(not(empty())));
    for(final Ulid ulid : ulids)
    {
      assertThat(ulid.getTimestamp(), is(initialTimestamp));
    }
  }
}
