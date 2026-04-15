package com.example.demo.domain.shared.vehicleblacklist;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QVehicleBlacklist is a Querydsl query type for VehicleBlacklist
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QVehicleBlacklist extends EntityPathBase<VehicleBlacklist> {

    private static final long serialVersionUID = -2094214091L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QVehicleBlacklist vehicleBlacklist = new QVehicleBlacklist("vehicleBlacklist");

    public final StringPath carNumber = createString("carNumber");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> endDate = createDateTime("endDate", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath reasonDetail = createString("reasonDetail");

    public final EnumPath<com.example.demo.domain.shared.vehicleblacklist.enums.BlacklistReasonType> reasonType = createEnum("reasonType", com.example.demo.domain.shared.vehicleblacklist.enums.BlacklistReasonType.class);

    public final DateTimePath<java.time.LocalDateTime> releasedAt = createDateTime("releasedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> startDate = createDateTime("startDate", java.time.LocalDateTime.class);

    public final EnumPath<com.example.demo.domain.shared.vehicleblacklist.enums.BlacklistStatus> status = createEnum("status", com.example.demo.domain.shared.vehicleblacklist.enums.BlacklistStatus.class);

    public final com.example.demo.domain.shared.vehicle.QVehicle vehicle;

    public QVehicleBlacklist(String variable) {
        this(VehicleBlacklist.class, forVariable(variable), INITS);
    }

    public QVehicleBlacklist(Path<? extends VehicleBlacklist> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QVehicleBlacklist(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QVehicleBlacklist(PathMetadata metadata, PathInits inits) {
        this(VehicleBlacklist.class, metadata, inits);
    }

    public QVehicleBlacklist(Class<? extends VehicleBlacklist> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.vehicle = inits.isInitialized("vehicle") ? new com.example.demo.domain.shared.vehicle.QVehicle(forProperty("vehicle"), inits.get("vehicle")) : null;
    }

}

