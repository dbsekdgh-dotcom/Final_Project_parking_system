package com.example.demo.domain.reservation;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReservation is a Querydsl query type for Reservation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReservation extends EntityPathBase<Reservation> {

    private static final long serialVersionUID = 1710244698L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReservation reservation = new QReservation("reservation");

    public final DateTimePath<java.time.LocalDateTime> actual_entry_at = createDateTime("actual_entry_at", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> cancelledAt = createDateTime("cancelledAt", java.time.LocalDateTime.class);

    public final StringPath carNumber = createString("carNumber");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final BooleanPath isFree = createBoolean("isFree");

    public final EnumPath<com.example.demo.domain.reservation.enums.Purpose> purpose = createEnum("purpose", com.example.demo.domain.reservation.enums.Purpose.class);

    public final NumberPath<Long> reservationId = createNumber("reservationId", Long.class);

    public final EnumPath<com.example.demo.domain.reservation.enums.Status> status = createEnum("status", com.example.demo.domain.reservation.enums.Status.class);

    public final com.example.demo.domain.resident.QUser user;

    public final com.example.demo.domain.vehicle.QVehicle vehicle;

    public final DateTimePath<java.time.LocalDateTime> visitEndAt = createDateTime("visitEndAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> visitStartAt = createDateTime("visitStartAt", java.time.LocalDateTime.class);

    public QReservation(String variable) {
        this(Reservation.class, forVariable(variable), INITS);
    }

    public QReservation(Path<? extends Reservation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReservation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReservation(PathMetadata metadata, PathInits inits) {
        this(Reservation.class, metadata, inits);
    }

    public QReservation(Class<? extends Reservation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.example.demo.domain.resident.QUser(forProperty("user"), inits.get("user")) : null;
        this.vehicle = inits.isInitialized("vehicle") ? new com.example.demo.domain.vehicle.QVehicle(forProperty("vehicle"), inits.get("vehicle")) : null;
    }

}

