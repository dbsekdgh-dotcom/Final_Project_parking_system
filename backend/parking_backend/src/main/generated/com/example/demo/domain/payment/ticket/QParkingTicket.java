package com.example.demo.domain.payment.ticket;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QParkingTicket is a Querydsl query type for ParkingTicket
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QParkingTicket extends EntityPathBase<ParkingTicket> {

    private static final long serialVersionUID = -1741648006L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QParkingTicket parkingTicket = new QParkingTicket("parkingTicket");

    public final NumberPath<Integer> appliedAmount = createNumber("appliedAmount", Integer.class);

    public final com.example.demo.domain.parking.log.QParkingLog parkingLog;

    public final NumberPath<Long> parkingTicketId = createNumber("parkingTicketId", Long.class);

    public final EnumPath<Status> status = createEnum("status", Status.class);

    public final com.example.demo.domain.system.store.QStore store;

    public final com.example.demo.domain.payment.ticketpolicy.QTicketPolicy ticketPolicy;

    public QParkingTicket(String variable) {
        this(ParkingTicket.class, forVariable(variable), INITS);
    }

    public QParkingTicket(Path<? extends ParkingTicket> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QParkingTicket(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QParkingTicket(PathMetadata metadata, PathInits inits) {
        this(ParkingTicket.class, metadata, inits);
    }

    public QParkingTicket(Class<? extends ParkingTicket> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.parkingLog = inits.isInitialized("parkingLog") ? new com.example.demo.domain.parking.log.QParkingLog(forProperty("parkingLog"), inits.get("parkingLog")) : null;
        this.store = inits.isInitialized("store") ? new com.example.demo.domain.system.store.QStore(forProperty("store"), inits.get("store")) : null;
        this.ticketPolicy = inits.isInitialized("ticketPolicy") ? new com.example.demo.domain.payment.ticketpolicy.QTicketPolicy(forProperty("ticketPolicy")) : null;
    }

}

