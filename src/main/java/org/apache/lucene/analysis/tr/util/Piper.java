package org.apache.lucene.analysis.tr.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.InputStream;
import java.io.OutputStream;

/**
 * <a href="http://blog.art-of-coding.eu/piping-between-processes/">Piping Between Processes</a>
 */
public final class Piper implements java.lang.Runnable {

    private final InputStream input;

    private final OutputStream output;

    public Piper(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public void run() {
        try (InputStream input = this.input; OutputStream output = this.output) {
            // Create 512 bytes buffer
            byte[] b = new byte[512];
            int read = 1;
            // As long as data is read; -1 means EOF
            while (read > -1) {
                // Read bytes into buffer
                read = input.read(b, 0, b.length);
                //System.out.println("read: " + new String(b));
                if (read > -1) {
                    // Write bytes to output
                    output.write(b, 0, read);
                }
            }
        } catch (Exception e) {
            // Something happened while reading or writing streams; pipe is broken
            throw new RuntimeException("Broken pipe", e);
        }
    }
}

