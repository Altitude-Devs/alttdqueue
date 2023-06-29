package com.alttd.alttdqueue.data;

import java.util.LinkedList;

public class QueueWithPos {

    private final LinkedList<QueuePlayer> list;
    private int pos;

    public QueueWithPos(LinkedList<QueuePlayer> list) {
        this.list = list;
        this.pos = 0;
    }

    public QueuePlayer getNextPlayer() {
        if (pos < list.size()) {
            return list.get(pos++);
        }
        return null;
    }

    public QueuePlayer peekNextPlayer() {
        if (pos < list.size()) {
            return list.get(pos);
        }
        return null;
    }
}
