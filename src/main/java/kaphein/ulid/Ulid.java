package kaphein.ulid;

import java.io.Serializable;
import java.util.Objects;

/**
 * An implementation of ULID.
 *
 * @author Hydrawisk793
 * @see <a href="https://github.com/ulid/spec">ULID specification</a>
 */
public class Ulid implements Serializable, Comparable<Ulid>
{
  /**
   * The minimum value of timestamp allowed in ULIDs.
   */
  public static final long TIMESTAMP_MIN_VALUE = 0L;

  /**
   * The maximum value of timestamp allowed in ULIDs.
   */
  public static final long TIMESTAMP_MAX_VALUE = 0x0000FFFFFFFFFFFFL;

  /**
   * The length of Crockford Base32-encoded timestamp text.
   */
  public static final int TIMESTAMP_ENCODED_LENGTH = 10;

  /**
   * The most significant 16 bits of the minimum randomness value allowed in
   * ULIDs.
   */
  public static final long RANDOMNESS_MIN_VALUE_MS_BITS = 0x0000000000000000L;

  /**
   * The least significant 64 bits of the minimum randomness value allowed in
   * ULIDs.
   */
  public static final long RANDOMNESS_MIN_VALUE_LS_BITS = 0x0000000000000000L;

  /**
   * The most significant 16 bits of the maximum randomness value allowed in
   * ULIDs.
   */
  public static final long RANDOMNESS_MAX_VALUE_MS_BITS = 0x000000000000FFFFL;

  /**
   * The least significant 64 bits of the maximum randomness value allowed in
   * ULIDs.
   */
  public static final long RANDOMNESS_MAX_VALUE_LS_BITS = 0xFFFFFFFFFFFFFFFFL;

  /**
   * The length of Crockford Base32-encoded randomess text.
   */
  public static final int RANDOMNESS_ENCODED_LENGTH = 16;

  /**
   * The number of bytes used to represent an ULID.
   */
  public static final int BYTES = 16;

  /**
   * The length of a Crockford Base32-encoded ULID.
   */
  public static final int ENCODED_LENGTH = 26;

  /**
   * The minimum ULID.
   */
  public static final Ulid MIN_VALUE = new Ulid(0L, 0L);

  /**
   * The maximum ULID.
   */
  public static final Ulid MAX_VALUE = new Ulid(
    0xFFFFFFFFFFFFFFFFL,
    0xFFFFFFFFFFFFFFFFL);

  /**
   * Constructs an ULID with a timestamp and a randomness.
   *
   * @param timestamp A timestamp in milliseconds.
   * @param randomness An {@code long} array with two elements that represents a
   * randomness for an ULID.<br>
   * The first element is the most significant 16 bits of the randomness and the
   * second element is the least significant 64 bits of the randomness.
   * @return An ULID.
   * @throws NullPointerException If {@code randomness} is {@code null}.
   * @throws IllegalArgumentException If {@code timestamp} is out of range or
   * the size of {@code randomness} is not 2.
   */
  public static Ulid from(
    long timestamp,
    long[] randomness
  )
  {
    final long[] values = new long[2];
    setTimestamp(values, timestamp);
    setRandomness(values, randomness);

    return new Ulid(values[0], values[1]);
  }

  /**
   * Constructs an ULID with a timestamp and a randomness.
   *
   * @param timestamp A timestamp in milliseconds.
   * @param randomness A byte array that represents a randomness for an ULID.
   * @param randomnessOffset An offset of {@code randomness} where the content
   * starts from.
   * @return An ULID.
   * @throws NullPointerException If {@code randomness} is {@code null}.
   * @throws IllegalArgumentException If {@code timestamp} is out of range,
   * {@code randomness} does not have enough bytes or {@code randomnessOffset}
   * is out of range.
   */
  public static Ulid from(
    long timestamp,
    byte[] randomness,
    int randomnessOffset
  )
  {
    final long[] values = new long[2];
    setTimestamp(values, timestamp);
    setRandomness(values, randomness, randomnessOffset);

    return new Ulid(values[0], values[1]);
  }

