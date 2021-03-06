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

import com.iexec.common.replicate.ReplicateStatusCause;
import com.iexec.common.utils.FileHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockitoAnnotations;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.File;
import java.util.List;

import static com.iexec.common.precompute.PreComputeUtils.*;
import static com.iexec.common.utils.IexecEnvUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * This is a primary validation test for
 * pre-compute execution.
 */
@ExtendWith(SystemStubsExtension.class)
class PreComputeAppTests {

    private static final String REPO_URL = "https://raw.githubusercontent.com/" +
            "iExecBlockchainComputing/tee-worker-pre-compute/develop/src/test/resources/";

    private static final String CHAIN_TASK_ID = "0xabc";
    private static final String DATASET_FILENAME = "my-dataset";
    private static final String DATASET_URL = REPO_URL + "encrypted-data.bin";
    private static final String DATASET_CHECKSUM =
            "0x02a12ef127dcfbdb294a090c8f0b69a0ca30b7940fc36cabf971f488efd374d7";
    private static final String RESOURCES = "src/test/resources/";
    private static final String KEY_FILE = RESOURCES + "key.txt";
    private static final String PLAIN_DATA_FILE = RESOURCES + "plain-data.txt";

    private static final String INPUT_FILE_2_URL = REPO_URL + "plain-data.txt";
    private static final String INPUT_FILE_1_URL = REPO_URL + "key.txt";


    @TempDir
    File outputDir;

