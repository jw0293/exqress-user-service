package com.example.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

@Data
@Entity
@Table
public class QRinfo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qrinfo_id")
    private Long id;

    @Column(nullable = false)
    private String qrId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String invoiceNo;

    @Column
    @ColumnDefault(value = "false")
    private boolean isComplete;

    @ManyToOne
    @JoinColumn(name = "user_entity_id")
    private UserEntity userEntity;

    public void connectUser(UserEntity user){
        this.userEntity = user;
    }
}
