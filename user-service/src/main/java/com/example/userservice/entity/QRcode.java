package com.example.userservice.entity;

import com.example.userservice.BaseTimeEntity;
import com.example.userservice.entity.state.FirstStateInfo;
import com.example.userservice.entity.state.LastStateInfo;
import com.example.userservice.entity.state.MiddleStateInfo;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table
public class QRcode extends BaseTimeEntity implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qrinfo_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String qrId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String invoiceNo;

    @Column
    @ColumnDefault(value = "false")
    private String state;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String company;

    @ManyToOne
    @JoinColumn(name = "user_entity_id")
    private UserEntity userEntity;

    @OneToOne
    @JoinColumn(name = "middle_state_id")
    private MiddleStateInfo middleStateInfo;

    @OneToOne
    @JoinColumn(name = "first_state_id")
    private FirstStateInfo firstStateInfo;

    @OneToOne
    @JoinColumn(name = "last_state_id")
    private LastStateInfo lastStateInfo;
}
