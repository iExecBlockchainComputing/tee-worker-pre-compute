/*
 * Copyright 2020-2025 IEXEC BLOCKCHAIN TECH
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
import com.iexec.common.utils.FileHashUtils;
import com.iexec.common.utils.FileHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.File;
import java.net.URL;
import java.util.List;

import static com.iexec.common.utils.IexecEnvUtils.IEXEC_INPUT_FILE_URL_PREFIX;
import static com.iexec.common.worker.tee.TeeSessionEnvironmentVariable.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * This is a primary validation test for
 * pre-compute execution.
 */
@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class PreComputeAppTests {

    private static final String REPO_URL = "https://raw.githubusercontent.com/" +
            "iExecBlockchainComputing/tee-worker-pre-compute/main/src/test/resources/";

    private static final String CHAIN_TASK_ID = "0xabc";
    private static final String DATASET_FILENAME = "my-dataset";
    private static final String DATASET_RESOURCE_NAME = "encrypted-data.bin";
    private static final String HTTP_DATASET_URL = REPO_URL + DATASET_RESOURCE_NAME;
    private static final String IPFS_DATASET_URL = "/ipfs/QmUbh7ugQ9WVprTVYjzrCS4d9cCy73zUz4MMchsrqzzu1w";
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
        preComputeApp = spy(new PreComputeApp(CHAIN_TASK_ID));
    }

    //region run
    @Test
    void shouldRunSuccessfullyWithDataset(EnvironmentVariables environment) throws PreComputeException {
        environment.set(
                IEXEC_TASK_ID, CHAIN_TASK_ID,
                IEXEC_PRE_COMPUTE_OUT, outputDir.getAbsolutePath(),
                IS_DATASET_REQUIRED, true,
                IEXEC_DATASET_URL, HTTP_DATASET_URL,
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

        verify(preComputeApp, never()).downloadEncryptedDataset();
        verify(preComputeApp, never()).decryptDataset(any());
        verify(preComputeApp, never()).savePlainDatasetFile(any());
    }
    //endregion

    @Test
    void shouldFindOutputFolder() {
        final PreComputeArgs preComputeArgs = getPreComputeArgsBuilder(HTTP_DATASET_URL).build();
        doReturn(preComputeArgs).when(preComputeApp).getPreComputeArgs();
        assertDoesNotThrow(() -> preComputeApp.checkOutputFolder());
    }

    @Test
    void shouldThrowSinceOutputFolderNotFound() {
        final PreComputeArgs preComputeArgs = getPreComputeArgsBuilder(HTTP_DATASET_URL)
                .outputDir("bad-output-dir-path")
                .build();
        doReturn(preComputeArgs).when(preComputeApp).getPreComputeArgs();
        PreComputeException e = assertThrows(
                PreComputeException.class,
                () -> preComputeApp.checkOutputFolder());
        assertThat(e.getExitCause()).isEqualTo(ReplicateStatusCause.PRE_COMPUTE_OUTPUT_FOLDER_NOT_FOUND);
    }

    @Test
    void shouldDownloadEncryptedDataset() throws Exception {
        final PreComputeArgs preComputeArgs = getPreComputeArgsBuilder(HTTP_DATASET_URL).build();
        doReturn(preComputeArgs).when(preComputeApp).getPreComputeArgs();
        byte[] actualContent = preComputeApp.downloadEncryptedDataset();
        byte[] expectedContent = FileHelper.readFileBytesFromUrl(HTTP_DATASET_URL);
        assertThat(actualContent).isEqualTo(expectedContent);
    }

    @Test
    void shouldThrowSinceDownloadFailed() {
        final PreComputeArgs preComputeArgs = getPreComputeArgsBuilder(HTTP_DATASET_URL)
                .encryptedDatasetUrl("http://bad-url")
                .build();
        doReturn(preComputeArgs).when(preComputeApp).getPreComputeArgs();
        PreComputeException e = assertThrows(
                PreComputeException.class,
                () -> preComputeApp.downloadEncryptedDataset());
        assertThat(e.getExitCause()).isEqualTo(ReplicateStatusCause.PRE_COMPUTE_DATASET_DOWNLOAD_FAILED);
    }

    @Test
    void shouldDownloadEncryptedDatasetFromIexecGateway() throws PreComputeException {
        final PreComputeArgs preComputeArgs = getPreComputeArgsBuilder(IPFS_DATASET_URL).build();
        doReturn(preComputeArgs).when(preComputeApp).getPreComputeArgs();
        final URL resourceFile = this.getClass().getClassLoader().getResource(DATASET_RESOURCE_NAME);
        assertThat(resourceFile).isNotNull();
        final byte[] expectedBytes = FileHelper.readAllBytes(resourceFile.getFile());
        try (MockedStatic<FileHelper> fileHelperMock = mockStatic(FileHelper.class)) {
            fileHelperMock.when(() -> FileHelper.readFileBytesFromUrl(anyString()))
                    .thenReturn(expectedBytes);
            assertThat(preComputeApp.downloadEncryptedDataset()).isNotNull();
            fileHelperMock.verify(() -> FileHelper.readFileBytesFromUrl(anyString()), times(1));
        }
    }

    @Test
    void shouldDownloadEncryptedDatasetFromIpfsGateway() throws PreComputeException {
        final PreComputeArgs preComputeArgs = getPreComputeArgsBuilder(IPFS_DATASET_URL).build();
        doReturn(preComputeArgs).when(preComputeApp).getPreComputeArgs();
        final URL resourceFile = this.getClass().getClassLoader().getResource(DATASET_RESOURCE_NAME);
        assertThat(resourceFile).isNotNull();
        final byte[] expectedBytes = FileHelper.readAllBytes(resourceFile.getFile());
        try (MockedStatic<FileHelper> fileHelperMock = mockStatic(FileHelper.class)) {
            fileHelperMock.when(() -> FileHelper.readFileBytesFromUrl(anyString()))
                    .thenReturn(null)
                    .thenReturn(expectedBytes);
            assertThat(preComputeApp.downloadEncryptedDataset()).isNotNull();
            fileHelperMock.verify(() -> FileHelper.readFileBytesFromUrl(anyString()), times(2));
        }
    }

    @Test
    void shouldDownloadEncryptedDatasetFromPinataGateway() throws PreComputeException {
        final PreComputeArgs preComputeArgs = getPreComputeArgsBuilder(IPFS_DATASET_URL).build();
        doReturn(preComputeArgs).when(preComputeApp).getPreComputeArgs();
        final URL resourceFile = this.getClass().getClassLoader().getResource(DATASET_RESOURCE_NAME);
        assertThat(resourceFile).isNotNull();
        final byte[] expectedBytes = FileHelper.readAllBytes(resourceFile.getFile());
        try (MockedStatic<FileHelper> fileHelperMock = mockStatic(FileHelper.class)) {
            fileHelperMock.when(() -> FileHelper.readFileBytesFromUrl(anyString()))
                    .thenReturn(null)
                    .thenReturn(null)
                    .thenReturn(expectedBytes);
            assertThat(preComputeApp.downloadEncryptedDataset()).isNotNull();
            fileHelperMock.verify(() -> FileHelper.readFileBytesFromUrl(anyString()), times(3));
        }
    }

    @Test
    void shouldNotDownloadEncryptedDatasetWhenFailureOnAllGateways() {
        final PreComputeArgs preComputeArgs = getPreComputeArgsBuilder(IPFS_DATASET_URL).build();
        doReturn(preComputeArgs).when(preComputeApp).getPreComputeArgs();
        final URL resourceFile = this.getClass().getClassLoader().getResource(DATASET_RESOURCE_NAME);
        assertThat(resourceFile).isNotNull();
        try (MockedStatic<FileHelper> fileHelperMock = mockStatic(FileHelper.class)) {
            fileHelperMock.when(() -> FileHelper.readFileBytesFromUrl(anyString()))
                    .thenReturn(null);
            assertThrows(
                    PreComputeException.class,
                    () -> preComputeApp.downloadEncryptedDataset()
            );
            fileHelperMock.verify(() -> FileHelper.readFileBytesFromUrl(anyString()), times(3));
        }
    }

    @Test
    void shouldThrowSinceDatasetChecksumNotValid() {
        final PreComputeArgs preComputeArgs = getPreComputeArgsBuilder(HTTP_DATASET_URL)
                .encryptedDatasetChecksum("badChecksum")
                .build();
        doReturn(preComputeArgs).when(preComputeApp).getPreComputeArgs();
        PreComputeException e = assertThrows(
                PreComputeException.class,
                () -> preComputeApp.downloadEncryptedDataset());
        assertThat(e.getExitCause()).isEqualTo(ReplicateStatusCause.PRE_COMPUTE_INVALID_DATASET_CHECKSUM);
    }

    @Test
    void shouldDecryptDataset() throws Exception {
        final PreComputeArgs preComputeArgs = getPreComputeArgsBuilder(HTTP_DATASET_URL).build();
        doReturn(preComputeArgs).when(preComputeApp).getPreComputeArgs();
        byte[] encryptedData = FileHelper.readFileBytesFromUrl(HTTP_DATASET_URL);
        byte[] expectedPlainData = FileHelper.readAllBytes(PLAIN_DATA_FILE);
        byte[] actualPlainData = preComputeApp.decryptDataset(encryptedData);
        assertThat(actualPlainData).isEqualTo(expectedPlainData);
    }

    @Test
    void shouldThrowSinceDecryptionFailed() {
        String badKey = FileHelper.readFile(KEY_FILE).replace("A", "B");
        final PreComputeArgs preComputeArgs = getPreComputeArgsBuilder(HTTP_DATASET_URL)
                .encryptedDatasetBase64Key(badKey)
                .build();
        byte[] encryptedData = FileHelper.readFileBytesFromUrl(HTTP_DATASET_URL);
        doReturn(preComputeArgs).when(preComputeApp).getPreComputeArgs();
        PreComputeException e = assertThrows(
                PreComputeException.class,
                () -> preComputeApp.decryptDataset(encryptedData));
        assertThat(e.getExitCause()).isEqualTo(ReplicateStatusCause.PRE_COMPUTE_DATASET_DECRYPTION_FAILED);
    }

    @Test
    void shouldSavePlainDatasetFile() throws Exception {
        final PreComputeArgs preComputeArgs = getPreComputeArgsBuilder(HTTP_DATASET_URL).build();
        doReturn(preComputeArgs).when(preComputeApp).getPreComputeArgs();
        byte[] plainContent = FileHelper.readAllBytes(PLAIN_DATA_FILE);
        preComputeApp.savePlainDatasetFile(plainContent);
        assertThat(new File(outputDir, DATASET_FILENAME)).exists();
    }

    @Test
    void shouldThrowSinceFailedToSavePlainDataset() {
        final PreComputeArgs preComputeArgs = getPreComputeArgsBuilder(HTTP_DATASET_URL)
                .outputDir("/some-folder-123/not-found")
                .build();
        doReturn(preComputeArgs).when(preComputeApp).getPreComputeArgs();
        PreComputeException e = assertThrows(PreComputeException.class,
                () -> preComputeApp.savePlainDatasetFile("data".getBytes()));
        assertThat(e.getExitCause()).isEqualTo(ReplicateStatusCause.PRE_COMPUTE_SAVING_PLAIN_DATASET_FAILED);
    }

    @Test
    void shouldDownloadInputFiles() throws Exception {
        final PreComputeArgs preComputeArgs = getPreComputeArgsBuilder(HTTP_DATASET_URL).build();
        doReturn(preComputeArgs).when(preComputeApp).getPreComputeArgs();
        preComputeApp.downloadInputFiles();
        assertThat(new File(outputDir, FileHashUtils.createFileNameFromUri(INPUT_FILE_1_URL))).exists();
        assertThat(new File(outputDir, FileHashUtils.createFileNameFromUri(INPUT_FILE_2_URL))).exists();
    }

    @Test
    void shouldThrowSinceFailedToDownloadInputFiles() {
        final PreComputeArgs preComputeArgs = getPreComputeArgsBuilder(HTTP_DATASET_URL)
                .inputFiles(List.of("http://bad-input-file-url"))
                .build();
        doReturn(preComputeArgs).when(preComputeApp).getPreComputeArgs();
        PreComputeException e = assertThrows(PreComputeException.class,
                () -> preComputeApp.downloadInputFiles());
        assertThat(e.getExitCause()).isEqualTo(ReplicateStatusCause.PRE_COMPUTE_INPUT_FILE_DOWNLOAD_FAILED);
    }

    private PreComputeArgs.PreComputeArgsBuilder getPreComputeArgsBuilder(String datasetUrl) {
        return PreComputeArgs.builder()
                .chainTaskId(CHAIN_TASK_ID)
                .outputDir(outputDir.getAbsolutePath())
                .isDatasetRequired(true)
                .encryptedDatasetUrl(datasetUrl)
                .encryptedDatasetBase64Key(FileHelper.readFile(KEY_FILE))
                .encryptedDatasetChecksum(DATASET_CHECKSUM)
                .plainDatasetFilename(DATASET_FILENAME)
                .inputFiles(List.of(INPUT_FILE_1_URL, INPUT_FILE_2_URL));
    }

}
