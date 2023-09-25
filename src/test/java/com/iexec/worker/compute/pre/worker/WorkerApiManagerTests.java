package com.iexec.worker.compute.pre.worker;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class WorkerApiManagerTests {
    @Test
    void shouldNotCreateNewInstance() throws NoSuchMethodException {
        final Constructor<WorkerApiManager> constructor = WorkerApiManager.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        final InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(exception.getTargetException() instanceof UnsupportedOperationException);
    }

    @Test
    void shouldGetExistingWorkerApiClient() throws NoSuchFieldException, IllegalAccessException {
        final WorkerApiClient workerApiClient = mock(WorkerApiClient.class);
        final Field workerApiClientField = WorkerApiManager.class.getDeclaredField("workerApiClient");
        workerApiClientField.setAccessible(true);
        workerApiClientField.set(null, workerApiClient);

        assertSame(workerApiClient, WorkerApiManager.getWorkerApiClient());
    }

    @Test
    void shouldCreateWorkerApiClient() throws NoSuchFieldException, IllegalAccessException {
        final Field workerApiClientField = WorkerApiManager.class.getDeclaredField("workerApiClient");
        workerApiClientField.setAccessible(true);
        workerApiClientField.set(null, null);

        assertNull(workerApiClientField.get(null));

        final WorkerApiClient workerApiClient = WorkerApiManager.getWorkerApiClient();
        assertNotNull(workerApiClient);
    }
}