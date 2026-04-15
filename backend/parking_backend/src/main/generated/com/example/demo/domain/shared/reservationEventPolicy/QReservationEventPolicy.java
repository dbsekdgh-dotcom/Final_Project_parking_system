package com.example.demo.domain.shared.reservationEventPolicy;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReservationEventPolicy is a Querydsl query type for ReservationEventPolicy
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReservationEventPolicy extends EntityPathBase<ReservationEventPolicy> {

    private static final long serialVersionUID = 1520521013L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReservationEventPolicy reservationEventPolicy = new QReservationEventPolicy("reservationEventPolicy");

    public final com.example.demo.domain.admin.entity.QAdmin admin;

    public final NumberPath<Integer> dailyLimitPerHousehold = createNumber("dailyLimitPerHousehold", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> endDate = createDateTime("endDate", java.time.LocalDateTime.class);

    public final StringPath eventName = createString("eventName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> maxActiveReservations = createNumber("maxActiveReservations", Integer.class);

    public final NumberPath<Integer> monthlyLimitPerHousehold = createNumber("monthlyLimitPerHousehold", Integer.class);

    public final BooleanPath noShowPenaltyEnabled = createBoolean("noShowPenaltyEnabled");

    public final NumberPath<Integer> permittedMinutes = createNumber("permittedMinutes", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> startDate = createDateTime("startDate", java.time.LocalDateTime.class);

    public QReservationEventPolicy(String variable) {
        this(ReservationEventPolicy.class, forVariable(variable), INITS);
    }

    public QReservationEventPolicy(Path<? extends ReservationEventPolicy> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReservationEventPolicy(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReservationEventPolicy(PathMetadata metadata, PathInits inits) {
        this(ReservationEventPolicy.class, metadata, inits);
    }

    public QReservationEventPolicy(Class<? extends ReservationEventPolicy> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.admin = inits.isInitialized("admin") ? new com.example.demo.domain.admin.entity.QAdmin(forProperty("admin")) : null;
    }

}

