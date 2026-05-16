package com.karina.smeet.entity.mongo;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "call_records")
@CompoundIndexes({
        @CompoundIndex(name = "idx_call_room", def = "{'roomId': 1, 'startedAt': -1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class CallRecord {

    @Id
    String id;

    String roomId;

    @Indexed(unique = true)
    String callSessionId;

    Instant startedAt;
    Instant endedAt;

    @Builder.Default
    private List<Participant> participants = new ArrayList<>();


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Participant {
        private String userId;
        private Instant joinedAt;
        private Instant leftAt;
    }
}
