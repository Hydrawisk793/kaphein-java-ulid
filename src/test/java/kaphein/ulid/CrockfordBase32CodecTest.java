package kaphein.ulid;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.threeten.bp.OffsetDateTime;

public class CrockfordBase32CodecTest
{
  private static final String[][] FIXTURES = {
    {
      "Foobar",
      "8SQPYRK1E8"
    },
    {
      "_______",
      "BXFNYQTZBXFG"
    },
    {
      "You shouldn't have done that.",
      "B5QQA83KD1QQAV34DRKQ8838C5V6A834DXQ6A83MD1GQ8BG"
    },
    {
      "You just activated my trap card.",
      "B5QQA83AENSQ8831CDT6JXK1EHJP883DF4G78WK1E0G66RBJCGQ0"
    },
    {
      "You are too slow... Want to try again?",
      "B5QQA831E9JJ0X3FDWG76V3FEWQ2WBH0AXGPWX10EHQJ0X3JF4G62SV1D5Q3Y"
    },
    {
      "The sights of hell bring its viewers back in.",
      "AHM6A83KD5KPGX3K41QPC838CNP6R832E9MPWSS0D5T7683PD5JQESBJECG64RB3DCG6JVHE"
    }
  };

  @Test
  public void encode()
  {
    final CrockfordBase32Codec codec = new CrockfordBase32Codec();

    assertThrows(NullPointerException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        codec.encode(null);
      }
    });

    for(int i = 0; i < FIXTURES.length; ++i)
    {
      final String[] fixture = FIXTURES[i];

      final String encoded = codec.encode(fixture[0].getBytes());

      assertThat(encoded, is(fixture[1]));
    }

    assertThat(
      codec.encode(new byte[] {
        (byte)0x73, (byte)0x4c,
        (byte)0xb1, (byte)0x50, (byte)0x95, (byte)0x95,
        (byte)0xf6, (byte)0x4c, (byte)0x1b, (byte)0xc6
      }),
      is("ED6B2M4NJQV4R6Y6"));
  }

  @Test
  public void encodeLong()
  {
    final CrockfordBase32Codec codec = new CrockfordBase32Codec();

    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        codec.encode(0L, -1);
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        codec.encode(0L, 14);
      }
    });

    assertThat(codec.encode(0xFFFFFFFFFFFFFFFFL, 13), is("FZZZZZZZZZZZZ"));

    final long timestamp = OffsetDateTime
      .parse("2023-09-18T17:48:00+09:00")
      .toInstant()
      .toEpochMilli();

    assertThat(codec.encode(timestamp, 10), is("01HAKQK7G0"));
  }

  @Test
  public void decodeAsString()
  {
    final CrockfordBase32Codec codec = new CrockfordBase32Codec();

    assertThrows(NullPointerException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        codec.decodeAsByteArray(null);
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        codec.decodeAsByteArray("ILOU");
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        codec.decodeAsByteArray("{}");
      }
    });
    for(int i = 0; i < FIXTURES.length; ++i)
    {
      final String[] fixture = FIXTURES[i];

      final byte[] decoded = codec.decodeAsByteArray(fixture[1]);

      assertThat(new String(decoded), is(fixture[0]));
    }
  }

  @Test
  public void decodeAsLong()
  {
    final CrockfordBase32Codec codec = new CrockfordBase32Codec();

    assertThrows(NullPointerException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        codec.decodeAsLong(null);
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        codec.decodeAsLong("ILOUILOUIL");
      }
    });
    assertThrows(IllegalArgumentException.class, new ThrowingRunnable()
    {
      @Override
      public void run()
        throws Throwable
      {
        codec.decodeAsLong("ZZZZZZZZZZZZZZ");
      }
    });

    assertThat(codec.decodeAsLong(""), is(0L));

    assertThat(codec.decodeAsLong("01HB3RTS5R"), is(1695565046968L));
  }
}
