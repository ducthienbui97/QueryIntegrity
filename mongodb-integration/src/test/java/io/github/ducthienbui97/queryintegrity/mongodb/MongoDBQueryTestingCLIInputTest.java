package io.github.ducthienbui97.queryintegrity.mongodb;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class MongoDBQueryTestingCLIInputTest extends MongoDBQueryTestingCLITest {
    @ParameterizedTest()
    @MethodSource("io.github.ducthienbui97.queryintegrity.mongodb.MongoDBQueryTestingCLITest#parameterFormats")
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
    @MethodSource("io.github.ducthienbui97.queryintegrity.mongodb.MongoDBQueryTestingCLITest#parameterFormats")
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
    @MethodSource("io.github.ducthienbui97.queryintegrity.mongodb.MongoDBQueryTestingCLITest#parameterFormats")
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
    @MethodSource("io.github.ducthienbui97.queryintegrity.mongodb.MongoDBQueryTestingCLITest#parameterFormats")
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
    @MethodSource("io.github.ducthienbui97.queryintegrity.mongodb.MongoDBQueryTestingCLITest#parameterFormats")
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
}
