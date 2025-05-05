package br.com.prill.smpp.service;

import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SubmitSmservice {

    public SubmitSmResp processSubmitSm(SubmitSm submitSm) {
        String message = new String(submitSm.getShortMessage());
        log.info("Received SMS from {} to {}: {}", submitSm.getSourceAddress(), submitSm.getDestAddress(), message);
        SubmitSmResp response = submitSm.createResponse();
        response.setMessageId("msg-" + System.currentTimeMillis());
        return response;
    }
}
