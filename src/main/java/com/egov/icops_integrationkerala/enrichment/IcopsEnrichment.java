package com.egov.icops_integrationkerala.enrichment;

import com.egov.icops_integrationkerala.config.IcopsConfiguration;
import com.egov.icops_integrationkerala.model.*;
import com.egov.icops_integrationkerala.util.DateStringConverter;
import com.egov.icops_integrationkerala.util.FileStorageUtil;
import com.egov.icops_integrationkerala.util.MdmsUtil;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
@Slf4j
public class IcopsEnrichment {

    private final FileStorageUtil fileStorageUtil;

    private final IcopsConfiguration config;

    private final DateStringConverter converter;


    public IcopsEnrichment(FileStorageUtil fileStorageUtil, IcopsConfiguration config,
                           DateStringConverter converter) {
        this.fileStorageUtil = fileStorageUtil;
        this.config = config;
        this.converter = converter;
    }

    public ProcessRequest getProcessRequest(Task task) {
        TaskDetails taskDetails = task.getTaskDetails();
        String fileStoreId = task.getDocuments().get(0).getFileStore();
        String docFileString = fileStorageUtil.getFileFromFileStoreService(fileStoreId, config.getEgovStateTenantId());
        ProcessRequest processRequest = ProcessRequest.builder()
                .processCaseno(taskDetails.getCaseDetails().getCaseId())
                .processDoc(docFileString)
                .processUniqueId(taskDetails.getSummonDetails().getSummonId())
                .processCourtName(taskDetails.getCaseDetails().getCourtName())
                .processJudge(taskDetails.getCaseDetails().getJudgeName())
                .processIssueDate(converter.convertDate(taskDetails.getSummonDetails().getIssueDate()))
                .processNextHearingDate(converter.convertDate(taskDetails.getCaseDetails().getHearingDate()))
                .processRespondentName(taskDetails.getRespondentDetails().getName())
                .processRespondentGender(taskDetails.getRespondentDetails().getGender())
                .processRespondentAge(String.valueOf(taskDetails.getRespondentDetails().getAge()))
                .processRespondentRelativeName(taskDetails.getRespondentDetails().getRelativeName())
                .processRespondentRelation(taskDetails.getRespondentDetails().getRelationWithRelative())
                .processReceiverAddress(taskDetails.getRespondentDetails().getAddress())
                .processReceiverState(taskDetails.getRespondentDetails().getState())
                .processReceiverDistrict(taskDetails.getRespondentDetails().getDistrict())
                .processReceiverPincode(taskDetails.getRespondentDetails().getPinCode())
                .processPartyType(taskDetails.getSummonDetails().getPartyType())
                .processDocType(taskDetails.getSummonDetails().getDocType())
                .processDocSubType(taskDetails.getSummonDetails().getDocSubType())
                .processCino(task.getCnrNumber())
                .cnrNo(task.getCnrNumber())
                .orderSignedDate(converter.convertDate(task.getCreatedDate().toString()))
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
        processRequest.setProcessOrigin("DRISTI");
        processRequest.setProcessCourtCode("KLTR13");
        processRequest.setProcessFirYear("2019");
        processRequest.setProcessFirPScode("15290042");
        processRequest.setProcessFirSrlno("1268");
        processRequest.setProcessPartyNumber("10");
        processRequest.setProcessReceiverTaluka("Mukundapuram");
        processRequest.setProcessRespondantType("W");
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
            byte[] decodedBytes = Base64.getDecoder().decode(processReport.getProcessReport());
            String filePath = "file.pdf";

            // Write the byte array to a PDF file
            try (OutputStream os = new FileOutputStream(filePath)) {
                os.write(decodedBytes);
                System.out.println("PDF file successfully created at: " + filePath);
                String fileStoreId = fileStorageUtil.saveDocumentToFileStore(filePath);
                fieldsList.add(new Field("policeReportFileStoreId", fileStoreId));
            } catch (IOException e) {
                log.error("Error occurred when generating file from base64 string", e);
                throw new CustomException("SUMMONS_FILE_SAVE_ERROR", "Failed to generate file from base64 string");
            } finally {
                File file = new File(filePath);
                if (file.exists() && file.isFile()) {
                    file.delete();
                }
            }
        }
        additionalFields.setFields(fieldsList);
        return additionalFields;
    }
}
