package io.github.ducthienbui97.queryintegrity.core;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;

@Slf4j
public class QueryTestingService<T, R> {
    public final int DEFAULT_TEST_COUNT = 1000;
    public final int DEFAULT_MAX_LEAF = 10;
    public final int DEFAULT_MIN_LEAF = 1;
    private final QueryFactory<T, R> queryFactory;
    private final ResultValidator<R> resultValidator;
    private final Random random = new Random();
    @Setter
    private int maxLeafCount = DEFAULT_MAX_LEAF;
    @Setter
    private int minLeafCount = DEFAULT_MIN_LEAF;

    public QueryTestingService(QueryFactory<T, R> queryFactory) {
        this(queryFactory, new ResultValidator<R>() {
        });
    }

    public QueryTestingService(QueryFactory<T, R> queryFactory, ResultValidator<R> resultValidator) {
        this.queryFactory = queryFactory;
        this.resultValidator = resultValidator;
    }

    public int runEqualTest() {
        return runEqualTest(DEFAULT_TEST_COUNT);
    }

    public int runEqualTest(int testCount) {
        log.info("Starting equal test:");
        int invalid = runTest(testCount, queryProxy -> QueryProxy.<T>builder()
                        .queryType(QueryProxy.QueryType.NOT)
                        .children(Collections.singletonList(queryProxy.reverse()))
                        .build(),
                resultValidator::isEquals,
                "Result of query {} not equal to query {}\n Expected {} equal to {}");
        log.info("Equal test: {} out of {} queries is invalid", invalid, testCount);
        return invalid;
    }

    public int runNotTest() {
        return runNotTest(DEFAULT_TEST_COUNT);
    }

    public int runNotTest(int testCount) {
        log.info("Starting not test:");
        int invalid = runTest(testCount,
                QueryProxy::reverse,
                (result1, result2) -> !resultValidator.isIntersected(result1, result2),
                "Result of query {} shares a nonempty subset with query {}\n Expected {} not intersect with {}");
        log.info("Not test: {} out of {} queries is invalid", invalid, testCount);
        return invalid;
    }

    public int runSubsetTest() {
        return runSubsetTest(DEFAULT_TEST_COUNT);
    }

    public int runSubsetTest(int testCount) {
        log.info("Starting subset test:");
        int halfTestCount = testCount / 2;
        int invalid = runTest(halfTestCount,
                queryProxy -> queryProxy.or(buildQuery()),
                resultValidator::isSubset,
                "Result of query {} is not a subset of query {}\n Expected {} is a subset of {}!");
        invalid += runTest(testCount - halfTestCount,
                queryProxy -> queryProxy.and(buildQuery()),
                (result1, result2) -> resultValidator.isSubset(result2, result1),
                "Result of query {} is not a superset of query {}\n Expected {} is a super set of {}!");
        log.info("Subset test: {} out of {} queries is invalid", invalid, testCount);
        return invalid;
    }

    private int runTest(int testCount,
                        Function<QueryProxy<T>, QueryProxy<T>> transform,
                        BiFunction<Collection<R>, Collection<R>, Boolean> validator,
                        String logString) {
        int invalid = 0;
        for (int i = 0; i < testCount; i++) {
            QueryProxy<T> queryProxy1 = buildQuery();
            QueryProxy<T> queryProxy2 = transform.apply(queryProxy1);
            T query1 = queryFactory.build(queryProxy1);
            T query2 = queryFactory.build(queryProxy2);
            Collection<R> result1 = queryFactory.getResult(query1);
            Collection<R> result2 = queryFactory.getResult(query2);
            boolean valid = validator.apply(result1, result2);
            if (!valid) {
                invalid++;
                log.error(logString,
                        queryFactory.toString(query1),
                        queryFactory.toString(query2),
                        queryFactory.toString(result1),
                        queryFactory.toString(result2));
            }
        }
        return invalid;
    }

    private QueryProxy<T> buildQuery() {
        return buildQuery(random.nextInt(maxLeafCount - minLeafCount + 1) + minLeafCount);
    }

    private QueryProxy<T> buildQuery(int leafCount) {
        if (leafCount == 1) {
            QueryProxy<T> nativeQuery = QueryProxy.<T>builder()
                    .queryType(QueryProxy.QueryType.NATIVE)
                    .nativeQuery(queryFactory.build())
                    .build();
            return (random.nextBoolean()) ? nativeQuery : nativeQuery.reverse();
        }
        int leftLeafCount = random.nextInt(leafCount - 1) + 1;
        return random.nextBoolean() ?
                buildQuery(leftLeafCount).and(buildQuery(leafCount - leftLeafCount)) :
                buildQuery(leftLeafCount).or(buildQuery(leafCount - leftLeafCount));
    }
}
