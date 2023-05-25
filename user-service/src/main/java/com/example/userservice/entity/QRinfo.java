package com.example.userservice.entity;

import com.example.userservice.entity.state.FirstStateInfo;
import com.example.userservice.entity.state.LastStateInfo;
import com.example.userservice.entity.state.MiddleStateInfo;
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

    @OneToOne
    @JoinColumn(name = "middle_state_id")
    private MiddleStateInfo middleStateInfo;

    @OneToOne
    @JoinColumn(name = "first_state_id")
    private FirstStateInfo firstStateInfo;

    @OneToOne
    @JoinColumn(name = "last_state_id")
    private LastStateInfo lastStateInfo;

    public void connectUser(UserEntity user){
        this.userEntity = user;
    }
}
