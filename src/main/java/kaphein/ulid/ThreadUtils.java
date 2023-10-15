package kaphein.ulid;

import java.util.Objects;

final class ThreadUtils
{
  public static <R> R callAndReinterruptIfNeeded(
    InterruptableCallable<R> callable,
    R returnValueWhenInterrupted
  )
  {
    Objects.requireNonNull(callable, "'callable' cannot be null");

    R result = returnValueWhenInterrupted;

    try
    {
      result = callable.call();
    }
    catch(final InterruptedException ie)
    {
      Thread.currentThread().interrupt();
    }

    return result;
  }

  private ThreadUtils()
  {
    throw new AssertionError(
      "Class " + getClass().getName() + " cannot be instantiated");
  }
}
