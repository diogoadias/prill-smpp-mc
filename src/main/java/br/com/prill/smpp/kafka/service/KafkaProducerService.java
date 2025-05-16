package br.com.prill.smpp.kafka.service;

import br.com.prill.smpp.dto.SubmitSmDTO;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, String message) {
        log.info("Enviando mensagem para o tópico {}: {}", topic, message);
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, message);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Mensagem enviada com sucesso para o tópico {}: {}", topic, message);
            } else {
                log.error("Erro ao enviar mensagem para o tópico {}: {}", topic, message, ex);
            }
        });
    }

    public void process(String topic, SubmitSmResp submitSmResp, SubmitSm message) {
        try {

            SubmitSmDTO submitSmDTO = new SubmitSmDTO(submitSmResp.getMessageId(), message);
            String jsonMessage = new ObjectMapper().writeValueAsString(submitSmDTO);
            sendMessage(topic, jsonMessage);
        } catch (JsonProcessingException e) {
            log.error("Erro ao converter mensagem para JSON: {}", e.getMessage());
        }
    }
}
