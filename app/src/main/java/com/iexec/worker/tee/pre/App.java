/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.iexec.worker.tee.pre;

import com.iexec.common.utils.FileHelper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {

    public static void main(String[] args) {
        log.info("Tee worker pre-compute started");
        log.info("Check iexec-common is present [folder:{}]", FileHelper.SLASH_IEXEC_IN);
    }
}
