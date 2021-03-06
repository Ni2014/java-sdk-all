package cn.leancloud.im;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import cn.leancloud.AVException;
import cn.leancloud.AVOSCloud;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.im.v2.Conversation;

/**
 * Created by fengjunwen on 2018/8/7.
 */

public abstract class AVIMBaseBroadcastReceiver extends BroadcastReceiver {
  AVCallback callback;

  public AVIMBaseBroadcastReceiver(AVCallback callback) {
    this.callback = callback;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    try {
      Throwable error = null;
      if (null != intent && null != intent.getExtras() && intent.getExtras().containsKey(Conversation.callbackExceptionKey)) {
        error = (Throwable) intent.getExtras().getSerializable(Conversation.callbackExceptionKey);
      }
      execute(intent, error);
      LocalBroadcastManager.getInstance(AVOSCloud.getContext()).unregisterReceiver(this);
    } catch (Exception e) {
      if (callback != null) {
        callback.internalDone(null, new AVException(e));
      }
    }
  }

  public abstract void execute(Intent intent, Throwable error);
}
