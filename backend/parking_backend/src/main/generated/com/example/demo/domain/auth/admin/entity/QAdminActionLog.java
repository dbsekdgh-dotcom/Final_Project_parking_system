package com.example.demo.domain.auth.admin.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAdminActionLog is a Querydsl query type for AdminActionLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAdminActionLog extends EntityPathBase<AdminActionLog> {

    private static final long serialVersionUID = -38309195L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAdminActionLog adminActionLog = new QAdminActionLog("adminActionLog");

    public final NumberPath<Long> actionId = createNumber("actionId", Long.class);

    public final EnumPath<com.example.demo.domain.auth.admin.enums.ActionType> actionType = createEnum("actionType", com.example.demo.domain.auth.admin.enums.ActionType.class);

    public final QAdmin admin;

    public final StringPath afterData = createString("afterData");

    public final StringPath beforeData = createString("beforeData");

    public final StringPath changedFields = createString("changedFields");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final BooleanPath isReverted = createBoolean("isReverted");

    public final DateTimePath<java.time.LocalDateTime> revertedAt = createDateTime("revertedAt", java.time.LocalDateTime.class);

    public final QAdmin revertedByAdmin;

    public final NumberPath<Long> targetId = createNumber("targetId", Long.class);

    public final EnumPath<com.example.demo.domain.auth.admin.enums.TargetType> targetType = createEnum("targetType", com.example.demo.domain.auth.admin.enums.TargetType.class);

    public QAdminActionLog(String variable) {
        this(AdminActionLog.class, forVariable(variable), INITS);
    }

    public QAdminActionLog(Path<? extends AdminActionLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAdminActionLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAdminActionLog(PathMetadata metadata, PathInits inits) {
        this(AdminActionLog.class, metadata, inits);
    }

    public QAdminActionLog(Class<? extends AdminActionLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.admin = inits.isInitialized("admin") ? new QAdmin(forProperty("admin")) : null;
        this.revertedByAdmin = inits.isInitialized("revertedByAdmin") ? new QAdmin(forProperty("revertedByAdmin")) : null;
    }

}

