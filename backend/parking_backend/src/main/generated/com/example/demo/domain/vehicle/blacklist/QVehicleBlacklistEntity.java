package com.example.demo.domain.vehicle.blacklist;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QVehicleBlacklistEntity is a Querydsl query type for VehicleBlacklistEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QVehicleBlacklistEntity extends EntityPathBase<VehicleBlacklistEntity> {

    private static final long serialVersionUID = -164556713L;

    public static final QVehicleBlacklistEntity vehicleBlacklistEntity = new QVehicleBlacklistEntity("vehicleBlacklistEntity");

    public final StringPath carNumber = createString("carNumber");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> endDate = createDateTime("endDate", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath reasonDetail = createString("reasonDetail");

    public final EnumPath<com.example.demo.domain.vehicle.blacklist.enums.BlacklistReasonType> reasonType = createEnum("reasonType", com.example.demo.domain.vehicle.blacklist.enums.BlacklistReasonType.class);

    public final DateTimePath<java.time.LocalDateTime> releasedAt = createDateTime("releasedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> startDate = createDateTime("startDate", java.time.LocalDateTime.class);

    public final EnumPath<com.example.demo.domain.vehicle.blacklist.enums.BlacklistStatus> status = createEnum("status", com.example.demo.domain.vehicle.blacklist.enums.BlacklistStatus.class);

    public final NumberPath<Long> vehicleId = createNumber("vehicleId", Long.class);

    public QVehicleBlacklistEntity(String variable) {
        super(VehicleBlacklistEntity.class, forVariable(variable));
    }

    public QVehicleBlacklistEntity(Path<? extends VehicleBlacklistEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QVehicleBlacklistEntity(PathMetadata metadata) {
        super(VehicleBlacklistEntity.class, metadata);
    }

}

