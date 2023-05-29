package com.example.userservice.entity.state;

import com.example.userservice.entity.QRcode;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Table
@Entity
public class LastStateInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "last_state_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String qrId;

    @Column(nullable = false)
    private String deliveryName;
    @Column(nullable = false)
    private String deliveryPhoneNumber;
    @Column(nullable = false)
    private String curState;

    @OneToOne(mappedBy = "lastStateInfo")
    private QRcode qRinfo;

    public void assignQRcode(QRcode qRcode){
        this.qRinfo = qRcode;
    }
}
