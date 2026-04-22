package com.example.demo.domain.report.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QVehicleReportStat is a Querydsl query type for VehicleReportStat
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QVehicleReportStat extends EntityPathBase<VehicleReportStat> {

    private static final long serialVersionUID = 148246227L;

    public static final QVehicleReportStat vehicleReportStat = new QVehicleReportStat("vehicleReportStat");

    public final StringPath carNumber = createString("carNumber");

    public final DateTimePath<java.time.LocalDateTime> lastReportedAt = createDateTime("lastReportedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> totalReportCount = createNumber("totalReportCount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> validReportCount = createNumber("validReportCount", Integer.class);

    public QVehicleReportStat(String variable) {
        super(VehicleReportStat.class, forVariable(variable));
    }

    public QVehicleReportStat(Path<? extends VehicleReportStat> path) {
        super(path.getType(), path.getMetadata());
    }

    public QVehicleReportStat(PathMetadata metadata) {
        super(VehicleReportStat.class, metadata);
    }

}

