package com.alttd.alttdqueue.data;

public enum QueueResponse
{
    NOT_FULL(true),
    ADDED_LOW_PRIORITY(false),
    ADDED_MID_PRIORITY(false),
    ADDED_HIGH_PRIORITY(false),
    ALREADY_ADDED(false),
    SKIP_QUEUE(false);

    private boolean connected;

    private QueueResponse(boolean connected)
    {
        this.connected = connected;
    }

    public boolean isConnected()
    {
        return connected;
    }
}
