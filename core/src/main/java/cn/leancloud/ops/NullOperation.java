package cn.leancloud.ops;

import java.util.Map;

public final class NullOperation extends BaseOperation {
  public static NullOperation INSTANCE = new NullOperation("", null);

  public NullOperation(String key, Object value) {
    super("", key, value, false);
  }
  public Object apply(Object obj) {
    return obj;
  }
  @Override
  protected ObjectFieldOperation mergeWithPrevious(ObjectFieldOperation other) {
    return other;
  }
  public Map<String, Object> encode() {
    return null;
  }
}
