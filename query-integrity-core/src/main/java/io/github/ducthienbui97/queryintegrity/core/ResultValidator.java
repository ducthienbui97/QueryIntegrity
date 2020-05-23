package io.github.ducthienbui97.queryintegrity.core;

import lombok.NonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class used to validate the result of different queries on system under test.
 * Validator is the class used to send result to the system under test and compare the result.
 *
 * @param <R> class of result used for the system under test.
 */
public interface ResultValidator<R> {
    /**
     * Check if result of 2 queries are equal
     *
     * @param result1 the first result
     * @param result2 the second result
     * @return true if 2 results are equal
     */
    default public boolean isEquals(@NonNull Collection<R> result1, @NonNull Collection<R> result2) {
        return result1.equals(result2);
    }

    ;

    /**
     * Check if result of 2 queries are intersected,
     * e.g: there is an nonempty result that is subset of the result of both queries.
     *
     * @param result1 the first result
     * @param result2 the second result
     * @return true if 2 results share an non empty subset.
     */
    default public boolean isIntersected(@NonNull Collection<R> result1, @NonNull Collection<R> result2) {
        Set<R> resultSet1 = new HashSet<>(result1);
        resultSet1.retainAll(result2);
        return !resultSet1.isEmpty();
    }

    /**
     * Check if result of 1 result is the subset of other
     *
     * @param result1 the result could be subset
     * @param result2 the result could be superset
     * @return true if result1 is the subset of result2
     */
    default public boolean isSubset(@NonNull Collection<R> result1, @NonNull Collection<R> result2) {
        return result2.containsAll(result1);
    }
}
