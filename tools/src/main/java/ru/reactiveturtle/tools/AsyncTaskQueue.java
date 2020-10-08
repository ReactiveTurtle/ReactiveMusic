package ru.reactiveturtle.tools;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

public class AsyncTaskQueue {
    private static final int MAX_EXECUTING_TASKS = 5;
    private List<BaseAsyncTask<?>> mTasks = new ArrayList<>();
    private List<String> mTaskIds = new ArrayList<>();
    private int mStartedTasksCount = 0;

    public synchronized void addLoader(String id, BaseAsyncTask<?> asyncTask, boolean isAddToStart) {
        int index = mTaskIds.indexOf(id);
        if (index > -1) {
            BaseAsyncTask<?> task = mTasks.get(index);
            if (!task.isCancelled()) {
                return;
            } else {
                mTaskIds.remove(index);
                mTasks.remove(index);
            }
        }
        BaseAsyncTask.EndCallback endCallback = () -> {
            System.out.println("EndCallbackCalled");
            asyncTask.setEndCallback(null);
            loadNext(id);
        };
        asyncTask.setEndCallback(endCallback);
        if (isAddToStart) {
            mTasks.add(0, asyncTask);
            mTaskIds.add(0, id);
        } else {
            mTasks.add(asyncTask);
            mTaskIds.add(id);
        }
        loadNext(null);
    }

    public synchronized void cancelLoad(String id) {
        int index = mTaskIds.indexOf(id);
        if (index > -1) {
            BaseAsyncTask<?> task = mTasks.get(index);
            System.out.println("CancelLoad: id: " + index + ", isCanceled: " + task.isCancelled()
                    + ", isFinished: " + task.isFinished() + ", status: " + task.getStatus());
            if (task.isCancelled() || task.getStatus() == AsyncTask.Status.PENDING) {
                mTaskIds.remove(index);
                mTasks.remove(index);
            } else if (task.isFinished()) {
                mStartedTasksCount--;
            } else if (task.isRunning()) {
                mStartedTasksCount--;
                task.cancel(true);
            }
        }
    }

    private synchronized void loadNext(String removeId) {
        System.out.println("LoadNext: removeId: " + removeId + ", startedTasks: " + mStartedTasksCount
                + ", mLoaderKeys: " + mTaskIds.size());
        cancelLoad(removeId);
        for (int i = 0; i < getTaskCount() && mStartedTasksCount < MAX_EXECUTING_TASKS; i++) {
            BaseAsyncTask<?> currentTask = mTasks.get(i);
            if (currentTask.getStatus() == AsyncTask.Status.PENDING) {
                mStartedTasksCount++;
                currentTask.execute();
            }
        }
    }

    public int getTaskCount() {
        return mTasks.size();
    }

    public boolean containsId(String id) {
        return mTaskIds.contains(id);
    }
}
