package kaphein.ulid;

import java.util.Random;

class PredictableRandom extends Random
{
  public PredictableRandom(int bias)
  {
    if(bias < 0)
    {
      throw new IllegalArgumentException("'bias' cannot be negative");
    }

    randomnessMsBits = (0xFFFFFFFF00000000L | (0x00000000FFFFFFFFL - bias));
  }

  @Override
  public long nextLong()
  {
    synchronized(thisLock)
    {
      long result;

      if(msBitsLsBitsFlag)
      {
        result = randomnessMsBits;
      }
      else
      {
        result = Ulid.RANDOMNESS_MAX_VALUE_MS_BITS;
      }

      msBitsLsBitsFlag = !msBitsLsBitsFlag;

      return result;
    }
  }

  private static final long serialVersionUID = 3718340320917784630L;

  private final Object thisLock = new Object();

  private volatile long randomnessMsBits;

  private volatile boolean msBitsLsBitsFlag = false;
}
