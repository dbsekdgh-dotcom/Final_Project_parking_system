package com.example.demo.domain.system.store.wallet;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStoreTicketWallet is a Querydsl query type for StoreTicketWallet
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreTicketWallet extends EntityPathBase<StoreTicketWallet> {

    private static final long serialVersionUID = 1120088493L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStoreTicketWallet storeTicketWallet = new QStoreTicketWallet("storeTicketWallet");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> issuedCount = createNumber("issuedCount", Integer.class);

    public final NumberPath<Integer> remainingCount = createNumber("remainingCount", Integer.class);

    public final com.example.demo.domain.system.store.QStore store;

    public final com.example.demo.domain.payment.ticketpolicy.QTicketPolicy ticketPolicy;

    public final NumberPath<Integer> usedCount = createNumber("usedCount", Integer.class);

    public final NumberPath<Long> walletId = createNumber("walletId", Long.class);

    public QStoreTicketWallet(String variable) {
        this(StoreTicketWallet.class, forVariable(variable), INITS);
    }

    public QStoreTicketWallet(Path<? extends StoreTicketWallet> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStoreTicketWallet(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStoreTicketWallet(PathMetadata metadata, PathInits inits) {
        this(StoreTicketWallet.class, metadata, inits);
    }

    public QStoreTicketWallet(Class<? extends StoreTicketWallet> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.store = inits.isInitialized("store") ? new com.example.demo.domain.system.store.QStore(forProperty("store"), inits.get("store")) : null;
        this.ticketPolicy = inits.isInitialized("ticketPolicy") ? new com.example.demo.domain.payment.ticketpolicy.QTicketPolicy(forProperty("ticketPolicy")) : null;
    }

}

