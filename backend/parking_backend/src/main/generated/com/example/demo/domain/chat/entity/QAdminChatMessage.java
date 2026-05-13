package com.example.demo.domain.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAdminChatMessage is a Querydsl query type for AdminChatMessage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAdminChatMessage extends EntityPathBase<AdminChatMessage> {

    private static final long serialVersionUID = -1083060187L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAdminChatMessage adminChatMessage = new QAdminChatMessage("adminChatMessage");

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> messageId = createNumber("messageId", Long.class);

    public final QAdminChatRoom room;

    public final com.example.demo.domain.auth.admin.entity.QAdmin sender;

    public QAdminChatMessage(String variable) {
        this(AdminChatMessage.class, forVariable(variable), INITS);
    }

    public QAdminChatMessage(Path<? extends AdminChatMessage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAdminChatMessage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAdminChatMessage(PathMetadata metadata, PathInits inits) {
        this(AdminChatMessage.class, metadata, inits);
    }

    public QAdminChatMessage(Class<? extends AdminChatMessage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.room = inits.isInitialized("room") ? new QAdminChatRoom(forProperty("room"), inits.get("room")) : null;
        this.sender = inits.isInitialized("sender") ? new com.example.demo.domain.auth.admin.entity.QAdmin(forProperty("sender")) : null;
    }

}

