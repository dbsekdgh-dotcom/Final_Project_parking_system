package com.example.demo.domain.payment;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPayment is a Querydsl query type for Payment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPayment extends EntityPathBase<Payment> {

    private static final long serialVersionUID = 591865038L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPayment payment = new QPayment("payment");

    public final NumberPath<Long> amount = createNumber("amount", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath externalPaymentId = createString("externalPaymentId");

    public final DateTimePath<java.time.LocalDateTime> paidAt = createDateTime("paidAt", java.time.LocalDateTime.class);

    public final com.example.demo.domain.parking.log.QParkingLog parkingLog;

    public final NumberPath<Long> paymentId = createNumber("paymentId", Long.class);

    public final EnumPath<com.example.demo.domain.payment.enums.PaymentMethod> paymentMethod = createEnum("paymentMethod", com.example.demo.domain.payment.enums.PaymentMethod.class);

    public final EnumPath<com.example.demo.domain.payment.enums.PaymentStatus> paymentStatus = createEnum("paymentStatus", com.example.demo.domain.payment.enums.PaymentStatus.class);

    public final EnumPath<com.example.demo.domain.payment.enums.PaymentType> paymentType = createEnum("paymentType", com.example.demo.domain.payment.enums.PaymentType.class);

    public final NumberPath<Long> priceSnapshot = createNumber("priceSnapshot", Long.class);

    public final NumberPath<Long> refundedAmount = createNumber("refundedAmount", Long.class);

    public final com.example.demo.domain.system.store.QStore store;

    public final NumberPath<Integer> ticketQuantity = createNumber("ticketQuantity", Integer.class);

    public final com.example.demo.domain.vehicle.QVehicle vehicle;

    public QPayment(String variable) {
        this(Payment.class, forVariable(variable), INITS);
    }

    public QPayment(Path<? extends Payment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPayment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPayment(PathMetadata metadata, PathInits inits) {
        this(Payment.class, metadata, inits);
    }

    public QPayment(Class<? extends Payment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.parkingLog = inits.isInitialized("parkingLog") ? new com.example.demo.domain.parking.log.QParkingLog(forProperty("parkingLog"), inits.get("parkingLog")) : null;
        this.store = inits.isInitialized("store") ? new com.example.demo.domain.system.store.QStore(forProperty("store"), inits.get("store")) : null;
        this.vehicle = inits.isInitialized("vehicle") ? new com.example.demo.domain.vehicle.QVehicle(forProperty("vehicle"), inits.get("vehicle")) : null;
    }

}

