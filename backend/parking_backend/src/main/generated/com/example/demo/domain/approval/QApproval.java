package com.example.demo.domain.approval;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QApproval is a Querydsl query type for Approval
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QApproval extends EntityPathBase<Approval> {

    private static final long serialVersionUID = 1413790846L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QApproval approval = new QApproval("approval");

    public final NumberPath<Long> approvalId = createNumber("approvalId", Long.class);

    public final EnumPath<com.example.demo.domain.approval.enums.ApprovalType> approvalType = createEnum("approvalType", com.example.demo.domain.approval.enums.ApprovalType.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> processedAt = createDateTime("processedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> processedByAdminId = createNumber("processedByAdminId", Long.class);

    public final StringPath rejectReason = createString("rejectReason");

    public final com.example.demo.domain.resident.QUser requestUserId;

    public final EnumPath<com.example.demo.domain.approval.enums.ApprovalStatus> status = createEnum("status", com.example.demo.domain.approval.enums.ApprovalStatus.class);

    public final NumberPath<Long> targetId = createNumber("targetId", Long.class);

    public QApproval(String variable) {
        this(Approval.class, forVariable(variable), INITS);
    }

    public QApproval(Path<? extends Approval> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QApproval(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QApproval(PathMetadata metadata, PathInits inits) {
        this(Approval.class, metadata, inits);
    }

    public QApproval(Class<? extends Approval> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.requestUserId = inits.isInitialized("requestUserId") ? new com.example.demo.domain.resident.QUser(forProperty("requestUserId"), inits.get("requestUserId")) : null;
    }

}

