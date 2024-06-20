package com.egov.icops_integrationkerala.enrichment;

import com.egov.icops_integrationkerala.model.PartyData;
import com.egov.icops_integrationkerala.model.ProcessRequest;
import com.egov.icops_integrationkerala.util.MdmsUtil;
import com.egov.icops_integrationkerala.util.NullToEmptyConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IcopsEnrichment {

    private final MdmsUtil mdmsUtil;

    private final NullToEmptyConverter converter;

    public IcopsEnrichment(MdmsUtil mdmsUtil, NullToEmptyConverter converter) {
        this.mdmsUtil = mdmsUtil;
        this.converter = converter;
    }

    public void enrichPoliceStationDetails(ProcessRequest processRequest) {
        log.info("Enriching Process Request Data for Case No: {}", processRequest.getProcessCaseno());
        processRequest.setProcessDocTypeCode("2");
        processRequest.setProcessDocSubTypeCode("2000020");
        processRequest.setProcessPoliceStationCode("15290042");
        processRequest.setProcessPoliceStationName("PUDUKKADU");
        processRequest.setProcessCino("KLER550001232023");
        processRequest.setProcessOrigin("DRISTI");
        processRequest.setProcessCourtCode("KLTR13");
        processRequest.setProcessFirYear("2019");
        processRequest.setProcessFirPScode("15290042");
        processRequest.setProcessFirSrlno("1268");
        processRequest.setProcessPartyNumber("10");
        processRequest.setProcessReceiverTaluka("Mukundapuram");
        processRequest.setProcessRespondntType("W");
        processRequest.setOrderSignedDate("29/04/2024");
        processRequest.setCaseListedDate("04/01/2024");
        processRequest.setCnrNo("KLER550001232023");
        processRequest.setCourtBenchCd("1");
        processRequest.setProcessInvAgency("Police");
        processRequest.setCourtBenchName("Principal Sub Judge");
        processRequest.setProcessReceiverType("W");
        PartyData partyData = new PartyData();
        partyData.setSpartyAge("15");
        converter.convertNullFieldsToEmptyString(partyData);
        processRequest.setSpartyData(partyData);
        converter.convertNullFieldsToEmptyString(processRequest);
    }
}
