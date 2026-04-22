package com.example.demo.domain.auth.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSocialAccount is a Querydsl query type for SocialAccount
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSocialAccount extends EntityPathBase<SocialAccount> {

    private static final long serialVersionUID = 894380994L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSocialAccount socialAccount = new QSocialAccount("socialAccount");

    public final DateTimePath<java.time.LocalDateTime> connectedAt = createDateTime("connectedAt", java.time.LocalDateTime.class);

    public final EnumPath<com.example.demo.domain.auth.user.enums.Provider> provider = createEnum("provider", com.example.demo.domain.auth.user.enums.Provider.class);

    public final StringPath providerId = createString("providerId");

    public final NumberPath<Long> socialAccountId = createNumber("socialAccountId", Long.class);

    public final com.example.demo.domain.resident.QUser user;

    public QSocialAccount(String variable) {
        this(SocialAccount.class, forVariable(variable), INITS);
    }

    public QSocialAccount(Path<? extends SocialAccount> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSocialAccount(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSocialAccount(PathMetadata metadata, PathInits inits) {
        this(SocialAccount.class, metadata, inits);
    }

    public QSocialAccount(Class<? extends SocialAccount> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.example.demo.domain.resident.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

