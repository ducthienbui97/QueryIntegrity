/**
 * Query Factory create query for a query test.
 *
 * @param <T> class of query used to the system under test.
 */
public interface QueryFactory<T> {
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
}
