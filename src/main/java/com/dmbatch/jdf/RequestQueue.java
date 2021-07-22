package com.dmbatch.jdf;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
class ConnectionInfo {
    @NonNull
    String appUser;
    String appPassword;
    @NonNull
    String queueName;
    @NonNull
    String port;
    @NonNull
    String queueManagerName;
    @NonNull
    String host;
    @NonNull
    String channel;
    @NonNull
    String applicationName;
}

@Getter
@Setter
public class RequestQueue {
    public ConnectionInfo connection;
    @NonNull
    String Message;

    public boolean validateInput() {
        if (this.getConnection() == null) {
            throw new Error("connection Object Required");
        }
        if (this.getMessage() == null) {
            throw new Error("message is Required");
        }
        if (this.connection.getApplicationName() == null) {
            throw new Error("applicationName is Required");
        }
        if (this.connection.getAppUser() == null) {
            throw new Error("appUser is Required");
        }
        if (this.connection.getChannel() == null) {
            throw new Error("channel is Required");
        }
        if (this.connection.getHost() == null) {
            throw new Error("host is Required");
        }
        if (this.connection.getPort() == null) {
            throw new Error("port is Required");
        }
        if (this.connection.getQueueManagerName() == null) {
            throw new Error("queueManagerName is Required");
        }
        if (this.connection.getApplicationName() == null) {
            throw new Error("applicationName  is Required");
        }
        return true;
    }
}

