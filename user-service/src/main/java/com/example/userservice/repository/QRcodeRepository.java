package com.example.userservice.repository;

import com.example.userservice.entity.QRcode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QRcodeRepository extends JpaRepository<QRcode, Long> {
    QRcode findByInvoiceNo(String invoiceNo);
    QRcode findByQrId(String qrId);
}
