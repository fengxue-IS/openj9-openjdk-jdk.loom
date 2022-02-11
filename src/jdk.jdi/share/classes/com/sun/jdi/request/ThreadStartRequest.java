/*
 * Copyright (c) 1998, 2022, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jdi.request;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ThreadStartEvent;
import jdk.internal.javac.PreviewFeature;

/**
 * Request for notification when a thread starts execution in the target VM.
 * When an enabled ThreadStartRequest is hit, an
 * {@link EventSet event set} containing a
 * {@link ThreadStartEvent ThreadStartEvent}
 * will be placed on the
 * {@link EventQueue EventQueue}.
 * The collection of existing ThreadStartRequests is
 * managed by the {@link EventRequestManager}
 *
 * @see ThreadStartEvent
 * @see EventQueue
 * @see EventRequestManager
 *
 * @author Robert Field
 * @since  1.3
 */
public interface ThreadStartRequest extends EventRequest {

    /**
     * Restricts the events generated by this request to those in
     * the given thread.
     * The behavior of this method is unspecified when the event is restricted
     * to only platform threads.
     * @param thread the thread to filter on.
     * @throws InvalidRequestStateException if this request is currently
     * enabled or has been deleted.
     * Filters may be added only to disabled requests.
     */
    void addThreadFilter(ThreadReference thread);

    /**
     * Restricts the events generated by this request to only
     * <a href="{@docRoot}/java.base/java/lang/Thread.html#platform-threads">platform threads</a>.
     * The behavior of this method is unspecified when the event is restricted
     * to a specific thread.
     *
     * @implSpec
     * The default implementation throws {@code UnsupportedOperationException}.
     *
     * @throws InvalidRequestStateException if this request is currently
     * enabled or has been deleted
     *
     * @since 19
     */
    @PreviewFeature(feature = PreviewFeature.Feature.VIRTUAL_THREADS)
    default void addPlatformThreadsOnlyFilter() {
        throw new UnsupportedOperationException("Method not implemented");
    }
}
