package com.example.demo.domain.system.store.dtos.response;

import com.example.demo.domain.system.store.Store;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreInfoResponseDto {
    private Long storeId;
    private String name;
    private String location;

    public static StoreInfoResponseDto from(Store store){
        return StoreInfoResponseDto.builder()
                .storeId(store.getStoreId())
                .name(store.getName())
                .location(store.getLocation())
                .build();
    }
}
