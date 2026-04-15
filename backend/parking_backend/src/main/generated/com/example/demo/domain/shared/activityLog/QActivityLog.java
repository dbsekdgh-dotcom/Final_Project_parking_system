package com.example.demo.domain.shared.activityLog;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QActivityLog is a Querydsl query type for ActivityLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QActivityLog extends EntityPathBase<ActivityLog> {

    private static final long serialVersionUID = -1675523197L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QActivityLog activityLog = new QActivityLog("activityLog");

    public final NumberPath<Long> activityId = createNumber("activityId", Long.class);

    public final EnumPath<com.example.demo.domain.shared.activityLog.enums.ActivityType> activityType = createEnum("activityType", com.example.demo.domain.shared.activityLog.enums.ActivityType.class);

    public final StringPath carNumber = createString("carNumber");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final com.example.demo.domain.shared.household.QHousehold household;

    public final StringPath message = createString("message");

    public final com.example.demo.domain.shared.parkinglog.QParkingLog parkingLog;

    public final com.example.demo.domain.shared.payment.QPayment payment;

    public final com.example.demo.domain.shared.reservation.QReservation reservation;

    public QActivityLog(String variable) {
        this(ActivityLog.class, forVariable(variable), INITS);
    }

    public QActivityLog(Path<? extends ActivityLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QActivityLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QActivityLog(PathMetadata metadata, PathInits inits) {
        this(ActivityLog.class, metadata, inits);
    }

    public QActivityLog(Class<? extends ActivityLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.household = inits.isInitialized("household") ? new com.example.demo.domain.shared.household.QHousehold(forProperty("household")) : null;
        this.parkingLog = inits.isInitialized("parkingLog") ? new com.example.demo.domain.shared.parkinglog.QParkingLog(forProperty("parkingLog"), inits.get("parkingLog")) : null;
        this.payment = inits.isInitialized("payment") ? new com.example.demo.domain.shared.payment.QPayment(forProperty("payment"), inits.get("payment")) : null;
        this.reservation = inits.isInitialized("reservation") ? new com.example.demo.domain.shared.reservation.QReservation(forProperty("reservation"), inits.get("reservation")) : null;
    }

}

