/*
 * Copyright 2025 IEXEC BLOCKCHAIN TECH
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

package com.iexec.worker.compute.pre.signer;

import com.iexec.common.replicate.ReplicateStatusCause;
import com.iexec.commons.poco.utils.HashUtils;
import com.iexec.commons.poco.utils.SignatureUtils;
import com.iexec.worker.compute.pre.PreComputeException;
import com.iexec.worker.compute.pre.utils.EnvUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static com.iexec.common.replicate.ReplicateStatusCause.PRE_COMPUTE_INVALID_TEE_SIGNATURE;
import static com.iexec.common.worker.tee.TeeSessionEnvironmentVariable.SIGN_TEE_CHALLENGE_PRIVATE_KEY;
import static com.iexec.common.worker.tee.TeeSessionEnvironmentVariable.SIGN_WORKER_ADDRESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class SignerServiceTests {

    private static final String CHAIN_TASK_ID = "0x123456789abcdef";
    private static final String WORKER_ADDRESS = "0xabcdef123456789";
    private static final String ENCLAVE_CHALLENGE_PRIVATE_KEY = "0xdd3b993ec21c71c1f6d63a5240850e0d4d8dd83ff70d29e49247958548c1d479";
    private static final String MESSAGE_HASH = "0x5cd0e9c5180dd35e2b8285d0db4ded193a9b4be6fbfab90cbadccecab130acad";
    private static final String EXPECTED_CHALLENGE = "0xfcc6bce5eb04284c2eb1ed14405b943574343b1abda33628fbf94a374b18dd16541c6ebf63c6943d8643ff03c7aa17f1cb17b0a8d297d0fd95fc914bdd0e85f81b";

    SignerService signerService = new SignerService();

    @Test
    void shouldSignEnclaveChallenge() {
        final String actualChallenge = Assertions.assertDoesNotThrow(() -> signerService.signEnclaveChallenge(MESSAGE_HASH, ENCLAVE_CHALLENGE_PRIVATE_KEY));
        assertEquals(EXPECTED_CHALLENGE, actualChallenge);
    }

    @Test
    void shouldNotSignEnclaveChallengeSinceInvalidTeeSignature() {
        try (MockedStatic<SignatureUtils> signatureUtils = Mockito.mockStatic(SignatureUtils.class)) {
            signatureUtils.when(() -> SignatureUtils.isExpectedSignerOnSignedMessageHash(any(), any(), any()))
                    .thenReturn(false);

            final PreComputeException exception = assertThrows(PreComputeException.class, () -> signerService.signEnclaveChallenge(MESSAGE_HASH, ENCLAVE_CHALLENGE_PRIVATE_KEY));
            assertEquals(PRE_COMPUTE_INVALID_TEE_SIGNATURE, exception.getExitCause());
        }
    }

    @Test
    void shouldGetChallenge() throws PreComputeException {
        SignerService mockSignerService = Mockito.mock(SignerService.class, Mockito.CALLS_REAL_METHODS);

        try (MockedStatic<EnvUtils> envUtils = Mockito.mockStatic(EnvUtils.class);
             MockedStatic<HashUtils> hashUtils = Mockito.mockStatic(HashUtils.class)) {

            envUtils.when(() -> EnvUtils.getEnvVarOrThrow(eq(SIGN_WORKER_ADDRESS.name()), any(ReplicateStatusCause.class)))
                    .thenReturn(WORKER_ADDRESS);
            envUtils.when(() -> EnvUtils.getEnvVarOrThrow(eq(SIGN_TEE_CHALLENGE_PRIVATE_KEY.name()), any(ReplicateStatusCause.class)))
                    .thenReturn(ENCLAVE_CHALLENGE_PRIVATE_KEY);
            hashUtils.when(() -> HashUtils.concatenateAndHash(CHAIN_TASK_ID, WORKER_ADDRESS))
                    .thenReturn(MESSAGE_HASH);

            when(mockSignerService.signEnclaveChallenge(MESSAGE_HASH, ENCLAVE_CHALLENGE_PRIVATE_KEY))
                    .thenReturn(EXPECTED_CHALLENGE);

            String actualChallenge = mockSignerService.getChallenge(CHAIN_TASK_ID);
            assertEquals(EXPECTED_CHALLENGE, actualChallenge);
        }
    }

}
