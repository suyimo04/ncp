package com.agri.trace.vo;

import com.agri.trace.entity.BizCertificate;
import com.agri.trace.entity.BizProducer;
import com.agri.trace.entity.BizProductBatch;
import com.agri.trace.entity.BizTestReport;
import lombok.Data;

@Data
public class CertificateVO {
    private BizCertificate certificate;
    private BizProductBatch batch;
    private BizTestReport report;
    private BizProducer producer;
}
