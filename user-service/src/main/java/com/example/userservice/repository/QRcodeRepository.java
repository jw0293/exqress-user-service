package com.example.userservice.repository;

import com.example.userservice.entity.QRcode;
import org.springframework.data.repository.CrudRepository;

public interface QRcodeRepository extends CrudRepository<QRcode, Long> {
    QRcode findByInvoiceNo(String invoiceNo);
    QRcode findByQrId(String qrId);
}
