package com.example.demo.domain.shared.vehicle;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QVehicle is a Querydsl query type for Vehicle
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QVehicle extends EntityPathBase<Vehicle> {

    private static final long serialVersionUID = 584331761L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QVehicle vehicle = new QVehicle("vehicle");

    public final StringPath carNumber = createString("carNumber");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.example.demo.domain.shared.vehicle.enums.VehicleStatus> status = createEnum("status", com.example.demo.domain.shared.vehicle.enums.VehicleStatus.class);

    public final com.example.demo.domain.shared.user.QUser user;

    public final StringPath vehicleName = createString("vehicleName");

    public QVehicle(String variable) {
        this(Vehicle.class, forVariable(variable), INITS);
    }

    public QVehicle(Path<? extends Vehicle> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QVehicle(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QVehicle(PathMetadata metadata, PathInits inits) {
        this(Vehicle.class, metadata, inits);
    }

    public QVehicle(Class<? extends Vehicle> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.example.demo.domain.shared.user.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

