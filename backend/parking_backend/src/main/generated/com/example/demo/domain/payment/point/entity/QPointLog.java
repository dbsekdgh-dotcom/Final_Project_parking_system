package com.example.demo.domain.payment.point.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPointLog is a Querydsl query type for PointLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPointLog extends EntityPathBase<PointLog> {

    private static final long serialVersionUID = -1193572887L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPointLog pointLog = new QPointLog("pointLog");

    public final NumberPath<Integer> afterPoint = createNumber("afterPoint", Integer.class);

    public final NumberPath<Integer> beforePoint = createNumber("beforePoint", Integer.class);

    public final NumberPath<Integer> changeAmount = createNumber("changeAmount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final com.example.demo.domain.payment.QPayment payment;

    public final NumberPath<Long> pointLogId = createNumber("pointLogId", Long.class);

    public final EnumPath<PointReason> reason = createEnum("reason", PointReason.class);

    public final com.example.demo.domain.resident.QUser user;

    public QPointLog(String variable) {
        this(PointLog.class, forVariable(variable), INITS);
    }

    public QPointLog(Path<? extends PointLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPointLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPointLog(PathMetadata metadata, PathInits inits) {
        this(PointLog.class, metadata, inits);
    }

    public QPointLog(Class<? extends PointLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.payment = inits.isInitialized("payment") ? new com.example.demo.domain.payment.QPayment(forProperty("payment"), inits.get("payment")) : null;
        this.user = inits.isInitialized("user") ? new com.example.demo.domain.resident.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

