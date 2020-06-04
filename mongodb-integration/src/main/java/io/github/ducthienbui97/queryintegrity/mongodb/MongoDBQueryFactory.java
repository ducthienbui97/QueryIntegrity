package io.github.ducthienbui97.queryintegrity.mongodb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import io.github.ducthienbui97.queryintegrity.core.QueryFactory;
import io.github.ducthienbui97.queryintegrity.core.QueryProxy;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Factory to generate MongoDB query for {@link io.github.ducthienbui97.queryintegrity.core.QueryTestingService}
 */
@Slf4j
public class MongoDBQueryFactory implements QueryFactory<Bson, Document> {
    /**
     * Client to connect to MongoDB instance.
     */
    @NonNull
    private final MongoClient mongoClient;
    /**
     * Mongo Database.
     */
    @NonNull
    private final MongoDatabase database;
    /**
     * Mongodb collection to run query on.
     */
    @NonNull
    private final MongoCollection<Document> collection;
    /**
     * Random field to generate random query.
     */
    private final Random random;
    /**
     * Possible query to choose from.
     * Generate based on {@link MongoDBQueryFactoryOptions#getFieldOptions()}
     * or {@link #setFieldFilterOptions(Map)}
     */
    private List<Bson> possibleQuery;


    /**
     * Create MongoDBQueryFactory from option
     *
     * @param options see {@link MongoDBQueryFactoryOptions}
     */
    public MongoDBQueryFactory(@NonNull MongoDBQueryFactoryOptions options) {
        this(options.getConnectionString(),
                options.getDatabaseName(),
                options.getCollectionName(),
                options.getFieldOptions(),
                options.getSeed());
    }

    /**
     * Create MongoDBQueryFactory.
     *
     * @param connectionString Connection string to connect to mongodb instance.
     * @param databaseName     Name of the database to run query on.
     * @param collectionName   Name of the collection to run query on.
     * @param fieldOptions     Option config to generate possible queries {@link #setFieldFilterOptions(Map)}
     * @param seed             Seed value for {@link java.util.Random}
     */
    public MongoDBQueryFactory(@NonNull String connectionString,
                               @NonNull String databaseName,
                               @NonNull String collectionName,
                               Map<String, Map<String, Collection<Collection<Object>>>> fieldOptions,
                               Long seed) {
        mongoClient = MongoClients.create(connectionString);
        database = mongoClient.getDatabase(databaseName);
        collection = database.getCollection(collectionName);

        if (seed != null) {
            random = new Random(seed);
        } else {
            random = new Random();
        }
        setFieldFilterOptions(fieldOptions);
    }

    /**
     * Flatten the setting in {@link MongoDBQueryFactoryOptions#getFieldOptions()} to a list of {@link MongoDBFieldOption}
     *
     * @param fieldOptionMap option setting (see {@link MongoDBQueryFactoryOptions#getFieldOptions()})
     * @return list of {@link MongoDBFieldOption} used to create list of {@link #possibleQuery}
     */
    protected static List<MongoDBFieldOption> buildFieldOptionList(Map<String, Map<String, Collection<Collection<Object>>>> fieldOptionMap) {
        return Optional.ofNullable(fieldOptionMap)
                .map(optionMap -> optionMap.entrySet().stream()
                        .flatMap(entry -> entry.getValue().entrySet().stream()
                                .flatMap(operatorEntry -> operatorEntry.getValue().stream()
                                        .map(param ->
                                                MongoDBFieldOption.builder()
                                                        .fieldName(entry.getKey())
                                                        .operator(operatorEntry.getKey())
                                                        .parameters(param).build()
                                        )))
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    /**
     * Generate list of possible query can be returned by {@link #build()} from json configuration
     * see {@link #setFieldFilterOptions(Map)} for json format
     *
     * @param json configuration string
     */
    public void setFieldFilterOptions(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        setFieldFilterOptions(objectMapper
                .readValue(json,
                        new TypeReference<Map<String, Map<String, Collection<Collection<Object>>>>>() {
                        }));
    }

    /**
     * Generate list of possible query can be returned by {@link #build()} from json configuration
     * see {@link #setFieldFilterOptions(Map)} for json format
     *
     * @param jsonFile file to load json configuration.
     */
    public void setFieldFilterOptions(File jsonFile) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        setFieldFilterOptions(objectMapper
                .readValue(jsonFile,
                        new TypeReference<Map<String, Map<String, Collection<Collection<Object>>>>>() {
                        }));
    }

    /**
     * Generate list of possible query can be returned by {@link #build()} from json configuration
     * see {@link #setFieldFilterOptions(Map)} for json format
     *
     * @param url url to load the json configuration.
     */
    public void setFieldFilterOptions(URL url) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        setFieldFilterOptions(objectMapper
                .readValue(url,
                        new TypeReference<Map<String, Map<String, Collection<Collection<Object>>>>>() {
                        }));
    }

