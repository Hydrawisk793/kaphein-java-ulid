package kaphein.ulid;

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
    synchronized(thisLock)
    {
      final long result = epochMilli;

      if(++count >= delay)
      {
        count = 0;

        ++epochMilli;
      }

      return result;
    }
  }

  private final Object thisLock = new Object();

  private final int delay;

  private volatile long epochMilli = 0;

  private volatile int count = 0;
}
