package com.example.demo.domain.parking.space;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QParkingSpace is a Querydsl query type for ParkingSpace
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QParkingSpace extends EntityPathBase<ParkingSpace> {

    private static final long serialVersionUID = 1471215964L;

    public static final QParkingSpace parkingSpace = new QParkingSpace("parkingSpace");

    public final EnumPath<com.example.demo.domain.parking.space.enums.Floor> floor = createEnum("floor", com.example.demo.domain.parking.space.enums.Floor.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDisabled = createBoolean("isDisabled");

    public final BooleanPath isEvCharge = createBoolean("isEvCharge");

    public final BooleanPath isReservation = createBoolean("isReservation");

    public final DateTimePath<java.time.LocalDateTime> lastStatusChangedAt = createDateTime("lastStatusChangedAt", java.time.LocalDateTime.class);

    public final StringPath spaceCode = createString("spaceCode");

    public final EnumPath<com.example.demo.domain.parking.space.enums.SpaceStatus> status = createEnum("status", com.example.demo.domain.parking.space.enums.SpaceStatus.class);

    public QParkingSpace(String variable) {
        super(ParkingSpace.class, forVariable(variable));
    }

    public QParkingSpace(Path<? extends ParkingSpace> path) {
        super(path.getType(), path.getMetadata());
    }

    public QParkingSpace(PathMetadata metadata) {
        super(ParkingSpace.class, metadata);
    }

}

