package com.example.demo.domain.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAdminChatRoomMember is a Querydsl query type for AdminChatRoomMember
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAdminChatRoomMember extends EntityPathBase<AdminChatRoomMember> {

    private static final long serialVersionUID = 780565111L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAdminChatRoomMember adminChatRoomMember = new QAdminChatRoomMember("adminChatRoomMember");

    public final com.example.demo.domain.auth.admin.entity.QAdmin admin;

    public final DateTimePath<java.time.LocalDateTime> joinedAt = createDateTime("joinedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> lastReadMessageId = createNumber("lastReadMessageId", Long.class);

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final QAdminChatRoom room;

    public QAdminChatRoomMember(String variable) {
        this(AdminChatRoomMember.class, forVariable(variable), INITS);
    }

    public QAdminChatRoomMember(Path<? extends AdminChatRoomMember> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAdminChatRoomMember(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAdminChatRoomMember(PathMetadata metadata, PathInits inits) {
        this(AdminChatRoomMember.class, metadata, inits);
    }

    public QAdminChatRoomMember(Class<? extends AdminChatRoomMember> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.admin = inits.isInitialized("admin") ? new com.example.demo.domain.auth.admin.entity.QAdmin(forProperty("admin")) : null;
        this.room = inits.isInitialized("room") ? new QAdminChatRoom(forProperty("room"), inits.get("room")) : null;
    }

}

