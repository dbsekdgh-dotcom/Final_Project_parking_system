package com.example.demo.domain.auth.admin.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAdmin is a Querydsl query type for Admin
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAdmin extends EntityPathBase<Admin> {

    private static final long serialVersionUID = -1454612871L;

    public static final QAdmin admin = new QAdmin("admin");

    public final NumberPath<Long> adminId = createNumber("adminId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> lastLoginAt = createDateTime("lastLoginAt", java.time.LocalDateTime.class);

    public final StringPath loginId = createString("loginId");

    public final StringPath name = createString("name");

    public final StringPath password = createString("password");

    public final EnumPath<com.example.demo.domain.auth.admin.enums.AdminStatus> status = createEnum("status", com.example.demo.domain.auth.admin.enums.AdminStatus.class);

    public QAdmin(String variable) {
        super(Admin.class, forVariable(variable));
    }

    public QAdmin(Path<? extends Admin> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAdmin(PathMetadata metadata) {
        super(Admin.class, metadata);
    }

}

