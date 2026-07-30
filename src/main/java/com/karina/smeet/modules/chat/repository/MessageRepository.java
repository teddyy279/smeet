package com.karina.smeet.modules.chat.repository;

import com.karina.smeet.entity.mongo.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


public interface MessageRepository extends MongoRepository<Message, String> {
    Optional<Message> findByIdAndDeletedAtIsNull(String messageId);

    List<Message> findByRoomIdAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(String roomId, Pageable pageable);


    //lt -> less than
    /*
    {
        'roomId': roomId,
        'deletedAt': null,
        '$or': [
            { 'createdAt': { '$lt': before } },
            { 'createdAt': before, '_id': { '$lt': beforeId } }
        ]
    }
     */
    @Query("""
           {
                'roomId': ?0,
                'deletedAt': null,
                '$or': [
                    { 'createdAt': { '$lt': ?1} },   
                    { 'createdAt': ?1, '_id': { '$lt': ?2}}
                ]     
           }
           """)
    List<Message> findOlderThanCursor(
            String roomId, Instant beforeCreatedAt, String beforeId, Pageable pageable);

    long countByRoomIdAndCreatedAtAfterAndDeletedAtIsNull(String roomId,Instant after);

    Optional<Message> findFirstByRoomIdAndDeletedAtNullOrderByCreatedAtDesc(String roomId);

    // Batch version of findFirstByRoomIdAndDeletedAtNullOrderByCreatedAtDesc: returns the latest
    // non-deleted message for every room in roomIds in a single aggregation instead of one query
    // per room (used when mapping a whole room list, e.g. RoomMapper#toRoomResponses).
    @Aggregation(pipeline = {
            "{ '$match': { 'roomId': { '$in': ?0 }, 'deletedAt': null } }",
            "{ '$sort': { 'createdAt': -1 } }",
            "{ '$group': { '_id': '$roomId', 'doc': { '$first': '$$ROOT' } } }"
    })
    List<RoomLastMessage> findLastMessagesByRoomIds(List<String> roomIds);

    record RoomLastMessage(@Field("_id") String roomId, Message doc) {}
}
