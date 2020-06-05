package io.github.ducthienbui97.queryintegrity.mongodb;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import de.bwaldvogel.mongo.wire.MongoWireProtocolHandler;
import io.github.ducthienbui97.queryintegrity.core.QueryTestingService;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class MongoDBQueryTestingCLITest {
    private static final String DATABASE_NAME = "testDb";
    private static final String COLLECTION_NAME = "testCollectionName";
    private MongoServer mongoServer;
    private String mongoURI;
    private String jsonFilePath;

    private static Stream<Arguments> parameterFormats() {
        return databaseFormats().flatMap(database -> collectionFormats().flatMap(collection ->
                connectionStringFormats().flatMap(connectionString -> configFileFormats().flatMap(configFile ->
                        notTestFormats().flatMap(notTest -> equalTestFormats().flatMap(equalTest ->
                                subsetTestFormats().map(subsetTest ->
                                        Arguments.of(database, collection, connectionString,
                                                configFile, notTest, equalTest, subsetTest)
                                )))))));
    }

    private static Stream<String> databaseFormats() {
        return Stream.of("-db", "--database");
    }

    private static Stream<String> collectionFormats() {
        return Stream.of("-c", "--collection");
    }

    private static Stream<String> connectionStringFormats() {
        return Stream.of("-u", "--url", "--connection");
    }

    private static Stream<String> configFileFormats() {
        return Stream.of("-f", "--file");
    }

    private static Stream<String> notTestFormats() {
        return Stream.of("-n", "--not", "--notTest");
    }

    private static Stream<String> equalTestFormats() {
        return Stream.of("-e", "--equal", "--equalTest");
    }

    private static Stream<String> subsetTestFormats() {
        return Stream.of("-s", "--subset", "--subsetTest");
    }

    @ParameterizedTest()
    @MethodSource("parameterFormats")
    public void testRunOnlySubsetTest(String database, String collection, String connectionString,
                                      String configFile, String notTest, String equalTest, String subsetTest) {
        Logger logger = (Logger) LoggerFactory.getLogger(QueryTestingService.class);
        logger.setLevel(Level.INFO);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        MongoDBQueryTestingCLI.main(new String[]{
                database, DATABASE_NAME,
                collection, COLLECTION_NAME,
                connectionString, mongoURI,
                configFile, jsonFilePath,
                subsetTest
        });
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList, allOf(
                not(hasItem(loggingEventMatcher(containsString("Not test")))),
                not(hasItem(loggingEventMatcher(containsString("Equal test")))),
                hasItem(loggingEventMatcher(containsString("Subset test")))));
        logger.setLevel(Level.OFF);
    }

    @ParameterizedTest()
    @MethodSource("parameterFormats")
    public void testRunOnlyNotTest(String database, String collection, String connectionString,
                                   String configFile, String notTest, String equalTest, String subsetTest) {
        Logger logger = (Logger) LoggerFactory.getLogger(QueryTestingService.class);
        logger.setLevel(Level.INFO);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        MongoDBQueryTestingCLI.main(new String[]{
                database, DATABASE_NAME,
                collection, COLLECTION_NAME,
                connectionString, mongoURI,
                configFile, jsonFilePath,
                notTest
        });
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList, allOf(
                hasItem(loggingEventMatcher(containsString("Not test"))),
                not(hasItem(loggingEventMatcher(containsString("Equal test")))),
                not(hasItem(loggingEventMatcher(containsString("Subset test"))))));
        logger.setLevel(Level.OFF);
    }

    @ParameterizedTest()
    @MethodSource("parameterFormats")
    public void testRunOnlyEqualTest(String database, String collection, String connectionString,
                                     String configFile, String notTest, String equalTest, String subsetTest) {
        Logger logger = (Logger) LoggerFactory.getLogger(QueryTestingService.class);
        logger.setLevel(Level.INFO);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        MongoDBQueryTestingCLI.main(new String[]{
                database, DATABASE_NAME,
                collection, COLLECTION_NAME,
                connectionString, mongoURI,
                configFile, jsonFilePath,
                equalTest
        });
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList, allOf(
                not(hasItem(loggingEventMatcher(containsString("Not test")))),
                hasItem(loggingEventMatcher(containsString("Equal test"))),
                not(hasItem(loggingEventMatcher(containsString("Subset test"))))
        ));
        logger.setLevel(Level.OFF);
    }

    @ParameterizedTest()
    @MethodSource("parameterFormats")
    public void testRunAllTests(String database, String collection, String connectionString,
                                String configFile, String notTest, String equalTest, String subsetTest) {
        Logger logger = (Logger) LoggerFactory.getLogger(QueryTestingService.class);
        logger.setLevel(Level.INFO);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        MongoDBQueryTestingCLI.main(new String[]{
                database, DATABASE_NAME,
                collection, COLLECTION_NAME,
                connectionString, mongoURI,
                configFile, jsonFilePath,
                equalTest, subsetTest, notTest
        });
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList, allOf(
                hasItem(loggingEventMatcher(containsString("Not test"))),
                hasItem(loggingEventMatcher(containsString("Equal test"))),
                hasItem(loggingEventMatcher(containsString("Subset test")))));
        logger.setLevel(Level.OFF);
    }

    @ParameterizedTest()
    @MethodSource("parameterFormats")
    public void testMongoServerConnected(String database, String collection, String connectionString,
                                         String configFile, String notTest, String equalTest, String subsetTest) {
        Logger logger = (Logger) LoggerFactory.getLogger(MongoWireProtocolHandler.class);
        logger.setLevel(Level.INFO);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        MongoDBQueryTestingCLI.main(new String[]{
                database, DATABASE_NAME,
                collection, COLLECTION_NAME,
                connectionString, mongoURI,
                configFile, jsonFilePath,
                equalTest, subsetTest, notTest});
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList, hasItem(loggingEventMatcher(containsString("connected"))));
        logger.setLevel(Level.OFF);
    }

    @ParameterizedTest()
    @MethodSource("parameterFormats")
    public void testFileNotFound(String database, String collection, String connectionString,
                                 String configFile, String notTest, String equalTest, String subsetTest) {
        StringWriter sw = new StringWriter();
        new CommandLine(new MongoDBQueryTestingCLI())
                .setErr(new PrintWriter(sw))
                .execute(database, DATABASE_NAME,
                        collection, COLLECTION_NAME,
                        connectionString, mongoURI,
                        configFile, "wrong" + jsonFilePath,
                        equalTest, subsetTest, notTest);
        assertThat(sw.toString(), containsString("java.io.FileNotFoundException"));
    }

    @ParameterizedTest()
    @MethodSource("parameterFormats")
    public void testDbNameNotSpecified(String database, String collection, String connectionString,
                                       String configFile, String notTest, String equalTest, String subsetTest) {
        StringWriter sw = new StringWriter();
        new CommandLine(new MongoDBQueryTestingCLI())
                .setErr(new PrintWriter(sw))
                .execute(collection, COLLECTION_NAME,
                        connectionString, mongoURI,
                        configFile, jsonFilePath,
                        equalTest, subsetTest, notTest);
        assertThat(sw.toString(), containsString("Missing required option"));
    }

    @ParameterizedTest()
    @MethodSource("parameterFormats")
    public void testCollectionNameNotSpecified(String database, String collection, String connectionString,
                                               String configFile, String notTest, String equalTest, String subsetTest) {
        StringWriter sw = new StringWriter();
        new CommandLine(new MongoDBQueryTestingCLI())
                .setErr(new PrintWriter(sw))
                .execute(database, DATABASE_NAME,
                        connectionString, mongoURI,
                        configFile, jsonFilePath,
                        equalTest, subsetTest, notTest);
        assertThat(sw.toString(), containsString("Missing required option"));
    }

    @ParameterizedTest()
    @MethodSource("parameterFormats")
    public void testMongoURINotSpecified(String database, String collection, String connectionString,
                                         String configFile, String notTest, String equalTest, String subsetTest) {
        StringWriter sw = new StringWriter();
        new CommandLine(new MongoDBQueryTestingCLI())
                .setErr(new PrintWriter(sw))
                .execute(database, DATABASE_NAME,
                        collection, COLLECTION_NAME,
                        configFile, jsonFilePath,
                        equalTest, subsetTest, notTest);
        assertThat(sw.toString(), containsString("Missing required option"));
    }

    @ParameterizedTest()
    @MethodSource("parameterFormats")
    public void testFilePathNotSpecified(String database, String collection, String connectionString,
                                         String configFile, String notTest, String equalTest, String subsetTest) {
        StringWriter sw = new StringWriter();
        new CommandLine(new MongoDBQueryTestingCLI())
                .setErr(new PrintWriter(sw))
                .execute(database, DATABASE_NAME,
                        collection, COLLECTION_NAME,
                        connectionString, mongoURI,
                        equalTest, subsetTest, notTest);
        assertThat(sw.toString(), containsString("Missing required option"));
    }

    private Matcher<ILoggingEvent> loggingEventMatcher(Matcher<String> messageMatcher) {
        return new TypeSafeMatcher<ILoggingEvent>() {
            @Override
            protected boolean matchesSafely(ILoggingEvent iLoggingEvent) {
                return messageMatcher.matches(iLoggingEvent.getFormattedMessage());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Message match ").appendDescriptionOf(messageMatcher);
            }
        };
    }

    @BeforeEach
    public void initializeMongoServer() {
        mongoServer = new MongoServer(new MemoryBackend());
        InetSocketAddress address = mongoServer.bind();
        mongoURI = "mongodb://" + address.getHostName() + ":" + address.getPort() + "/";
    }

    @AfterEach
    public void pullDown() {
        mongoServer.shutdown();
        mongoServer = null;
    }

    @BeforeEach
    public void writeJsonFile() throws IOException {
        File jsonFile = File.createTempFile("json", ".json");
        jsonFile.deleteOnExit();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(DateFormat.getDateTimeInstance());
        objectMapper.writeValue(jsonFile, fieldOptions());
        jsonFilePath = jsonFile.getPath();
    }

    private Map<String, Map<String, Collection<Collection<Object>>>> fieldOptions() {
        return ImmutableMap.of(
                "text", ImmutableMap.of(
                        "regex", singletonList(singletonList("tes.*")),
                        "in", ImmutableList.of(
                                singletonList(ImmutableList.of("test", "testing", "yo")),
                                singletonList(ImmutableList.of("text1"))),
                        "nin", ImmutableList.of(
                                singletonList(ImmutableList.of("test", "testing", "yo")),
                                singletonList(ImmutableList.of("text1")))));
    }
}
