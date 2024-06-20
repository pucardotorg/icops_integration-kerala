package com.egov.icops_integrationkerala.enrichment;

import com.egov.icops_integrationkerala.config.IcopsConfiguration;
import com.egov.icops_integrationkerala.model.PartyData;
import com.egov.icops_integrationkerala.model.ProcessRequest;
import com.egov.icops_integrationkerala.model.TaskSummon;
import com.egov.icops_integrationkerala.util.DateStringConverter;
import com.egov.icops_integrationkerala.util.FileStorageUtil;
import com.egov.icops_integrationkerala.util.MdmsUtil;
import lombok.extern.slf4j.Slf4j;
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
        processRequest.setProcessRespondntType("W");
        processRequest.setOrderSignedDate(converter.convertDate("2024-04-29"));
        processRequest.setCaseListedDate(converter.convertDate("2024-04-01"));
        //processRequest.setCourtBenchCd("1");
        processRequest.setProcessInvAgency("Police");
        //processRequest.setCourtBenchName("Principal Sub Judge");
        processRequest.setProcessReceiverType("W");
    }
}
