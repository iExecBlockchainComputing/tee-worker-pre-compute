/*
 * Copyright 2020 IEXEC BLOCKCHAIN TECH
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

package com.iexec.worker.tee.pre;

import com.iexec.common.precompute.PreComputeExitCode;
import com.iexec.common.precompute.PreComputeUtils;
import com.iexec.common.utils.FileHelper;
import com.iexec.common.utils.IexecEnvUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

/**
 * This is a primary validation test for
 * pre-compute execution.
 */
class PreComputeAppTests {

    private static final String CHAIN_TASK_ID = "0xabc";
    private static final String DATASET_FILENAME = "my-dataset";
    private static final String DATASET_URL = "https://raw.githubusercontent.com/" +
            "iExecBlockchainComputing/tee-worker-pre-compute/" +
            "develop/src/test/resources/encrypted-data.bin";
    private static final String DATASET_CHECKSUM =
            "0x02a12ef127dcfbdb294a090c8f0b69a0ca30b7940fc36cabf971f488efd374d7";
    private static final String RESOURCES = "src/test/resources/";
    private static final String KEY_FILE = RESOURCES + "key.txt";
    private static final String PLAIN_DATA_FILE = RESOURCES + "plain-data.txt";

    @TempDir
    File outputDir;

    @Spy
    PreComputeApp preComputeApp = new PreComputeApp();

    @BeforeEach
    void beforeEach() throws Exception {
        // mock System.getenv() calls
        MockitoAnnotations.openMocks(this);
        doReturn(CHAIN_TASK_ID).when(preComputeApp)
                .getEnvVarOrThrow(IexecEnvUtils.IEXEC_TASK_ID_ENV_PROPERTY);
        doReturn(outputDir.getAbsolutePath()).when(preComputeApp)
                .getEnvVarOrThrow(PreComputeUtils.IEXEC_PRE_COMPUTE_OUT);
        doReturn(DATASET_URL).when(preComputeApp)
                .getEnvVarOrThrow(PreComputeUtils.IEXEC_DATASET_URL);
        String base64DatasetKey = FileHelper.readFile(KEY_FILE);
        doReturn(base64DatasetKey).when(preComputeApp)
                .getEnvVarOrThrow(PreComputeUtils.IEXEC_DATASET_KEY);
        doReturn(DATASET_CHECKSUM).when(preComputeApp)
                .getEnvVarOrThrow(PreComputeUtils.IEXEC_DATASET_CHECKSUM);
        doReturn(DATASET_FILENAME).when(preComputeApp)
                .getEnvVarOrThrow(IexecEnvUtils.IEXEC_DATASET_FILENAME_ENV_PROPERTY);
    }


    @Test
    void shouldRunSuccessfully() throws Exception {
        assertDoesNotThrow(() -> preComputeApp.run());
        String decryptedFilepath = outputDir.getAbsolutePath() + "/" + DATASET_FILENAME;
        String decryptedFileContent = FileHelper.readFile(decryptedFilepath);
        String originalFileContent = FileHelper.readFile(PLAIN_DATA_FILE);
        assertThat(decryptedFileContent).isEqualTo(originalFileContent);
    }

    @Test
    void shouldThrowAndExitWhenEnvVarIsEmpty() throws Exception {
        doCallRealMethod().when(preComputeApp)
                .getEnvVarOrThrow(IexecEnvUtils.IEXEC_TASK_ID_ENV_PROPERTY);
        runAndCheckErrorCode(PreComputeExitCode.EMPTY_REQUIRED_ENV_VAR);
    }

    @Test
    void shouldThrowAndExitWithOutputFolderNotFound() throws Exception {
        doReturn("bad-output-dir-path").when(preComputeApp)
                .getEnvVarOrThrow(PreComputeUtils.IEXEC_PRE_COMPUTE_OUT);
        runAndCheckErrorCode(PreComputeExitCode.OUTPUT_FOLDER_NOT_FOUND);
    }

    @Test
    void shouldThrowAndExitWithDownloadFailed() throws Exception {
        doReturn("http://bad-url").when(preComputeApp)
                .getEnvVarOrThrow(PreComputeUtils.IEXEC_DATASET_URL);
        runAndCheckErrorCode(PreComputeExitCode.DATASET_DOWNLOAD_FAILED);
    }

    @Test
    void shouldThrowAndExitWithDatasetChecksumNotValid() throws Exception {
        doReturn("badChecksum").when(preComputeApp)
                .getEnvVarOrThrow(PreComputeUtils.IEXEC_DATASET_CHECKSUM);
        runAndCheckErrorCode(PreComputeExitCode.INVALID_DATASET_CHECKSUM);
    }

    @Test
    void shouldThrowAndExitWithDecryptionFailed() throws Exception {
        String badBase64DatasetKey = FileHelper.readFile(KEY_FILE).replace("A", "B");
        doReturn(badBase64DatasetKey).when(preComputeApp)
                .getEnvVarOrThrow(PreComputeUtils.IEXEC_DATASET_KEY);
        runAndCheckErrorCode(PreComputeExitCode.DATASET_DECRYPTION_FAILED);
    }

    private void runAndCheckErrorCode(PreComputeExitCode code) {
        try {
            preComputeApp.run();
            assertThat(true).isFalse(); // should fail if reached
        } catch (PreComputeException e) {
            assertThat(e.getExitCode()).isEqualTo(code);
        }
    }
}
