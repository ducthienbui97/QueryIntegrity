# mongodb-integration

This module provides an implementation of query integrity check for mongodb server.

```xml
<dependency>
    <groupId>io.github.ducthienbui97</groupId>
    <artifactId>mongodb-integration</artifactId> 
    <version>1.0</version>
</dependency>
```

## Implementation

### MongoDB Query Factory

Implementation of Query Factory interface to randomly generate MongoDb queries. It needs to be provided with MongoDb
server connection information as well as a list of queries it can send to the server. 

The query random configuration is in the following structure:
```json
{
  "field name": {
    "operator name": [
      ["possible parameter for operator"],
      ["possible", "parameters", "for", "operator"]
    ]
  }
}
```

### MongoDB Query Testing CLI

Implementation of a simple command line interface program to run query integrity check on a mongodb server:
````
Usage: MongoDB Query Testing service [ens] -c=<collectionName>
                                     -db=<databaseName> -f=<configFile>
                                     [-seed=<seed>] -u=<connectionString>
Run queryintegrity test in your MongoDB deployment.
  -c, --collection=<collectionName>
                             Collection name.
  -db, --database=<databaseName>
                             Database name.
  -e, --equal, --equalTest   Run equal test.
  -f, --file=<configFile>    Json configure file.
  -n, --not, --notTest       Run not test.
  -s, --subset, --subsetTest Run subset test.
     --seed=<seed>          Random seed.
  -u, --url, --connection=<connectionString>
                             Connection string name.
````
