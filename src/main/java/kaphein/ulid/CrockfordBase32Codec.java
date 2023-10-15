package kaphein.ulid;

import java.util.Objects;

/**
 * This class is thread-safe.
 *
 * @author Hydrawisk793
 * @see https://www.crockford.com/base32.html
 */
class CrockfordBase32Codec
{
  public String encode(byte[] bytes)
  {
    Objects.requireNonNull(bytes, "'bytes' cannot be null");

    // aaaaa | aaabb | bbbbb | bcccc | ccccd | ddddd | ddeee | eeeee

    final StringBuilder builder = new StringBuilder();

    for(int len = bytes.length, src = 0; src < len;)
    {
      for(int seq = 0; src < len; ++seq)
      {
        int charIndex = 0;

        switch((seq & 0x07))
        {
        case 0:
          charIndex = ((bytes[src] & 0xF8) >>> 3);
          break;
        case 1:
          charIndex = ((bytes[src] & 0x07) << 2);

          if(++src < len)
          {
            charIndex |= ((bytes[src] & 0xC0) >>> 6);
          }
          break;
        case 2:
          charIndex = ((bytes[src] & 0x3E) >>> 1);
          break;
        case 3:
          charIndex = ((bytes[src] & 0x01) << 4);

          if(++src < len)
          {
            charIndex |= ((bytes[src] & 0xF0) >>> 4);
          }
          break;
        case 4:
          charIndex = ((bytes[src] & 0x0F) << 1);

          if(++src < len)
          {
            charIndex |= ((bytes[src] & 0x80) >>> 7);
          }
          break;
        case 5:
          charIndex = ((bytes[src] & 0x7C) >>> 2);
          break;
        case 6:
          charIndex = ((bytes[src] & 0x03) << 3);

          if(++src < len)
          {
            charIndex |= ((bytes[src] & 0xE0) >>> 5);
          }
          break;
        default:
          charIndex = (bytes[src] & 0x1F);
          ++src;
        }

        builder.append(ENCODING_CHARS.charAt(charIndex));
      }
    }

    return builder.toString();
  }

  public String encode(long value, int len)
  {
    if(len < 0)
    {
      throw new IllegalArgumentException("'len' cannot be negative");
    }
    if(len > ENCODED_LONG_MAX_LENGTH)
    {
      throw new IllegalArgumentException("'len' is too large");
    }

    final char[] chars = new char[len];
    for(long current = value; len > 0;)
    {
      --len;

      final int index = (int)(current & 0x1F);
      chars[len] = ENCODING_CHARS.charAt(index);
      current = (current - index) >>> 5;
    }

    return new String(chars);
  }

  public byte[] decodeAsByteArray(String str)
  {
    Objects.requireNonNull(str, "'str' cannot be null");

    // 1111 1222 | 2233 3334 | 4444 5555 | 5666 6677 | 7778 8888

    final String upperCased = str.toUpperCase();

    final int strLen = str.length();
    final int byteLen = ((strLen >>> 3) * 5) + BYTE_LEN_INC[strLen & 0x07];
    final byte[] bytes = new byte[byteLen];

    for(int dest = 0, src = 0; dest < byteLen;)
    {
      for(int seq = 0; dest < byteLen; ++seq, ++src)
      {
        final int currentIndex = findIndexOf(upperCased, src);

        // 1111 1222 | 2233 3334 | 4444 5555 | 5666 6677 | 7778 8888

        switch((seq & 0x07))
        {
        case 0:
          bytes[dest] = (byte)((currentIndex & 0x1F) << 3);
          break;
        case 1:
          bytes[dest] |= (byte)((currentIndex & 0x1C) >>> 2);

          if(++dest < byteLen)
          {
            bytes[dest] = (byte)((currentIndex & 0x03) << 6);
          }
          break;
        case 2:
          bytes[dest] |= (byte)((currentIndex & 0x1F) << 1);
          break;
        case 3:
          bytes[dest] |= (byte)((currentIndex & 0x10) >>> 4);

          if(++dest < byteLen)
          {
            bytes[dest] = (byte)((currentIndex & 0x0F) << 4);
          }
          break;
        case 4:
          bytes[dest] |= (byte)((currentIndex & 0x1E) >>> 1);

          if(++dest < byteLen)
          {
            bytes[dest] = (byte)((currentIndex & 0x01) << 7);
          }
          break;
        case 5:
          bytes[dest] |= (byte)((currentIndex & 0x1F) << 2);
          break;
        case 6:
          bytes[dest] |= (byte)((currentIndex & 0x18) >>> 3);

          if(++dest < byteLen)
          {
            bytes[dest] = (byte)((currentIndex & 0x07) << 5);
          }
          break;
        default:
          bytes[dest] |= (byte)(currentIndex & 0x1F);
          ++dest;
        }
      }
    }

    return bytes;
  }

  public long decodeAsLong(String str)
  {
    Objects.requireNonNull(str, "'str' cannot be null");

    final int len = str.length();
    if(len > ENCODED_LONG_MAX_LENGTH)
    {
      throw new IllegalArgumentException("The length of 'str' is too long");
    }

    final String upperCased = str.toUpperCase();

    long decodedValue = 0L;
    for(int i = 0; i < len; ++i)
    {
      // 1111 1222 | 2233 3334 | 4444 5555 | 5666 6677 | 7778 8888

      final int currentIndex = findIndexOf(upperCased, i);
      decodedValue <<= 5;
      decodedValue += currentIndex;
    }

    return decodedValue;
  }

  private static final String ENCODING_CHARS = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";

  private static final int ENCODED_LONG_MAX_LENGTH = 13;

  private static final int[] DECODING_INDICIES = {
    -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1,
    0, 1, 2, 3, 4, 5, 6, 7,
    8, 9, -1, -1, -1, -1, -1, -1,
    -1, 10, 11, 12, 13, 14, 15, 16,
    17, -1, 18, 19, -1, 20, 21, -1,
    22, 23, 24, 25, 26, -1, 27, 28,
    29, 30, 31
  };

  private static int[] BYTE_LEN_INC = {
    0, 0, 1, 1, 2, 3, 3, 4
  };

  private static int findIndexOf(String str, int i)
  {
    final int codePoint = str.codePointAt(i);
    if(codePoint >= DECODING_INDICIES.length)
    {
      throw new IllegalArgumentException(
        "'" + str.charAt(i) + "' is not a valid character");
    }
    final int currentIndex = DECODING_INDICIES[codePoint];
    if(currentIndex < 0)
    {
      throw new IllegalArgumentException(
        "'" + str.charAt(i) + "' is not a valid character");
    }

    return currentIndex;
  }
}
