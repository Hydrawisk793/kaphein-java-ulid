package kaphein.ulid;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.threeten.bp.OffsetDateTime;

public class UlidTest
{
  @Test
  public void noArgsConstructor()
  {
    assertThat(new Ulid(), is(Ulid.MIN_VALUE));
  }

  @Test
  public void copyConstructor()
  {
    assertThrows(NullPointerException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        new Ulid((Ulid)null);
      }
    });

    final Ulid ulid = Ulid.MAX_VALUE;

    assertAreEqual(ulid, new Ulid(ulid));
  }

  @Test
  public void fromTimestampAndRandomnessLongArray()
  {
    assertThrows(NullPointerException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        Ulid.from(0L, (long[])null);
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        Ulid.from(-1L, RANDOMNESS);
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        Ulid.from(
          0L,
          new long[] {0L});
      }
    });

    final Ulid ulid = Ulid.from(
      TIMESTAMP,
      RANDOMNESS);

    assertThat(ulid.getTimestamp(), is(TIMESTAMP));
    assertThat(ulid.getRandomnessAsLongArray(), is(RANDOMNESS));
    assertThat(ulid.toString(), startsWith(TIMESTAMP_ENCODED_TEXT));
  }

  @Test
  public void fromTimestampAndRandomnessByteArray()
  {
    assertThrows(NullPointerException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        Ulid.from(0L, (byte[])null, 0);
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        Ulid.from(-1L, RANDOMNESS_BYTES, 0);
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        Ulid.from(0L, new byte[] {(byte)0xFF}, 0);
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        Ulid.from(0L, RANDOMNESS_BYTES, -1);
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        Ulid.from(
          0L,
          RANDOMNESS_BYTES,
          RANDOMNESS_BYTES.length);
      }
    });

    final Ulid ulid = Ulid.from(
      TIMESTAMP,
      RANDOMNESS_BYTES,
      0);

    assertThat(ulid.getTimestamp(), is(TIMESTAMP));
    assertThat(ulid.getRandomnessAsByteArray(), is(RANDOMNESS_BYTES));
    assertThat(ulid.toString(), startsWith(TIMESTAMP_ENCODED_TEXT));
  }

  @Test
  public void fromByteArray()
  {
    assertThrows(NullPointerException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        Ulid.from((byte[])null, 0);
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        Ulid.from(new byte[] {(byte)0x00}, 0);
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        Ulid.from(ULID_BYTES, -1);
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        Ulid.from(ULID_BYTES, ULID_BYTES.length);
      }
    });

    final Ulid ulid = Ulid.from(ULID_BYTES, 0);

    assertThat(ulid, is(Ulid.from(
      TIMESTAMP,
      RANDOMNESS_BYTES,
      0)));
    assertThat(ulid.toByteArray(), is(ULID_BYTES));
  }

  @Test
  public void parse()
  {
    assertThrows(NullPointerException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        Ulid.parse((String)null);
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        Ulid.parse("");
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        Ulid.parse("0101010101");
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        Ulid.parse("010101010101010101010101010101010101010101");
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        Ulid.parse("01HB3RTWZ3HYE0L2TXVDP8TCW0");
      }
    });

    final Ulid ulid = Ulid.parse(ULID_ENCODED_TEXT);

    assertThat(ulid.getTimestamp(), is(TIMESTAMP));
    assertThat(
      ulid.getRandomnessAsByteArray(),
      is(RANDOMNESS_BYTES));
    assertThat(ulid.toString(), is(ULID_ENCODED_TEXT));
  }

  @Test
  public void compareTo()
  {
    final Ulid ulid = Ulid.parse(ULID_ENCODED_TEXT);

    assertThrows(NullPointerException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        ulid.compareTo(null);
      }
    });
    testCompareTo(ulid, Ulid.parse(ULID_ENCODED_TEXT), 0);
    testCompareTo(ulid, Ulid.parse("01HAKQK7G1PKCTV8M94TEF1FHK"), -1);
    testCompareTo(ulid, Ulid.parse("01HAKQK7FZPKCTV8M94TEF1FHK"), 1);
    testCompareTo(ulid, Ulid.parse("01HAKQK7G0PKCTV8M94TEF1FHM"), -1);
    testCompareTo(ulid, Ulid.parse("01HAKQK7G0PKCTV8M94TEF1FHJ"), 1);
    testCompareTo(ulid, Ulid.parse("01HAKQK7G0QKCTV8M94TEF1FHK"), -1);
    testCompareTo(ulid, Ulid.parse("01HAKQK7G0NKCTV8M94TEF1FHK"), 1);
  }

  @SuppressWarnings("unlikely-arg-type")
  @Test
  public void equalsAndHashCode()
  {
    final Ulid ulid = Ulid.parse(ULID_ENCODED_TEXT);

    assertThat(ulid.equals(null), is(false));
    assertThat(ulid.equals(0), is(false));
    assertThat(ulid.equals(ulid), is(true));

    testEqualsAndHashCode(ulid, Ulid.parse(ULID_ENCODED_TEXT), true);

    testEqualsAndHashCode(
      ulid,
      Ulid.parse("01HAKQK7G0QKCTV8M94TEF1FHK"),
      false);

    testEqualsAndHashCode(
      ulid,
      Ulid.parse("01HAKQK7G0PKCTV8M94TEF1FHM"),
      false);

    testEqualsAndHashCode(
      ulid,
      Ulid.from(
        ulid.getTimestamp(),
        Ulid
          .parse("01HAKQK7G0PKCTV8M94TEF1FHM")
          .getRandomnessAsLongArray()),
      false);

    testEqualsAndHashCode(
      ulid,
      Ulid.from(
        ulid.getTimestamp() - 1,
        ulid.getRandomnessAsLongArray()),
      false);
  }

  @Test
  public void toStringMethod()
  {
    final Ulid ulid1 = Ulid.from(
      TIMESTAMP,
      RANDOMNESS_BYTES,
      0);

    assertThat(ulid1.toString().length(), is(Ulid.ENCODED_LENGTH));

    final Ulid ulid2 = Ulid.parse(ULID_ENCODED_TEXT);

    assertThat(ulid2.toString(), is(ULID_ENCODED_TEXT));
    assertThat(ulid2, is(Ulid.parse(ULID_ENCODED_TEXT)));

    final Ulid ulid3 = Ulid.parse(ULID_ENCODED_TEXT);

    final String first = ulid3.toString();
    final String second = ulid3.toString();

    assertThat(first, is(second));
  }

  @Test
  public void toByteArray()
  {
    final Ulid ulid = Ulid.from(TIMESTAMP, RANDOMNESS);

    assertThat(ulid.toByteArray(), is(ULID_BYTES));

    final byte[] bytes1 = new byte[16];
    final byte[] bytes11 = ulid.toByteArray(bytes1);

    assertThrows(NullPointerException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        ulid.toByteArray(null);
      }
    });
    assertThat(bytes11, sameInstance(bytes1));
    assertThat(bytes1, is(ULID_BYTES));

    final byte[] bytes2 = new byte[32];
    final byte[] bytes22 = ulid.toByteArray(bytes2, 4);

    assertThrows(NullPointerException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        ulid.toByteArray(null, 0);
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        ulid.toByteArray(bytes2, -1);
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        ulid.toByteArray(bytes2, bytes2.length);
      }
    });
    assertThat(bytes22, sameInstance(bytes2));
    assertThat(
      Collections.indexOfSubList(
        Arrays.asList(ArrayUtils.toObject(bytes2)),
        Arrays.asList(ArrayUtils.toObject(ULID_BYTES))),
      is(4));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void writeAndReadBack()
    throws IOException,
    ClassNotFoundException
  {
    final Ulid ulid = Ulid.from(
      TIMESTAMP,
      RANDOMNESS_BYTES,
      0);

    byte[] serialized = null;
    try(
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos))
    {
      oos.writeObject(ulid);
      serialized = baos.toByteArray();
    }

    Object readObject = null;
    try(
      ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
      ObjectInputStream ois = new ObjectInputStream(bais))
    {
      readObject = ois.readObject();
    }

    assertThat((Class<Ulid>)readObject.getClass(), sameInstance(Ulid.class));

    final Ulid readUlid = (Ulid)readObject;

    assertThat(readUlid, is(ulid));
  }

  private static final long TIMESTAMP = OffsetDateTime
    .parse("2023-09-18T17:48:00+09:00")
    .toInstant()
    .toEpochMilli();

  private static final String TIMESTAMP_ENCODED_TEXT = "01HAKQK7G0";

  private static final long[] RANDOMNESS = {
    0x000000000000B4D9L,
    0xADA289269CF0BE33L
  };

  private static final byte[] RANDOMNESS_BYTES = {
    (byte)0xB4, (byte)0xD9, (byte)0xAD, (byte)0xA2,
    (byte)0x89, (byte)0x26, (byte)0x9C, (byte)0xF0,
    (byte)0xBE, (byte)0x33
  };

  private static final byte[] ULID_BYTES = {
    (byte)0x01, (byte)0x8A, (byte)0xA7, (byte)0x79,
    (byte)0x9E, (byte)0x00,
    (byte)0xB4, (byte)0xD9, (byte)0xAD, (byte)0xA2,
    (byte)0x89, (byte)0x26, (byte)0x9C, (byte)0xF0,
    (byte)0xBE, (byte)0x33
  };

  private static final String ULID_ENCODED_TEXT = "01HAKQK7G0PKCTV8M94TEF1FHK";

  private static void assertAreEqual(Ulid l, Ulid r)
  {
    assertThat(l.getTimestamp(), is(r.getTimestamp()));
    assertThat(
      l.getRandomnessAsLongArray(),
      is(r.getRandomnessAsLongArray()));
    assertThat(
      l.getRandomnessAsByteArray(),
      is(r.getRandomnessAsByteArray()));
    assertThat(
      l.getMostSignificantBits(),
      is(r.getMostSignificantBits()));
    assertThat(
      l.getLeastSignificantBits(),
      is(r.getLeastSignificantBits()));
    assertThat(l, is(r));
    assertThat(r, is(l));
    assertThat(l, comparesEqualTo(r));
    assertThat(r, comparesEqualTo(l));
  }

  private static void testEqualsAndHashCode(
    Ulid l,
    Ulid r,
    boolean expected
  )
  {
    assertThat(l.equals(l), is(true));
    assertThat(r.equals(r), is(true));

    assertThat(l.hashCode(), is(l.hashCode()));
    assertThat(r.hashCode(), is(r.hashCode()));

    if(expected)
    {
      assertThat(l.equals(r), is(true));
      assertThat(r.equals(l), is(true));
      assertThat(l.hashCode(), is(r.hashCode()));
    }
    else
    {
      assertThat(l.equals(r), is(false));
      assertThat(r.equals(l), is(false));
    }
  }

  private static void testCompareTo(
    Ulid l,
    Ulid r,
    int expected
  )
  {
    if(expected < 0)
    {
      assertThat(l, lessThan(r));
      assertThat(r, greaterThan(l));
    }
    else if(expected > 0)
    {
      assertThat(l, greaterThan(r));
      assertThat(r, lessThan(l));
    }
    else
    {
      assertThat(l, comparesEqualTo(r));
      assertThat(r, comparesEqualTo(l));
    }
  }
}