    PreComputeApp preComputeApp;

    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.openMocks(this);
        preComputeApp = spy(new PreComputeApp(CHAIN_TASK_ID));
    }

    //region run
     @Test
     void shouldRunSuccessfullyWithDataset(EnvironmentVariables environment) throws PreComputeException {
         environment.set(
                 IEXEC_TASK_ID, CHAIN_TASK_ID,
                 IEXEC_PRE_COMPUTE_OUT, outputDir.getAbsolutePath(),
                 IS_DATASET_REQUIRED, true,
                 IEXEC_DATASET_URL, DATASET_URL,
                 IEXEC_DATASET_KEY, FileHelper.readFile(KEY_FILE),
                 IEXEC_DATASET_CHECKSUM, DATASET_CHECKSUM,
                 IEXEC_DATASET_FILENAME, DATASET_FILENAME,
                 IEXEC_INPUT_FILES_NUMBER, 2,
                 IEXEC_INPUT_FILE_URL_PREFIX + "1", INPUT_FILE_1_URL,
                 IEXEC_INPUT_FILE_URL_PREFIX + "2", INPUT_FILE_2_URL
         );

         final byte[] encryptedDataset = "encryptedDataset".getBytes();
         final byte[] plainContent = "plainContent".getBytes();

         doNothing().when(preComputeApp).checkOutputFolder();
         doReturn(encryptedDataset).when(preComputeApp).downloadEncryptedDataset();
         doReturn(plainContent).when(preComputeApp).decryptDataset(encryptedDataset);
         doNothing().when(preComputeApp).savePlainDatasetFile(plainContent);
         doNothing().when(preComputeApp).downloadInputFiles();

         assertDoesNotThrow(() -> preComputeApp.run());
     }

     @Test
     void shouldRunSuccessfullyWithoutDataset(EnvironmentVariables environment) throws PreComputeException {
         environment.set(
                 IEXEC_TASK_ID, CHAIN_TASK_ID,
                 IEXEC_PRE_COMPUTE_OUT, outputDir.getAbsolutePath(),
                 IS_DATASET_REQUIRED, false,
                 IEXEC_INPUT_FILES_NUMBER, 2,
                 IEXEC_INPUT_FILE_URL_PREFIX + "1", INPUT_FILE_1_URL,
                 IEXEC_INPUT_FILE_URL_PREFIX + "2", INPUT_FILE_2_URL
         );

         doNothing().when(preComputeApp).checkOutputFolder();
         doNothing().when(preComputeApp).downloadInputFiles();

         assertDoesNotThrow(() -> preComputeApp.run());

         verify(preComputeApp, times(0)).downloadEncryptedDataset();
         verify(preComputeApp, times(0)).decryptDataset(any());
         verify(preComputeApp, times(0)).savePlainDatasetFile(any());
     }
     //endregion

    @Test
    void shouldFindOutputFolder() {
        doReturn(goodPreComputeArgs()).when(preComputeApp).getPreComputeArgs();
        assertDoesNotThrow(() -> preComputeApp.checkOutputFolder());
    }

    @Test
    void shouldThrowSinceOutputFolderNotFound() {
        PreComputeArgs preComputeArgs = goodPreComputeArgs();
        preComputeArgs.setOutputDir("bad-output-dir-path");
        doReturn(preComputeArgs).when(preComputeApp).getPreComputeArgs();
        PreComputeException e = assertThrows(
                PreComputeException.class,
                () -> preComputeApp.checkOutputFolder());
        assertThat(e.getExitCause()).isEqualTo(ReplicateStatusCause.PRE_COMPUTE_OUTPUT_FOLDER_NOT_FOUND);
    }

    @Test
    void shouldDownloadEncryptedDataset() throws Exception {
        doReturn(goodPreComputeArgs()).when(preComputeApp).getPreComputeArgs();
        byte[] actualContent = preComputeApp.downloadEncryptedDataset();
        byte[] expectedContent = FileHelper.readFileBytesFromUrl(DATASET_URL);
        assertThat(actualContent).isEqualTo(expectedContent);
    }

    @Test
    void shouldThrowSinceDownloadFailed() {
        PreComputeArgs preComputeArgs = goodPreComputeArgs();
        preComputeArgs.setEncryptedDatasetUrl("http://bad-url");
        doReturn(preComputeArgs).when(preComputeApp).getPreComputeArgs();
        PreComputeException e = assertThrows(
                PreComputeException.class,
                () -> preComputeApp.downloadEncryptedDataset());
        assertThat(e.getExitCause()).isEqualTo(ReplicateStatusCause.PRE_COMPUTE_DATASET_DOWNLOAD_FAILED);
    }

    @Test
    void shouldThrowSinceDatasetChecksumNotValid() {
        PreComputeArgs preComputeArgs = goodPreComputeArgs();
        preComputeArgs.setEncryptedDatasetChecksum("badChecksum");
        doReturn(preComputeArgs).when(preComputeApp).getPreComputeArgs();
        PreComputeException e = assertThrows(
                PreComputeException.class,
                () -> preComputeApp.downloadEncryptedDataset());
        assertThat(e.getExitCause()).isEqualTo(ReplicateStatusCause.PRE_COMPUTE_INVALID_DATASET_CHECKSUM);
    }

    @Test
    void shouldDecryptDataset() throws Exception {
        doReturn(goodPreComputeArgs()).when(preComputeApp).getPreComputeArgs();
        byte[] encryptedData = FileHelper.readFileBytesFromUrl(DATASET_URL);
        byte[] expectedPlainData = FileHelper.readAllBytes(PLAIN_DATA_FILE);
        byte[] actualPlainData = preComputeApp.decryptDataset(encryptedData);
        assertThat(actualPlainData).isEqualTo(expectedPlainData);
    }

    @Test
    void shouldThrowSinceDecryptionFailed() {
        PreComputeArgs preComputeArgs = goodPreComputeArgs();
        String badKey = FileHelper.readFile(KEY_FILE).replace("A", "B");
        preComputeArgs.setEncryptedDatasetBase64Key(badKey);
        byte[] encryptedData = FileHelper.readFileBytesFromUrl(DATASET_URL);
        doReturn(preComputeArgs).when(preComputeApp).getPreComputeArgs();
        PreComputeException e = assertThrows(
                PreComputeException.class,
                () -> preComputeApp.decryptDataset(encryptedData));
        assertThat(e.getExitCause()).isEqualTo(ReplicateStatusCause.PRE_COMPUTE_DATASET_DECRYPTION_FAILED);
    }

    @Test
    void shouldSavePlainDatasetFile() throws Exception {
        doReturn(goodPreComputeArgs()).when(preComputeApp).getPreComputeArgs();
        byte[] plainContent = FileHelper.readAllBytes(PLAIN_DATA_FILE);
        preComputeApp.savePlainDatasetFile(plainContent);
        assertThat(new File(outputDir, DATASET_FILENAME)).exists();
    }

    @Test
    void shouldThrowSinceFailedToSavePlainDataset() {
        PreComputeArgs preComputeArgs = goodPreComputeArgs();
        preComputeArgs.setOutputDir("/some-folder-123/not-found");
        doReturn(preComputeArgs).when(preComputeApp).getPreComputeArgs();
        PreComputeException e = assertThrows(PreComputeException.class,
                () -> preComputeApp.savePlainDatasetFile("data".getBytes()));
        assertThat(e.getExitCause()).isEqualTo(ReplicateStatusCause.PRE_COMPUTE_SAVING_PLAIN_DATASET_FAILED);
    }

    @Test
    void shouldDownloadInputFiles() throws Exception {
        doReturn(goodPreComputeArgs()).when(preComputeApp).getPreComputeArgs();
        preComputeApp.downloadInputFiles();
        assertThat(new File(outputDir, "plain-data.txt")).exists();
        assertThat(new File(outputDir, "key.txt")).exists();
    }

    @Test
    void shouldThrowSinceFailedToDownloadInputFiles() {
        PreComputeArgs preComputeArgs = goodPreComputeArgs();
        preComputeArgs.setInputFiles(List.of("http://bad-input-file-url"));
        doReturn(preComputeArgs).when(preComputeApp).getPreComputeArgs();
        PreComputeException e = assertThrows(PreComputeException.class,
                () -> preComputeApp.downloadInputFiles());
        assertThat(e.getExitCause()).isEqualTo(ReplicateStatusCause.PRE_COMPUTE_INPUT_FILE_DOWNLOAD_FAILED);
    }

    private PreComputeArgs goodPreComputeArgs() {
        return PreComputeArgs.builder()
                .chainTaskId(CHAIN_TASK_ID)
                .outputDir(outputDir.getAbsolutePath())
                .isDatasetRequired(true)
                .encryptedDatasetUrl(DATASET_URL)
                .encryptedDatasetBase64Key(FileHelper.readFile(KEY_FILE))
                .encryptedDatasetChecksum(DATASET_CHECKSUM)
                .plainDatasetFilename(DATASET_FILENAME)
                .inputFiles(List.of(INPUT_FILE_1_URL, INPUT_FILE_2_URL))
                .build();
    }

}
