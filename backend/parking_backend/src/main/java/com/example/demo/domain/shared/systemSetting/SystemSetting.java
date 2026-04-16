package com.example.demo.domain.shared.systemSetting;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "system_setting")
public class SystemSetting {
    @Id
    @Column(name="setting_key", length = 150)
    private String settingKey;
    @Column(name="setting_value", nullable = false, columnDefinition="TEXT")
    private String settingValue;
    @Column(name="description", nullable = true)
    private String description;
    @Column(name="is_editable", nullable = false)
    private Boolean isEditable = true;


    public void updateValue(String settingValue){
        this.settingValue = settingValue;
    }
}
