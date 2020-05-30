package io.github.ducthienbui97.queryintegrity.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.OngoingStubbing;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class QueryTestingServiceTest {
    @Mock
    private ResultValidator<String> resultValidator;

    @Mock
    private QueryFactory<String, String> queryFactory;

    private QueryTestingService<String, String> queryTestingService;

    @BeforeEach
    public void setup() {
        queryTestingService = new QueryTestingService<>(queryFactory, resultValidator);
        when(queryFactory.build(any())).thenReturn("test");
        when(queryFactory.build()).thenReturn("test");
        when(queryFactory.toString(any())).thenCallRealMethod();
        when(resultValidator.isEquals(any(), any())).thenReturn(true);
        when(resultValidator.isIntersected(any(), any())).thenReturn(true);
        when(resultValidator.isSubset(any(), any())).thenReturn(true);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testEveryEqualRunTwoQueriesAreBuild(int runTime) {
        queryTestingService.runEqualTest(runTime);
        verify(queryFactory, times(runTime * 2)).build(any(QueryProxy.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testEveryNotRunTwoQueriesAreBuild(int runTime) {
        queryTestingService.runNotTest(runTime);
        verify(queryFactory, times(runTime * 2)).build(any(QueryProxy.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testEverySubsetRunTwoQueriesAreBuild(int runTime) {
        queryTestingService.runSubsetTest(runTime);
        verify(queryFactory, times(runTime * 2)).build(any(QueryProxy.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testEqualTestValidateEveryRun(int runTime) {
        queryTestingService.runEqualTest(runTime);
        verify(resultValidator, times(runTime)).isEquals(any(), any());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testNotTestValidateEveryRun(int runTime) {
        queryTestingService.runNotTest(runTime);
        verify(resultValidator, times(runTime)).isIntersected(any(), any());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testSubsetTestValidateEveryRun(int runTime) {
        queryTestingService.runSubsetTest(runTime);
        verify(resultValidator, times(runTime)).isSubset(any(), any());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testMinLeafNodeEqualTest(int leaf) {
        queryTestingService.setMaxLeafCount(5);
        queryTestingService.setMinLeafCount(leaf);
        queryTestingService.runEqualTest();
        verify(queryFactory, atLeast(queryTestingService.DEFAULT_TEST_COUNT * leaf)).build();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testMinLeafNodeNotTest(int leaf) {
        queryTestingService.setMaxLeafCount(5);
        queryTestingService.setMinLeafCount(leaf);
        queryTestingService.runNotTest();
        verify(queryFactory, atLeast(queryTestingService.DEFAULT_TEST_COUNT * leaf)).build();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testMinLeafNodeSubsetTest(int leaf) {
        queryTestingService.setMaxLeafCount(5);
        queryTestingService.setMinLeafCount(leaf);
        queryTestingService.runSubsetTest();
        verify(queryFactory, atLeast(2 * queryTestingService.DEFAULT_TEST_COUNT * leaf)).build();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testMaxLeafNodeEqualTest(int leaf) {
        queryTestingService.setMinLeafCount(5);
        queryTestingService.setMaxLeafCount(leaf);
        queryTestingService.runEqualTest();
        verify(queryFactory, atMost(queryTestingService.DEFAULT_TEST_COUNT * leaf)).build();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testMaxLeafNodeNotTest(int leaf) {
        queryTestingService.setMinLeafCount(5);
        queryTestingService.setMaxLeafCount(leaf);
        queryTestingService.runNotTest();
        verify(queryFactory, atMost(queryTestingService.DEFAULT_TEST_COUNT * leaf)).build();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testMaxLeafNodeSubsetTest(int leaf) {
        queryTestingService.setMinLeafCount(5);
        queryTestingService.setMaxLeafCount(leaf);
        queryTestingService.runSubsetTest();
        verify(queryFactory, atMost(2 * queryTestingService.DEFAULT_TEST_COUNT * leaf)).build();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testInvalidReportEqualTest(int invalidCount) {
        OngoingStubbing<Boolean> stubbing = when(resultValidator.isEquals(any(), any()));
        for (int i = 0; i < invalidCount; i++) {
            stubbing = stubbing.thenReturn(false);
        }
        stubbing.thenReturn(true);
        assertThat(queryTestingService.runEqualTest(), is(invalidCount));
        verify(resultValidator, times(queryTestingService.DEFAULT_TEST_COUNT)).isEquals(any(), any());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testInvalidReportNotTest(int invalidCount) {
        OngoingStubbing<Boolean> stubbing = when(resultValidator.isIntersected(any(), any()));
        for (int i = 0; i < invalidCount; i++) {
            stubbing = stubbing.thenReturn(true);
        }
        stubbing.thenReturn(false);
        assertThat(queryTestingService.runNotTest(), is(invalidCount));
        verify(resultValidator, times(queryTestingService.DEFAULT_TEST_COUNT)).isIntersected(any(), any());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testInvalidReportSubsetTest(int invalidCount) {
        OngoingStubbing<Boolean> stubbing = when(resultValidator.isSubset(any(), any()));
        for (int i = 0; i < invalidCount; i++) {
            stubbing = stubbing.thenReturn(false);
        }
        stubbing.thenReturn(true);
        assertThat(queryTestingService.runSubsetTest(), is(invalidCount));
        verify(resultValidator, times(queryTestingService.DEFAULT_TEST_COUNT)).isSubset(any(), any());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testUsingDefaultResultValidatorIsEquals(int inValidCount) {
        OngoingStubbing<Collection<String>> stubbing = when(queryFactory.getResult(any()));
        for (int i = 0; i < inValidCount; i++) {
            stubbing = stubbing.thenReturn(Collections.singletonList("unMatched1"))
                    .thenReturn(Collections.singletonList("unMatched2"));
        }
        stubbing.thenReturn(Collections.emptyList());
        queryTestingService = new QueryTestingService<>(queryFactory);
        assertThat(queryTestingService.runEqualTest(), is(inValidCount));
        verify(queryFactory, times(queryTestingService.DEFAULT_TEST_COUNT * 2)).getResult(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testUsingDefaultResultValidatorIsIntersected(int inValidCount) {
        OngoingStubbing<Collection<String>> stubbing = when(queryFactory.getResult(any()));
        for (int i = 0; i < queryTestingService.DEFAULT_TEST_COUNT; i++) {
            stubbing = stubbing.thenReturn(Arrays.asList("test1", "test2", "test3"));
            if (i >= inValidCount) {
                stubbing = stubbing.thenReturn(Arrays.asList("test5", "test6", "test7"));
            } else {
                stubbing = stubbing.thenReturn(Arrays.asList("test2", "test3", "test4"));
            }
        }
        queryTestingService = new QueryTestingService<>(queryFactory);
        assertThat(queryTestingService.runNotTest(), is(inValidCount));
        verify(queryFactory, times(queryTestingService.DEFAULT_TEST_COUNT * 2)).getResult(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testUsingDefaultResultValidatorIsSubset(int inValidCount) {
        OngoingStubbing<Collection<String>> stubbing = when(queryFactory.getResult(any()));
        for (int i = 0; i < queryTestingService.DEFAULT_TEST_COUNT; i++) {
            stubbing = stubbing.thenReturn(Arrays.asList("test1", "test2", "test3"));
            if (i >= inValidCount) {
                stubbing = stubbing.thenReturn(Arrays.asList("test3", "test2", "test1"));
            } else {
                stubbing = stubbing.thenReturn(Arrays.asList("test2", "test3", "test7"));
            }
        }
        queryTestingService = new QueryTestingService<>(queryFactory);
        assertThat(queryTestingService.runSubsetTest(), is(inValidCount));
        verify(queryFactory, times(queryTestingService.DEFAULT_TEST_COUNT * 2)).getResult(any());
    }
}
