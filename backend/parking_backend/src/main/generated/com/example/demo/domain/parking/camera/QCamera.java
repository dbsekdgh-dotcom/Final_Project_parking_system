package com.example.demo.domain.parking.camera;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCamera is a Querydsl query type for Camera
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCamera extends EntityPathBase<Camera> {

    private static final long serialVersionUID = 637590856L;

    public static final QCamera camera = new QCamera("camera");

    public final StringPath cameraCode = createString("cameraCode");

    public final EnumPath<com.example.demo.domain.parking.camera.enums.CameraType> cameraType = createEnum("cameraType", com.example.demo.domain.parking.camera.enums.CameraType.class);

    public final StringPath description = createString("description");

    public final EnumPath<com.example.demo.domain.parking.camera.enums.Floor> floor = createEnum("floor", com.example.demo.domain.parking.camera.enums.Floor.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath location = createString("location");

    public final StringPath rtspUrl = createString("rtspUrl");

    public QCamera(String variable) {
        super(Camera.class, forVariable(variable));
    }

    public QCamera(Path<? extends Camera> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCamera(PathMetadata metadata) {
        super(Camera.class, metadata);
    }

}

