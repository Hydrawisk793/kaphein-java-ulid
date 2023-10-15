package kaphein.ulid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * <p>
 * A monotonic ULID generator.
 * </p>
 * <p>
 * Instances of this class are thread-safe if supplied
 * {@link EpochMilliSupplier} and {@link Random} are thread-safe.
 * </p>
 *
 * @author Hydrawisk793
 */
public class MonotonicUlidGenerator extends AbstractUlidGenerator
{
  /**
   * Constructs an instance of {@link MonotonicUlidGenerator}.
   */
  public MonotonicUlidGenerator()
  {
    this(null, null);
  }

  /**
   * Constructs an instance of {@link MonotonicUlidGenerator}.
   *
   * @param epochMilliSupplier A epoch milli supplier. If {@code null}, the
   * default one that uses {@link System#currentTimeMillis()} is selected.
   * @param rng A random generator. If {@code null}, the default random
   * generator is selected.
   */
  public MonotonicUlidGenerator(
    EpochMilliSupplier epochMilliSupplier,
    Random rng
  )
  {
    super(epochMilliSupplier, rng);

    thisLock = new Object();
    lastTimestamp = 0L;
    lastRandomnessMsv = 0L;
    lastRandomnessLsv = 0L;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Ulid> generate(int count)
  {
    final int c = count;

    return ThreadUtils
      .callAndReinterruptIfNeeded(
        new InterruptableCallable<List<Ulid>>()
        {
          @Override
          public List<Ulid> call()
            throws InterruptedException
          {
            return generateImpl(c, null, false);
          }
        },
        Collections.<Ulid>emptyList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Ulid> generate(int count, long timestamp)
  {
    final int c = count;
    final long t = timestamp;

    return ThreadUtils
      .callAndReinterruptIfNeeded(
        new InterruptableCallable<List<Ulid>>()
        {
          @Override
          public List<Ulid> call()
            throws InterruptedException
          {
            return generateImpl(c, t, false);
          }
        }, Collections.<Ulid>emptyList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Ulid> generateExact(int count)
    throws InterruptedException
  {
    return generateImpl(count, null, true);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Ulid> generateExact(int count, long timestamp)
    throws InterruptedException
  {
    return generateImpl(count, timestamp, true);
  }

  private static List<Ulid> createUlidList(int count)
  {
    return new LinkedList<>();
  }

  private static List<Ulid> toImmutableArrayList(List<Ulid> ulids)
  {
    return Collections.unmodifiableList(new ArrayList<>(ulids));
  }

  private List<Ulid> generateImpl(
    int count,
    Long timestamp,
    boolean waitForNextTimestampWhenOverflow
  )
    throws InterruptedException
  {
    if(count < 0)
    {
      throw new IllegalArgumentException("'count' cannot be negative");
    }

    if(null != timestamp && timestamp < Ulid.TIMESTAMP_MIN_VALUE)
    {
      throw new IllegalArgumentException(
        "'timestamp' cannot be lower than " + Ulid.TIMESTAMP_MIN_VALUE);
    }

    final List<Ulid> ulids = createUlidList(count);

    if(count > 0)
    {
      generateAndAdd(ulids, count, timestamp, waitForNextTimestampWhenOverflow);
    }

    return toImmutableArrayList(ulids);
  }

  private void generateAndAdd(
    List<Ulid> ulids,
    int count,
    Long timestamp,
    boolean waitForNextTimestampWhenOverflow
  )
    throws InterruptedException
  {
    final EpochMilliSupplier epochMilliSupplier = getEpochMilliSupplier();
    long now = (null == timestamp ? epochMilliSupplier.get() : timestamp);
    long timestampToUse;
    final long[] randomness = new long[2];
    final long[] maxRandomness = new long[2];
    boolean canGenerate = true;

    while(canGenerate && count > 0)
    {
      boolean shouldGenerate = true;

      synchronized(thisLock)
      {
        boolean initializingRandomness = true;

        do
        {
          if(!lastRandomnessInitialized)
          {
            final long[] initialRandomness = generateRandomness();
            lastRandomnessMsv = initialRandomness[0];
            lastRandomnessLsv = initialRandomness[1];

            lastRandomnessInitialized = true;
          }

          timestampToUse = lastTimestamp;
          randomness[0] = lastRandomnessMsv;
          randomness[1] = lastRandomnessLsv;
          maxRandomness[0] = lastRandomnessMsv;
          maxRandomness[1] = lastRandomnessLsv;

          if(now > timestampToUse)
          {
            timestampToUse = now;

            final long[] newRandomness = generateRandomness();
            randomness[0] = newRandomness[0];
            randomness[1] = newRandomness[1];
            maxRandomness[0] = newRandomness[0];
            maxRandomness[1] = newRandomness[1];

            initializingRandomness = false;
          }
          else if(
            Ulid.RANDOMNESS_MAX_VALUE_MS_BITS == maxRandomness[0]
              && Ulid.RANDOMNESS_MAX_VALUE_LS_BITS == maxRandomness[1]
          )
          {
            if(waitForNextTimestampWhenOverflow)
            {
              Thread.sleep(1);
              now = epochMilliSupplier.get();
            }
            else
            {
              // Cannot generate ULIDs anymore with the current timestamp.
              canGenerate = false;
              shouldGenerate = false;
              initializingRandomness = false;
            }
          }
          else
          {
            initializingRandomness = false;
          }
        }
        while(initializingRandomness);

        if(addRandomness(maxRandomness, count))
        {
          maxRandomness[0] = Ulid.RANDOMNESS_MAX_VALUE_MS_BITS;
          maxRandomness[1] = Ulid.RANDOMNESS_MAX_VALUE_LS_BITS;
        }

        lastTimestamp = timestampToUse;
        lastRandomnessMsv = maxRandomness[0];
        lastRandomnessLsv = maxRandomness[1];
      }

      while(shouldGenerate && count > 0)
      {
        ulids.add(Ulid.from(timestampToUse, randomness));

        if(--count > 0)
        {
          if(incrementRandomness(randomness))
          {
            shouldGenerate = false;
          }
        }
      }
    }
  }

  private final Object thisLock;

  private volatile long lastTimestamp;

  private volatile long lastRandomnessMsv;

  private volatile long lastRandomnessLsv;

  private volatile boolean lastRandomnessInitialized;
}
