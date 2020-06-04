# query-integrity-core

This module provides testing service as well as interfaces to generate queries as well as a validator for the results.

```xml
<dependency>
    <groupId>io.github.ducthienbui97</groupId>
    <artifactId>query-integrity-core</artifactId> 
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Implementations

### Query Testing Service 

Query Testing Service randomly generates multiple pairs of queries to send to the system and compare the result to determine if
 there is any violation of metamorphic relations.

### Query Factory

Query Factory is an interface that has to be implemented to provide correct query for each system. The implementation
 including create a new random query and sending the query to the system and retrieve the result.
 
### Result Validator
 
 Result Validator is an interface that has 3 validation functions, 1 for each testing type:
 - `isEqual` function check if 2 sets of result are equal. By default, it will compare both element and order. A `false`
 response from this function is a violation in the `equal` test.
 - `isIntersected` function check if 2 sets of result have any common element. A `true` response from this function is
  a validation in the `not` test.
 - `isSubset` function check if 1 of the 2 sets is a subset of the other. A `false` response from this function is
   a validation in the `subset`.

Result Validator has a default implementation using simple java hash and equals functions.
 