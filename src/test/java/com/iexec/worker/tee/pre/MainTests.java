package com.iexec.worker.tee.pre;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.org.webcompere.systemstubs.SystemStubs.catchSystemExit;

class MainTests {

    @BeforeEach
    void init() {
        Main.preComputeAppRunner = null;
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void shouldExitWithCode(int expectedExitCode) throws Exception {
        final PreComputeAppRunner preComputeAppRunner = mock(PreComputeAppRunner.class);
        when(preComputeAppRunner.start()).thenReturn(expectedExitCode);
        Main.preComputeAppRunner = preComputeAppRunner;

        final int actualExitCode = catchSystemExit(() -> Main.main(null));
        assertEquals(expectedExitCode, actualExitCode);
    }

    @Test
    void shouldGetExistingPreComputeAppRunner() {
        final PreComputeAppRunner preComputeAppRunner = new PreComputeAppRunner();
        Main.preComputeAppRunner = preComputeAppRunner;

        assertSame(preComputeAppRunner, Main.getPreComputeAppRunner());
    }

    @Test
    void shouldCreatePreComputeAppRunner() {
        assertNotNull(Main.getPreComputeAppRunner());
    }
}