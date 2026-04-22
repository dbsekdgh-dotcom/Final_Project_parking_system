package com.example.demo.global.security.store;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

public class StoreAuthDto extends User {
    private final Long storeId;
    private final String storeName;

    public StoreAuthDto(Long storeId, String storeName){
        super(String.valueOf(storeId), "", List.of(new SimpleGrantedAuthority("ROLE_STORE")));
        this.storeId = storeId;
        this.storeName= storeName;
    }
    public Long getStoreId(){
        return storeId;
    }
    @Override
    public String getUsername() { return String.valueOf(storeId); }
}
