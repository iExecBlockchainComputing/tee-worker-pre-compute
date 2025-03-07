/*
 * Copyright 2022-2025 IEXEC BLOCKCHAIN TECH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iexec.worker.compute.pre;

import com.iexec.common.replicate.ReplicateStatusCause;
import com.iexec.common.worker.tee.TeeSessionEnvironmentVariable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static com.iexec.common.utils.IexecEnvUtils.IEXEC_INPUT_FILE_URL_PREFIX;
import static com.iexec.common.worker.tee.TeeSessionEnvironmentVariable.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SystemStubsExtension.class)
class PreComputeArgsTests {

    private static final String CHAIN_TASK_ID = "0xabc123";
    private static final String OUTPUT_DIR = "/iexec_out";
    private static final String DATASET_URL = "https://dataset.url";
    private static final String DATASET_KEY = "datasetKey123";
    private static final String DATASET_CHECKSUM = "0x123checksum";
    private static final String DATASET_FILENAME = "dataset.txt";
    private static final String INPUT_FILE_URL_1 = "https://input1.url";
    private static final String INPUT_FILE_URL_2 = "https://input2.url";

    @Test
    void shouldReadArgsWithoutDataset(EnvironmentVariables environment) throws Exception {
        environment.set(IEXEC_PRE_COMPUTE_OUT, OUTPUT_DIR);
        environment.set(IS_DATASET_REQUIRED, "false");
        environment.set(IEXEC_INPUT_FILES_NUMBER, "1");
        environment.set(IEXEC_INPUT_FILE_URL_PREFIX + "1", INPUT_FILE_URL_1);
        final PreComputeArgs args = PreComputeArgs.readArgs(CHAIN_TASK_ID);
        assertEquals(CHAIN_TASK_ID, args.getChainTaskId());
        assertEquals(OUTPUT_DIR, args.getOutputDir());
        assertFalse(args.isDatasetRequired());
        assertEquals(1, args.getInputFiles().size());
        assertEquals(INPUT_FILE_URL_1, args.getInputFiles().get(0));
    }

    @Test
    void shouldReadArgsWithDataset(EnvironmentVariables environment) throws Exception {
        environment.set(IEXEC_PRE_COMPUTE_OUT, OUTPUT_DIR);
        environment.set(IS_DATASET_REQUIRED, "true");
        environment.set(IEXEC_DATASET_URL, DATASET_URL);
        environment.set(IEXEC_DATASET_KEY, DATASET_KEY);
        environment.set(IEXEC_DATASET_CHECKSUM, DATASET_CHECKSUM);
        environment.set(IEXEC_DATASET_FILENAME, DATASET_FILENAME);
        environment.set(IEXEC_INPUT_FILES_NUMBER, "0");
        final PreComputeArgs args = PreComputeArgs.readArgs(CHAIN_TASK_ID);
        assertEquals(CHAIN_TASK_ID, args.getChainTaskId());
        assertEquals(OUTPUT_DIR, args.getOutputDir());
        assertTrue(args.isDatasetRequired());
        assertEquals(DATASET_URL, args.getEncryptedDatasetUrl());
        assertEquals(DATASET_KEY, args.getEncryptedDatasetBase64Key());
        assertEquals(DATASET_CHECKSUM, args.getEncryptedDatasetChecksum());
        assertEquals(DATASET_FILENAME, args.getPlainDatasetFilename());
        assertEquals(0, args.getInputFiles().size());
    }

    @Test
    void shouldReadArgsWithMultipleInputFiles(EnvironmentVariables environment) throws Exception {
        environment.set(IEXEC_PRE_COMPUTE_OUT, OUTPUT_DIR);
        environment.set(IS_DATASET_REQUIRED, "false");
        environment.set(IEXEC_INPUT_FILES_NUMBER, "2");
        environment.set(IEXEC_INPUT_FILE_URL_PREFIX + "1", INPUT_FILE_URL_1);
        environment.set(IEXEC_INPUT_FILE_URL_PREFIX + "2", INPUT_FILE_URL_2);
        final PreComputeArgs args = PreComputeArgs.readArgs(CHAIN_TASK_ID);
        assertEquals(2, args.getInputFiles().size());
        assertEquals(INPUT_FILE_URL_1, args.getInputFiles().get(0));
        assertEquals(INPUT_FILE_URL_2, args.getInputFiles().get(1));
    }

    @ParameterizedTest
    @EnumSource(
            value = TeeSessionEnvironmentVariable.class,
            names = {"IEXEC_PRE_COMPUTE_OUT", "IS_DATASET_REQUIRED", "IEXEC_INPUT_FILES_NUMBER"}
    )
    void shouldThrowWhenRequiredEnvVarMissing(TeeSessionEnvironmentVariable missingVar, EnvironmentVariables environment) {
        if (!missingVar.equals(IEXEC_PRE_COMPUTE_OUT)) {
            environment.set(IEXEC_PRE_COMPUTE_OUT, OUTPUT_DIR);
        }
        if (!missingVar.equals(IS_DATASET_REQUIRED)) {
            environment.set(IS_DATASET_REQUIRED, "false");
        }
        if (!missingVar.equals(IEXEC_INPUT_FILES_NUMBER)) {
            environment.set(IEXEC_INPUT_FILES_NUMBER, "0");
        }
        PreComputeException exception = assertThrows(PreComputeException.class, () -> PreComputeArgs.readArgs(CHAIN_TASK_ID));
        if (missingVar == IEXEC_PRE_COMPUTE_OUT) {
            assertEquals(ReplicateStatusCause.PRE_COMPUTE_OUTPUT_PATH_MISSING, exception.getExitCause());
        } else if (missingVar == IS_DATASET_REQUIRED) {
            assertEquals(ReplicateStatusCause.PRE_COMPUTE_IS_DATASET_REQUIRED_MISSING, exception.getExitCause());
        } else if (missingVar == IEXEC_INPUT_FILES_NUMBER) {
            assertEquals(ReplicateStatusCause.PRE_COMPUTE_INPUT_FILES_NUMBER_MISSING, exception.getExitCause());
        }
    }

    @ParameterizedTest
    @EnumSource(
            value = TeeSessionEnvironmentVariable.class,
            names = {"IEXEC_DATASET_URL", "IEXEC_DATASET_KEY", "IEXEC_DATASET_CHECKSUM", "IEXEC_DATASET_FILENAME"}
    )
    void shouldThrowWhenDatasetEnvVarMissing(TeeSessionEnvironmentVariable missingVar, EnvironmentVariables environment) {
        environment.set(IEXEC_PRE_COMPUTE_OUT, OUTPUT_DIR);
        environment.set(IS_DATASET_REQUIRED, "true");
        environment.set(IEXEC_INPUT_FILES_NUMBER, "0");
        if (!missingVar.equals(IEXEC_DATASET_URL)) {
            environment.set(IEXEC_DATASET_URL, DATASET_URL);
        }
        if (!missingVar.equals(IEXEC_DATASET_KEY)) {
            environment.set(IEXEC_DATASET_KEY, DATASET_KEY);
        }
        if (!missingVar.equals(IEXEC_DATASET_CHECKSUM)) {
            environment.set(IEXEC_DATASET_CHECKSUM, DATASET_CHECKSUM);
        }
        if (!missingVar.equals(IEXEC_DATASET_FILENAME)) {
            environment.set(IEXEC_DATASET_FILENAME, DATASET_FILENAME);
        }
        PreComputeException exception = assertThrows(PreComputeException.class,
                () -> PreComputeArgs.readArgs(CHAIN_TASK_ID));
        if (missingVar.equals(IEXEC_DATASET_URL)) {
            assertEquals(ReplicateStatusCause.PRE_COMPUTE_DATASET_URL_MISSING, exception.getExitCause());
        } else if (missingVar.equals(IEXEC_DATASET_KEY)) {
            assertEquals(ReplicateStatusCause.PRE_COMPUTE_DATASET_KEY_MISSING, exception.getExitCause());
        } else if (missingVar.equals(IEXEC_DATASET_CHECKSUM)) {
            assertEquals(ReplicateStatusCause.PRE_COMPUTE_DATASET_CHECKSUM_MISSING, exception.getExitCause());
        } else if (missingVar.equals(IEXEC_DATASET_FILENAME)) {
            assertEquals(ReplicateStatusCause.PRE_COMPUTE_DATASET_FILENAME_MISSING, exception.getExitCause());
        }
    }

    @Test
    void shouldThrowWhenInputFileUrlMissing(EnvironmentVariables environment) {
        environment.set(IEXEC_PRE_COMPUTE_OUT, OUTPUT_DIR);
        environment.set(IS_DATASET_REQUIRED, "false");
        environment.set(IEXEC_INPUT_FILES_NUMBER, "1");
        PreComputeException exception = assertThrows(PreComputeException.class,
                () -> PreComputeArgs.readArgs(CHAIN_TASK_ID));
        assertEquals(ReplicateStatusCause.PRE_COMPUTE_AT_LEAST_ONE_INPUT_FILE_URL_MISSING, exception.getExitCause());
    }

    @Test
    void shouldCreateEmptyInputFilesListWhenNumberIsZero(EnvironmentVariables environment) throws Exception {
        environment.set(IEXEC_PRE_COMPUTE_OUT, OUTPUT_DIR);
        environment.set(IS_DATASET_REQUIRED, "false");
        environment.set(IEXEC_INPUT_FILES_NUMBER, "0");
        PreComputeArgs args = PreComputeArgs.readArgs(CHAIN_TASK_ID);
        assertNotNull(args.getInputFiles());
        assertEquals(0, args.getInputFiles().size());
    }

    @Test
    void shouldHandleInvalidInputFilesNumberFormat(EnvironmentVariables environment) {
        environment.set(IEXEC_PRE_COMPUTE_OUT, OUTPUT_DIR);
        environment.set(IS_DATASET_REQUIRED, "false");
        environment.set(IEXEC_INPUT_FILES_NUMBER, "not-a-number");
        assertThrows(NumberFormatException.class, () -> PreComputeArgs.readArgs(CHAIN_TASK_ID));
    }
}
