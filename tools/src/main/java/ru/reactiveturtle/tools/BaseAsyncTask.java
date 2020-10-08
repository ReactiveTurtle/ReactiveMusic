package ru.reactiveturtle.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

public class BaseAsyncTask<Result> extends AsyncTask<Void, Void, Result> {
    @SuppressLint("StaticFieldLeak")
    private Context context;

    public BaseAsyncTask(@NonNull Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public Context getContext() {
        return context;
    }

    @Override
    protected Result doInBackground(Void... voids) {
        return null;
    }

    private boolean isFinished = false;

    public boolean isFinished() {
        return isFinished;
    }

    public boolean isRunning() {
        return getStatus() == Status.RUNNING && !isFinished;
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        isFinished = true;
        if (finishCallback != null) {
            finishCallback.onFinish(result);
        }
        context = null;
    }

    @Override
    protected void onCancelled(Result result) {
        super.onCancelled(result);
        if (finishCallback != null) {
            finishCallback.onFinish(result);
        }
    }

    private FinishCallback<Result> finishCallback;

    public void setFinishCallback(FinishCallback<Result> finishCallback) {
        this.finishCallback = finishCallback;
    }

    public interface FinishCallback<Result> {
        void onFinish(Result result);
    }
}