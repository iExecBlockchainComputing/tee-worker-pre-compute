package com.iexec.worker.tee.pre;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.org.webcompere.systemstubs.SystemStubs.catchSystemExit;

class MainTests {

    @Mock
    PreComputeAppRunner preComputeAppRunner;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void shouldExitWithCode(int expectedExitCode) throws Exception {
        when(preComputeAppRunner.start()).thenReturn(expectedExitCode);
        try (MockedStatic<PreComputeAppRunner> runner = Mockito.mockStatic(PreComputeAppRunner.class)) {
            runner.when(PreComputeAppRunner::build).thenReturn(preComputeAppRunner);

            final int actualExitCode = catchSystemExit(Main::main);
            assertEquals(expectedExitCode, actualExitCode);
        }
    }

}