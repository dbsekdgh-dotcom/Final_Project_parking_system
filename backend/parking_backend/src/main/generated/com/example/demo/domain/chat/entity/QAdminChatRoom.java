package com.example.demo.domain.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAdminChatRoom is a Querydsl query type for AdminChatRoom
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAdminChatRoom extends EntityPathBase<AdminChatRoom> {

    private static final long serialVersionUID = 1256707453L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAdminChatRoom adminChatRoom = new QAdminChatRoom("adminChatRoom");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final com.example.demo.domain.auth.admin.entity.QAdmin createdBy;

    public final ListPath<AdminChatRoomMember, QAdminChatRoomMember> members = this.<AdminChatRoomMember, QAdminChatRoomMember>createList("members", AdminChatRoomMember.class, QAdminChatRoomMember.class, PathInits.DIRECT2);

    public final NumberPath<Long> roomId = createNumber("roomId", Long.class);

    public final StringPath roomName = createString("roomName");

    public final EnumPath<com.example.demo.domain.chat.enums.RoomType> roomType = createEnum("roomType", com.example.demo.domain.chat.enums.RoomType.class);

    public QAdminChatRoom(String variable) {
        this(AdminChatRoom.class, forVariable(variable), INITS);
    }

    public QAdminChatRoom(Path<? extends AdminChatRoom> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAdminChatRoom(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAdminChatRoom(PathMetadata metadata, PathInits inits) {
        this(AdminChatRoom.class, metadata, inits);
    }

    public QAdminChatRoom(Class<? extends AdminChatRoom> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.createdBy = inits.isInitialized("createdBy") ? new com.example.demo.domain.auth.admin.entity.QAdmin(forProperty("createdBy")) : null;
    }

}

