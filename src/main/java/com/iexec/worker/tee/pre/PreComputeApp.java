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
import com.iexec.common.security.CipherUtils;
import com.iexec.common.utils.FileHelper;
import com.iexec.common.utils.IexecEnvUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.security.GeneralSecurityException;

@Slf4j
public class PreComputeApp {

    private final String chainTaskId;
    private final String iexecInFolder;
    private final String datasetFilename;
    private final String base64DatasetKey;
    private String plainDatasetFilepath;

    public PreComputeApp() throws PreComputeException {
        this.chainTaskId = getEnvVarOrThrow(IexecEnvUtils.IEXEC_TASK_ID_ENV_PROPERTY);
        this.iexecInFolder = getEnvVarOrThrow(IexecEnvUtils.IEXEC_IN_ENV_PROPERTY);
        this.datasetFilename = getEnvVarOrThrow(IexecEnvUtils.IEXEC_DATASET_FILENAME_ENV_PROPERTY);
        this.base64DatasetKey = getEnvVarOrThrow(PreComputeUtils.IEXEC_DATASET_KEY_PROPERTY);
    }

    public static void run() {
        log.info("TEE pre-compute started");
        try {
            new PreComputeApp().start();
        } catch(PreComputeException e) {
            log.error("TEE pre-compute failed with a known error [exitCode:{}]",
                    e.getExitCode(), e);
            System.exit(e.getExitCode().getValue());
        } catch (Exception e) {
            log.error("TEE pre-compute failed with an unexpected error", e);
            System.exit(1);
        }
        log.info("TEE pre-compute finished");
    }

    /**
     * This method decrypts t
     * 
     * Before:
     * |--
     * 
     * After:
     * 
     * @throws PreComputeException
     */
    public void start() throws PreComputeException {
        checkInputFolder();
        checkDatasetFile();
        checkDatasetChecksum();
        checkDatasetKey();
        decryptDataset();
        // renamePlainDatasetFile();
        unzipPlainDataset();
    }

    String getEnvVarOrThrow(String envVarName) throws PreComputeException {
        String envVar = System.getenv(envVarName);
        if (StringUtils.isBlank(envVar)) {
            log.error("Required env var is blank [name:{}]", envVarName);
            throw new PreComputeException(PreComputeExitCode.EMPTY_REQUIRED_ENV_VAR);
        }
        return envVar;
    }

    void checkInputFolder() throws PreComputeException {
        log.info("Checking input folder [chainTaskId:{}, path:{}]",
                this.chainTaskId, this.iexecInFolder);
        if (!new File(this.iexecInFolder).isDirectory()) {
            log.error("Input folder not found [chainTaskId:{}, path:{}]",
                    this.chainTaskId, this.iexecInFolder);
            throw new PreComputeException(PreComputeExitCode.INPUT_FOLDER_NOT_FOUND);
        }
    }

    void checkDatasetFile() throws PreComputeException {
        log.info("Checking dataset file [chainTaskId:{}, filename:{}]",
                this.chainTaskId, this.datasetFilename);
        if (!new File(getDatasetFilepath()).isFile()) {
            log.error("Dataset file not found [chainTaskId:{}, filename:{}]",
                    this.chainTaskId, this.datasetFilename);
            throw new PreComputeException(PreComputeExitCode.DATASET_FILE_NOT_FOUND);
        }
    }

    void checkDatasetChecksum() {
        log.info("Checking dataset file checksum [chainTaskId:{}]");
        if (!isValidDatasetChecksum()) {
            log.info("Invalid dataset checksum [chainTaskId:{}, expected:{}]", this.chainTaskId);
        }
    }

    boolean isValidDatasetChecksum() {
        return false;
    }

    void checkDatasetKey() throws PreComputeException {
        log.info("Checking dataset key [chainTaskId:{}]", this.chainTaskId);
        if (StringUtils.isBlank(this.base64DatasetKey)) {
            log.error("Empty dataset key [chainTaskId:{}]", this.chainTaskId);
            throw new PreComputeException(PreComputeExitCode.INVALID_DATASET_KEY);
        }
    }

    /**
     * Decrypt dataset, replace the encrypted file
     * with the new plain file, and unzip it if
     * needed.
     * @throws PreComputeException
     */
    void decryptDataset() throws PreComputeException {
        log.info("Decrypting dataset [chainTaskId:{}]", this.chainTaskId);
        byte[] plainData = new byte[0];
        byte[] base64DatasetKeyBytes = this.base64DatasetKey.getBytes();
        String datasetFilepath = getDatasetFilepath();
        // read dataset file
        byte[] datasetFileContent = FileHelper.readAllBytes(datasetFilepath);
        if (datasetFileContent == null) {
            log.error("Failed to read dataset file content [chainTaskId:{}, path:{}]",
                    this.chainTaskId, datasetFilepath);
            throw new PreComputeException(PreComputeExitCode.IO_ERROR);
        }
        // decrypt dataset
        try {
            plainData = CipherUtils.aesDecrypt(datasetFileContent, base64DatasetKeyBytes);
            log.info("Decrypted dataset [chainTaskId:{}]", this.chainTaskId);
        } catch (GeneralSecurityException e) {
            log.error("Failed to decrypt dataset [chainTaskId:{}]", this.chainTaskId, e);
            throw new PreComputeException(PreComputeExitCode.DATASET_DECRYPTION_ERROR);
        }
        // remove old encrypted file
        log.info("Removing encrypted dataset file [chainTaskId:{}]",
                this.chainTaskId, datasetFilepath);
        if (!FileHelper.deleteFile(datasetFilepath)) {
            log.error("Failed to remove encrypted dataset file [chainTaskId:{}, path:{}]",
                    this.chainTaskId, datasetFilepath);
            throw new PreComputeException(PreComputeExitCode.IO_ERROR);
        }
        log.info("Removed encrypted dataset file [chainTaskId:{}]", this.chainTaskId);
        // write decrypted file to disk
        log.info("Writing plain dataset file [chainTaskId:{}]", this.chainTaskId);
        String plainDatasetFilepath = datasetFilepath;
        if (!FilenameUtils.getExtension(plainDatasetFilepath).equals("zip")) {
            plainDatasetFilepath += ".zip";
        }
        if (!FileHelper.writeFile(plainDatasetFilepath, plainData)) {
            log.error("Failed to write plain dataset file [chainTaskId:{}, path:{}]",
                    this.chainTaskId, plainDatasetFilepath);
            throw new PreComputeException(PreComputeExitCode.IO_ERROR);
        }
        this.plainDatasetFilepath = plainDatasetFilepath;
        log.info("Wrote plain dataset file to disk [chainTaskId:{}]", this.chainTaskId);
    }

    // /**
    //  * We rename the dataset file into something
    //  * predictable by the worker and the other
    //  * compute components. We use the chainTaskId.
    //  */
    // void renamePlainDatasetFile() {
    //     String newName = FilenameUtils.getPath(this.plainDatasetFilepath) +
    //             this.chainTaskId;
    //     FileHelper.renameFile(this.plainDatasetFilepath, newName);
    // }

    void unzipPlainDataset() {
        String newFolderPath = FileHelper.unZipFile(this.plainDatasetFilepath);
        if (newFolderPath.isEmpty()) {
            log.error("Failed to unzip plain dataset file [chainTaskId:{}]",
                    this.chainTaskId);
        }
    }

    String getDatasetFilepath() {
        return this.iexecInFolder + File.separator + this.datasetFilename;
    }
}
