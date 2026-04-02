package com.example.demo.domain.shared.store;

import com.example.demo.domain.admin.entity.Admin;
import com.example.demo.domain.shared.store.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"createdBy","updatedBy"})
@Getter
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
}
