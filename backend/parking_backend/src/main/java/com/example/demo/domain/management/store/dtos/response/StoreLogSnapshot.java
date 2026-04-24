package com.example.demo.domain.management.store.dtos.response;

import com.example.demo.domain.system.store.Store;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreLogSnapshot {
    private String name;
    private String terminalPassword;
    private String status;

    public static StoreLogSnapshot from(Store store){
        return StoreLogSnapshot.builder()
                .name(store.getName())
                .terminalPassword(store.getTerminalPassword())
                .status(store.getStatus().name())
                .build();
    }
}
