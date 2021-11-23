package com.nixsolutions.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Data
@Entity
@Table(name = "tenants")
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

    @Id
    private String name;
    private int allowedCallsPerSecond;
}