  /**
   * Constructs an ULID with a byte array.
   *
   * @param bytes An byte array that represents an ULID.
   * @param offset An offset of {@code bytes} where the content starts from.
   * @return An ULID.
   * @throws NullPointerException If {@code bytes} is {@code null}.
   * @throws IllegalArgumentException If {@code offset} is out of range or
   * {@code bytes} does not have enough bytes.
   */
  public static Ulid from(
    byte[] bytes,
    int offset
  )
  {
    final long[] values = new long[2];
    setTimestamp(values, bytes, offset);
    setRandomness(values, bytes, offset + TIMESTAMP_BYTE_COUNT);

    return new Ulid(values[0], values[1]);
  }

  /**
   * Parses a encoded string that represents an ULID.
   *
   * @param text A Crockford Base32-encoded text.
   * @return An ULID.
   * @throws NullPointerException If {@code text} is {@code null}.
   * @throws IllegalArgumentException If {@code text} is empty, contains
   * insufficient or exceeded number of characters or contains invalid
   * characters.
   */
  public static Ulid parse(String text)
  {
    Objects.requireNonNull(text, "'text' cannot be null");
    if(ENCODED_LENGTH != text.length())
    {
      throw new IllegalArgumentException(
        "The length of 'text' does not match the expected length");
    }

    final long[] values = new long[2];
    setTimestamp(
      values,
      CODEC.decodeAsLong(text.substring(0, TIMESTAMP_ENCODED_LENGTH)));
    setRandomness(
      values,
      CODEC.decodeAsByteArray(text.substring(TIMESTAMP_ENCODED_LENGTH)),
      0);

    return new Ulid(values[0], values[1]);
  }

  /**
   * Constructs an ULID instance with no arguments.<br>
   * The instance is equal to {@link Ulid#MIN_VALUE}.
   */
  public Ulid()
  {
    this(0L, 0L);
  }

  /**
   * Copy-constructs an ULID with specified source.
   *
   * @param src The source ULID to be copied.
   * @throws NullPointerException If {@code src} is {@code null}.
   */
  public Ulid(Ulid src)
  {
    this(src.mostSigBits, src.leastSigBits);
  }

  /**
   * Constructs an ULID with most significant 64 bits and least significant 64
   * bits.
   *
   * @param mostSigBits The most significant 64 bits.
   * @param leastSigBits The least significant 64 bits.
   */
  protected Ulid(long mostSigBits, long leastSigBits)
  {
    this.mostSigBits = mostSigBits;
    this.leastSigBits = leastSigBits;
  }

  /**
   * Gets the most significant 64 bits of the ULID.
   *
   * @return The most significant 64 bits of the ULID.
   */
  public long getMostSignificantBits()
  {
    return mostSigBits;
  }

  /**
   * Gets the least significant 64 bits of the ULID.
   *
   * @return The least significant 64 bits of the ULID.
   */
  public long getLeastSignificantBits()
  {
    return leastSigBits;
  }

  /**
   * Gets the timestamp of the ULID.
   *
   * @return The timestamp of the ULID.
   */
  public long getTimestamp()
  {
    return (mostSigBits & TIMESTAMP_BIT_MASK) >>> 16;
  }

  /**
   * Gets the randomness of the ULID as a byte array.
   *
   * @return The randomness of the ULID.
   */
  public byte[] getRandomnessAsByteArray()
  {
    final byte[] bytes = new byte[RANDOMNESS_BYTE_COUNT];
    bytes[0] = (byte)((mostSigBits & 0x000000000000FF00L) >>> 8);
    bytes[1] = (byte)(mostSigBits & 0x00000000000000FFL);
    bytes[2] = (byte)((leastSigBits & 0xFF00000000000000L) >>> 56);
    bytes[3] = (byte)((leastSigBits & 0x00FF000000000000L) >>> 48);
    bytes[4] = (byte)((leastSigBits & 0x0000FF0000000000L) >>> 40);
    bytes[5] = (byte)((leastSigBits & 0x000000FF00000000L) >>> 32);
    bytes[6] = (byte)((leastSigBits & 0x00000000FF000000L) >>> 24);
    bytes[7] = (byte)((leastSigBits & 0x0000000000FF0000L) >>> 16);
    bytes[8] = (byte)((leastSigBits & 0x000000000000FF00L) >>> 8);
    bytes[9] = (byte)(leastSigBits & 0x00000000000000FFL);

    return bytes;
  }

  /**
   * Gets the randomness of the ULID as a long array.
   *
   * @return The randomness of the ULID.
   */
  public long[] getRandomnessAsLongArray()
  {
    return new long[] {
      (mostSigBits & RANDOMNESS_MAX_VALUE_MS_BITS),
      leastSigBits
    };
  }

