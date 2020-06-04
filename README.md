# Query Integrity
Query Integrity is framework to create and run tests on software using [Metamorphic Testing](https://en.wikipedia.org/wiki/Metamorphic_testing).
The main purpose of the tool is to automatically test and find inconsistency of data query result in order to find bugs 
and problems in data retrieving related features of software.

## The idea

In any data retrieving system, the relation between the result of any 2 queries should be logically corrected 
(e.g. A search for "A" should not return any result from a search for "not A"). 
These relations are metamorphic relations.
Failing to produce a correct relation between some pair of queries can be a result of:
- A bug
- A technical limitation
- A shortcoming of system design
- ...

These problems might not appear in development or even in testing due to small size of test data, deterministic natural
of manually created tests. We only test what we know can go wrong, but the bugs are normally found where we did not know
that can go wrong.

A simple solution for that is to automatically generate multiple pairs of queries that we know what relation of their
results should be. Hundreds or thousands of queries can be sent and validate without the creator know what query it is 
or what is the supposed answer for that query.

## The tests

This project currently supports 3 kinds of metamorphic relations that are common among data retrieving systems:

### The `not` test

A query search for `A` should not return any result in the query search for `not A`.

### The `subset` test

- A query search for `A` should return all the result in the query search for `A and B`.
- A query search for `A or B` should return all the result in the query search for `A`. 

### The `equal` test

- A query search for `A` should return the same result as a query search for `B` if A and B are logically equivalent 
(e.g. `A` and `not not A`, `A and B` and `B and A`, ...)

## The project

This project provide following modules:

- [query-integrity-core](/query-integrity-core) provides testing service as well as interfaces to generate queries that
fit your system as well as a validator for the results.

- [mongodb-integration](/mongodb-integration) provides an implementation to test mongodb server.