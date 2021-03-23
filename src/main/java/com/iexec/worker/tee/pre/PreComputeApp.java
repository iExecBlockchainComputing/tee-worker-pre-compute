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
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.security.GeneralSecurityException;

@Slf4j
public class PreComputeApp {

    private final String chainTaskId;
    private final String iexecInFolder;
    private final String base64DatasetKey;
    private final String datasetFilename;
    private final String datasetFilepath;

    public PreComputeApp() throws PreComputeException {
        this.chainTaskId = getEnvVarOrThrow(IexecEnvUtils.IEXEC_TASK_ID_ENV_PROPERTY);
        this.iexecInFolder = getEnvVarOrThrow(IexecEnvUtils.IEXEC_IN_ENV_PROPERTY);
        this.base64DatasetKey = getEnvVarOrThrow(PreComputeUtils.IEXEC_DATASET_KEY_PROPERTY);
        this.datasetFilename = getEnvVarOrThrow(IexecEnvUtils.IEXEC_DATASET_FILENAME_ENV_PROPERTY);
        this.datasetFilepath = this.iexecInFolder + File.separator + this.datasetFilename;
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
     * 1. Run all necessary checks
     * <p>
     * 2. Decrypt the dataset.
     * <p>
     * 3. Unzip the plain dataset file to prepare
     * the correct hierarchy.
     * <p>
     * 
     * Before:
     * <pre>
     * iexec_in
     *   ├── 0x369024f0e0db0858e43ced79292aec047e4ceffeec3c66590975707dc2eda098
     *   └── input-file1.txt
     * </pre>
     * where 0x360.... is the encrypted dataset file.
     * <p>
     * 
     * After:
     * <pre>
     * iexec_in
     *   ├── 0x369024f0e0db0858e43ced79292aec047e4ceffeec3c66590975707dc2eda098/
     *   │   ├── plain-file1.txt
     *   │   └── plain-file2.txt
     *   └── input-file1.txt
     * </pre>
     * @throws PreComputeException
     */
    public void start() throws PreComputeException {
        checkInputFolder();
        checkDatasetFile();
        checkDatasetChecksum();
        checkDatasetKey();
        decryptDataset();
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
        log.info("Checking dataset file [chainTaskId:{}, path:{}]",
                this.chainTaskId, this.datasetFilepath);
        if (!new File(this.datasetFilepath).isFile()) {
            log.error("Dataset file not found [chainTaskId:{}, path:{}]",
                    this.chainTaskId, this.datasetFilepath);
            throw new PreComputeException(PreComputeExitCode.DATASET_FILE_NOT_FOUND);
        }
    }

    void checkDatasetChecksum() throws PreComputeException {
        // TODO
        log.info("Checking dataset checksum [chainTaskId:{}]", this.chainTaskId);
        if (!isValidDatasetChecksum()) {
            log.info("Invalid dataset checksum [chainTaskId:{}, expected:{}, actual:{}]",
                    this.chainTaskId, "expected", "actual");
            throw new PreComputeException(PreComputeExitCode.INVALID_DATASET_CHECKSUM);
        }
    }

    boolean isValidDatasetChecksum() {
        // TODO
        return true;
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
        // read dataset file
        byte[] datasetFileContent = FileHelper.readAllBytes(this.datasetFilepath);
        if (datasetFileContent == null) {
            log.error("Failed to read dataset file content [chainTaskId:{}, path:{}]",
                    this.chainTaskId, this.datasetFilepath);
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
        log.info("Removing encrypted dataset file [chainTaskId:{}, path:{}]",
                this.chainTaskId, this.datasetFilepath);
        if (!FileHelper.deleteFile(this.datasetFilepath)) {
            log.error("Failed to remove encrypted dataset file [chainTaskId:{}, path:{}]",
                    this.chainTaskId, this.datasetFilepath);
            throw new PreComputeException(PreComputeExitCode.IO_ERROR);
        }
        log.info("Removed encrypted dataset file [chainTaskId:{}]", this.chainTaskId);
        // write decrypted file to disk
        log.info("Writing plain dataset file [chainTaskId:{}, path:{}]",
                this.chainTaskId, this.datasetFilepath);
        if (!FileHelper.writeFile(this.datasetFilepath, plainData)) {
            log.error("Failed to write plain dataset file [chainTaskId:{}, path:{}]",
                    this.chainTaskId, this.datasetFilepath);
            throw new PreComputeException(PreComputeExitCode.IO_ERROR);
        }
        log.info("Wrote plain dataset file to disk [chainTaskId:{}]", this.chainTaskId);
    }
}
