package io.github.ducthienbui97.queryintegrity.mongodb;

import io.github.ducthienbui97.queryintegrity.core.QueryTestingService;
import lombok.SneakyThrows;
import org.bson.Document;
import org.bson.conversions.Bson;
import picocli.CommandLine;

import java.io.File;

@CommandLine.Command(name = "MongoDB Query Testing service",
        description = "Run query-integrity test in your MongoDB deployment.")
public class MongoDBQueryTestingCLI implements Runnable {
    @CommandLine.Option(names = {"-db", "--database"}, description = "Database name.", required = true)
    private String databaseName;
    @CommandLine.Option(names = {"-c", "--collection"}, description = "Collection name.", required = true)
    private String collectionName;
    @CommandLine.Option(names = {"-u", "--url", "--connection"}, description = "Connection string name.", required = true)
    private String connectionString;
    @CommandLine.Option(names = {"-f", "--file"}, description = "Json configure file.", required = true)
    private File configFile;
    @CommandLine.Option(names = {"--seed"}, description = "Random seed.")
    private Long seed;
    @CommandLine.Option(names = {"-n", "--not", "--notTest"}, description = "Run not test.")
    private Boolean runNotTest = false;
    @CommandLine.Option(names = {"-e", "--equal", "--equalTest"}, description = "Run equal test.")
    private Boolean runEqualTest = false;
    @CommandLine.Option(names = {"-s", "--subset", "--subsetTest"}, description = "Run subset test.")
    private Boolean runSubsetTest = false;

    public static void main(String[] args) {
        new CommandLine(new MongoDBQueryTestingCLI()).execute(args);
    }

    @SneakyThrows
    @Override
    public void run() {
        MongoDBQueryFactory mongoDbQueryFactory = new MongoDBQueryFactory(connectionString,
                databaseName, collectionName, null, seed);
        mongoDbQueryFactory.setFieldFilterOptions(configFile);
        QueryTestingService<Bson, Document> queryTestingService = new QueryTestingService<>(mongoDbQueryFactory);
        if (runNotTest) {
            queryTestingService.runNotTest();
        }
        if (runEqualTest) {
            queryTestingService.runEqualTest();
        }
        if (runSubsetTest) {
            queryTestingService.runSubsetTest();
        }
    }
}
