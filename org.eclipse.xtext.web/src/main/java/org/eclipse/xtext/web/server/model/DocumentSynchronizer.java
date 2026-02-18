/**
 * Copyright (c) 2015, 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.web.server.model;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.xtext.service.OperationCanceledManager;
import org.eclipse.xtext.util.CancelIndicator;

import com.google.inject.Inject;

/**
 * DocumentSynchronizer replacement using ReentrantReadWriteLock.
 * Supports priority flag and cancellation semantics similar to the original Semaphore-based version.
 */
public class DocumentSynchronizer implements CancelIndicator {

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    private final AtomicInteger waitingPriorityJobs = new AtomicInteger();

    @Inject
    private OperationCanceledManager operationCanceledManager;

    // Volatile cancel flag visible to all threads
    private volatile boolean canceled;

    // -------------------
    // Cancel indicator
    // -------------------
    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    // -------------------
    // Read lock operations
    // -------------------
    /**
     * Acquire a read lock. Multiple readers can run concurrently.
     * @param priority if true, this read counts as a priority job and cannot be immediately canceled.
     * @return true if canceled flag was reset by this call
     */
    public boolean acquireRead(boolean priority) {
        if (priority) {
            waitingPriorityJobs.incrementAndGet();
            canceled = true;
        }

        rwLock.readLock().lock();

        if (priority) {
            // decrement and reset canceled if last priority
            if (waitingPriorityJobs.decrementAndGet() == 0) {
                return canceled = false;
            }
            return false;
        } else {
            this.operationCanceledManager.checkCanceled(this);
        }
        return false;
    }

    /**
     * Release a read lock.
     */
    public void releaseRead() {
        if (rwLock.getReadHoldCount() == 0) {
            throw new IllegalStateException("Cannot release read lock without acquiring it first.");
        }
        rwLock.readLock().unlock();
    }

    // -------------------
    // Write lock operations
    // -------------------
    /**
     * Acquire a write lock. Exclusive access.
     * @param priority if true, counts as priority job and cannot be immediately canceled.
     * @return true if canceled flag was reset by this call
     */
    public boolean acquireWrite(boolean priority) {
        if (priority) {
            waitingPriorityJobs.incrementAndGet();
            canceled = true;
        }

        rwLock.writeLock().lock();

        if (priority) {
            if (waitingPriorityJobs.decrementAndGet() == 0) {
                return canceled = false;
            }
            return false;
        } else {
            operationCanceledManager.checkCanceled(this);
        }
        return false;
    }

    /**
     * Release a write lock.
     */
    public void releaseWrite() {
        if (!rwLock.isWriteLockedByCurrentThread()) {
            throw new IllegalStateException("Cannot release write lock without acquiring it first.");
        }
        rwLock.writeLock().unlock();
    }

    public void forceReleaseLocks() {
        // Release write lock if held by current thread
        while (rwLock.isWriteLockedByCurrentThread()) {
            rwLock.writeLock().unlock();
        }

        // Release all read locks held by current thread
        int readHoldCount = rwLock.getReadHoldCount();
        for (int i = 0; i < readHoldCount; i++) {
            rwLock.readLock().unlock();
        }
    }
}
