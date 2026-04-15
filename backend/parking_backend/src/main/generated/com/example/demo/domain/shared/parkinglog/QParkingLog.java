package com.example.demo.domain.shared.parkinglog;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QParkingLog is a Querydsl query type for ParkingLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QParkingLog extends EntityPathBase<ParkingLog> {

    private static final long serialVersionUID = 1222979989L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QParkingLog parkingLog = new QParkingLog("parkingLog");

    public final NumberPath<Long> calculatedFee = createNumber("calculatedFee", Long.class);

    public final StringPath carNumberSnapshot = createString("carNumberSnapshot");

    public final DateTimePath<java.time.LocalDateTime> enteredAt = createDateTime("enteredAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> entryCameraId = createNumber("entryCameraId", Long.class);

    public final StringPath entryPlateImage = createString("entryPlateImage");

    public final DateTimePath<java.time.LocalDateTime> entryTime = createDateTime("entryTime", java.time.LocalDateTime.class);

    public final NumberPath<Long> exitCameraId = createNumber("exitCameraId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> exitedAt = createDateTime("exitedAt", java.time.LocalDateTime.class);

    public final StringPath exitPlateImage = createString("exitPlateImage");

    public final DateTimePath<java.time.LocalDateTime> exitTime = createDateTime("exitTime", java.time.LocalDateTime.class);

    public final NumberPath<Integer> fee = createNumber("fee", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> freeExitUntil = createDateTime("freeExitUntil", java.time.LocalDateTime.class);

    public final NumberPath<Integer> graceMinutesSnapshot = createNumber("graceMinutesSnapshot", Integer.class);

    public final BooleanPath isBlacklist = createBoolean("isBlacklist");

    public final DateTimePath<java.time.LocalDateTime> paidAt = createDateTime("paidAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> parkingFeePolicyId = createNumber("parkingFeePolicyId", Long.class);

    public final NumberPath<Long> parkingLogId = createNumber("parkingLogId", Long.class);

    public final com.example.demo.domain.shared.parkingspace.QParkingSpace parkingSpace;

    public final EnumPath<com.example.demo.domain.shared.parkinglog.enums.ParkingStatus> parkingStatus = createEnum("parkingStatus", com.example.demo.domain.shared.parkinglog.enums.ParkingStatus.class);

    public final EnumPath<com.example.demo.domain.shared.parkinglog.enums.ParkingTypeSnapshot> parkingTypeSnapshot = createEnum("parkingTypeSnapshot", com.example.demo.domain.shared.parkinglog.enums.ParkingTypeSnapshot.class);

    public final DateTimePath<java.time.LocalDateTime> paymentRequestedAt = createDateTime("paymentRequestedAt", java.time.LocalDateTime.class);

    public final EnumPath<com.example.demo.domain.shared.parkinglog.enums.PaymentStatus> paymentStatus = createEnum("paymentStatus", com.example.demo.domain.shared.parkinglog.enums.PaymentStatus.class);

    public final NumberPath<Integer> rawFee = createNumber("rawFee", Integer.class);

    public final NumberPath<Integer> totalDiscountAmount = createNumber("totalDiscountAmount", Integer.class);

    public final NumberPath<Integer> totalDiscountMinutes = createNumber("totalDiscountMinutes", Integer.class);

    public final com.example.demo.domain.shared.vehicle.QVehicle vehicle;

    public QParkingLog(String variable) {
        this(ParkingLog.class, forVariable(variable), INITS);
    }

    public QParkingLog(Path<? extends ParkingLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QParkingLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QParkingLog(PathMetadata metadata, PathInits inits) {
        this(ParkingLog.class, metadata, inits);
    }

    public QParkingLog(Class<? extends ParkingLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.parkingSpace = inits.isInitialized("parkingSpace") ? new com.example.demo.domain.shared.parkingspace.QParkingSpace(forProperty("parkingSpace")) : null;
        this.vehicle = inits.isInitialized("vehicle") ? new com.example.demo.domain.shared.vehicle.QVehicle(forProperty("vehicle"), inits.get("vehicle")) : null;
    }

}

