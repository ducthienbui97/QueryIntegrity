package io.github.ducthienbui97.queryintegrity.mongodb;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Singular;

import java.util.Collection;
import java.util.Map;

/**
 * Options to setup MongoDBQueryFactory
 * - connection string: url/string to connect to mongodb instance.
 * - database name: database used to run test.
 * - collection name: collection used to run test on.
 * - fieldOptions: options for generating a single query.
 * - seed: seeding value for random.
 */
@Builder
@Data
public class MongoDBQueryFactoryOptions {
    /**
     * Connection string to connect to mongodb instance.
     */
    @NonNull
    private String connectionString;
    /**
     * Name of the database to run query on.
     */
    @NonNull
    private String databaseName;
    /**
     * Name of the collection to run query on.
     */
    @NonNull
    private String collectionName;
    /**
     * Possible options can be used to create a single query condition in the following format:
     * {
     * --'field name': {
     * ----'operator name': [
     * ------[possible parameter for operator]
     * ------[possible, parameters, for, operator]
     * ----]
     * --}
     * }
     * Then every time {@link MongoDBQueryFactory#build()} is called,
     * a {@link org.bson.conversions.Bson} condition is created with 1 possible field name,
     * 1 possible operator on that field name, 1 possible parameter collection on that operator.
     */
    @Singular
    private Map<String, Map<String, Collection<Collection<Object>>>> fieldOptions;
    /**
     * Seed value for {@link java.util.Random}
     */
    private Long seed;
}
