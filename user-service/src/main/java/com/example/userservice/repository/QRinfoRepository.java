package com.example.userservice.repository;

import com.example.userservice.entity.QRinfo;
import org.springframework.data.repository.CrudRepository;

public interface QRinfoRepository extends CrudRepository<QRinfo, Long> {
    QRinfo findByQrId(String qrId);
}
