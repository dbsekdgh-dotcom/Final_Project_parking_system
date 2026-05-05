package com.example.demo.domain.store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStoreTicketConfig is a Querydsl query type for StoreTicketConfig
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreTicketConfig extends EntityPathBase<StoreTicketConfig> {

    private static final long serialVersionUID = 1790040018L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStoreTicketConfig storeTicketConfig = new QStoreTicketConfig("storeTicketConfig");

    public final DateTimePath<java.time.LocalDateTime> lastIssuedAt = createDateTime("lastIssuedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> monthlyQuota = createNumber("monthlyQuota", Integer.class);

    public final com.example.demo.domain.system.store.QStore store;

    public final NumberPath<Long> storeTicketConfigId = createNumber("storeTicketConfigId", Long.class);

    public final com.example.demo.domain.payment.ticketpolicy.QTicketPolicy ticketPolicy;

    public QStoreTicketConfig(String variable) {
        this(StoreTicketConfig.class, forVariable(variable), INITS);
    }

    public QStoreTicketConfig(Path<? extends StoreTicketConfig> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStoreTicketConfig(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStoreTicketConfig(PathMetadata metadata, PathInits inits) {
        this(StoreTicketConfig.class, metadata, inits);
    }

    public QStoreTicketConfig(Class<? extends StoreTicketConfig> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.store = inits.isInitialized("store") ? new com.example.demo.domain.system.store.QStore(forProperty("store"), inits.get("store")) : null;
        this.ticketPolicy = inits.isInitialized("ticketPolicy") ? new com.example.demo.domain.payment.ticketpolicy.QTicketPolicy(forProperty("ticketPolicy")) : null;
    }

}