  /**
   * Compares this ULID with the specified ULID for order. Returns a negative
   * integer, zero, or a positive integer as this ULID is less than, equal to,
   * or greater than the specified ULID.
   *
   * @param other The ULID to be compared.
   * @return a negative integer, zero, or a positive integer as this ULID is
   * less than, equal to, or greater than the specified ULID.
   * @throws NullPointerException If {@code other} is {@code null}.
   */
  @Override
  public int compareTo(Ulid other)
  {
    Objects.requireNonNull(other, "'other' cannot be null");

    int result = 0;

    final long mslvDiff = mostSigBits - other.mostSigBits;
    if(0L != mslvDiff)
    {
      result = (mslvDiff < 0L ? -1 : 1);
    }
    else
    {
      final long lslvDiff = leastSigBits - other.leastSigBits;
      if(0L != lslvDiff)
      {
        result = (lslvDiff < 0L ? -1 : 1);
      }
    }

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj)
  {
    boolean result = this == obj;

    if(!result)
    {
      result = obj instanceof Ulid;
      if(result)
      {
        final Ulid other = (Ulid)obj;

        result = mostSigBits == other.mostSigBits
          && leastSigBits == other.leastSigBits;
      }
    }

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int)(leastSigBits ^ (leastSigBits >>> 32));
    result = prime * result + (int)(mostSigBits ^ (mostSigBits >>> 32));

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    return CODEC.encode(getTimestamp(), TIMESTAMP_ENCODED_LENGTH)
      + CODEC.encode(getRandomnessAsByteArray());
  }

  /**
   * Converts this ULID to a byte array.
   *
   * @return An byte array that represents the ULID.
   */
  public byte[] toByteArray()
  {
    return toByteArray(new byte[BYTES], 0);
  }

  /**
   * Converts this ULID to a byte array and copies the content into specified
   * destination.
   *
   * @param bytes The destination where the content is copied into.
   * @return The reference of {@code bytes}.
   * @throws NullPointerException If {@code bytes} is {@code null}.
   * @throws IllegalArgumentException If {@code offset} is negative or out of
   * bounds or {@code bytes} does not have enough bytes.
   */
  public byte[] toByteArray(byte[] bytes)
  {
    return toByteArray(bytes, 0);
  }

  /**
   * Converts this ULID to a byte array and copies the content into specified
   * destination.
   *
   * @param bytes The destination where the content are copied into.
   * @param offset An offset of {@code bytes} where the copy is started from.
   * @return The reference of {@code bytes}.
   * @throws NullPointerException If {@code bytes} is {@code null}.
   * @throws IllegalArgumentException If {@code offset} is negative or out of
   * bounds or {@code bytes} does not have enough bytes.
   */
  public byte[] toByteArray(byte[] bytes, int offset)
  {
    throwIfInvalid(bytes, "bytes", offset, "offset", BYTES);

    bytes[offset] = (byte)((mostSigBits & 0xFF00000000000000L) >>> 56);
    bytes[++offset] = (byte)((mostSigBits & 0x00FF000000000000L) >>> 48);
    bytes[++offset] = (byte)((mostSigBits & 0x0000FF0000000000L) >>> 40);
    bytes[++offset] = (byte)((mostSigBits & 0x000000FF00000000L) >>> 32);
    bytes[++offset] = (byte)((mostSigBits & 0x00000000FF000000L) >>> 24);
    bytes[++offset] = (byte)((mostSigBits & 0x0000000000FF0000L) >>> 16);
    bytes[++offset] = (byte)((mostSigBits & 0x000000000000FF00L) >>> 8);
    bytes[++offset] = (byte)(mostSigBits & 0x00000000000000FFL);
    bytes[++offset] = (byte)((leastSigBits & 0xFF00000000000000L) >>> 56);
    bytes[++offset] = (byte)((leastSigBits & 0x00FF000000000000L) >>> 48);
    bytes[++offset] = (byte)((leastSigBits & 0x0000FF0000000000L) >>> 40);
    bytes[++offset] = (byte)((leastSigBits & 0x000000FF00000000L) >>> 32);
    bytes[++offset] = (byte)((leastSigBits & 0x00000000FF000000L) >>> 24);
    bytes[++offset] = (byte)((leastSigBits & 0x0000000000FF0000L) >>> 16);
    bytes[++offset] = (byte)((leastSigBits & 0x000000000000FF00L) >>> 8);
    bytes[++offset] = (byte)(leastSigBits & 0x00000000000000FFL);

    return bytes;
  }

  private static final long serialVersionUID = 296607666948453597L;

  private static final int TIMESTAMP_BYTE_COUNT = 6;

  private static final long TIMESTAMP_BIT_MASK = 0xFFFFFFFFFFFF0000L;

  private static final int RANDOMNESS_BYTE_COUNT = 10;

  private static final CrockfordBase32Codec CODEC = new CrockfordBase32Codec();

  private static void throwIfInvalid(
    byte[] bytes,
    String bytesParamName,
    int offset,
    String offsetParamName,
    int minimumRequiredSize
  )
  {
    if(null == bytes)
    {
      throw new NullPointerException("'" + bytesParamName + "' cannot be null");
    }

    if(offset < 0)
    {
      throw new IllegalArgumentException(
        "'" + offsetParamName + "' cannot be negative");
    }

    if(bytes.length < minimumRequiredSize + offset)
    {
      throw new IllegalArgumentException(
        "'" + bytesParamName + "' does not have enough bytes");
    }
  }

  private static void setTimestamp(long[] valuesOut, long timestamp)
  {
    if(timestamp < TIMESTAMP_MIN_VALUE)
    {
      throw new IllegalArgumentException(
        "The minimum timestamp value is " + TIMESTAMP_MIN_VALUE);
    }

    valuesOut[0] = (timestamp & TIMESTAMP_MAX_VALUE) << 16;
  }

  private static void setTimestamp(long[] valuesInOut, byte[] bytes, int offset)
  {
    throwIfInvalid(
      bytes, "bytes",
      offset, "offset",
      TIMESTAMP_BYTE_COUNT);

    long mslv = (valuesInOut[0] & RANDOMNESS_MAX_VALUE_MS_BITS);

    mslv |= ((bytes[offset] & 0xFFL) << 56);
    mslv |= ((bytes[++offset] & 0xFFL) << 48);
    mslv |= ((bytes[++offset] & 0xFFL) << 40);
    mslv |= ((bytes[++offset] & 0xFFL) << 32);
    mslv |= ((bytes[++offset] & 0xFFL) << 24);
    mslv |= ((bytes[++offset] & 0xFFL) << 16);

    valuesInOut[0] = mslv;
  }

  private static void setRandomness(
    long[] valuesInOut,
    long[] randomness
  )
  {
    Objects.requireNonNull(randomness, "'randomness' cannot be null");
    if(randomness.length < 2)
    {
      throw new IllegalArgumentException("The size of 'randomness' must be 2");
    }

    valuesInOut[0] = (valuesInOut[0] & TIMESTAMP_BIT_MASK)
      | (randomness[0] & RANDOMNESS_MAX_VALUE_MS_BITS);
    valuesInOut[1] = randomness[1];
  }

  private static void setRandomness(
    long[] valuesInOut,
    byte[] randomness,
    int randomnessOffset
  )
  {
    throwIfInvalid(
      randomness, "randomness",
      randomnessOffset, "randomnessOffset",
      RANDOMNESS_BYTE_COUNT);

    long mslv = (valuesInOut[0] & TIMESTAMP_BIT_MASK);
    long lslv = 0L;

    mslv |= ((randomness[randomnessOffset] & 0xFFL) << 8);
    mslv |= (randomness[++randomnessOffset] & 0xFFL);
    lslv |= ((randomness[++randomnessOffset] & 0xFFL) << 56);
    lslv |= ((randomness[++randomnessOffset] & 0xFFL) << 48);
    lslv |= ((randomness[++randomnessOffset] & 0xFFL) << 40);
    lslv |= ((randomness[++randomnessOffset] & 0xFFL) << 32);
    lslv |= ((randomness[++randomnessOffset] & 0xFFL) << 24);
    lslv |= ((randomness[++randomnessOffset] & 0xFFL) << 16);
    lslv |= ((randomness[++randomnessOffset] & 0xFFL) << 8);
    lslv |= (randomness[++randomnessOffset] & 0xFFL);

    valuesInOut[0] = mslv;
    valuesInOut[1] = lslv;
  }

  /**
   * The most significant 64 bits of the ULID.
   */
  private final long mostSigBits;

  /**
   * The least significant 64 bits of the ULID.
   */
  private final long leastSigBits;
}
