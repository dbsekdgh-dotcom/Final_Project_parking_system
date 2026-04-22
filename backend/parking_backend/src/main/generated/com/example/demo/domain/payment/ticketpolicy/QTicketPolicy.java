package com.example.demo.domain.payment.ticketpolicy;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTicketPolicy is a Querydsl query type for TicketPolicy
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTicketPolicy extends EntityPathBase<TicketPolicy> {

    private static final long serialVersionUID = -434630378L;

    public static final QTicketPolicy ticketPolicy = new QTicketPolicy("ticketPolicy");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final EnumPath<com.example.demo.domain.payment.ticketpolicy.enums.DiscountType> discountType = createEnum("discountType", com.example.demo.domain.payment.ticketpolicy.enums.DiscountType.class);

    public final NumberPath<Integer> discountValue = createNumber("discountValue", Integer.class);

    public final BooleanPath isFreeTicket = createBoolean("isFreeTicket");

    public final NumberPath<Integer> maxDiscountAmount = createNumber("maxDiscountAmount", Integer.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final BooleanPath stackable = createBoolean("stackable");

    public final EnumPath<com.example.demo.domain.payment.ticketpolicy.enums.Status> status = createEnum("status", com.example.demo.domain.payment.ticketpolicy.enums.Status.class);

    public final NumberPath<Long> ticketPolicyId = createNumber("ticketPolicyId", Long.class);

    public final EnumPath<com.example.demo.domain.payment.ticketpolicy.enums.UseType> useType = createEnum("useType", com.example.demo.domain.payment.ticketpolicy.enums.UseType.class);

    public final NumberPath<Integer> validDays = createNumber("validDays", Integer.class);

    public final NumberPath<Integer> validMinutes = createNumber("validMinutes", Integer.class);

    public QTicketPolicy(String variable) {
        super(TicketPolicy.class, forVariable(variable));
    }

    public QTicketPolicy(Path<? extends TicketPolicy> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTicketPolicy(PathMetadata metadata) {
        super(TicketPolicy.class, metadata);
    }

}

