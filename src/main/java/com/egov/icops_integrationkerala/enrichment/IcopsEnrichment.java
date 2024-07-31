package com.egov.icops_integrationkerala.enrichment;

import com.egov.icops_integrationkerala.config.IcopsConfiguration;
import com.egov.icops_integrationkerala.model.*;
import com.egov.icops_integrationkerala.repository.IcopsRepository;
import com.egov.icops_integrationkerala.util.DateStringConverter;
import com.egov.icops_integrationkerala.util.FileStorageUtil;
import com.egov.icops_integrationkerala.util.IdgenUtil;
import com.egov.icops_integrationkerala.util.MdmsUtil;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@Slf4j
public class IcopsEnrichment {

    private final FileStorageUtil fileStorageUtil;

    private final IcopsConfiguration config;

    private final DateStringConverter converter;
    private final MdmsUtil util;

    private final IdgenUtil idgenUtil;

    private final IcopsRepository repository;


    @Autowired
    public IcopsEnrichment(FileStorageUtil fileStorageUtil, IcopsConfiguration config,
                           DateStringConverter converter, MdmsUtil util, IdgenUtil idgenUtil, IcopsRepository repository) {
        this.fileStorageUtil = fileStorageUtil;
        this.config = config;
        this.converter = converter;
        this.util = util;
        this.idgenUtil = idgenUtil;
        this.repository = repository;
    }

    public ProcessRequest getProcessRequest(Task task) {
        TaskDetails taskDetails = task.getTaskDetails();
        String fileStoreId = task.getDocuments().get(0).getFileStore();
        String docFileString = fileStorageUtil.getFileFromFileStoreService(fileStoreId, config.getEgovStateTenantId());
        String processUniqueId = idgenUtil.getIdList(taskRequest.getRequestInfo(), config.getEgovStateTenantId(),
                config.getIdName(),null,1).get(0);
        ProcessRequest processRequest = ProcessRequest.builder()
                .processCaseno(taskDetails.getCaseDetails().getCaseId())
                .processDoc(docFileString)
                .processUniqueId(processUniqueId)
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

    public IcopsTracker enrichIcopsTrackerForUpdate(IcopsProcessReport icopsProcessReport) {
        List<IcopsTracker> icopsTrackers = repository.getIcopsTracker(icopsProcessReport.getProcessUniqueId());
        if (icopsTrackers.size() != 1) {
            throw new RuntimeException("Invalid Icops Tracker field with processNumber : " + icopsProcessReport.getProcessUniqueId());
        }
        IcopsTracker icopsTracker = icopsTrackers.get(0);

        icopsTracker.setAdditionalDetails(convertProcessReportData(icopsProcessReport));
        return icopsTracker;
    }

    private AdditionalFields convertProcessReportData(IcopsProcessReport icopsProcessReport) {
        AdditionalFields additionalFields = new AdditionalFields();
        log.info("IcopsProcessReport : {}", icopsProcessReport);
        List<Field> fieldsList = new ArrayList<>();
        if (icopsProcessReport.getProcessUniqueId() != null) {
            fieldsList.add(new Field("processUniqueId", icopsProcessReport.getProcessUniqueId()));
        }
        if (icopsProcessReport.getProcessCourtCode() != null) {
            fieldsList.add(new Field("processCourtCode", icopsProcessReport.getProcessCourtCode()));
        }
        if (icopsProcessReport.getProcessActionDate() != null) {
            fieldsList.add(new Field("processActionDate", icopsProcessReport.getProcessActionDate()));
        }
        if (icopsProcessReport.getProcessActionStatusCd() != null) {
            fieldsList.add(new Field("processActionStatusCd", icopsProcessReport.getProcessActionStatusCd()));
        }
        if (icopsProcessReport.getProcessActionStatus() != null) {
            fieldsList.add(new Field("processActionStatus", icopsProcessReport.getProcessActionStatus()));
        }
        if (icopsProcessReport.getProcessActionSubStatusCd() != null) {
            fieldsList.add(new Field("processActionSubStatusCd", icopsProcessReport.getProcessActionSubStatusCd()));
        }
        if (icopsProcessReport.getProcessActionSubStatus() != null) {
            fieldsList.add(new Field("processActionSubStatus", icopsProcessReport.getProcessActionSubStatus()));
        }
        if (icopsProcessReport.getProcessFailureReason() != null) {
            fieldsList.add(new Field("processFailureReason", icopsProcessReport.getProcessFailureReason()));
        }
        if (icopsProcessReport.getProcessMethodOfExecution() != null) {
            fieldsList.add(new Field("processMethodOfExecution", icopsProcessReport.getProcessMethodOfExecution()));
        }
        if (icopsProcessReport.getProcessExecutedTo() != null) {
            fieldsList.add(new Field("processExecutedTo", icopsProcessReport.getProcessExecutedTo()));
        }
        if (icopsProcessReport.getProcessExecutedToRelation() != null) {
            fieldsList.add(new Field("processExecutedToRelation", icopsProcessReport.getProcessExecutedToRelation()));
        }
        if (icopsProcessReport.getProcessExecutionPlace() != null) {
            fieldsList.add(new Field("processExecutionPlace", icopsProcessReport.getProcessExecutionPlace()));
        }
        if (icopsProcessReport.getProcessActionRemarks() != null) {
            fieldsList.add(new Field("processActionRemarks", icopsProcessReport.getProcessActionRemarks()));
        }
        if (icopsProcessReport.getProcessExecutingOfficerName() != null) {
            fieldsList.add(new Field("processExecutingOfficerName", icopsProcessReport.getProcessExecutingOfficerName()));
        }
        if (icopsProcessReport.getProcessExecutingOfficerRank() != null) {
            fieldsList.add(new Field("processExecutingOfficerRank", icopsProcessReport.getProcessExecutingOfficerRank()));
        }
        if (icopsProcessReport.getProcessExecutingOfficeCode() != null) {
            fieldsList.add(new Field("processExecutingOfficeCode", icopsProcessReport.getProcessExecutingOfficeCode()));
        }
        if (icopsProcessReport.getProcessExecutingOffice() != null) {
            fieldsList.add(new Field("processExecutingOffice", icopsProcessReport.getProcessExecutingOffice()));
        }
        if (icopsProcessReport.getProcessSubmittingOfficerName() != null) {
            fieldsList.add(new Field("processSubmittingOfficerName", icopsProcessReport.getProcessSubmittingOfficerName()));
        }
        if (icopsProcessReport.getProcessSubmittingOfficerRank() != null) {
            fieldsList.add(new Field("processSubmittingOfficerRank", icopsProcessReport.getProcessSubmittingOfficerRank()));
        }
        if (icopsProcessReport.getProcessSubmittingOfficeCode() != null) {
            fieldsList.add(new Field("processSubmittingOfficeCode", icopsProcessReport.getProcessSubmittingOfficeCode()));
        }
        if (icopsProcessReport.getProcessSubmittingOffice() != null) {
            fieldsList.add(new Field("processSubmittingOffice", icopsProcessReport.getProcessSubmittingOffice()));
        }
        if (icopsProcessReport.getProcessReportSubmittingDateTime() != null) {
            fieldsList.add(new Field("processReportSubmittingDateTime", icopsProcessReport.getProcessReportSubmittingDateTime()));
        }
        if (icopsProcessReport.getProcessReport() != null) {
            byte[] decodedBytes = Base64.getDecoder().decode(icopsProcessReport.getProcessReport());
            String filePath = "file.pdf";

            // Write the byte array to a PDF file
            try (OutputStream os = new FileOutputStream(filePath)) {
                os.write(decodedBytes);
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
