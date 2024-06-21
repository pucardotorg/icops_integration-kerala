package com.egov.icops_integrationkerala.enrichment;

import com.egov.icops_integrationkerala.config.IcopsConfiguration;
import com.egov.icops_integrationkerala.model.*;
import com.egov.icops_integrationkerala.util.DateStringConverter;
import com.egov.icops_integrationkerala.util.FileStorageUtil;
import com.egov.icops_integrationkerala.util.MdmsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
        log.info("ProcessReport : {}", processReport);
        List<Field> fieldsList = new ArrayList<>();
        if (processReport.getProcessUniqueId() != null) {
            fieldsList.add(new Field("processUniqueId", processReport.getProcessUniqueId()));
        }
        if (processReport.getProcessCourtCode() != null) {
            fieldsList.add(new Field("processCourtCode", processReport.getProcessCourtCode()));
        }
        if (processReport.getProcessActionDate() != null) {
            fieldsList.add(new Field("processActionDate", processReport.getProcessActionDate()));
        }
        if (processReport.getProcessActionStatusCd() != null) {
            fieldsList.add(new Field("processActionStatusCd", processReport.getProcessActionStatusCd()));
        }
        if (processReport.getProcessActionStatus() != null) {
            fieldsList.add(new Field("processActionStatus", processReport.getProcessActionStatus()));
        }
        if (processReport.getProcessActionSubStatusCd() != null) {
            fieldsList.add(new Field("processActionSubStatusCd", processReport.getProcessActionSubStatusCd()));
        }
        if (processReport.getProcessActionSubStatus() != null) {
            fieldsList.add(new Field("processActionSubStatus", processReport.getProcessActionSubStatus()));
        }
        if (processReport.getProcessFailureReason() != null) {
            fieldsList.add(new Field("processFailureReason", processReport.getProcessFailureReason()));
        }
        if (processReport.getProcessMethodOfExecution() != null) {
            fieldsList.add(new Field("processMethodOfExecution", processReport.getProcessMethodOfExecution()));
        }
        if (processReport.getProcessExecutedTo() != null) {
            fieldsList.add(new Field("processExecutedTo", processReport.getProcessExecutedTo()));
        }
        if (processReport.getProcessExecutedToRelation() != null) {
            fieldsList.add(new Field("processExecutedToRelation", processReport.getProcessExecutedToRelation()));
        }
        if (processReport.getProcessExecutionPlace() != null) {
            fieldsList.add(new Field("processExecutionPlace", processReport.getProcessExecutionPlace()));
        }
        if (processReport.getProcessActionRemarks() != null) {
            fieldsList.add(new Field("processActionRemarks", processReport.getProcessActionRemarks()));
        }
        if (processReport.getProcessExecutingOfficerName() != null) {
            fieldsList.add(new Field("processExecutingOfficerName", processReport.getProcessExecutingOfficerName()));
        }
        if (processReport.getProcessExecutingOfficerRank() != null) {
            fieldsList.add(new Field("processExecutingOfficerRank", processReport.getProcessExecutingOfficerRank()));
        }
        if (processReport.getProcessExecutingOfficeCode() != null) {
            fieldsList.add(new Field("processExecutingOfficeCode", processReport.getProcessExecutingOfficeCode()));
        }
        if (processReport.getProcessExecutingOffice() != null) {
            fieldsList.add(new Field("processExecutingOffice", processReport.getProcessExecutingOffice()));
        }
        if (processReport.getProcessSubmittingOfficerName() != null) {
            fieldsList.add(new Field("processSubmittingOfficerName", processReport.getProcessSubmittingOfficerName()));
        }
        if (processReport.getProcessSubmittingOfficerRank() != null) {
            fieldsList.add(new Field("processSubmittingOfficerRank", processReport.getProcessSubmittingOfficerRank()));
        }
        if (processReport.getProcessSubmittingOfficeCode() != null) {
            fieldsList.add(new Field("processSubmittingOfficeCode", processReport.getProcessSubmittingOfficeCode()));
        }
        if (processReport.getProcessSubmittingOffice() != null) {
            fieldsList.add(new Field("processSubmittingOffice", processReport.getProcessSubmittingOffice()));
        }
        if (processReport.getProcessReportSubmittingDateTime() != null) {
            fieldsList.add(new Field("processReportSubmittingDateTime", processReport.getProcessReportSubmittingDateTime()));
        }
        if (processReport.getProcessReport() != null) {
            String fileStoreId = fileStorageUtil.saveDocumentToFileStore(processReport.getProcessReport());
            fieldsList.add(new Field("policeReportFileStoreId", fileStoreId));
        }
        additionalFields.setFields(fieldsList);
        return additionalFields;
    }
}
