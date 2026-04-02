package com.example.demo.domain.shared.systemSetting;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "system_setting")
public class SystemSetting {
    @Id
    @Column(name="setting_key", length = 150)
    private String settingKey;
    @Column(name="setting_value", nullable = false, columnDefinition="TEXT")
    private String settingValue;
}
