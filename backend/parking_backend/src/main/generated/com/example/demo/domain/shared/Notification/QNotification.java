package com.example.demo.domain.shared.Notification;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNotification is a Querydsl query type for Notification
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotification extends EntityPathBase<Notification> {

    private static final long serialVersionUID = -364107787L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNotification notification = new QNotification("notification");

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> notificationId = createNumber("notificationId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> readAt = createDateTime("readAt", java.time.LocalDateTime.class);

    public final EnumPath<com.example.demo.domain.shared.Notification.enums.Status> status = createEnum("status", com.example.demo.domain.shared.Notification.enums.Status.class);

    public final StringPath title = createString("title");

    public final EnumPath<com.example.demo.domain.shared.Notification.enums.Type> type = createEnum("type", com.example.demo.domain.shared.Notification.enums.Type.class);

    public final com.example.demo.domain.shared.user.QUser user;

    public QNotification(String variable) {
        this(Notification.class, forVariable(variable), INITS);
    }

    public QNotification(Path<? extends Notification> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNotification(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNotification(PathMetadata metadata, PathInits inits) {
        this(Notification.class, metadata, inits);
    }

    public QNotification(Class<? extends Notification> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.example.demo.domain.shared.user.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

