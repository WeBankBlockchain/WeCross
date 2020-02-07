package com.webank.wecross.proposal;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ProposalPool {
    private String name = "default";

    public static final int maxSize = 128;
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    private LinkedHashMap<Integer, Proposal> pool = new LinkedHashMap<>();

    public void put(int seq, Proposal proposal) throws Exception {
        lock.writeLock().lock();
        try {
            checkAndClear();

            if (pool.get(new Integer(seq)) != null) {
                throw new Exception("Proposal of seq " + seq + " is exist in " + name);
            }

            if (pool.size() + 1 > maxSize) {
                throw new Exception("Proposal pool of " + name + "is full with size " + maxSize);
            }

            pool.put(new Integer(seq), proposal);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            pool.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void sendSignedPayload(int seq, byte[] signBytes) throws Exception {

        lock.writeLock().lock();
        Proposal proposal;
        try {
            proposal = pool.get(seq);

            if (proposal != null) {
                pool.remove(seq); // Just remove from the pool, even if this proposal's payload send
                // failed;
            }

            checkAndClear();
        } finally {
            lock.writeLock().unlock();
        }

        if (proposal == null) {
            throw new Exception("Proposal is not exist in proposal pool: " + name);
        }

        proposal.sendSignedPayload(signBytes);
    }

    public int size() {
        lock.writeLock().lock();
        try {
            checkAndClear();
            return this.pool.size();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean isEmpty() {
        lock.writeLock().lock();
        try {
            checkAndClear();
            return this.pool.isEmpty();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    private void checkAndClear() {
        Set<Integer> seqToClear = new HashSet<>();
        for (Map.Entry<Integer, Proposal> entry : pool.entrySet()) {
            if (entry.getValue().isTimeout()) {
                seqToClear.add(entry.getKey());
            } else {
                break; // Pool is an linkedHashMap, has add order.
            }
        }

        for (Integer seq : seqToClear) {
            pool.remove(seq);
        }
    }
}
