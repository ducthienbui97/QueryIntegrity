package io.github.ducthienbui97.queryintegrity.core;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class ResultValidatorTest {
    private ResultValidator<String> resultValidator;

    private static Stream<Arguments> subsetArguments() {
        return supersetArguments().map(arguments -> Arguments.of(arguments.get()[1], arguments.get()[0]));
    }

    private static Stream<Arguments> nonIntersectArguments() {
        return collectionStream().flatMap(list1 ->
                collectionStream().filter(list2 -> list2.size() < list1.size())
                        .map(list2 -> {
                            list1.removeAll(list2);
                            return Arguments.of(list1, list2);
                        }));
    }

    private static Stream<Arguments> supersetArguments() {
        return collectionStream().flatMap(list ->
                collectionStream().filter(list2 -> list2.size() < list.size())
                        .map(list2 -> Arguments.of(list, list2)));
    }

    private static Stream<Arguments> equalsArgument() {
        return collectionStream().map(list -> Arguments.of(list, new ArrayList<>(list)));
    }

    private static Stream<Arguments> hasNullArguments() {
        return Stream.concat(
                Stream.concat(
                        collectionStream().map(list -> Arguments.of(null, list)),
                        collectionStream().map(list -> Arguments.of(list, null))),
                Stream.of(Arguments.of(null, null)));

    }

    private static Stream<Collection<String>> collectionStream() {
        List<String> input = new ArrayList<>();
        Stream.Builder<Collection<String>> inputBuilder = Stream.builder();
        while (input.size() < 10) {
            inputBuilder.accept(new ArrayList<>(input));
            input.add("test" + input.size());
        }
        return inputBuilder.build();
    }

    @BeforeEach
    public void setup() {
        resultValidator = new ResultValidator<String>() {
        };
    }

    @ParameterizedTest
    @MethodSource("equalsArgument")
    public void testIsEqualsCorrectlyCompareEquals(Collection<String> result1, Collection<String> result2) {
        assertThat(resultValidator.isEquals(result1, result2), is(true));
    }

    @ParameterizedTest
    @MethodSource({"subsetArguments", "supersetArguments", "nonIntersectArguments"})
    public void testIsEqualsCorrectlyCompareNotEqual(Collection<String> result1, Collection<String> result2) {
        assertThat(resultValidator.isEquals(result1, result2), is(false));
    }

    @ParameterizedTest
    @MethodSource({"equalsArgument", "subsetArguments"})
    public void testIsSubsetCorrectlyCompareSubset(Collection<String> result1, Collection<String> result2) {
        assertThat(resultValidator.isSubset(result1, result2), is(true));
    }

    @ParameterizedTest
    @MethodSource({"supersetArguments", "nonIntersectArguments"})
    public void testIsSubsetCorrectlyCompareNonSubset(Collection<String> result1, Collection<String> result2) {
        assertThat(resultValidator.isSubset(result1, result2), is(false));
    }

    @ParameterizedTest
    @MethodSource({"equalsArgument", "subsetArguments", "supersetArguments"})
    public void testIsIntersectedCorrectlyCompareIntersectedSets(Collection<String> result1, Collection<String> result2) {
        assertThat(resultValidator.isIntersected(result1, result2), is(!result1.isEmpty() && !result2.isEmpty()));
    }

    @ParameterizedTest
    @MethodSource({"nonIntersectArguments"})
    public void testIsIntersectedCorrectlyCompareNonIntersect(Collection<String> result1, Collection<String> result2) {
        assertThat(resultValidator.isIntersected(result1, result2), is(false));
    }

    @ParameterizedTest
    @MethodSource({"hasNullArguments"})
    public void testIsIntersectedNotAcceptNull(Collection<String> result1, Collection<String> result2) {
        assertThrows(NullPointerException.class, () -> resultValidator.isIntersected(result1, result2));
    }

    @ParameterizedTest
    @MethodSource({"hasNullArguments"})
    public void testIsSubsetNotAcceptNull(Collection<String> result1, Collection<String> result2) {
        assertThrows(NullPointerException.class, () -> resultValidator.isSubset(result1, result2));
    }

    @ParameterizedTest
    @MethodSource({"hasNullArguments"})
    public void testIsEqualsNotAcceptNull(Collection<String> result1, Collection<String> result2) {
        assertThrows(NullPointerException.class, () -> resultValidator.isEquals(result1, result2));
    }
}
