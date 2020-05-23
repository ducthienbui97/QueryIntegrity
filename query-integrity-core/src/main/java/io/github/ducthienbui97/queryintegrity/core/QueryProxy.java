package io.github.ducthienbui97.queryintegrity.core;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class QueryProxy<T> {
    @NonNull
    private QueryType queryType;
    private List<QueryProxy<T>> children;
    private T nativeQuery;

    /**
     * Return a new query that is the reverse of current query
     * NOTE: the current query might be changed.
     *
     * @return reversed query.
     */
    public QueryProxy<T> reverse() {
        switch (queryType) {
            case NOT:
                return this.children.get(0);
            case OR:
                return QueryProxy.<T>builder()
                        .queryType(QueryType.AND)
                        .children(getChildren().stream()
                                .map(QueryProxy::reverse)
                                .collect(Collectors.toList()))
                        .build();
            case AND:
                return QueryProxy.<T>builder()
                        .queryType(QueryType.OR)
                        .children(getChildren().stream()
                                .map(QueryProxy::reverse)
                                .collect(Collectors.toList()))
                        .build();
            default:
                return QueryProxy.<T>builder()
                        .queryType(QueryType.NOT)
                        .children(Collections.singletonList(this))
                        .build();
        }
    }

    /**
     * Return a new query that is the and of current query and other query.
     *
     * @param otherQuery the query current query will AND with.
     * @return the AND query.
     */
    public QueryProxy<T> and(QueryProxy<T> otherQuery) {
        List<QueryProxy<T>> queryProxies = new ArrayList<>();
        queryProxies.add(this);
        queryProxies.add(otherQuery);
        return QueryProxy.<T>builder()
                .queryType(QueryType.AND)
                .children(queryProxies)
                .build();
    }

    /**
     * Return a new query that is the or of current query and other query.
     *
     * @param otherQuery the query current query will OR with.
     * @return the OR query.
     */
    public QueryProxy<T> or(QueryProxy<T> otherQuery) {
        List<QueryProxy<T>> queryProxies = new ArrayList<>();
        queryProxies.add(this);
        queryProxies.add(otherQuery);
        return QueryProxy.<T>builder()
                .queryType(QueryType.OR)
                .children(queryProxies)
                .build();
    }

    public enum QueryType {
        /**
         * An OR query is a query that contains multiple children queries.
         * The result of the query should equal to union of all its children queries results.
         */
        OR,
        /**
         * An AND query is a query that contains multiple children queries.
         * The result of the query should equal to intersection of all its children queries results.
         */
        AND,
        /**
         * A NOT query is a query that contains 1 children query.
         * The result of the query should not contains anything in its child query results.
         */
        NOT,
        /**
         * A NATIVE query doesn't contain any child query.
         * But instead has a native query that can be used for system under test.
         */
        NATIVE
    }

}
