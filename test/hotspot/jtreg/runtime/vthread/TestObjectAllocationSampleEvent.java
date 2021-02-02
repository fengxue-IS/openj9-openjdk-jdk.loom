/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import jdk.jfr.consumer.RecordingStream;

/**
 * @test Re-written from jfr test ObjectAllocationSampleEvent 
 * @summary Tests ObjectAllocationSampleEvent
 * @requires vm.hasJFR
 * @library /test/lib
 * @run main/othervm TestObjectAllocationSampleEvent
 */
public class TestObjectAllocationSampleEvent {


    public static void main(String... args) throws Exception {
        Thread thread = Thread.builder().task(new Task()).virtual().build();
        thread.start();
        thread.join();
    }
}

class Task implements Runnable {
    
    private static final int OBJECT_SIZE = 4 * 1024;
    private static final int OBJECTS_TO_ALLOCATE = 16 * 1000;
    private static final String BYTE_ARRAY_CLASS_NAME = new byte[0].getClass().getName();

    // Make sure allocation isn't dead code eliminated.
    public static byte[] tmp;

    public void run() {
        
        try (RecordingStream rs = new RecordingStream()) {
        }
        for (int i = 0; i < OBJECTS_TO_ALLOCATE; ++i) {
            tmp = new byte[OBJECT_SIZE];
        }

        // Needs to wait for crash...
        try {           
            Thread.sleep(1_000);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }    
    }
}
