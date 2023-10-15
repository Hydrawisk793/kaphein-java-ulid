package kaphein.ulid;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * A base class for {@link UlidGenerator} implementation classes.
 *
 * @author Hydrawisk793
 */
public abstract class AbstractUlidGenerator implements UlidGenerator
{
  /**
   * Constructs an instance of {@link AbstractUlidGenerator}.
   *
   * @param epochMilliSupplier A epoch milli supplier. If {@code null}, the
   * default one that uses {@link System#currentTimeMillis()} is selected.
   * @param rng A random generator. If {@code null}, the default random
   * generator is selected.
   */
  protected AbstractUlidGenerator(
    EpochMilliSupplier epochMilliSupplier,
    Random rng
  )
  {
    this.epochMilliSupplier = (null == epochMilliSupplier
      ? DEFAULT_EPOCH_MILLI_SUPPLIER
      : epochMilliSupplier);
    this.rng = (null == rng ? DEFAULT_RNG : rng);
  }

  /**
   * Gets the epoch milli supplier.
   *
   * @return The epoch milli supplier.
   */
  public EpochMilliSupplier getEpochMilliSupplier()
  {
    return epochMilliSupplier;
  }

  /**
   * Gets the random generator.
   *
   * @return The random generator.
   */
  public Random getRandomGenerator()
  {
    return rng;
  }

  /**
   * <p>
   * Generates the exact number of ULIDs exact number of times.
   * </p>
   * <p>
   * The generator tries to call {@link Thread#sleep} to wait for the next
   * timestamp and an {@link InterruptedException} may be thrown if an interrupt
   * occurs.
   * </p>
   * <p>
   * If the generator cannot generate ULIDs anymore because of any other
   * reasons, an exception will be thrown.
   * </p>
   *
   * @param count The desired number of generated ULIDs.
   * @return A list of generated ULIDs.
   * @throws InterruptedException If the generator tries to call
   * {@link Thread#sleep} to wait for the next timestamp and the method call is
   * failed because of an interrupt.
   */
  public abstract List<Ulid> generateExact(int count)
    throws InterruptedException;

  /**
   * <p>
   * Generates the exact number of ULIDs exact number of times with an initial
   * timestamp.
   * </p>
   * <p>
   * The generator tries to call {@link Thread#sleep} to wait for the next
   * timestamp and an {@link InterruptedException} may be thrown if an interrupt
   * occurs.
   * </p>
   * <p>
   * If the generator cannot generate ULIDs anymore because of any other
   * reasons, an exception will be thrown.
   * </p>
   *
   * @param count The desired number of generated ULIDs.
   * @param timestamp The initial timestamp.
   * @return A list of generated ULIDs.
   * @throws InterruptedException If the generator tries to call
   * {@link Thread#sleep} to wait for the next timestamp and the method call is
   * failed because of an interrupt.
   */
  public abstract List<Ulid> generateExact(int count, long timestamp)
    throws InterruptedException;

  /**
   * Generates a 80-bit randomness.
   *
   * @return a 80-bit randomness.<br>
   * The first element is the most significant 16 bits and the second element is
   * the least significant 64 bits.
   */
  protected long[] generateRandomness()
  {
    Objects.requireNonNull(rng, "'rng' cannot be null");

    return new long[] {
      (rng.nextLong() & Ulid.RANDOMNESS_MAX_VALUE_MS_BITS),
      rng.nextLong()
    };
  }

  /**
   * Adds a 31-bit unsigned integer to a 80-bit unsigned randomness.
   *
   * @param randomnessInOut A 80-bit unsigned randomness to be added.
   * @param addend A 31-bit unsigned integer to add.
   * @return {@code true} if overflow, {@code false} otherwise.
   */
  protected boolean addRandomness(long[] randomnessInOut, int addend)
  {
    Objects.requireNonNull(randomnessInOut, "'randomnessInOut' cannot be null");
    if(randomnessInOut.length < 2)
    {
      throw new IllegalArgumentException(
        "The size of 'randomnessInOut' must be 2");
    }

    if(addend < 0)
    {
      throw new IllegalArgumentException("'addend' cannot be negative");
    }

    long msBits = (randomnessInOut[0] & Ulid.RANDOMNESS_MAX_VALUE_MS_BITS);
    long lsBitsHigh = (randomnessInOut[1] & 0xFFFFFFFF00000000L) >>> 32;
    long lsBitsLow = (randomnessInOut[1] & 0x00000000FFFFFFFFL);

    lsBitsLow += addend;
    lsBitsHigh += ((lsBitsLow & 0xFFFFFFFF00000000L) >>> 32);
    msBits += ((lsBitsHigh & 0xFFFFFFFF00000000L) >>> 32);

    randomnessInOut[0] = msBits;
    randomnessInOut[1] = ((lsBitsHigh & 0x00000000FFFFFFFFL) << 32)
      | (lsBitsLow & 0x00000000FFFFFFFFL);

    return 0L != (msBits & (~Ulid.RANDOMNESS_MAX_VALUE_MS_BITS));
  }

  /**
   * Increments a 80-bit unsigned randomness.
   *
   * @param randomnessInOut A 80-bit unsigned randomness to be incremented.
   * @return {@code true} if overflow, {@code false} otherwise.
   */
  protected boolean incrementRandomness(long[] randomnessInOut)
  {
    Objects.requireNonNull(randomnessInOut, "'randomnessInOut' cannot be null");
    if(randomnessInOut.length < 2)
    {
      throw new IllegalArgumentException(
        "The size of 'randomnessInOut' must be 2");
    }

    long msBits = randomnessInOut[0];
    long lsBits = randomnessInOut[1];

    if(0L == ++lsBits)
    {
      ++msBits;
    }

    randomnessInOut[0] = msBits;
    randomnessInOut[1] = lsBits;

    return 0L != (msBits & (~Ulid.RANDOMNESS_MAX_VALUE_MS_BITS));
  }

  private static final EpochMilliSupplier DEFAULT_EPOCH_MILLI_SUPPLIER = new EpochMilliSupplier()
  {
    @Override
    public long get()
    {
      return System.currentTimeMillis();
    }
  };

  private static final Random DEFAULT_RNG = RandomUtils
    .getSecureRandomInstanceIfPossible();

  private final EpochMilliSupplier epochMilliSupplier;

  private final Random rng;
}
