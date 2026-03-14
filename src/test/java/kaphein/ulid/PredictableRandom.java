package kaphein.ulid;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    thisLock.lock();
    try
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
    finally
    {
      thisLock.unlock();
    }
  }

  private static final long serialVersionUID = 3718340320917784630L;

  private final Lock thisLock = new ReentrantLock();

  private volatile long randomnessMsBits;

  private volatile boolean msBitsLsBitsFlag = false;
}
