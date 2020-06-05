package io.github.ducthienbui97.queryintegrity.mongodb;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.provider.Arguments;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;

public class MongoDBQueryTestingCLITest {
    protected static final String DATABASE_NAME = "testDb";
    protected static final String COLLECTION_NAME = "testCollectionName";
    protected MongoServer mongoServer;
    protected String mongoURI;
    protected String jsonFilePath;

    protected static Stream<Arguments> parameterFormats() {
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

    protected Matcher<ILoggingEvent> loggingEventMatcher(Matcher<String> messageMatcher) {
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
