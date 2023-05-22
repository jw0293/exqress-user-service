package com.example.userservice.entity.state;

import com.example.userservice.entity.QRinfo;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

@Data
@Entity
@Table
public class FirstStateInfo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "first_stat_id")
    private Long id;

    @Column(nullable = false)
    private String deliveryName;
    @Column(nullable = false)
    private String deliveryPhoneNumber;
    @Column(nullable = false)
    @ColumnDefault(value = "배송 시작")
    private String curState;

    @OneToOne(mappedBy = "firstStateInfo")
    private QRinfo qRinfo;
}
