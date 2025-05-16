package br.com.prill.smpp.service;

import br.com.prill.smpp.dto.SubmitSmDTO;
import br.com.prill.smpp.entity.TransmitterEvent;
import br.com.prill.smpp.kafka.service.KafkaProducerService;
import br.com.prill.smpp.repository.TransmitterEventRepository;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.type.Address;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SubmitSmService {

    public SubmitSm process(String pdu) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Converte o PDU em um SubmitSmDTO
            SubmitSmDTO submitSmDTO = mapper.readValue(pdu, SubmitSmDTO.class);
            log.info("PDU convertido para SubmitSmDTO");

            // Mapeia os campos do SubmitSmDTO para um clouhopper SubmitSm
            SubmitSm submitSm = submitSmDTO.getSubmitSm();
//            submitSm.setServiceType(submitSmDTO.getSubmitSm().getServiceType());
//            submitSm.getSourceAddress().setTon(submitSmDTO.getSubmitSm().getSourceAddress().getSourceTon());
//            submitSm.getSourceAddress().setNpi(submitSmDTO.getSubmitSm().getSourceNpi());
//            submitSm.setSourceAddress(new Address((byte) 0, (byte) 0, submitSmDTO.getSubmitSm().getSourceAddress()));
//            submitSm.getDestAddress().setTon(submitSmDTO.getSubmitSm().getDestTon());
//            submitSm.getDestAddress().setNpi(submitSmDTO.getSubmitSm().getDestNpi());
//            submitSm.setDestAddress(new Address((byte) 0, (byte) 0, submitSmDTO.getSubmitSm().getDestinationAddress()));
//            submitSm.setEsmClass(submitSmDTO.getSubmitSm().getEsmClass());
//            submitSm.setEsmClass(submitSmDTO.getSubmitSm().getEsmClass());
//            submitSm.setProtocolId(submitSmDTO.getSubmitSm().getProtocolId());
//            submitSm.setPriority(submitSmDTO.getSubmitSm().getPriorityFlag());
//            submitSm.setScheduleDeliveryTime(submitSmDTO.getSubmitSm().getScheduleDeliveryTime());
//            submitSm.setValidityPeriod(submitSmDTO.getSubmitSm().getValidityPeriod());
//            submitSm.setRegisteredDelivery(submitSmDTO.getSubmitSm().getRegisteredDelivery());
//            submitSm.setReplaceIfPresent(submitSmDTO.getSubmitSm().getReplaceIfPresentFlag());
//            submitSm.setDataCoding(submitSmDTO.getSubmitSm().getDataCoding());
//            submitSm.setDefaultMsgId(submitSmDTO.getSubmitSm().getDefaultMsgId());
//            submitSm.setShortMessage(submitSmDTO.getSubmitSm().getShortMessage().getBytes());

            log.info("SubmitSmDTO converted to clouhopper SubmitSm");
            return submitSm;
        } catch (Exception e) {
            log.error("Fail to process PDU", e);
        }
        return null;
    }

    public SubmitSmResp processSubmitSm(SubmitSm submitSm) {
        String message = new String(submitSm.getShortMessage());
        log.info("Received SMS from {} to {}: {}", submitSm.getSourceAddress().getAddress(), submitSm.getDestAddress().getAddress(), message);

        SubmitSmResp response = submitSm.createResponse();
        response.setMessageId("msg-" + System.currentTimeMillis());

        return response;
    }
}
