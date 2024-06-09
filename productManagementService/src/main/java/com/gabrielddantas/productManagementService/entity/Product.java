package com.gabrielddantas.productManagementService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"code"})})
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(length = 32, nullable = false)
  private String name;

  @Column(length = 24, nullable = false)
  private String model;

  @Column(length = 8, nullable = false)
  private String code;

  private Float price;
}
