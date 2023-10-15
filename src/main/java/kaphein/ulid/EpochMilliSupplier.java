package kaphein.ulid;

/**
 * <p>
 * An interface for a supplier that supplies a value of milliseconds from
 * {@code 1970-01-01T00:00:00Z}.
 * </p>
 * <p>
 * It is strongly recommended that implementation classes are thread-safe.
 * </p>
 *
 * @author Hydrawisk793
 */
public interface EpochMilliSupplier
{
  /**
   * <p>
   * Gets a current value of milliseconds from {@code 1970-01-01T00:00:00Z}.
   * </p>
   * <p>
   * If no value is available, an exception will be thrown.
   * </p>
   * <p>
   * Usually, the result should be same as the return value of
   * {@link System#currentTimeMillis()}. But implementations may return
   * different values depending on their requirements.
   * </p>
   *
   * @return A value of milliseconds from {@code 1970-01-01T00:00:00Z}.
   */
  long get();
}
