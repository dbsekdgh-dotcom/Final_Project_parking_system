package com.example.demo.domain.shared.household;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QHousehold is a Querydsl query type for Household
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QHousehold extends EntityPathBase<Household> {

    private static final long serialVersionUID = 1568074903L;

    public static final QHousehold household = new QHousehold("household");

    public final NumberPath<Integer> activeReservationCount = createNumber("activeReservationCount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> householdId = createNumber("householdId", Long.class);

    public final EnumPath<com.example.demo.domain.shared.household.enums.IsActive> isActive = createEnum("isActive", com.example.demo.domain.shared.household.enums.IsActive.class);

    public final NumberPath<Integer> monthlyVisitCount = createNumber("monthlyVisitCount", Integer.class);

    public final NumberPath<Integer> todayVisitCount = createNumber("todayVisitCount", Integer.class);

    public final NumberPath<Integer> totalVisitCount = createNumber("totalVisitCount", Integer.class);

    public final NumberPath<Integer> unitNo = createNumber("unitNo", Integer.class);

    public QHousehold(String variable) {
        super(Household.class, forVariable(variable));
    }

    public QHousehold(Path<? extends Household> path) {
        super(path.getType(), path.getMetadata());
    }

    public QHousehold(PathMetadata metadata) {
        super(Household.class, metadata);
    }

}

