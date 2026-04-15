package com.example.demo.domain.shared.parkingfeepolicy;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QParkingFeePolicy is a Querydsl query type for ParkingFeePolicy
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QParkingFeePolicy extends EntityPathBase<ParkingFeePolicy> {

    private static final long serialVersionUID = 797343797L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QParkingFeePolicy parkingFeePolicy = new QParkingFeePolicy("parkingFeePolicy");

    public final com.example.demo.domain.admin.entity.QAdmin admin;

    public final NumberPath<Integer> baseFee = createNumber("baseFee", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> dailyMaxFee = createNumber("dailyMaxFee", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> effectiveFrom = createDateTime("effectiveFrom", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> effectiveTo = createDateTime("effectiveTo", java.time.LocalDateTime.class);

    public final NumberPath<Integer> graceMinutes = createNumber("graceMinutes", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final EnumPath<com.example.demo.domain.shared.parkingfeepolicy.enums.ParkingType> parkingType = createEnum("parkingType", com.example.demo.domain.shared.parkingfeepolicy.enums.ParkingType.class);

    public final NumberPath<Integer> unitFee = createNumber("unitFee", Integer.class);

    public final NumberPath<Integer> unitMinutes = createNumber("unitMinutes", Integer.class);

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QParkingFeePolicy(String variable) {
        this(ParkingFeePolicy.class, forVariable(variable), INITS);
    }

    public QParkingFeePolicy(Path<? extends ParkingFeePolicy> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QParkingFeePolicy(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QParkingFeePolicy(PathMetadata metadata, PathInits inits) {
        this(ParkingFeePolicy.class, metadata, inits);
    }

    public QParkingFeePolicy(Class<? extends ParkingFeePolicy> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.admin = inits.isInitialized("admin") ? new com.example.demo.domain.admin.entity.QAdmin(forProperty("admin")) : null;
    }

}

