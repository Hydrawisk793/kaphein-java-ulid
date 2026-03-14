package kaphein.ulid;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class ControlledEpochMilliSupplier implements EpochMilliSupplier
{
  public ControlledEpochMilliSupplier(long initialEpochMilli, int delay)
  {
    if(delay < 0)
    {
      throw new IllegalArgumentException("'delay' cannot be negative");
    }

    epochMilli = initialEpochMilli;

    this.delay = delay;
  }

  @Override
  public long get()
  {
    thisLock.lock();
    try
    {
      final long result = epochMilli;

      if(++count >= delay)
      {
        count = 0;

        ++epochMilli;
      }

      return result;
    }
    finally
    {
      thisLock.unlock();
    }
  }

  private final Lock thisLock = new ReentrantLock();

  private final int delay;

  private volatile long epochMilli = 0;

  private volatile int count = 0;
}
