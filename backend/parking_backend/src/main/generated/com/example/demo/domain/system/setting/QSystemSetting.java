package com.example.demo.domain.system.setting;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSystemSetting is a Querydsl query type for SystemSetting
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSystemSetting extends EntityPathBase<SystemSetting> {

    private static final long serialVersionUID = -1350693228L;

    public static final QSystemSetting systemSetting = new QSystemSetting("systemSetting");

    public final StringPath description = createString("description");

    public final BooleanPath isEditable = createBoolean("isEditable");

    public final StringPath settingKey = createString("settingKey");

    public final StringPath settingValue = createString("settingValue");

    public QSystemSetting(String variable) {
        super(SystemSetting.class, forVariable(variable));
    }

    public QSystemSetting(Path<? extends SystemSetting> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSystemSetting(PathMetadata metadata) {
        super(SystemSetting.class, metadata);
    }

}

