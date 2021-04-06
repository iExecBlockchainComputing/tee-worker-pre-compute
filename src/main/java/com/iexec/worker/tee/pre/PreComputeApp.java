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
import com.iexec.common.utils.HashUtils;
import com.iexec.common.utils.IexecEnvUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.security.GeneralSecurityException;

@Slf4j
@NoArgsConstructor
public class PreComputeApp {

    private String chainTaskId;
    private String preComputeInFolder;
    private String preComputeOutFolder;
    private String base64DatasetKey;
    private String encryptedDatasetChecksum;
    private String datasetFilename;
    private String encryptedDatasetFilepath;
    private String plainDatasetFilepath;

    public static void run() {
        log.info("TEE pre-compute started");
        try {
            new PreComputeApp().start();
        } catch(PreComputeException e) {
            log.error("TEE pre-compute failed with a known error [exitCode:{}]",
                    e.getExitCode(), e);
            System.exit(e.getExitCode().value());
        } catch (Exception e) {
            log.error("TEE pre-compute failed with an unknown error", e);
            System.exit(1);
        }
        log.info("TEE pre-compute finished");
    }

    /**
     * Run all necessary checks and decrypt the dataset file.
     * The encrypted file should be in /pre-compute-in folder
     * and the decrypted file will be written to /iexec_in.
     * <p>
     * 
     * Before:
     * <pre>
     * ---------
     * /pre-compute-in
     *   └── dataset-name <-- encrypted file
     * 
     * /iexec_in
     *   └── 
     * </pre>
     * <p>
     * 
     * After:
     * <pre>
     * ---------
     * /pre-compute-in
     *   └──
     * 
     * /iexec_in
     *   └── dataset-name <-- decrypted file
     * </pre>
     * <p>
     * The decrypted file can be a single file or a an archive.
     * Archives won't be extracted.
     * 
     * @throws PreComputeException
     */
    void start() throws PreComputeException {
        this.chainTaskId = getEnvVarOrThrow(IexecEnvUtils.IEXEC_TASK_ID_ENV_PROPERTY);
        this.datasetFilename = getEnvVarOrThrow(IexecEnvUtils.IEXEC_DATASET_FILENAME_ENV_PROPERTY);
        this.preComputeInFolder = getEnvVarOrThrow(PreComputeUtils.IEXEC_PRE_COMPUTE_IN_PROPERTY);
        this.preComputeOutFolder = getEnvVarOrThrow(PreComputeUtils.IEXEC_PRE_COMPUTE_OUT_PROPERTY);
        this.base64DatasetKey = getEnvVarOrThrow(PreComputeUtils.IEXEC_DATASET_KEY_PROPERTY);
        this.encryptedDatasetChecksum = getEnvVarOrThrow(PreComputeUtils.IEXEC_DATASET_CHECKSUM_PROPERTY);
        this.encryptedDatasetFilepath = this.preComputeInFolder + File.separator + this.datasetFilename;
        this.plainDatasetFilepath = this.preComputeOutFolder + File.separator + this.datasetFilename;
        checkInputFolder();
        checkOutputFolder();
        checkDatasetFile();
        checkDatasetChecksum();
        decryptDataset();
    }

    String getEnvVarOrThrow(String envVarName) throws PreComputeException {
        String envVar = System.getenv(envVarName);
        if (envVar == null || envVar.isEmpty()) {
            log.error("Required env var is blank [name:{}]", envVarName);
            throw new PreComputeException(PreComputeExitCode.EMPTY_REQUIRED_ENV_VAR);
        }
        return envVar;
    }

    void checkInputFolder() throws PreComputeException {
        log.info("Checking input folder [chainTaskId:{}, path:{}]",
                this.chainTaskId, this.preComputeInFolder);
        if (new File(this.preComputeInFolder).isDirectory()) {
            return;
        }
        log.error("Input folder not found [chainTaskId:{}, path:{}]",
                this.chainTaskId, this.preComputeInFolder);
        throw new PreComputeException(PreComputeExitCode.INPUT_FOLDER_NOT_FOUND);
    }

    void checkOutputFolder() throws PreComputeException {
        log.info("Checking output folder [chainTaskId:{}, path:{}]",
                this.chainTaskId, this.preComputeOutFolder);
        if (new File(this.preComputeOutFolder).isDirectory()) {
            return;
        }
        log.error("Output folder not found [chainTaskId:{}, path:{}]",
                this.chainTaskId, this.preComputeOutFolder);
        throw new PreComputeException(PreComputeExitCode.OUTPUT_FOLDER_NOT_FOUND);
    }

    void checkDatasetFile() throws PreComputeException {
        log.info("Checking dataset file [chainTaskId:{}, path:{}]",
                this.chainTaskId, this.encryptedDatasetFilepath);
        if (new File(this.encryptedDatasetFilepath).isFile()) {
            return;
        }
        log.error("Dataset file not found [chainTaskId:{}, path:{}]",
                this.chainTaskId, this.encryptedDatasetFilepath);
        throw new PreComputeException(PreComputeExitCode.DATASET_FILE_NOT_FOUND);
    }

    void checkDatasetChecksum() throws PreComputeException {
        log.info("Checking dataset checksum [chainTaskId:{}]", this.chainTaskId);
        String actualChecksum = HashUtils.getFileSha256(this.encryptedDatasetFilepath);
        if (actualChecksum.equals(this.encryptedDatasetChecksum)) {
            return;
        }
        log.info("Invalid dataset checksum [chainTaskId:{}, expected:{}, actual:{}]",
                this.chainTaskId, this.encryptedDatasetChecksum, actualChecksum);
        throw new PreComputeException(PreComputeExitCode.INVALID_DATASET_CHECKSUM);
    }

    /**
     * Decrypt dataset, remove the encrypted file,
     * and write the new plain file in preComputeOutFolder.
     * @throws PreComputeException
     */
    void decryptDataset() throws PreComputeException {
        log.info("Decrypting dataset [chainTaskId:{}]", this.chainTaskId);
        byte[] plainData = new byte[0];
        byte[] base64DatasetKeyBytes = this.base64DatasetKey.getBytes();
        // read dataset file
        byte[] datasetFileContent = FileHelper.readAllBytes(this.encryptedDatasetFilepath);
        if (datasetFileContent == null) {
            log.error("Failed to read dataset file content [chainTaskId:{}, path:{}]",
                    this.chainTaskId, this.encryptedDatasetFilepath);
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
                this.chainTaskId, this.encryptedDatasetFilepath);
        if (!FileHelper.deleteFile(this.encryptedDatasetFilepath)) {
            log.error("Failed to remove encrypted dataset file [chainTaskId:{}, path:{}]",
                    this.chainTaskId, this.encryptedDatasetFilepath);
            throw new PreComputeException(PreComputeExitCode.IO_ERROR);
        }
        log.info("Removed encrypted dataset file [chainTaskId:{}]", this.chainTaskId);
        // write decrypted file to disk
        log.info("Writing plain dataset file [chainTaskId:{}, path:{}]",
                this.chainTaskId, this.plainDatasetFilepath);
        if (!FileHelper.writeFile(this.plainDatasetFilepath, plainData)) {
            log.error("Failed to write plain dataset file [chainTaskId:{}, path:{}]",
                    this.chainTaskId, this.plainDatasetFilepath);
            throw new PreComputeException(PreComputeExitCode.IO_ERROR);
        }
        log.info("Wrote plain dataset file to disk [chainTaskId:{}]", this.chainTaskId);
    }
}
