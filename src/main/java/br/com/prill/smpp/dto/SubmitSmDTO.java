package br.com.prill.smpp.dto;

import com.cloudhopper.smpp.pdu.SubmitSm;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class SubmitSmDTO {

    private String eventId;

    @JsonSerialize(using = SubmitSmSerializer.class)
    private SubmitSm submitSm;

}