package cn.leancloud.im.v2;

public class AVIMClientOpenOption {
  /**
   * 是否强制单点登陆，默认为强制
   */
  private boolean isForceSingleLogin = true;
  private boolean isReconnect = false;

  public AVIMClientOpenOption() {
  }

  /**
   * 判断是否恢复重连
   * @return
   */
  public boolean isReconnect() {
    return this.isReconnect;
  }

  /**
   * 设置恢复重连标记
   *
   * @param reconnect
   */
  public void setReconnect(boolean reconnect) {
    this.isReconnect = reconnect;
  }
}
