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
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.security.GeneralSecurityException;

@Slf4j
public class PreComputeApp {

    private String chainTaskId; // just for convenience
    private PreComputeInput input;
    private byte[] encryptedDatasetContent;
    private byte[] plainDatasetContent;

    /**
     * Download, decrypt, and save the plain dataset file in "/iexec_in".
     * If the decrypted file is an archive, it won't be extracted.
     * 
     * @throws PreComputeException
     */
    void run() throws PreComputeException {
        init();
        checkOutputFolder();
        downloadEncryptedDataset();
        decryptDataset();
        writePlainDatasetFile();
    }

    void init() throws PreComputeException {
        input = PreComputeInput.builder()
                .chainTaskId(getEnvVarOrThrow(IexecEnvUtils.IEXEC_TASK_ID_ENV_PROPERTY))
                .outputDir(getEnvVarOrThrow(PreComputeUtils.IEXEC_PRE_COMPUTE_OUT))
                .encryptedDatasetUrl(getEnvVarOrThrow(PreComputeUtils.IEXEC_DATASET_URL))
                .encryptedDatasetBase64Key(getEnvVarOrThrow(PreComputeUtils.IEXEC_DATASET_KEY))
                .encryptedDatasetChecksum(getEnvVarOrThrow(PreComputeUtils.IEXEC_DATASET_CHECKSUM))
                .plainDatasetFilename(getEnvVarOrThrow(IexecEnvUtils.IEXEC_DATASET_FILENAME_ENV_PROPERTY))
                .build();
        chainTaskId = input.getChainTaskId();

    }

    String getEnvVarOrThrow(String envVarName) throws PreComputeException {
        String envVar = System.getenv(envVarName);
        if (envVar == null || envVar.isEmpty()) {
            log.error("Required env var is empty [name:{}]", envVarName);
            throw new PreComputeException(PreComputeExitCode.EMPTY_REQUIRED_ENV_VAR);
        }
        return envVar;
    }

    void checkOutputFolder() throws PreComputeException {
        String outputDir = input.getOutputDir();
        log.info("Checking output folder [chainTaskId:{}, path:{}]",
                chainTaskId, outputDir);
        if (new File(outputDir).isDirectory()) {
            return;
        }
        log.error("Output folder not found [chainTaskId:{}, path:{}]",
                chainTaskId, outputDir);
        throw new PreComputeException(PreComputeExitCode.OUTPUT_FOLDER_NOT_FOUND);
    }

    void downloadEncryptedDataset() throws PreComputeException {
        String encryptedDatasetUrl = input.getEncryptedDatasetUrl();
        log.info("Downloading encrypted dataset file [chainTaskId:{}, url:{}]",
                chainTaskId, encryptedDatasetUrl);
        encryptedDatasetContent = FileHelper.readFileBytesFromUrl(encryptedDatasetUrl);
        if (encryptedDatasetContent == null) {
            log.error("Failed to download encrypted dataset file [chainTaskId:{}, url:{}]",
                    chainTaskId, encryptedDatasetUrl);
            throw new PreComputeException(PreComputeExitCode.DATASET_DOWNLOAD_FAILED);
        }
        log.info("Checking encrypted dataset checksum [chainTaskId:{}]", chainTaskId);
        String expectedChecksum = input.getEncryptedDatasetChecksum();
        String actualChecksum = HashUtils.sha256(encryptedDatasetContent);
        if (!actualChecksum.equals(expectedChecksum)) {
            log.info("Invalid dataset checksum [chainTaskId:{}, expected:{}, actual:{}]",
                    chainTaskId, expectedChecksum, actualChecksum);
            throw new PreComputeException(PreComputeExitCode.INVALID_DATASET_CHECKSUM);
        }
    }

    void decryptDataset() throws PreComputeException {
        log.info("Decrypting dataset [chainTaskId:{}]", chainTaskId);
        String key = input.getEncryptedDatasetBase64Key();
        try {
            plainDatasetContent = CipherUtils.aesDecrypt(encryptedDatasetContent, key.getBytes());
        } catch (GeneralSecurityException e) {
            log.error("Failed to decrypt dataset [chainTaskId:{}]", chainTaskId, e);
            throw new PreComputeException(PreComputeExitCode.DATASET_DECRYPTION_FAILED);
        }
        log.info("Decrypted dataset [chainTaskId:{}]", chainTaskId);
    }

    void writePlainDatasetFile() throws PreComputeException {
        String plainDatasetFilepath = input.getOutputDir() + File.separator +
                input.getPlainDatasetFilename();
        log.info("Writing plain dataset file [chainTaskId:{}, path:{}]",
                chainTaskId, plainDatasetFilepath);
        if (!FileHelper.writeFile(plainDatasetFilepath, plainDatasetContent)) {
            log.error("Failed to write plain dataset file [chainTaskId:{}, path:{}]",
                    chainTaskId, plainDatasetFilepath);
            throw new PreComputeException(PreComputeExitCode.WRITING_PLAIN_DATASET_FAILED);
        }
        log.info("Plain dataset file is written to disk [chainTaskId:{}]", chainTaskId);
    }
}
