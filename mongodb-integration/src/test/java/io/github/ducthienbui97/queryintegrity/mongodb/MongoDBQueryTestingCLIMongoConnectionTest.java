package io.github.ducthienbui97.queryintegrity.mongodb;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import de.bwaldvogel.mongo.wire.MongoWireProtocolHandler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

public class MongoDBQueryTestingCLIMongoConnectionTest extends MongoDBQueryTestingCLITest {
    @ParameterizedTest()
    @MethodSource("io.github.ducthienbui97.queryintegrity.mongodb.MongoDBQueryTestingCLITest#parameterFormats")
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
}
