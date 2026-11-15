package com.cap43.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "system_config")
public class SystemConfig {
    @Id
    private String configKey;
    private String configValue;
    private String description;
}
