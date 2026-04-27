package com.example.demo.domain.management.store.dtos.response;

import com.example.demo.domain.system.store.Store;
import com.example.demo.domain.system.store.enums.Status;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StoreDetailResponseDto {
    private Long storeId;
    private String name;
    private String location;
    private Status status;
    private String terminalPassword;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StoreDetailResponseDto from(Store store){
        return StoreDetailResponseDto.builder()
                .storeId(store.getStoreId())
                .name(store.getName())
                .location(store.getLocation())
                .status(store.getStatus())
                .terminalPassword(store.getTerminalPassword())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
}
