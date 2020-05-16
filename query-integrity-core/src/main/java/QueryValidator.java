/**
 * Class used to validate the result of different queries on system under test.
 * Validator is the class used to send query to the system under test and compare the result.
 *
 * @param <T> class of query used to the system under test.
 */
public interface QueryValidator<T> {
    /**
     * Check if result of 2 queries are equal
     *
     * @param query1 the first query
     * @param query2 the second query
     * @return true if 2 results are equal
     */
    public boolean isEquals(T query1, T query2);

    /**
     * Check if result of 2 queries are intersected,
     * e.g: there is an nonempty result that is subset of the result of both queries.
     *
     * @param query1 the first query
     * @param query2 the second query
     * @return true if 2 results share an non empty subset.
     */
    public boolean isIntersected(T query1, T query2);

    /**
     * Check if result of 1 query is the subset of other
     *
     * @param query1 the query could be subset
     * @param query2 the query could be superset
     * @return true if result of query1 is the subset of result of query2
     */
    public boolean isSubset(T query1, T query2);
}
