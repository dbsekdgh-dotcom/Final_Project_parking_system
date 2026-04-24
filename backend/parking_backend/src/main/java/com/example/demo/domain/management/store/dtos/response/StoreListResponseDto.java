package com.example.demo.domain.management.store.dtos.response;

import com.example.demo.domain.system.store.Store;
import com.example.demo.domain.system.store.enums.Status;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StoreListResponseDto {
    private Long storeId;
    private String name;
    private String location;
    private Status status;
    private LocalDateTime createdAt;

    public static StoreListResponseDto from(Store store){
        return StoreListResponseDto.builder()
                .storeId(store.getStoreId())
                .name(store.getName())
                .location(store.getLocation())
                .status(store.getStatus())
                .createdAt(store.getCreatedAt())
                .build();
    }
}
