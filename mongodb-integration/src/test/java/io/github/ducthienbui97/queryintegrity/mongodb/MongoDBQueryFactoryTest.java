package io.github.ducthienbui97.queryintegrity.mongodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mongodb.client.model.Filters;
import de.bwaldvogel.mongo.MongoDatabase;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.CollectionOptions;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import io.github.ducthienbui97.queryintegrity.core.QueryProxy;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.text.DateFormat;
import java.time.Instant;
import java.time.Period;
import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class MongoDBQueryFactoryTest {
    private static final String DATABASE_NAME = "testDb";
    private static final String COLLECTION_NAME = "testCollectionName";
    private static final Instant NOW = Instant.now();
    private static final List<Map<String, Object>> TEST_DATA = ImmutableList.of(
            ImmutableMap.of(
                    "date", NOW.minus(Period.ofDays(1)),
                    "text", "text1",
                    "object", new de.bwaldvogel.mongo.bson.Document(ImmutableMap.of(
                            "name", "testName",
                            "otherName", "nothing"
                    ))
            ),
            ImmutableMap.of(
                    "date", NOW.plus(Period.ofDays(1)),
                    "text", "test",
                    "number", 10
            ),
            ImmutableMap.of(
                    "text", "",
                    "object", new de.bwaldvogel.mongo.bson.Document(ImmutableMap.of(
                            "name", "test"
                    )),
                    "number", 100,
                    "array", ImmutableList.of("a", "b", "c")
            )
    );
    private static MongoServer mongoServer;
    private MongoDBQueryFactory mongoDBQueryFactory;

    public static List<MongoDBFieldOption> fieldNameWithFilterOption() {
        return MongoDBQueryFactory.buildFieldOptionList(fieldOptions());
    }

    public static Map<String, Map<String, Collection<Collection<Object>>>> fieldOptions() {
        return ImmutableMap.of(
                "date", ImmutableMap.of(
                        "exists", singletonList(emptyList()),
                        "gt", singletonList(singletonList(Date.from(NOW))),
                        "lt", singletonList(singletonList(Date.from(NOW)))),
                "text", ImmutableMap.of(
                        "regex", singletonList(singletonList("tes.*")),
                        "in", ImmutableList.of(
                                singletonList(ImmutableList.of("test", "testing", "yo")),
                                singletonList(ImmutableList.of("text1"))),
                        "nin", ImmutableList.of(
                                singletonList(ImmutableList.of("test", "testing", "yo")),
                                singletonList(ImmutableList.of("text1")))),
                "object.name", ImmutableMap.of(
                        "exists", ImmutableList.of(singletonList(false), singletonList(true))),
                "object.otherName", ImmutableMap.of(
                        "exists", ImmutableList.of(singletonList(false), singletonList(true))),
                "number", ImmutableMap.of(
                        "exists", ImmutableList.of(singletonList(false), singletonList(true)),
                        "gte", ImmutableList.of(singletonList(0), singletonList(100), singletonList(10)),
                        "mod", ImmutableList.of(ImmutableList.of(10L, 0L), ImmutableList.of(99L, 1L)))
        );
    }

    @BeforeEach
    public void setup() {
        mongoDBQueryFactory = new MongoDBQueryFactory(buildFactoryOption());
    }

    @AfterAll
    public static void pullDown() {
        mongoServer.shutdown();
        mongoServer = null;
    }

    @ParameterizedTest
    @MethodSource("fieldNameWithFilterOption")
    public void testFieldFilterSetup(MongoDBFieldOption fieldOption) {
        assertThat(fieldOptions().get(fieldOption.getFieldName()).get(fieldOption.getOperator()),
                hasItem(fieldOption.getParameters()));
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("fieldNameWithFilterOption")
    public void testFieldFilterSetupFromJson(MongoDBFieldOption fieldOption) {
        mongoDBQueryFactory.setFieldFilterOptions((List<MongoDBFieldOption>) null);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(DateFormat.getDateTimeInstance());
        String json = objectMapper.writeValueAsString(fieldOptions());
        mongoDBQueryFactory.setFieldFilterOptions(json);
        assertThat(fieldOptions().get(fieldOption.getFieldName()).get(fieldOption.getOperator()),
                hasItem(fieldOption.getParameters()));
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("fieldNameWithFilterOption")
    public void testFieldFilterSetupFromFile(MongoDBFieldOption fieldOption) {
        mongoDBQueryFactory.setFieldFilterOptions((List<MongoDBFieldOption>) null);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(DateFormat.getDateTimeInstance());
        File jsonFile = File.createTempFile("json", ".json");
        jsonFile.deleteOnExit();
        objectMapper.writeValue(jsonFile, fieldOptions());
        mongoDBQueryFactory.setFieldFilterOptions(jsonFile);
        assertThat(fieldOptions().get(fieldOption.getFieldName()).get(fieldOption.getOperator()),
                hasItem(fieldOption.getParameters()));
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("fieldNameWithFilterOption")
    public void testFieldFilterSetupFromURL(MongoDBFieldOption fieldOption) {
        mongoDBQueryFactory.setFieldFilterOptions((List<MongoDBFieldOption>) null);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(DateFormat.getDateTimeInstance());
        File jsonFile = File.createTempFile("json", ".json");
        jsonFile.deleteOnExit();
        objectMapper.writeValue(jsonFile, fieldOptions());
        mongoDBQueryFactory.setFieldFilterOptions(jsonFile.toURI().toURL());
        assertThat(fieldOptions().get(fieldOption.getFieldName()).get(fieldOption.getOperator()),
                hasItem(fieldOption.getParameters()));
    }


    @SneakyThrows
    @Test
    public void testFieldFilterSetupFromURLFailIfFormatNotCorrect() {
        File jsonFile = File.createTempFile("json", ".json");
        jsonFile.deleteOnExit();
        assertThrows(Exception.class, () -> mongoDBQueryFactory.setFieldFilterOptions(jsonFile.toURI().toURL()));
    }

    @SneakyThrows
    @Test
    public void testFieldFilterSetupFromFileFailIfFormatNotCorrect() {
        File jsonFile = File.createTempFile("json", ".json");
        jsonFile.deleteOnExit();
        assertThrows(Exception.class, () -> mongoDBQueryFactory.setFieldFilterOptions(jsonFile));
    }

    @ParameterizedTest
    @MethodSource("fieldNameWithFilterOption")
    public void testQueryBuilderWithCorrectOperatorAndFieldName(MongoDBFieldOption fieldOption) {
        mongoDBQueryFactory.setFieldFilterOptions(ImmutableList.of(fieldOption));
        Bson query = mongoDBQueryFactory.build();
        assertThat(query.toString(), containsString(String.format("fieldName='%s'", fieldOption.getFieldName())));
        assertThat(query.toString(), either(containsString(String.format("operator='$%s'", fieldOption.getOperator())))
                .or(containsString("BsonRegularExpression")));
    }

    @RepeatedTest(100)
    public void testBuildQueryInQueryList() {
        List<MongoDBFieldOption> fieldOptions = fieldNameWithFilterOption();
        Bson query = mongoDBQueryFactory.build();
        assertThat(fieldOptions, hasItem(new TypeSafeMatcher<MongoDBFieldOption>() {
            @Override
            protected boolean matchesSafely(MongoDBFieldOption fieldOption) {
                return allOf(containsString(String.format("fieldName='%s'", fieldOption.getFieldName())),
                        either(containsString(String.format("operator='$%s'", fieldOption.getOperator())))
                                .or(containsString("BsonRegularExpression"))).matches(query.toString());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Match ").appendValue(query);
            }
        }));
    }

    @ParameterizedTest
    @MethodSource("fieldNameWithFilterOption")
    public void testResultShouldReturnItemInTestData(MongoDBFieldOption fieldOption) {
        mongoDBQueryFactory.setFieldFilterOptions(ImmutableList.of(fieldOption));
        Bson query = mongoDBQueryFactory.build();
        Collection<Document> result = mongoDBQueryFactory.getResult(query);
        log.info("Result for {} is {}", query, result);
        assertThat(result, everyItem(inTestData()));
    }

    @RepeatedTest(100)
    public void testAndQueryResultShouldReturnItemInTestData() {
        QueryProxy<Bson> nativeQuery1 = buildNativeQuery();
        QueryProxy<Bson> nativeQuery2 = buildNativeQuery();
        QueryProxy<Bson> nativeQuery3 = buildNativeQuery();
        QueryProxy<Bson> queryProxy = nativeQuery1.or(nativeQuery2.reverse()).reverse().and(nativeQuery3);
        Bson query = mongoDBQueryFactory.build(queryProxy);
        Collection<Document> result = mongoDBQueryFactory.getResult(query);
        log.info("Result for {} is {}", query, result);
        assertThat(result, everyItem(inTestData()));
    }

    @RepeatedTest(100)
    public void testNotQueryResultShouldReturnItemInTestData() {
        QueryProxy<Bson> nativeQuery1 = buildNativeQuery();
        QueryProxy<Bson> nativeQuery2 = buildNativeQuery();
        QueryProxy<Bson> nativeQuery3 = buildNativeQuery();
        QueryProxy<Bson> queryProxy = nativeQuery1.or(nativeQuery2).and(nativeQuery3).reverse();
        Bson query = mongoDBQueryFactory.build(queryProxy);
        Collection<Document> result = mongoDBQueryFactory.getResult(query);
        log.info("Result for {} is {}", query, result);
        assertThat(result, everyItem(inTestData()));
    }

    @RepeatedTest(100)
    public void testOrQueryResultShouldReturnItemInTestData() {
        QueryProxy<Bson> nativeQuery1 = buildNativeQuery();
        QueryProxy<Bson> nativeQuery2 = buildNativeQuery();
        QueryProxy<Bson> nativeQuery3 = buildNativeQuery();
        QueryProxy<Bson> queryProxy = nativeQuery1.and(nativeQuery2.reverse()).or(nativeQuery3);
        Bson query = mongoDBQueryFactory.build(queryProxy);
        Collection<Document> result = mongoDBQueryFactory.getResult(query);
        log.info("Result for {} is {}", query, result);
        assertThat(result, everyItem(inTestData()));
    }

    @RepeatedTest(100)
    public void testToStringContainsOutputIds() {
        Bson query = mongoDBQueryFactory.build();
        Collection<Document> result = mongoDBQueryFactory.getResult(query);
        log.info("Result for {} is {}", query, result);
        result.forEach(data -> assertThat(mongoDBQueryFactory.toString(result),
                containsString(data.getObjectId("_id").toString())));
    }

    @RepeatedTest(100)
    public void testNativeQuery() {
        QueryProxy<Bson> queryProxy = buildNativeQuery();
        assertThat(mongoDBQueryFactory.build(queryProxy), equalTo(queryProxy.getNativeQuery()));
    }

    @RepeatedTest(100)
    public void testNotQuery() {
        QueryProxy<Bson> nativeQuery = buildNativeQuery();
        assertThat(mongoDBQueryFactory.build(nativeQuery.reverse()), equalTo(Filters.nor(nativeQuery.getNativeQuery())));
    }

    @RepeatedTest(100)
    public void testAndQuery() {
        QueryProxy<Bson> nativeQuery1 = buildNativeQuery();
        QueryProxy<Bson> nativeQuery2 = buildNativeQuery();
        assertThat(mongoDBQueryFactory.build(nativeQuery1.and(nativeQuery2.reverse())),
                equalTo(Filters.and(nativeQuery1.getNativeQuery(), Filters.nor(nativeQuery2.getNativeQuery()))));
    }

    @RepeatedTest(100)
    public void testOrQuery() {
        QueryProxy<Bson> nativeQuery1 = buildNativeQuery();
        QueryProxy<Bson> nativeQuery2 = buildNativeQuery();
        assertThat(mongoDBQueryFactory.build(nativeQuery1.reverse().or(nativeQuery2.reverse())),
                equalTo(Filters.or(Filters.nor(nativeQuery1.getNativeQuery()), Filters.nor(nativeQuery2.getNativeQuery()))));
    }

    @RepeatedTest(100)
    public void testComplexQuery() {
        QueryProxy<Bson> nativeQuery1 = buildNativeQuery();
        QueryProxy<Bson> nativeQuery2 = buildNativeQuery();
        QueryProxy<Bson> nativeQuery3 = buildNativeQuery();
        QueryProxy<Bson> queryProxy = nativeQuery1.or(nativeQuery2.reverse()).reverse().and(nativeQuery3);
        Bson expectedQuery = Filters.and(Filters.and(Filters.nor(nativeQuery1.getNativeQuery()), nativeQuery2.getNativeQuery()), nativeQuery3.getNativeQuery());
        assertThat(mongoDBQueryFactory.build(queryProxy), equalTo(expectedQuery));
    }

    @ParameterizedTest()
    @ValueSource(longs = {10L, 100L, 1000L, 99999L, 1000000007L})
    public void testRandomSeedRepeatable(Long seed) {
        mongoDBQueryFactory.setSeed(seed);
        Bson query1 = mongoDBQueryFactory.build();
        mongoDBQueryFactory.setSeed(seed);
        Bson query2 = mongoDBQueryFactory.build();
        assertThat(query1, equalTo(query2));
    }

    @ParameterizedTest()
    @ValueSource(longs = {10L, 100L, 1000L, 99999L, 1000000007L})
    public void testRandomSeedInConstructorRepeatable(Long seed) {
        mongoDBQueryFactory.setSeed(seed);
        Bson query1 = mongoDBQueryFactory.build();
        val option = buildFactoryOption();
        option.setSeed(seed);
        mongoDBQueryFactory = new MongoDBQueryFactory(option);
        Bson query2 = mongoDBQueryFactory.build();
        assertThat(query1, equalTo(query2));
    }

    @Test()
    public void testIfFilterSettingNotMatch() {
        mongoDBQueryFactory.setFieldFilterOptions(ImmutableList.of(
                MongoDBFieldOption.builder().fieldName("test").operator("test").build()
        ));
        assertThrows(Exception.class, mongoDBQueryFactory::build);
    }

    @Test
    public void optionCantBeNull() {
        assertThrows(NullPointerException.class, () -> new MongoDBQueryFactory(null));
    }

    @Test
    public void connectionStringCantBeNull() {
        val option = buildFactoryOption();
        assertThrows(NullPointerException.class, () -> new MongoDBQueryFactory(null, option.getDatabaseName(), option.getCollectionName(), option.getFieldOptions(), option.getSeed()));
    }

    @Test
    public void dbNameCantBeNull() {
        val option = buildFactoryOption();
        assertThrows(NullPointerException.class, () -> new MongoDBQueryFactory(option.getConnectionString(), null, option.getCollectionName(), option.getFieldOptions(), option.getSeed()));
    }

    @Test
    public void collectionNameCantBeNull() {
        val option = buildFactoryOption();
        assertThrows(NullPointerException.class, () -> new MongoDBQueryFactory(option.getConnectionString(), option.getCollectionName(), null, option.getFieldOptions(), option.getSeed()));
    }

    @Test
    public void fieldOptionCantBeNullWhenBuildIsCalled() {
        val option = buildFactoryOption();
        mongoDBQueryFactory = new MongoDBQueryFactory(option.getConnectionString(), option.getCollectionName(), option.getCollectionName(), null, option.getSeed());
        assertThrows(NullPointerException.class, () -> mongoDBQueryFactory.build());
    }

    private QueryProxy<Bson> buildNativeQuery() {
        return QueryProxy.<Bson>builder()
                .nativeQuery(mongoDBQueryFactory.build())
                .queryType(QueryProxy.QueryType.NATIVE)
                .build();
    }

    @BeforeAll
    public static void initializeMongoServer() {
        val backend = new Backend();
        val database = backend.resolveDatabase(DATABASE_NAME);
        val collection = database.createCollectionOrThrowIfExists(COLLECTION_NAME, CollectionOptions.withDefaults());
        for (Map<String, Object> data : TEST_DATA) {
            collection.addDocument(new de.bwaldvogel.mongo.bson.Document(data));
        }
        mongoServer = new MongoServer(backend);
        mongoServer.bind();
    }

    private Matcher<Map<String, Object>> matchDocument(Document document) {
        return new TypeSafeMatcher<Map<String, Object>>() {
            @Override
            protected boolean matchesSafely(Map<String, Object> data) {
                return isEqual(document, data);
            }

            private boolean isEqual(Document document, Map<String, Object> objectMap) {
                return document.keySet().containsAll(objectMap.keySet())
                        && document.entrySet().stream().allMatch(documentEntry -> {
                    if (documentEntry.getKey().equals("_id")) {
                        return true;
                    }
                    if (!objectMap.containsKey(documentEntry.getKey())) {
                        return false;
                    }
                    if (documentEntry.getValue() instanceof Document) {
                        return isEqual((Document) documentEntry.getValue(), (Map<String, Object>) objectMap.get(documentEntry.getKey()));
                    } else if (documentEntry.getValue() instanceof Date) {
                        return Date.from((Instant) objectMap.get(documentEntry.getKey())).equals(documentEntry.getValue());
                    } else {
                        return Objects.equals(documentEntry.getValue(), objectMap.get(documentEntry.getKey()));
                    }
                });
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("equal").appendValue(document);
            }
        };
    }

    private Matcher<Document> inTestData() {
        return new TypeSafeMatcher<Document>() {
            @Override
            protected boolean matchesSafely(Document document) {
                return hasItem(matchDocument(document)).matches(TEST_DATA);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Test dataset has item");
            }
        };
    }

    private MongoDBQueryFactoryOptions buildFactoryOption() {
        val address = mongoServer.getLocalAddress();
        String mongoURI = "mongodb://" + address.getHostName() + ":" + address.getPort() + "/";
        return MongoDBQueryFactoryOptions
                .builder()
                .connectionString(mongoURI)
                .databaseName(DATABASE_NAME)
                .collectionName(COLLECTION_NAME)
                .fieldOptions(fieldOptions())
                .build();
    }

    protected static class Backend extends MemoryBackend {
        @Override
        // Override to make this public
        public synchronized MongoDatabase resolveDatabase(String database) {
            return super.resolveDatabase(database);
        }
    }
}