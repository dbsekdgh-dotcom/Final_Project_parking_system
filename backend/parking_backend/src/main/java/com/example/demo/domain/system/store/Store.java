package com.example.demo.domain.system.store;

import com.example.demo.domain.auth.admin.entity.Admin;
import com.example.demo.domain.system.store.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"createdBy","updatedBy"})
@Getter
@Setter
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storeId;
    @Column(nullable = false)
    private String name;
    private String location;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    @Column(nullable = false)
    private String terminalPassword;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    @JoinColumn(name = "created_by")
    @ManyToOne(fetch = FetchType.LAZY)
    private Admin createdBy;
    @JoinColumn(name = "updated_by")
    @ManyToOne(fetch = FetchType.LAZY)
    private Admin updatedBy;

    public void updateName(String name){
        this.name = name;
    }
    public void updateTerminalPassword(String terminalPassword){
        this.terminalPassword = terminalPassword;
    }
    public void updateStatus(Status status){
        this.status = status;
    }
    public void updateUpdatedBy(Admin admin){
        this.updatedBy = admin;
    }

}
