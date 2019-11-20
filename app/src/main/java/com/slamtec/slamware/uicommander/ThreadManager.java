package com.slamtec.slamware.uicommander;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadManager {
    private ThreadManager() {
    }

    private static ThreadManager sInstance = new ThreadManager();
    private ThreadPoolProxy mLongPool;
    private ThreadPoolProxy mShortPool;

    public static ThreadManager getInstance() {
        synchronized (ThreadManager.class) {
            if (sInstance == null) {
                sInstance = new ThreadManager();
            }
        }
        return sInstance;
    }

    public synchronized ThreadPoolProxy createLongPool() {
        synchronized (ThreadPoolProxy.class) {
            if (mLongPool == null) {
                mLongPool = new ThreadPoolProxy(5, Integer.MAX_VALUE, 5000L);
            }
            return mLongPool;
        }
    }

    public synchronized ThreadPoolProxy createShortPool() {
        synchronized (ThreadPoolProxy.class) {
            if (mShortPool == null) {
                mShortPool = new ThreadPoolProxy(3, 20, 5000L);
            }
            return mShortPool;
        }
    }

    public class ThreadPoolProxy {
        private ThreadPoolExecutor mThreadPool;
        private int mCorePoolSize;
        private int mMaxPoolSize;
        private long mTime;

        public ThreadPoolProxy(int corePoolSize, int maxPoolSize, long time) {
            mCorePoolSize = corePoolSize;
            mMaxPoolSize = maxPoolSize;
            mTime = time;
        }

        public void execute(Runnable runnable) {
            if (mThreadPool == null) {
                mThreadPool = new ThreadPoolExecutor(mCorePoolSize, mMaxPoolSize, mTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(10));
            }
            mThreadPool.execute(runnable);
        }

        public void cancel(Runnable runnable) {
            if (mThreadPool != null && !mThreadPool.isShutdown() && !mThreadPool.isTerminated()) {
                mThreadPool.remove(runnable);
            }
        }

        public void cancleAll() {
            if (mThreadPool != null && !mThreadPool.isShutdown() && !mThreadPool.isTerminated()) {
                mThreadPool.getQueue().clear();
            }
        }
    }
}