    /**
     * Generate list of possible query can be returned by {@link #build()} from json configuration
     *
     * @param fieldOptions Possible options can be used to create a single query condition in the following format:
     *                     {
     *                     --'field name': {
     *                     ----'operator name': [
     *                     ------[possible parameter for operator]
     *                     ------[possible, parameters, for, operator]
     *                     ----]
     *                     --}
     *                     }
     *                     Then every time {@link MongoDBQueryFactory#build()} is called,
     *                     a {@link org.bson.conversions.Bson} condition is created with 1 possible field name,
     *                     1 possible operator on that field name, 1 possible parameter collection on that operator.
     */
    public void setFieldFilterOptions(Map<String, Map<String, Collection<Collection<Object>>>> fieldOptions) {
        setFieldFilterOptions(buildFieldOptionList(fieldOptions));
    }

    /**
     * Generate list of possible query can be returned by {@link #build()} from configuration
     *
     * @param fieldOptions List of {@link MongoDBFieldOption} created by
     *                     flatting input field of {@link #setFieldFilterOptions(Map)}
     *                     with {@link #buildFieldOptionList(Map)}
     */
    public void setFieldFilterOptions(List<MongoDBFieldOption> fieldOptions) {
        possibleQuery = Optional.ofNullable(fieldOptions)
                .map(fieldOptionList -> fieldOptionList.stream().map(fieldOption -> {
                    String fieldName = fieldOption.getFieldName();
                    String operator = fieldOption.getOperator();
                    Object[] methodParams = Stream.concat(Stream.of(fieldName), fieldOption.getParameters().stream()).toArray();
                    Optional<Bson> query = Arrays.stream(Filters.class.getMethods())
                            .filter(method -> method.getName().equals(operator))
                            .map(method -> {
                                try {
                                    return (Bson) method.invoke(null, methodParams);
                                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                                    return null;
                                }
                            }).filter(Objects::nonNull)
                            .findFirst();
                    return query.orElseGet(() -> {
                        log.warn("Operator {} not found for parameters {}", operator, fieldOption.getParameters());
                        return null;
                    });
                }).filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    public void setSeed(long seed) {
        random.setSeed(seed);
    }

    @Override
    public Bson build() {
        return possibleQuery.get(random.nextInt(possibleQuery.size()));
    }

    @Override
    public Bson build(QueryProxy<Bson> queryProxy) {
        switch (queryProxy.getQueryType()) {
            case OR:
                return Filters.or(queryProxy
                        .getChildren()
                        .stream()
                        .map(this::build)
                        .collect(Collectors.toList()));
            case AND:
                return Filters.and(queryProxy
                        .getChildren()
                        .stream()
                        .map(this::build)
                        .collect(Collectors.toList()));
            case NOT:
                return Filters.nor(build(queryProxy.getChildren().get(0)));
            default:
                return queryProxy.getNativeQuery();
        }
    }

    @Override
    public Collection<Document> getResult(Bson query) {
        log.debug("Sending {}", query);
        return Lists.newArrayList(collection.find(query));
    }

    @Override
    public String toString(Object resultOrQuery) {
        if (resultOrQuery instanceof Collection) {
            return ((Collection<Document>) resultOrQuery)
                    .stream()
                    .map(data -> data.get("_id"))
                    .collect(Collectors.toList())
                    .toString();
        }
        return resultOrQuery.toString();
    }
}
