package com.egov.icops_integrationkerala.enrichment;

import com.egov.icops_integrationkerala.config.IcopsConfiguration;
import com.egov.icops_integrationkerala.model.*;
import com.egov.icops_integrationkerala.util.DateStringConverter;
import com.egov.icops_integrationkerala.util.FileStorageUtil;
import com.egov.icops_integrationkerala.util.MdmsUtil;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IcopsEnrichment {

    private final MdmsUtil mdmsUtil;

    private final FileStorageUtil fileStorageUtil;

    private final IcopsConfiguration config;

    private final DateStringConverter converter;


    public IcopsEnrichment(MdmsUtil mdmsUtil, FileStorageUtil fileStorageUtil, IcopsConfiguration config,
                           DateStringConverter converter) {
        this.mdmsUtil = mdmsUtil;
        this.fileStorageUtil = fileStorageUtil;
        this.config = config;
        this.converter = converter;
    }

    public ProcessRequest getProcessRequest(TaskSummon taskSummon) {
        String docFileString = fileStorageUtil.getFileFromFileStoreService(taskSummon.getSummonsDocument().getFileStoreId(),
                config.getEgovStateTenantId());
        ProcessRequest processRequest = ProcessRequest.builder()
                .processCaseno(taskSummon.getCaseDetails().getCaseId())
                .processDoc(docFileString)
                .processUniqueId(taskSummon.getSummonDetails().getSummonId())
                .processCourtName(taskSummon.getCaseDetails().getCourtName())
                .processJudge(taskSummon.getCaseDetails().getJudgeName())
                .processIssueDate(converter.convertDate(taskSummon.getSummonDetails().getIssueDate()))
                .processNextHearingDate(converter.convertDate(taskSummon.getCaseDetails().getHearingDate()))
                .processRespondentName(taskSummon.getRespondentDetails().getName())
                .processRespondentGender(taskSummon.getRespondentDetails().getGender())
                .processRespondentAge(String.valueOf(taskSummon.getRespondentDetails().getAge()))
                .processRespondentRelativeName(taskSummon.getRespondentDetails().getRelativeName())
                .processRespondentRelation(taskSummon.getRespondentDetails().getRelationWithRelative())
                .processReceiverAddress(taskSummon.getRespondentDetails().getAddress())
                .processReceiverState(taskSummon.getRespondentDetails().getState())
                .processReceiverDistrict(taskSummon.getRespondentDetails().getDistrict())
                .processReceiverPincode(taskSummon.getRespondentDetails().getPinCode())
                .processPartyType(taskSummon.getSummonDetails().getPartyType())
                .processDocType(taskSummon.getSummonDetails().getDocType())
                .processDocSubType(taskSummon.getSummonDetails().getDocSubType())
                .build();
        enrichPoliceStationDetails(processRequest);
        return processRequest;
    }


    private void enrichPoliceStationDetails(ProcessRequest processRequest) {
        log.info("Enriching Process Request Data for Case No: {}", processRequest.getProcessCaseno());
        processRequest.setProcessDocTypeCode("2");
        processRequest.setProcessDocSubTypeCode("2000020");
        processRequest.setProcessPoliceStationCode("15290042");
        processRequest.setProcessPoliceStationName("PUDUKKADU");
        processRequest.setProcessCino("KLER550001232023");
        processRequest.setCnrNo("KLER550001232023");
        processRequest.setProcessOrigin("DRISTI");
        processRequest.setProcessCourtCode("KLTR13");
        processRequest.setProcessFirYear("2019");
        processRequest.setProcessFirPScode("15290042");
        processRequest.setProcessFirSrlno("1268");
        processRequest.setProcessPartyNumber("10");
        processRequest.setProcessReceiverTaluka("Mukundapuram");
        processRequest.setProcessRespondantType("W");
        processRequest.setOrderSignedDate(converter.convertDate("2024-04-29"));
        processRequest.setCaseListedDate(converter.convertDate("2024-04-01"));
        //processRequest.setCourtBenchCd("1");
        //processRequest.setProcessInvAgency("Police");
        //processRequest.setCourtBenchName("Principal Sub Judge");
        processRequest.setProcessReceiverType("W");
    }

    public ChannelReport getChannelReport(ProcessReport processReport) {
        ChannelReport channelReport = new ChannelReport();
        channelReport.setSummonId(processReport.getProcessUniqueId());
        if (processReport.getProcessActionStatus().equalsIgnoreCase("SERVED")) {
            channelReport.setDeliveryStatus("DELIVERED_SUCCESSFULLY");
        } else if (processReport.getProcessActionStatus().equalsIgnoreCase("NOT_SERVED") ||
                processReport.getProcessActionStatus().equalsIgnoreCase("NOT_ARRESTED") ) {
            channelReport.setDeliveryStatus("SUMMONS_NOT_SERVED");
        } else if (processReport.getProcessActionStatus().equalsIgnoreCase("RETURNED")) {
            channelReport.setDeliveryStatus("SUMMONS_FAILED");
        } else {
            channelReport.setDeliveryStatus("SUMMONS_STATUS_UNKNOWN");
        }
        channelReport.setAdditionalFields(convertProcessReportData(processReport));
        return channelReport;
    }

    private AdditionalFields convertProcessReportData(ProcessReport processReport) {
        AdditionalFields additionalFields = new AdditionalFields();
        try {
            for (java.lang.reflect.Field field : processReport.getClass().getDeclaredFields()) {
                Object value = field.get(processReport);
                if (value != null) {
                    additionalFields.addFieldsItem(new Field(field.getName(), value.toString()));
                }
            }
        } catch (Exception e) {
            log.error("Error occurred when converting process Report data", e);
            throw new CustomException("PROCESS_REPORT_DATA_CONVERT_ERROR", "Error occurred when converting process Report data");
        }
        return additionalFields;
    }
}
