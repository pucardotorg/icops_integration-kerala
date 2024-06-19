package com.egov.icops_integrationkerala.enrichment;

import com.egov.icops_integrationkerala.model.ProcessRequest;
import com.egov.icops_integrationkerala.util.MdmsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IcopsEnrichment {

    private final MdmsUtil mdmsUtil;

    public IcopsEnrichment(MdmsUtil mdmsUtil) {
        this.mdmsUtil = mdmsUtil;
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
    }
}
