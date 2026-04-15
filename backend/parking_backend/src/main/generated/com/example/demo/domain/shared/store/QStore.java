package com.example.demo.domain.shared.store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStore is a Querydsl query type for Store
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStore extends EntityPathBase<Store> {

    private static final long serialVersionUID = -1067432805L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStore store = new QStore("store");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final com.example.demo.domain.admin.entity.QAdmin createdBy;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final StringPath location = createString("location");

    public final StringPath name = createString("name");

    public final EnumPath<com.example.demo.domain.shared.store.enums.Status> status = createEnum("status", com.example.demo.domain.shared.store.enums.Status.class);

    public final NumberPath<Long> storeId = createNumber("storeId", Long.class);

    public final StringPath terminalPassword = createString("terminalPassword");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final com.example.demo.domain.admin.entity.QAdmin updatedBy;

    public QStore(String variable) {
        this(Store.class, forVariable(variable), INITS);
    }

    public QStore(Path<? extends Store> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStore(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStore(PathMetadata metadata, PathInits inits) {
        this(Store.class, metadata, inits);
    }

    public QStore(Class<? extends Store> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.createdBy = inits.isInitialized("createdBy") ? new com.example.demo.domain.admin.entity.QAdmin(forProperty("createdBy")) : null;
        this.updatedBy = inits.isInitialized("updatedBy") ? new com.example.demo.domain.admin.entity.QAdmin(forProperty("updatedBy")) : null;
    }

}

