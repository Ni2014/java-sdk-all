package cn.leancloud.im;

import cn.leancloud.AVException;
import cn.leancloud.AVQuery;
import cn.leancloud.Configure;
import cn.leancloud.im.v2.*;
import cn.leancloud.im.v2.callback.*;
import cn.leancloud.im.v2.messages.AVIMRecalledMessage;
import cn.leancloud.im.v2.messages.AVIMTextMessage;
import cn.leancloud.session.AVConnectionManager;
import junit.framework.TestCase;

import java.sql.Time;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AVIMConversationTest extends TestCase {
  private CountDownLatch countDownLatch = null;
  private boolean opersationSucceed = false;
  private AVIMClient client = null;
  private List<String> memebers = Arrays.asList("User2", "User3");
  private String convName = "RealtimeUnitTest";
  private DummyConversationEventHandler conversationEventHandler = new DummyConversationEventHandler(0x00FFFF);

  public AVIMConversationTest(String suiteName) {
    super(suiteName);
    Configure.initialize();
    AVIMClient.setClientEventHandler(new DummyClientEventHandler());
    AVIMMessageManager.setConversationEventHandler(conversationEventHandler);
    AVConnectionManager manager = AVConnectionManager.getInstance();
    manager.startConnection();
    try {
      Thread.sleep(10000);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Override
  protected void setUp() throws Exception {
    this.countDownLatch = new CountDownLatch(1);
    opersationSucceed = false;
    conversationEventHandler.resetAllCount();
  }

  @Override
  protected void tearDown() throws Exception {
    this.countDownLatch = null;
    CountDownLatch tmpCounter = new CountDownLatch(1);
    if (null != client) {
      client.close(new AVIMClientCallback() {
        @Override
        public void done(AVIMClient client, AVIMException e) {
          tmpCounter.countDown();
        }
      });
      tmpCounter.await();
    }
    client = null;
  }

  public void testSendTextMessage() throws Exception {
    CountDownLatch tmpCounter = new CountDownLatch(1);
    client = AVIMClient.getInstance("testUser1");
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    client.createConversation(memebers, convName, null, false, true, new AVIMConversationCreatedCallback() {
      @Override
      public void done(AVIMConversation conversation, AVIMException e) {
        if (null != e) {
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          AVIMTextMessage msg = new AVIMTextMessage();
          msg.setText("test run @" + System.currentTimeMillis());
          conversation.sendMessage(msg, new AVIMConversationCallback() {
            @Override
            public void done(AVIMException ex) {
              if (null != ex) {
                System.out.println("failed to send message");
                ex.printStackTrace();
              } else {
                System.out.println("succeed to send message");
                opersationSucceed = true;
              }
              countDownLatch.countDown();
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testSendAndReceiveTextMessage() throws Exception {
    AVIMMessageManager.registerDefaultMessageHandler(new DummyMessageHandler());
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    final CountDownLatch tmpCounter2 = new CountDownLatch(1);
    client = AVIMClient.getInstance("testUser1");
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    client.createConversation(memebers, convName, null, false, true, new AVIMConversationCreatedCallback() {
      @Override
      public void done(AVIMConversation conversation, AVIMException e) {
        if (null != e) {
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          try {
            tmpCounter2.await();
          } catch (Exception ex) {
            ex.printStackTrace();
          }
          AVIMTextMessage msg = new AVIMTextMessage();
          msg.setText("test run @" + System.currentTimeMillis());
          conversation.sendMessage(msg, new AVIMConversationCallback() {
            @Override
            public void done(AVIMException ex) {
              if (null != ex) {
                System.out.println("failed to send message");
                ex.printStackTrace();
              } else {
                System.out.println("succeed to send message");
                opersationSucceed = true;
              }
              countDownLatch.countDown();
            }
          });
        }
      }
    });
    AVIMClient client2 = AVIMClient.getInstance("User2");
    client2.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (null != e) {
          System.out.println("User2 open failed.");
        }
        tmpCounter2.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
    Thread.sleep(3000);
  }

  public void testRecallMessage() throws Exception {
    CountDownLatch tmpCounter = new CountDownLatch(1);
    client = AVIMClient.getInstance("testUser1");
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    client.createConversation(memebers, convName, null, false, true, new AVIMConversationCreatedCallback() {
      @Override
      public void done(AVIMConversation conversation, AVIMException e) {
        if (null != e) {
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          AVIMTextMessage msg = new AVIMTextMessage();
          msg.setText("test run @" + System.currentTimeMillis());
          conversation.sendMessage(msg, new AVIMConversationCallback() {
            @Override
            public void done(AVIMException ex) {
              if (null != ex) {
                System.out.println("failed to send message");
                ex.printStackTrace();
                countDownLatch.countDown();
              } else {
                System.out.println("succeed to send message");
                conversation.recallMessage(msg, new AVIMMessageRecalledCallback() {
                  @Override
                  public void done(AVIMRecalledMessage recalledMessage, AVException e) {
                    if (null != e) {
                      System.out.println("failed to recall message");
                    } else {
                      opersationSucceed = true;
                    }
                    countDownLatch.countDown();
                  }
                });
              }
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }


  public void testUpdateMessage() throws Exception {
    CountDownLatch tmpCounter = new CountDownLatch(1);
    client = AVIMClient.getInstance("testUser1");
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    client.createConversation(memebers, convName, null, false, true, new AVIMConversationCreatedCallback() {
      @Override
      public void done(AVIMConversation conversation, AVIMException e) {
        if (null != e) {
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          AVIMTextMessage msg = new AVIMTextMessage();
          msg.setText("test run @" + System.currentTimeMillis());
          conversation.sendMessage(msg, new AVIMConversationCallback() {
            @Override
            public void done(AVIMException ex) {
              if (null != ex) {
                System.out.println("failed to send message");
                ex.printStackTrace();
                countDownLatch.countDown();
              } else {
                System.out.println("succeed to send message");
                AVIMTextMessage newMsg = new AVIMTextMessage();
                newMsg.setText("test updated @" + System.currentTimeMillis());
                conversation.updateMessage(msg, newMsg, new AVIMMessageUpdatedCallback() {
                  @Override
                  public void done(AVIMMessage curMessage, AVException e) {
                    if (null != e) {
                      System.out.println("failed to update message");
                    } else {
                      opersationSucceed = true;
                    }
                    countDownLatch.countDown();
                  }
                });
              }
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testQueryMessages() throws Exception {
    CountDownLatch tmpCounter = new CountDownLatch(1);
    client = AVIMClient.getInstance("testUser1");
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    client.createConversation(memebers, convName, null, false, true, new AVIMConversationCreatedCallback() {
      @Override
      public void done(AVIMConversation conversation, AVIMException e) {
        if (null != e) {
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          conversation.queryMessages(new AVIMMessagesQueryCallback() {
            @Override
            public void done(List<AVIMMessage> messages, AVIMException ex) {
              if (null != ex) {
                System.out.println("failed to query message");
                ex.printStackTrace();
              } else {
                System.out.println("succeed to query message");
                opersationSucceed = true;
              }
              countDownLatch.countDown();
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testJoinedNotification() throws Exception {
    client = AVIMClient.getInstance("testUser1");
    CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    Map<String, Object> attr = new HashMap<>();
    attr.put("testTs", System.currentTimeMillis());
    attr.put("owner", "testUser1");
    client.createConversation(memebers, convName, attr, false, false, new AVIMConversationCreatedCallback() {
      @Override
      public void done(AVIMConversation conversation, AVIMException e) {
        if (null != e) {
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          AVIMTextMessage msg = new AVIMTextMessage();
          msg.setText("test run @" + System.currentTimeMillis());
          conversation.sendMessage(msg, new AVIMConversationCallback() {
            @Override
            public void done(AVIMException ex) {
              if (null != ex) {
                System.out.println("failed to send message");
                ex.printStackTrace();
              } else {
                System.out.println("succeed to send message");
                opersationSucceed = true;
              }
              countDownLatch.countDown();
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
    Thread.sleep(2000);
  }

  public void testConversationQueryWithCache() throws Exception {
    client = AVIMClient.getInstance("testUser1");
    CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();

    AVIMConversationsQuery query = client.getConversationsQuery();
    query.containsMembers(Arrays.asList("testUser1"));
    query.addAscendingOrder("updatedAt");
    query.findInBackground(new AVIMConversationQueryCallback() {
      @Override
      public void done(List<AVIMConversation> conversations, AVIMException e) {
        if (null != e) {
          System.out.println("failed to query converstaion.");
        } else {
          System.out.println("succeed to query converstaion.");
          for (AVIMConversation conv: conversations) {
            System.out.println(conv);
          }
          // in core library, no db cache.
          opersationSucceed = true;
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testConversationQueryWithNetwork() throws Exception {
    client = AVIMClient.getInstance("testUser1");
    CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();

    AVIMConversationsQuery query = client.getConversationsQuery();
    query.containsMembers(Arrays.asList("testUser1"));
    query.addAscendingOrder("updatedAt");
    query.setQueryPolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    query.findInBackground(new AVIMConversationQueryCallback() {
      @Override
      public void done(List<AVIMConversation> conversations, AVIMException e) {
        if (null != e) {
          System.out.println("failed to query converstaion.");
        } else {
          System.out.println("succeed to query converstaion.");
          opersationSucceed = conversations.size() > 0;
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testDummyConversationEventHandlerCounter() throws Exception {
    AVIMClient client = AVIMClient.getInstance("testUser");
    AVIMConversation conv = client.getConversation("conversationId");
    this.conversationEventHandler.onBlocked(client, conv, "nobody");
    this.conversationEventHandler.onInvited(client, conv, "nobody");
    this.conversationEventHandler.onMemberBlocked(client, conv, Arrays.asList("Tom", "Jerry"),"nobody");
    assertTrue(this.conversationEventHandler.getCount(0x00ffFF) == 3);
  }

  public void testConversationJoinAndQuit() throws Exception {
    client = AVIMClient.getInstance("testUser1");
    CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5b6a909c756571003d3e1603";
    AVIMConversation conversation = client.getConversation(conversationId, true, false);
    conversation.join(new AVIMConversationCallback() {
      @Override
      public void done(AVIMException e) {
        if (null != e) {
          System.out.println("failed to join conversation. cause:" + e.getMessage());
          countDownLatch.countDown();
        } else {
          System.out.println("succeed to join conversation.");
          try {
            Thread.sleep(1000);
          } catch (Exception ex) {
            ex.printStackTrace();
          }
          conversation.quit(new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
              if (null != e) {
                System.out.println("failed to quit conversation. cause:" + e.getMessage());
              } else {
                System.out.println("succeed to quit conversation.");
                opersationSucceed = true;
              }
              countDownLatch.countDown();
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testConversationRead() throws Exception {
    client = AVIMClient.getInstance("testUser1");
    CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5805eefd8159ccabfc39bc1c";
    AVIMConversation conversation = client.getConversation(conversationId, false, false);
    conversation.join(new AVIMConversationCallback() {
      @Override
      public void done(AVIMException e) {
        if (null != e) {
          System.out.println("failed to join conversation. cause: " + e.getMessage());
          countDownLatch.countDown();
        } else {
          System.out.println("succeed to join conversation");
          conversation.read();
          try {
            Thread.sleep(1000);
          } catch (Exception ex) {
            ex.printStackTrace();
          }
          conversation.quit(new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
              if (null != e) {
                System.out.println("failed to quit conversation. cause:" + e.getMessage());
              } else {
                System.out.println("succeed to quit conversation.");
                opersationSucceed = true;
              }
              countDownLatch.countDown();
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testConversationFetch() throws Exception {
    client = AVIMClient.getInstance("testUser1");
    CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5805eefd8159ccabfc39bc1c";
    AVIMConversation conversation = client.getConversation(conversationId, false, false);
    conversation.fetchInfoInBackground(new AVIMConversationCallback() {
      @Override
      public void done(AVIMException e) {
        if (null != e) {
          System.out.println("failed to fetch info. cause: " + e.getMessage());
        } else {
          System.out.println("succeed to fetch info.");
          opersationSucceed = true;
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testConversationFetchLastTime() throws Exception {
    client = AVIMClient.getInstance("testUser1");
    CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5805eefd8159ccabfc39bc1c";
    AVIMConversation conversation = client.getConversation(conversationId, false, false);
    conversation.fetchReceiptTimestamps(new AVIMConversationCallback() {
      @Override
      public void done(AVIMException e) {
        if (null != e) {
          System.out.println("failed to fetch ReceiptTimestamps. cause: " + e.getMessage());
          opersationSucceed = true;
        } else {
          System.out.println("succeed to fetch ReceiptTimestamps.");
          System.out.println("LastReadAt: " + conversation.getLastReadAt());
          System.out.println("LastDeliveredAt: " + conversation.getLastDeliveredAt());
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testConversationFetchLastTimeWithMembership() throws Exception {
    client = AVIMClient.getInstance("testUser1");
    CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5805eefd8159ccabfc39bc1c";
    AVIMConversation conversation = client.getConversation(conversationId, false, false);
    conversation.join(new AVIMConversationCallback() {
      @Override
      public void done(AVIMException e) {
        if (null != e) {
          System.out.println("failed to join conversation. cause:" + e.getMessage());
          countDownLatch.countDown();
        } else {
          conversation.fetchReceiptTimestamps(new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
              if (null != e) {
                System.out.println("failed to fetch ReceiptTimestamps. cause: " + e.getMessage());
                countDownLatch.countDown();
              } else {
                System.out.println("succeed to fetch ReceiptTimestamps.");
                System.out.println("LastReadAt: " + conversation.getLastReadAt());
                System.out.println("LastDeliveredAt: " + conversation.getLastDeliveredAt());
                conversation.quit(new AVIMConversationCallback() {
                  @Override
                  public void done(AVIMException e) {
                    if (null != e) {
                      System.out.println("failed to quit conversation. cause:" + e.getMessage());
                    } else {
                      opersationSucceed = true;
                    }
                    countDownLatch.countDown();
                  }
                });
              }
            }
          });
        }
      }
    });

    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testMuteConversation() throws Exception {
    client = AVIMClient.getInstance("testUser1");
    CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5805eefd8159ccabfc39bc1c";
    AVIMConversation conversation = client.getConversation(conversationId, false, false);
    conversation.join(new AVIMConversationCallback() {
      @Override
      public void done(AVIMException e) {
        if (null != e) {
          System.out.println("failed to join conversation. cause: " + e.getMessage());
          countDownLatch.countDown();
        } else {
          System.out.println("succeed to join conversation");
          try {
            Thread.sleep(1000);
          } catch (Exception ex) {
            ex.printStackTrace();
          }
          conversation.mute(new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
              if (null != e) {
                System.out.println("failed to mute conversation. cause:" + e.getMessage());
                countDownLatch.countDown();
              } else {
                System.out.println("succeed to mute conversation.");
                conversation.unmute(new AVIMConversationCallback() {
                  @Override
                  public void done(AVIMException e) {
                    if (null != e) {
                      System.out.println("failed to unmute conversation. cause:" + e.getMessage());
                      countDownLatch.countDown();
                    } else {
                      System.out.println("succeed to unmute conversation.");
                      conversation.quit(new AVIMConversationCallback() {
                        @Override
                        public void done(AVIMException e) {
                          if (null != e) {
                            System.out.println("failed to quit conversation. cause:" + e.getMessage());
                          } else {
                            System.out.println("succeed to quit conversation.");
                            opersationSucceed = true;
                          }
                          countDownLatch.countDown();
                        }
                      });
                    }
                  }
                });
              }
            }
          });


        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testMuteConversationMembers() throws Exception {
    client = AVIMClient.getInstance("testUser1");
    CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    List<String> members = Arrays.asList("testUser2", "testUser3", "testUser4");
    List<String> blockMembers = Arrays.asList("testUser3", "testUser4");
    client.createConversation(members, "UnitTestConversation", null, false, true, new AVIMConversationCreatedCallback() {
      @Override
      public void done(AVIMConversation conversation, AVIMException e) {
        if (null != e) {
          System.out.println("failed to create conversation. cause: " + e.getMessage());
          countDownLatch.countDown();
        } else {
          System.out.println("succeed to create conversation.");
          conversation.muteMembers(blockMembers, new AVIMOperationPartiallySucceededCallback() {
            @Override
            public void done(AVIMException e, List<String> successfulClientIds, List<AVIMOperationFailure> failures) {
              if (null != e) {
                System.out.println("failed to mute conversation members. cause: " + e.getMessage());
                countDownLatch.countDown();
              } else {
                System.out.println("succeed to mute conversation members.");
                conversation.unmuteMembers(blockMembers, new AVIMOperationPartiallySucceededCallback() {
                  @Override
                  public void done(AVIMException e, List<String> successfulClientIds, List<AVIMOperationFailure> failures) {
                    if (null != e) {
                      System.out.println("failed to mute conversation members. cause: " + e.getMessage());
                    } else {
                      System.out.println("succeed to unmute conversation members.");
                      opersationSucceed = true;
                    }
                    countDownLatch.countDown();
                  }
                });
              }
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testSendAndReceiveMessage() throws Exception {
    final String senderId = "sender-" + System.currentTimeMillis();
    Thread.sleep(8000);
    final String receiverId = "receiver-" + System.currentTimeMillis();

    CountDownLatch receierOnlineLatch = new CountDownLatch(1);

    Runnable sendThread = new Runnable() {
      @Override
      public void run() {
        CountDownLatch tmpCounter = new CountDownLatch(1);
        AVIMClient client = AVIMClient.getInstance(senderId);
        client.open(new AVIMClientCallback() {
          @Override
          public void done(AVIMClient client, AVIMException e) {
            if (null != e) {
              System.out.println("failed to open sender client.");
              e.printStackTrace();
              tmpCounter.countDown();
            } else {
              try {
                receierOnlineLatch.await();
              } catch (Exception ex) {
                ex.printStackTrace();
              }
              client.createConversation(Arrays.asList(receiverId), null, null, false, true,
                      new AVIMConversationCreatedCallback() {
                        @Override
                        public void done(AVIMConversation conversation, AVIMException e) {
                          if (null != e) {
                            System.out.println("failed to create conversation from sender client.");
                            e.printStackTrace();
                            tmpCounter.countDown();
                          } else {
                            conversation.blockMembers(Arrays.asList("blockedUser"), new AVIMOperationPartiallySucceededCallback() {
                              @Override
                              public void done(AVIMException e, List<String> successfulClientIds, List<AVIMOperationFailure> failures) {
                                if (null != e) {
                                  System.out.println("failed to block members from sender client.");
                                  tmpCounter.countDown();
                                } else {
                                  AVIMTextMessage msg = new AVIMTextMessage();
                                  msg.setText("try to unblock user. @" + System.currentTimeMillis());
                                  conversation.sendMessage(msg, new AVIMConversationCallback() {
                                    @Override
                                    public void done(AVIMException e) {
                                      if (null != e) {
                                        System.out.println("failed to send message from sender client.");
                                        tmpCounter.countDown();
                                      } else {
                                        conversation.unblockMembers(Arrays.asList("blockedUser"), new AVIMOperationPartiallySucceededCallback() {
                                          @Override
                                          public void done(AVIMException e, List<String> successfulClientIds, List<AVIMOperationFailure> failures) {
                                            if (null != e) {
                                              System.out.println("failed to unblock members from sender client.");
                                              e.printStackTrace();
                                            }
                                            client.close(new AVIMClientCallback() {
                                              @Override
                                              public void done(AVIMClient client, AVIMException e) {
                                                if (null != e) {
                                                  System.out.println("failed to close sender client.");
                                                } else {
                                                  System.out.println("succeed to run all flow on sender side.");
                                                }
                                                tmpCounter.countDown();
                                              }
                                            });
                                          }
                                        });
                                      }
                                    }
                                  });

                                }
                              }
                            });
                          }
                        }
                      });
            }
          }
        });
        try {
          tmpCounter.await(60, TimeUnit.SECONDS);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    };
    Runnable receiveThread = new Runnable() {
      @Override
      public void run() {
        CountDownLatch tmpCounter = new CountDownLatch(1);
        AVIMClient client = AVIMClient.getInstance(receiverId);
        client.open(new AVIMClientCallback() {
          @Override
          public void done(AVIMClient client, AVIMException e) {
            receierOnlineLatch.countDown();
            if (null != e) {
              System.out.println("failed to open receiver client");
              e.printStackTrace();
              tmpCounter.countDown();
            } else {
              try {
                Thread.sleep(60000);
              } catch (Exception ex) {
                ex.printStackTrace();
              }
              client.close(new AVIMClientCallback() {
                @Override
                public void done(AVIMClient client, AVIMException e) {
                  if (null != e) {
                    System.out.println("failed to close receiver client");
                    e.printStackTrace();
                  }
                  tmpCounter.countDown();
                }
              });
            }
          }
        });
        try {
          tmpCounter.await(60, TimeUnit.SECONDS);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    };
    new Thread(sendThread).start();
    Thread.sleep(6000);
    new Thread(receiveThread).start();
    Thread.sleep(80000);
    int notifyCount = this.conversationEventHandler.getCount(0x00FFFF);
    System.out.println("notifyCount=" + notifyCount);
    assertTrue(notifyCount > 2);
  }
}
