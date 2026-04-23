package com.example.demo.domain.system.store.transaction;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStoreTicketTransaction is a Querydsl query type for StoreTicketTransaction
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreTicketTransaction extends EntityPathBase<StoreTicketTransaction> {

    private static final long serialVersionUID = -149320099L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStoreTicketTransaction storeTicketTransaction = new QStoreTicketTransaction("storeTicketTransaction");

    public final NumberPath<Integer> afterBalance = createNumber("afterBalance", Integer.class);

    public final NumberPath<Integer> beforeBalance = createNumber("beforeBalance", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> createdById = createNumber("createdById", Long.class);

    public final EnumPath<com.example.demo.domain.system.store.transaction.enums.CreatedByType> createdByType = createEnum("createdByType", com.example.demo.domain.system.store.transaction.enums.CreatedByType.class);

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final NumberPath<Long> referenceId = createNumber("referenceId", Long.class);

    public final EnumPath<com.example.demo.domain.system.store.transaction.enums.ReferenceType> referenceType = createEnum("referenceType", com.example.demo.domain.system.store.transaction.enums.ReferenceType.class);

    public final com.example.demo.domain.system.store.QStore store;

    public final com.example.demo.domain.payment.ticketpolicy.QTicketPolicy ticketPolicy;

    public final NumberPath<Long> transactionId = createNumber("transactionId", Long.class);

    public final EnumPath<com.example.demo.domain.system.store.transaction.enums.TransactionType> transactionType = createEnum("transactionType", com.example.demo.domain.system.store.transaction.enums.TransactionType.class);

    public final com.example.demo.domain.system.store.wallet.QStoreTicketWallet wallet;

    public QStoreTicketTransaction(String variable) {
        this(StoreTicketTransaction.class, forVariable(variable), INITS);
    }

    public QStoreTicketTransaction(Path<? extends StoreTicketTransaction> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStoreTicketTransaction(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStoreTicketTransaction(PathMetadata metadata, PathInits inits) {
        this(StoreTicketTransaction.class, metadata, inits);
    }

    public QStoreTicketTransaction(Class<? extends StoreTicketTransaction> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.store = inits.isInitialized("store") ? new com.example.demo.domain.system.store.QStore(forProperty("store"), inits.get("store")) : null;
        this.ticketPolicy = inits.isInitialized("ticketPolicy") ? new com.example.demo.domain.payment.ticketpolicy.QTicketPolicy(forProperty("ticketPolicy")) : null;
        this.wallet = inits.isInitialized("wallet") ? new com.example.demo.domain.system.store.wallet.QStoreTicketWallet(forProperty("wallet"), inits.get("wallet")) : null;
    }

}

