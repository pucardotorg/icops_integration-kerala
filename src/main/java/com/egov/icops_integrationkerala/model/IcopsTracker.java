package com.egov.icops_integrationkerala.model;

import lombok.*;
import org.egov.common.contract.models.AuditDetails;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IcopsTracker {
    private String processNumber;
    private String tenantId;
    private String taskNumber;
    private String taskType;
    private String fileStoreId;
    private Object taskDetails;
    private DeliveryStatus deliveryStatus;
    private String remarks;
    private Object additionalDetails;
    private Integer rowVersion;
    private String bookingDate;
    private String receivedDate;
    private String acknowledgementId;
    private AuditDetails auditDetails;
}
