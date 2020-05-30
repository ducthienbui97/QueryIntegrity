package io.github.ducthienbui97.queryintegrity.mongodb;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class MongoDBQueryTestingCLITest {
    private static final String DATABASE_NAME = "testDb";
    private static final String COLLECTION_NAME = "testCollectionName";
    private MongoServer mongoServer;
    private String mongoURI;
    private String jsonFilePath;

    @Test
    public void testRunOnlySubsetTest() {
        Logger logger = (Logger) LoggerFactory.getLogger(QueryTestingService.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        MongoDBQueryTestingCLI.main(new String[]{
                "-db=" + DATABASE_NAME,
                "-c=" + COLLECTION_NAME,
                "-u=" + mongoURI,
                "-f=" + jsonFilePath,
                "--subset"
        });
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList, allOf(
                not(hasItem(loggingEventMatcher(containsString("Not test")))),
                not(hasItem(loggingEventMatcher(containsString("Equal test")))),
                hasItem(loggingEventMatcher(containsString("Subset test")))));
    }

    @Test
    public void testRunOnlyNotTest() {
        Logger logger = (Logger) LoggerFactory.getLogger(QueryTestingService.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        MongoDBQueryTestingCLI.main(new String[]{
                "-db=" + DATABASE_NAME,
                "-c=" + COLLECTION_NAME,
                "-u=" + mongoURI,
                "-f=" + jsonFilePath,
                "--not"
        });
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList, allOf(
                hasItem(loggingEventMatcher(containsString("Not test"))),
                not(hasItem(loggingEventMatcher(containsString("Equal test")))),
                not(hasItem(loggingEventMatcher(containsString("Subset test"))))));
    }

    @Test
    public void testRunOnlyEqualTest() {
        Logger logger = (Logger) LoggerFactory.getLogger(QueryTestingService.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        MongoDBQueryTestingCLI.main(new String[]{
                "-db=" + DATABASE_NAME,
                "-c=" + COLLECTION_NAME,
                "-u=" + mongoURI,
                "-f=" + jsonFilePath,
                "--equal"
        });
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList, allOf(
                not(hasItem(loggingEventMatcher(containsString("Not test")))),
                hasItem(loggingEventMatcher(containsString("Equal test"))),
                not(hasItem(loggingEventMatcher(containsString("Subset test"))))
        ));
    }

    @Test
    public void testRunAllTests() {
        Logger logger = (Logger) LoggerFactory.getLogger(QueryTestingService.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        MongoDBQueryTestingCLI.main(new String[]{
                "-db=" + DATABASE_NAME,
                "-c=" + COLLECTION_NAME,
                "-u=" + mongoURI,
                "-f=" + jsonFilePath,
                "-e", "-n", "-s"
        });
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList, allOf(
                hasItem(loggingEventMatcher(containsString("Not test"))),
                hasItem(loggingEventMatcher(containsString("Equal test"))),
                hasItem(loggingEventMatcher(containsString("Subset test")))));
    }

    @Test
    public void testMongoServerConnected() {
        Logger logger = (Logger) LoggerFactory.getLogger(MongoWireProtocolHandler.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        MongoDBQueryTestingCLI.main(new String[]{"-db=" + DATABASE_NAME,
                "-c=" + COLLECTION_NAME,
                "-u=" + mongoURI,
                "-f=" + jsonFilePath,
                "-e", "-n", "-s"});
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList, hasItem(loggingEventMatcher(containsString("connected"))));
    }

    @Test
    public void testFileNotFound() {
        StringWriter sw = new StringWriter();
        new CommandLine(new MongoDBQueryTestingCLI())
                .setErr(new PrintWriter(sw))
                .execute(
                        "-db=" + DATABASE_NAME,
                        "-c=" + COLLECTION_NAME,
                        "-u=" + mongoURI,
                        "-f=" + "wrong" + jsonFilePath,
                        "-e", "-n", "-s");
        assertThat(sw.toString(), containsString("java.io.FileNotFoundException"));
    }

    @Test
    public void testDbNameNotSpecified() {
        StringWriter sw = new StringWriter();
        new CommandLine(new MongoDBQueryTestingCLI())
                .setErr(new PrintWriter(sw))
                .execute("-c=" + COLLECTION_NAME,
                        "-u=" + mongoURI,
                        "-f=" + "wrong" + jsonFilePath,
                        "-e", "-n", "-s");
        assertThat(sw.toString(), containsString("Missing required option"));
    }

    @Test
    public void testCollectionNameNotSpecified() {
        StringWriter sw = new StringWriter();
        new CommandLine(new MongoDBQueryTestingCLI())
                .setErr(new PrintWriter(sw))
                .execute("-db=" + DATABASE_NAME,
                        "-u=" + mongoURI,
                        "-f=" + "wrong" + jsonFilePath,
                        "-e", "-n", "-s");
        assertThat(sw.toString(), containsString("Missing required option"));
    }

    @Test
    public void testMongoURINotSpecified() {
        StringWriter sw = new StringWriter();
        new CommandLine(new MongoDBQueryTestingCLI())
                .setErr(new PrintWriter(sw))
                .execute("-db=" + DATABASE_NAME,
                        "-c=" + COLLECTION_NAME,
                        "-f=" + jsonFilePath,
                        "-e", "-n", "-s");
        assertThat(sw.toString(), containsString("Missing required option"));
    }

    @Test
    public void testFilePathNotSpecified() {
        StringWriter sw = new StringWriter();
        new CommandLine(new MongoDBQueryTestingCLI())
                .setErr(new PrintWriter(sw))
                .execute("-db=" + DATABASE_NAME,
                        "-c=" + COLLECTION_NAME,
                        "-u=" + mongoURI,
                        "-e", "-n", "-s");
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
