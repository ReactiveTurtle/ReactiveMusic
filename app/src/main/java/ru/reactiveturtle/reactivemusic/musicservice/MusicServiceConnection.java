package ru.reactiveturtle.reactivemusic.musicservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class MusicServiceConnection {
    private Context context;
    private ServiceConnection serviceConnection;
    private MusicService.Binder binder;

    public MusicServiceConnection(Context context) {
        this.context = context;
        this.serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                MusicServiceConnection.this.binder = (MusicService.Binder) binder;
                if (connectionCallback != null) {
                    connectionCallback.onConnected(MusicServiceConnection.this.binder);
                }
            }

            public void onServiceDisconnected(ComponentName name) {
                binder = null;
                if (connectionCallback != null) {
                    connectionCallback.onDisconnected();
                }
            }
        };
    }

    public void bind() {
        if (!isBound()) {
            context.bindService(new Intent(context, MusicService.class), serviceConnection, Context.BIND_ABOVE_CLIENT);
        }
    }

    public void unbind() {
        if (isBound()) {
            context.unbindService(serviceConnection);
        }
    }

    private boolean isBound() {
        return binder != null;
    }

    private ConnectionCallback connectionCallback;

    public void setConnectionCallback(ConnectionCallback connectionCallback) {
        this.connectionCallback = connectionCallback;
    }

    public interface ConnectionCallback {
        void onConnected(MusicService.Binder binder);

        void onDisconnected();
    }
}
