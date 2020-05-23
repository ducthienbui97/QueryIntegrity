package io.github.ducthienbui97.queryintegration.core;

import java.util.Collection;

/**
 * Query Factory create query for a query test.
 *
 * @param <T> class of query used to the system under test.
 * @param <R> class of result used for the system under test.
 */
public interface QueryFactory<T, R> {
    /**
     * Build criteria for querying/searching system under test.
     *
     * @return a random generated query for the system under test.
     */
    public T build();

    /**
     * Build criteria for querying/searching system under test from a {@link QueryProxy}
     *
     * @param queryProxy the proxy query that need to be converted to
     *                   native system under test's query.
     * @return query for system under test.
     */
    public T build(QueryProxy<T> queryProxy);

    /**
     * Convert query to result.
     *
     * @param query the query to be used to get the result.
     * @return the result of input query.
     */
    public Collection<R> getResult(T query);

    /**
     * Convert result or query to readable string
     *
     * @param resultOrQuery a result or query needed to be converted.
     * @return human readable string of input object.
     */
    public default String toString(Object resultOrQuery) {
        return resultOrQuery.toString();
    }
}
