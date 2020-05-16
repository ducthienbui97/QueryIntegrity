import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.OngoingStubbing;

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

    @InjectMocks
    private QueryTestingService<String, String> queryTestingService;

    @BeforeEach
    public void setup() {
        when(queryFactory.build(any())).thenReturn("test");
        when(queryFactory.build()).thenReturn("test");
        when(resultValidator.isEquals(any(), any())).thenReturn(true);
        when(resultValidator.isIntersected(any(), any())).thenReturn(true);
        when(resultValidator.isSubset(any(), any())).thenReturn(true);
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 15, 1, 20})
    public void testEveryEqualRunTwoQueriesAreBuild(int runTime) {
        queryTestingService.runEqualTest(runTime);
        verify(queryFactory, times(runTime * 2)).build(any(QueryProxy.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 15, 1, 20})
    public void testEveryNotRunTwoQueriesAreBuild(int runTime) {
        queryTestingService.runNotTest(runTime);
        verify(queryFactory, times(runTime * 2)).build(any(QueryProxy.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 15, 1, 20})
    public void testEverySubsetRunTwoQueriesAreBuild(int runTime) {
        queryTestingService.runSubsetTest(runTime);
        verify(queryFactory, times(runTime * 2)).build(any(QueryProxy.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 15, 1, 20})
    public void testEqualTestValidateEveryRun(int runTime) {
        queryTestingService.runEqualTest(runTime);
        verify(resultValidator, times(runTime)).isEquals(any(), any());
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 15, 1, 20})
    public void testNotTestValidateEveryRun(int runTime) {
        queryTestingService.runNotTest(runTime);
        verify(resultValidator, times(runTime)).isIntersected(any(), any());
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 15, 1, 20})
    public void testSubsetTestValidateEveryRun(int runTime) {
        queryTestingService.runSubsetTest(runTime);
        verify(resultValidator, times(runTime)).isSubset(any(), any());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testMinLeafNodeEqualTest(int leaf) {
        queryTestingService.setMinLeafCount(leaf);
        queryTestingService.runEqualTest();
        verify(queryFactory, atLeast(queryTestingService.DEFAULT_TEST_COUNT * leaf)).build();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testMinLeafNodeNotTest(int leaf) {
        queryTestingService.setMinLeafCount(leaf);
        queryTestingService.runNotTest();
        verify(queryFactory, atLeast(queryTestingService.DEFAULT_TEST_COUNT * leaf)).build();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testMinLeafNodeSubsetTest(int leaf) {
        queryTestingService.setMinLeafCount(leaf);
        queryTestingService.runSubsetTest();
        verify(queryFactory, atLeast(2 * queryTestingService.DEFAULT_TEST_COUNT * leaf)).build();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testMaxLeafNodeEqualTest(int leaf) {
        queryTestingService.setMaxLeafCount(leaf);
        queryTestingService.runEqualTest();
        verify(queryFactory, atMost(queryTestingService.DEFAULT_TEST_COUNT * leaf)).build();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testMaxLeafNodeNotTest(int leaf) {
        queryTestingService.setMaxLeafCount(leaf);
        queryTestingService.runNotTest();
        verify(queryFactory, atMost(queryTestingService.DEFAULT_TEST_COUNT * leaf)).build();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void testMaxLeafNodeSubsetTest(int leaf) {
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
            stubbing = stubbing.thenReturn(false);
        }
        stubbing.thenReturn(true);
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
}
