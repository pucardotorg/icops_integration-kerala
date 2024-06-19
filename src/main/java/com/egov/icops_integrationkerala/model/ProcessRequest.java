package com.egov.icops_integrationkerala.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessRequest {

    @JsonProperty("processCaseno")
    private String processCaseno;

    @JsonProperty("processUniqueId")
    private String processUniqueId;

    @JsonProperty("sparty_data")
    private PartyData spartyData;

    @JsonProperty("processCourtCode")
    private String processCourtCode;

    @JsonProperty("processCourtName")
    private String processCourtName;

    @JsonProperty("processNextHearingDate")
    private String processNextHearingDate;

    @JsonProperty("processRespondentAge")
    private String processRespondentAge;

    @JsonProperty("processJudge")
    private String processJudge;

    @JsonProperty("processPoliceStationCode")
    private String processPoliceStationCode;

    @JsonProperty("processPoliceStationName")
    private String processPoliceStationName;

    @JsonProperty("processReceiverAddress")
    private String processReceiverAddress;

    @JsonProperty("processReceiverState")
    private String processReceiverState;

    @JsonProperty("processReceiverDistrict")
    private String processReceiverDistrict;

    @JsonProperty("processReceiverTaluka")
    private String processReceiverTaluka;

    @JsonProperty("processReceiverVillage")
    private String processReceiverVillage;

    @JsonProperty("processReceiverTown")
    private String processReceiverTown;

    @JsonProperty("processReceiverWard")
    private String processReceiverWard;


    @JsonProperty("processReceiverVillage1")
    private String processReceiverVillage1;

    @JsonProperty("processReceiverVillage2")
    private String processReceiverVillage2;

    @JsonProperty("processReceiverMobile")
    private String processReceiverMobile;

    @JsonProperty("processReceiverPincode")
    private String processReceiverPincode;

    @JsonProperty("processIssueDate")
    private String processIssueDate;

    @JsonProperty("processDocTypeCode")
    private String processDocTypeCode;

    @JsonProperty("processDocType")
    private String processDocType;

    @JsonProperty("processDocSubTypeCode")
    private String processDocSubTypeCode;

    @JsonProperty("processDocSubType")
    private String processDocSubType;

    @JsonProperty("processReceiverEmail")
    private String processReceiverEmail;

    @JsonProperty("processInvAgency")
    private String processInvAgency;

    @JsonProperty("processFirPScode")
    private String processFirPScode;

    @JsonProperty("processFirSrlno")
    private String processFirSrlno;

    @JsonProperty("processFirYear")
    private String processFirYear;

    @JsonProperty("processRespondentName")
    private String processRespondentName;

    @JsonProperty("processRespondentAliasName")
    private String processRespondentAliasName;

    @JsonProperty("processRespondentRelativeName")
    private String processRespondentRelativeName;

    @JsonProperty("processRespondentRelation")
    private String processRespondentRelation;

    @JsonProperty("processRespondentGender")
    private String processRespondentGender;

    @JsonProperty("processPartyNumber")
    private String processPartyNumber;

    @JsonProperty("processPartyType")
    private String processPartyType;

    @JsonProperty("processRespondantType")
    private String processRespondntType;

    @JsonProperty("processCino")
    private String processCino;

    @JsonProperty("processReceiverType")
    private String processReceiverType;

    @JsonProperty("processDoc")
    private String processDoc;

    @JsonProperty("processOrigin")
    private String processOrigin;
}
