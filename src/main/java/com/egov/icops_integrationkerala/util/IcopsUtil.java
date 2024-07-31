package com.egov.icops_integrationkerala.util;

import com.egov.icops_integrationkerala.config.IcopsConfiguration;
import com.egov.icops_integrationkerala.model.*;
import com.egov.icops_integrationkerala.repository.IcopsRepository;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class IcopsUtil {

    private final IcopsConfiguration config;

    private final IcopsRepository repository;

    @Autowired
    public IcopsUtil(IcopsConfiguration config, IcopsRepository repository) {
        this.config = config;
        this.repository = repository;
    }

    public IcopsTracker createPostTrackerBody(TaskRequest request, ProcessRequest processRequest, ChannelMessage channelMessage, DeliveryStatus status) {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        return IcopsTracker.builder()
                .processNumber(processRequest.getProcessUniqueId())
                .tenantId(config.getEgovStateTenantId())
                .taskNumber(request.getTask().getTaskNumber())
                .taskType(request.getTask().getTaskType())
                .fileStoreId(request.getTask().getDocuments().get(0).getFileStore())
                .taskDetails(request.getTask().getTaskDetails())
                .deliveryStatus(status)
                .remarks(channelMessage.getFailureMsg())
                .additionalDetails(request.getTask().getAdditionalDetails())
                .rowVersion(0)
                .bookingDate(currentDate)
                .acknowledgementId(channelMessage.getAcknowledgeUniqueNumber())
                .build();
    }


}
