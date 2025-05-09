package br.com.prill.smpp.dto;

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
    private String sourceAddress;
    private String destinationAddress;
    private String shortMessage;
    private byte dataCoding;
    private byte esmClass;
    private byte priorityFlag;
    private String serviceType;
}