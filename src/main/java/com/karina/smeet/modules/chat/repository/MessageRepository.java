package com.karina.smeet.modules.chat.repository;

import com.karina.smeet.entity.mongo.Message;
import org.springframework.data.domain.Pageable;
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
}
