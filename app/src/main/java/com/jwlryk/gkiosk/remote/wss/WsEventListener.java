package com.jwlryk.gkiosk.remote.wss;

public interface WsEventListener {
    default void onOpen() {}
    default void onClose(int code, String reason, boolean remote) {}
    default void onError(Exception ex) {}
    default void onProductMessage(int productNumber) {}
    default void onRefresh() {}
    default void onRawMessage(String message) {}
}

