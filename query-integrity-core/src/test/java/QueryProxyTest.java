import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class QueryProxyTest {

    public static Stream<Arguments> pairQueryProvider() {
        return queryProvider().flatMap(query1 -> queryProvider().map(query2 -> Arguments.of(query1, query2)));
    }

    public static Stream<QueryProxy<String>> queryProvider() {
        return Stream.of(
                nativeQuery(),
                notQuery(),
                andQuery(),
                orQuery()
        );
    }

    private static QueryProxy<String> nativeQuery() {
        return QueryProxy.<String>builder()
                .nativeQuery("test")
                .queryType(QueryProxy.QueryType.NATIVE)
                .build();
    }

    private static QueryProxy<String> notQuery() {
        return nativeQuery().reverse();
    }

    private static QueryProxy<String> andQuery() {
        QueryProxy<String> andQuery = QueryProxy.<String>builder()
                .queryType(QueryProxy.QueryType.AND)
                .children(new ArrayList<>())
                .build();
        andQuery.getChildren().add(notQuery());
        andQuery.getChildren().add(nativeQuery());
        return andQuery;
    }

    private static QueryProxy<String> orQuery() {
        QueryProxy<String> orQuery = QueryProxy.<String>builder()
                .queryType(QueryProxy.QueryType.OR)
                .children(new ArrayList<>())
                .build();
        orQuery.getChildren().add(notQuery());
        orQuery.getChildren().add(andQuery());
        orQuery.getChildren().add(nativeQuery());
        return orQuery;
    }

    @Test
    public void testReverseNativeQueryShouldBeNOTQuery() {
        QueryProxy<String> reversedQuery = nativeQuery().reverse();
        assertThat(reversedQuery.getChildren().size(), is(1));
        assertThat(reversedQuery.getChildren(), hasItems(nativeQuery()));
        assertThat(reversedQuery.getQueryType(), is(QueryProxy.QueryType.NOT));
    }

    @Test
    public void testReverseNotQueryShouldBeItsChildQuery() {
        QueryProxy<String> reversedQuery = notQuery().reverse();
        assertThat(notQuery().getChildren().get(0), equalTo(reversedQuery));
    }

    @ParameterizedTest
    @MethodSource("queryProvider")
    public void reverseOfReverseQueryShouldBeOriginalQuery(QueryProxy<String> queryProxy) {
        assertThat(queryProxy, equalTo(queryProxy.reverse().reverse()));
    }

    @ParameterizedTest
    @MethodSource("pairQueryProvider")
    public void andQueryShouldAndOf2OriginalQuery(QueryProxy<String> query1, QueryProxy<String> query2) {
        QueryProxy<String> andQuery = query1.and(query2);
        assertThat(andQuery.getQueryType(), is(QueryProxy.QueryType.AND));
        assertThat(andQuery.getChildren().size(), is(2));
        assertThat(andQuery.getChildren(), hasItems(query2, query1));
    }

    @ParameterizedTest
    @MethodSource("pairQueryProvider")
    public void orQueryShouldAndOf2OriginalQuery(QueryProxy<String> query1, QueryProxy<String> query2) {
        QueryProxy<String> orQuery = query1.or(query2);
        assertThat(orQuery.getQueryType(), is(QueryProxy.QueryType.OR));
        assertThat(orQuery.getChildren().size(), is(2));
        assertThat(orQuery.getChildren(), hasItems(query2, query1));
    }
}
