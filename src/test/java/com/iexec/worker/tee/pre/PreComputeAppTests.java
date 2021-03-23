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

class PreComputeAppTests {

    private static final String DIR = "src/test/resources/";
    private static final String ENC_FILE_NAME = "encrypted-data.bin";
    private static final String ENC_FILE_PATH = DIR + ENC_FILE_NAME;
    private static final String KEY_FILE = DIR + "key.txt";
    private static final String PLAIN_FILE = DIR + "plain-data.txt";

    @TempDir
    File tempDir;

    @Spy
    PreComputeApp preComputeApp = new PreComputeApp();

    @BeforeEach
    void beforeEach() throws Exception {
        // set env variables
        String chainTaskId = "chainTaskId";
        String iexecInFolder = tempDir.getAbsolutePath();
        String base64DatasetKey = FileHelper.readFile(KEY_FILE);
        String datasetChecksum = "0x02a12ef127dcfbdb294a090c8f0b69a0ca30b7940fc36cabf971f488efd374d7";
        String datasetFilename = ENC_FILE_NAME;
        // copy encrypted file in temp dir (iexec_in)
        FileHelper.copyFile(ENC_FILE_PATH, iexecInFolder + "/" + ENC_FILE_NAME);
        // mock System.getenv() calls
        MockitoAnnotations.openMocks(this);
        doReturn(chainTaskId).when(preComputeApp)
                .getEnvVarOrThrow(IexecEnvUtils.IEXEC_TASK_ID_ENV_PROPERTY);
        doReturn(iexecInFolder).when(preComputeApp)
                .getEnvVarOrThrow(IexecEnvUtils.IEXEC_IN_ENV_PROPERTY);
        doReturn(base64DatasetKey).when(preComputeApp)
                .getEnvVarOrThrow(PreComputeUtils.IEXEC_DATASET_KEY_PROPERTY);
        doReturn(datasetChecksum).when(preComputeApp)
                .getEnvVarOrThrow(PreComputeUtils.IEXEC_DATASET_CHECKSUM_PROPERTY);
        doReturn(datasetFilename).when(preComputeApp)
                .getEnvVarOrThrow(IexecEnvUtils.IEXEC_DATASET_FILENAME_ENV_PROPERTY);
    }


    @Test
    void shouldStart() throws Exception {
        assertDoesNotThrow(() -> preComputeApp.start());
        String originalFileContent = FileHelper.readFile(PLAIN_FILE);
        String decryptedFileContent =
                FileHelper.readFile(tempDir.getAbsolutePath() + "/" + ENC_FILE_NAME);
        assertThat(decryptedFileContent).isEqualTo(originalFileContent);
    }

    @Test
    void shouldThrowAndExitWithEnvVarIsEmpty() throws Exception {
        doCallRealMethod().when(preComputeApp)
                .getEnvVarOrThrow(IexecEnvUtils.IEXEC_TASK_ID_ENV_PROPERTY);
        runAndCheckExitCode(PreComputeExitCode.EMPTY_REQUIRED_ENV_VAR);
    }

    @Test
    void shouldThrowAndExitWithInputFolderNotFound() throws Exception {
        doReturn("badPath").when(preComputeApp)
                .getEnvVarOrThrow(IexecEnvUtils.IEXEC_IN_ENV_PROPERTY);
        runAndCheckExitCode(PreComputeExitCode.INPUT_FOLDER_NOT_FOUND);
    }

    @Test
    void shouldThrowAndExitWithDatasetFileNotFound() throws Exception {
        doReturn("badName").when(preComputeApp)
                .getEnvVarOrThrow(IexecEnvUtils.IEXEC_DATASET_FILENAME_ENV_PROPERTY);
        runAndCheckExitCode(PreComputeExitCode.DATASET_FILE_NOT_FOUND);
    }

    @Test
    void shouldThrowAndExitWithDatasetChecksumNotValid() throws Exception {
        doReturn("badChecksum").when(preComputeApp)
                .getEnvVarOrThrow(PreComputeUtils.IEXEC_DATASET_CHECKSUM_PROPERTY);
        runAndCheckExitCode(PreComputeExitCode.INVALID_DATASET_CHECKSUM);
    }

    @Test
    void shouldThrowAndExitWithEncryptionError() throws Exception {
        String badBase64DatasetKey = FileHelper.readFile(KEY_FILE).replace("A", "B");
        doReturn(badBase64DatasetKey).when(preComputeApp)
                .getEnvVarOrThrow(PreComputeUtils.IEXEC_DATASET_KEY_PROPERTY);
        runAndCheckExitCode(PreComputeExitCode.DATASET_DECRYPTION_ERROR);
    }

    private void runAndCheckExitCode(PreComputeExitCode code) {
        try {
            preComputeApp.start();
            assertThat(true).isFalse(); // should fail if reached
        } catch (PreComputeException e) {
            assertThat(e.getExitCode()).isEqualTo(code);
        }
    }
}
