package kaphein.ulid;

import java.util.List;

/**
 * <p>
 * An interface for ULID generator classes.
 * </p>
 * <p>
 * It is strongly recommended that implementation classes are thread-safe.
 * </p>
 *
 * @author Hydrawisk793
 */
public interface UlidGenerator
{
  /**
   * <p>
   * Generates ULIDs.
   * </p>
   * <p>
   * Depending on implementation, the actual number of generated ULIDs may be
   * less than {@code count}.
   * </p>
   *
   * @param count The desired number of generated ULIDs.
   * @return A list of generated ULIDs.
   */
  List<Ulid> generate(int count);

  /**
   * <p>
   * Generates ULIDs with an initial timestamp.
   * </p>
   * <p>
   * Depending on implementation, the actual number of generated ULIDs may be
   * less than {@code count}.
   * </p>
   *
   * @param count The desired number of generated ULIDs.
   * @param timestamp The initial timestamp.
   * @return A list of generated ULIDs.
   */
  List<Ulid> generate(int count, long timestamp);
}
