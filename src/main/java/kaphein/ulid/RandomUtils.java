package kaphein.ulid;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

final class RandomUtils
{
  public static Random getSecureRandomInstanceIfPossible()
  {
    Random random = null;

    try
    {
      random = tryInvokeGetInstanceStrong();
    }
    catch(final NoSuchAlgorithmException e)
    {
      // Ignore the exception.
    }

    if(null == random)
    {
      try
      {
        random = SecureRandom.getInstance("SHA1PRNG");
      }
      catch(final NoSuchAlgorithmException nsae)
      {
        // Ignore the exception.
      }
    }

    if(null == random)
    {
      random = new Random();
    }

    return random;
  }

  private static SecureRandom tryInvokeGetInstanceStrong()
    throws NoSuchAlgorithmException
  {
    SecureRandom random = null;

    try
    {
      final Method getInstanceStrongMethod = SecureRandom.class
        .getMethod("getInstanceStrong");
      random = (SecureRandom)getInstanceStrongMethod.invoke(null);
    }
    catch(
      NoSuchMethodException
      | InvocationTargetException
      | IllegalArgumentException
      | IllegalAccessException e)
    {
      // Ignore the exception.
    }

    return random;
  }

  private RandomUtils()
  {
    throw new AssertionError(
      "Class " + getClass().getName() + " cannot be instantiated");
  }
}
