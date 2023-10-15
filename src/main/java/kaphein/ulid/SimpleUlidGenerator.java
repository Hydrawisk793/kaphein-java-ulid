package kaphein.ulid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * <p>
 * A simple ULID generator that generates ULIDs without monotonicity.
 * </p>
 * <p>
 * Instances of this class are thread-safe if supplied
 * {@link EpochMilliSupplier} and {@link Random} are thread-safe.
 * </p>
 *
 * @author Hydrawisk793
 */
public class SimpleUlidGenerator extends AbstractUlidGenerator
{
  /**
   * Constructs an instance of {@link SimpleUlidGenerator}.
   */
  public SimpleUlidGenerator()
  {
    this(null, null);
  }

  /**
   * Constructs an instance of {@link SimpleUlidGenerator}.
   *
   * @param epochMilliSupplier A epoch milli supplier. If {@code null}, the
   * default one that uses {@link System#currentTimeMillis()} is selected.
   * @param rng A random generator. If {@code null}, the default random
   * generator is selected.
   */
  public SimpleUlidGenerator(
    EpochMilliSupplier epochMilliSupplier,
    Random rng
  )
  {
    super(epochMilliSupplier, rng);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Ulid> generate(int count)
  {
    if(count < 0)
    {
      throw new IllegalArgumentException("'count' cannot be negative");
    }

    final Set<Ulid> ulids = new LinkedHashSet<>();

    if(count > 0)
    {
      generateAndAdd(ulids, count, getEpochMilliSupplier().get());
    }

    return Collections.unmodifiableList(new ArrayList<>(ulids));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Ulid> generate(int count, long timestamp)
  {
    if(count < 0)
    {
      throw new IllegalArgumentException("'count' cannot be negative");
    }

    if(timestamp < Ulid.TIMESTAMP_MIN_VALUE)
    {
      throw new IllegalArgumentException(
        "'timestamp' cannot be lower than " + Ulid.TIMESTAMP_MIN_VALUE);
    }

    final Set<Ulid> ulids = new LinkedHashSet<>();

    if(count > 0)
    {
      generateAndAdd(ulids, count, timestamp);
    }

    return Collections.unmodifiableList(new ArrayList<>(ulids));
  }

  @Override
  public List<Ulid> generateExact(int count)
    throws InterruptedException
  {
    if(count < 0)
    {
      throw new IllegalArgumentException("'count' cannot be negative");
    }

    final Set<Ulid> ulids = new LinkedHashSet<>();

    if(count > 0)
    {
      generateAndAddExactTimes(ulids, count, getEpochMilliSupplier().get());
    }

    return Collections.unmodifiableList(new ArrayList<>(ulids));
  }

  @Override
  public List<Ulid> generateExact(int count, long timestamp)
    throws InterruptedException
  {
    if(count < 0)
    {
      throw new IllegalArgumentException("'count' cannot be negative");
    }

    if(timestamp < Ulid.TIMESTAMP_MIN_VALUE)
    {
      throw new IllegalArgumentException(
        "'timestamp' cannot be lower than " + Ulid.TIMESTAMP_MIN_VALUE);
    }

    final Set<Ulid> ulids = new LinkedHashSet<>();

    if(count > 0)
    {
      generateAndAddExactTimes(ulids, count, timestamp);
    }

    return Collections.unmodifiableList(new ArrayList<>(ulids));
  }

  private void generateAndAdd(
    Set<Ulid> ulids,
    int count,
    long timestamp
  )
  {
    do
    {
      ulids.add(Ulid.from(timestamp, generateRandomness()));
    }
    while(--count > 0);
  }

  private void generateAndAddExactTimes(
    Set<Ulid> ulids,
    int count,
    long timestamp
  )
  {
    do
    {
      if(ulids.add(Ulid.from(timestamp, generateRandomness())))
      {
        --count;
      }
    }
    while(count > 0);
  }
}
