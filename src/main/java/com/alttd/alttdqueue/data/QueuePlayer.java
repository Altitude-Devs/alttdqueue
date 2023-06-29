package com.alttd.alttdqueue.data;

import java.util.UUID;

public record QueuePlayer(UUID uuid, long queueJoinTime, Priority priority) {
}
