package cn.leancloud.im;

import cn.leancloud.AVLogger;
import cn.leancloud.Messages;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.im.v2.*;
import cn.leancloud.im.v2.callback.*;
import cn.leancloud.session.AVConnectionManager;
import cn.leancloud.session.AVConversationHolder;
import cn.leancloud.session.AVSession;
import cn.leancloud.session.AVSessionManager;
import cn.leancloud.utils.LogUtil;
import com.alibaba.fastjson.JSON;

import java.util.List;
import java.util.Map;

public class DirectlyOperationTube implements OperationTube {
  private static final AVLogger LOGGER = LogUtil.getLogger(DirectlyOperationTube.class);

  private final boolean needCacheRequestKey;
  public DirectlyOperationTube(boolean needCacheRequestKey) {
    this.needCacheRequestKey = needCacheRequestKey;
  }

  public boolean openClient(String clientId, String tag, String userSessionToken,
                  boolean reConnect, AVIMClientCallback callback) {
    LOGGER.d("openClient...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, null, requestId, callback);
    }
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId);
    session.open(tag, userSessionToken, reConnect, requestId);
    return true;
  }

  public boolean queryClientStatus(String clientId, final AVIMClientStatusCallback callback) {
    LOGGER.d("queryClientStatus...");

    AVIMClient.AVIMClientStatus status = AVIMClient.AVIMClientStatus.AVIMClientStatusNone;
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId);
    if (AVSession.Status.Opened == session.getCurrentStatus()) {
      status = AVIMClient.AVIMClientStatus.AVIMClientStatusOpened;
    } else {
      status = AVIMClient.AVIMClientStatus.AVIMClientStatusPaused;
    }
    if (null != callback) {
      callback.internalDone(status, null);
    }
    return true;
  }

  public boolean closeClient(String self, AVIMClientCallback callback) {
    LOGGER.d("openClient...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(self, null, requestId, callback);
    }
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(self);
    session.close(requestId);
    return true;
  }

  public boolean queryOnlineClients(String self, List<String> clients, final AVIMOnlineClientsCallback callback) {
    LOGGER.d("queryOnlineClients...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(self, null, requestId, callback);
    }
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(self);
    session.queryOnlinePeers(clients, requestId);
    return true;
  }

  public boolean createConversation(final String self, final List<String> memberList,
                          final Map<String, Object> attribute, final boolean isTransient, final boolean isUnique,
                          final boolean isTemp, int tempTTL, final AVIMConversationCreatedCallback callback) {
    LOGGER.d("createConversation...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(self, null, requestId, callback);
    }
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(self);
    session.createConversation(memberList, attribute, isTransient, isUnique, isTemp, tempTTL, false, requestId);
    return true;
  }


  public boolean sendMessage(String clientId, String conversationId, int convType, final AVIMMessage message,
                             final AVIMMessageOption messageOption, final AVIMConversationCallback callback) {
    LOGGER.d("updateMessage...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, conversationId, requestId, callback);
    }
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId);
    AVConversationHolder holder = session.getConversationHolder(conversationId, convType);
    message.setFrom(clientId);
    holder.sendMessage(message, requestId, messageOption);
    return false;
  }

  public boolean updateMessage(String clientId, int convType, AVIMMessage oldMessage, AVIMMessage newMessage, AVIMMessageUpdatedCallback callback) {
    LOGGER.d("updateMessage...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, oldMessage.getConversationId(), requestId, callback);
    }
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId);
    AVConversationHolder holder = session.getConversationHolder(oldMessage.getConversationId(), convType);
    holder.patchMessage(oldMessage, newMessage, null, Conversation.AVIMOperation.CONVERSATION_UPDATE_MESSAGE,
            requestId);
    return true;
  }

  public boolean recallMessage(String clientId, int convType, AVIMMessage message, AVIMMessageRecalledCallback callback) {
    LOGGER.d("recallMessage...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, message.getConversationId(), requestId, callback);
    }
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId);
    AVConversationHolder holder = session.getConversationHolder(message.getConversationId(), convType);
    holder.patchMessage(null, null, message, Conversation.AVIMOperation.CONVERSATION_RECALL_MESSAGE,
            requestId);
    return true;
  }

  public boolean fetchReceiptTimestamps(String clientId, String conversationId, Conversation.AVIMOperation operation,
                                 AVIMConversationCallback callback) {
    return true;
  }

  public boolean queryMessages(String clientId, String conversationId, int convType, String params,
                        Conversation.AVIMOperation operation, AVIMMessagesQueryCallback callback) {
    LOGGER.d("recallMessage...");
    int requestId = WindTalker.getNextIMRequestId();
    if (this.needCacheRequestKey) {
      RequestCache.getInstance().addRequestCallback(clientId, conversationId, requestId, callback);
    }
    AVSession session = AVSessionManager.getInstance().getOrCreateSession(clientId);
    AVConversationHolder holder = session.getConversationHolder(conversationId, convType);
    Map<String, Object> queryParam = JSON.parseObject(params, Map.class);
    holder.processConversationCommandFromClient(operation, queryParam, requestId);
    return true;
  }

  public void onOperationCompleted(String clientId, String conversationId, int requestId,
                                   Conversation.AVIMOperation operation, Throwable throwable) {
    LOGGER.d("enter onOperationCompleted with clientId=" + clientId + ", convId=" + conversationId + ", requestId="
      + requestId + ", operation=" + operation);
    AVCallback callback = RequestCache.getInstance().getRequestCallback(clientId, conversationId, requestId);
    if (null == callback) {
      LOGGER.w("encounter illegal response, ignore it: clientId=" + clientId + ", convId=" + conversationId + ", requestId=" + requestId);
      return;
    }
    switch (operation) {
      case CLIENT_OPEN:
      case CLIENT_DISCONNECT:
        callback.internalDone(AVIMClient.getInstance(clientId), AVIMException.wrapperAVException(throwable));
        break;
      case CONVERSATION_UPDATE_MESSAGE:
      case CONVERSATION_RECALL_MESSAGE:
      case CONVERSATION_CREATION:
      case CONVERSATION_MESSAGE_QUERY:
        callback.internalDone(AVIMException.wrapperAVException(throwable));
        break;
      case CONVERSATION_SEND_MESSAGE:
        callback.internalDone(AVIMException.wrapperAVException(throwable));
        break;
      default:
        LOGGER.w("no operation matched, ignore response.");
        break;
    }
    RequestCache.getInstance().cleanRequestCallback(clientId, conversationId, requestId);
  }

  public void onOperationCompletedEx(String clientId, String conversationId, int requestId,
                                     Conversation.AVIMOperation operation, Map<String, Object> resultData) {
    LOGGER.d("enter onOperationCompletedEx with clientId=" + clientId + ", convId=" + conversationId + ", requestId="
            + requestId + ", operation=" + operation);
    AVCallback callback = RequestCache.getInstance().getRequestCallback(clientId, conversationId, requestId);
    if (null == callback) {
      LOGGER.w("encounter illegal response, ignore it: clientId=" + clientId + ", convId=" + conversationId + ", requestId=" + requestId);
      return;
    }
    switch (operation) {
      case CLIENT_ONLINE_QUERY:
        callback.internalDone(resultData, null);
        break;
      case CLIENT_REFRESH_TOKEN:
        break;
      case CONVERSATION_UPDATE_MESSAGE:
      case CONVERSATION_RECALL_MESSAGE:
        break;
      case CONVERSATION_CREATION:
        break;
      case CONVERSATION_MESSAGE_QUERY:
        break;
      default:
        break;
    }
  }
  public void onMessageArrived(String clientId, String conversationId, int requestId,
                               Conversation.AVIMOperation operation, Messages.GenericCommand command) {
    ;
  }
  public void onLiveQueryCompleted(int requestId, Throwable throwable) {
    ;
  }
  public void onPushMessage(String message, String messageId) {
    ;
  }
}