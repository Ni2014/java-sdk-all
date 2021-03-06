package cn.leancloud.im.v2;

public class AVIMBinaryMessage extends AVIMMessage {
  private byte[] bytes = new byte[0];

  /**
   * default constructor
   */
  public AVIMBinaryMessage() {
    super();
  }

  /**
   * constructor
   * @param conversationId - conversation id
   * @param from           - message sender
   */
  public AVIMBinaryMessage(String conversationId, String from) {
    super(conversationId, from);
  }

  /**
   * constructor
   * @param conversationId - conversation id
   * @param from           - message sender
   * @param timestamp      - message send timestamp
   * @param deliveredAt    - message deliver timestamp
   */
  public AVIMBinaryMessage(String conversationId, String from, long timestamp, long deliveredAt) {
    super(conversationId, from, timestamp, deliveredAt);
  }

  /**
   * constructor
   * @param conversationId - conversation id
   * @param from           - message sender
   * @param timestamp      - message send timestamp
   * @param deliveredAt    - message deliver timestamp
   * @param readAt         - message read timestamp
   */
  public AVIMBinaryMessage(String conversationId, String from, long timestamp, long deliveredAt, long readAt) {
    super(conversationId, from, timestamp, deliveredAt, readAt);
  }

  /**
   * create binary instance by copying AVIMMessage instance(except content field).
   * @param other
   * @return
   */
  public static AVIMBinaryMessage createInstanceFromMessage(AVIMMessage other) {
    if (null == other) {
      return null;
    }
    AVIMBinaryMessage msg = new AVIMBinaryMessage();
    msg.conversationId = other.conversationId;
    msg.currentClient = other.currentClient;
    msg.from = other.from;
    msg.ioType = other.ioType;
    msg.mentionList = other.mentionList;
    msg.mentionAll = other.mentionAll;
    msg.messageId = other.messageId;
    msg.uniqueToken = other.uniqueToken;
    msg.status = other.status;

    msg.deliveredAt = other.deliveredAt;
    msg.readAt = other.readAt;
    msg.timestamp = other.timestamp;
    msg.updateAt = other.updateAt;
    return msg;
  }

  /**
   * set binary content.
   * @param val
   */
  public void setBytes(byte[] val) {
    bytes = val;
  }

  /**
   * get binary content.
   * @return
   */
  public byte[] getBytes() {
    return bytes;
  }
}
