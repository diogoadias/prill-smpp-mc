package br.com.prill.smpp.kafka.service;

import br.com.prill.smpp.repository.TransmitterEventRepository;
import br.com.prill.smpp.service.DeliverSmService;
import br.com.prill.smpp.service.SubmitSmService;
import br.com.prill.smpp.service.TransmitterEventService;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.tlv.Tlv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class KafkaConsumerService {
    
    @Autowired
    SubmitSmService submitSmService;

    @Autowired
    DeliverSmService deliverSmService;

    @Autowired
    TransmitterEventService transmitterEventService;

    @KafkaListener(topics = "${spring.kafka.consumer.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String message) {
        log.info("MReceived message from Kafka: {}", message);
        SubmitSm submitSm = submitSmService.process(message);
        SubmitSmResp submitSmResp = submitSmService.processSubmitSm(submitSm);
        submitSm.addOptionalParameter(new Tlv((short) 0x001E, submitSmResp.getMessageId().getBytes(StandardCharsets.UTF_8)));
        transmitterEventService.saveMessage(submitSmResp, submitSm);
        deliverSmService.processDeliverSm(submitSm);
    }
}
