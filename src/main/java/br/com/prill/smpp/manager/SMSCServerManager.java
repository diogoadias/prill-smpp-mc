package br.com.prill.smpp.manager;

import br.com.prill.smpp.kafka.service.KafkaProducerService;
import br.com.prill.smpp.service.DeliverSmService;
import br.com.prill.smpp.service.SubmitSmservice;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppServerHandler;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;

import com.cloudhopper.smpp.pdu.BaseBind;
import com.cloudhopper.smpp.pdu.BaseBindResp;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.type.SmppProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class SMSCServerManager implements SmppServerHandler {

    private final DeliverSmService deliverSmService;
    private final SubmitSmservice submitSmservice;
    private final KafkaProducerService kafkaProducerService;
    private final String password;
    private final String transmitterTopic;


    public SMSCServerManager(DeliverSmService deliverSmService, SubmitSmservice submitSmservice, KafkaProducerService kafkaProducerService, String password, String transmitterTopic) {
        this.deliverSmService = deliverSmService;
        this.submitSmservice = submitSmservice;
        this.kafkaProducerService = kafkaProducerService;
        this.password = password;
        this.transmitterTopic = transmitterTopic;
    }

    @Override
    public void sessionBindRequested(Long sessionId, SmppSessionConfiguration sessionConfiguration, BaseBind bindRequest) throws SmppProcessingException {

        if (bindRequest.getPassword() == null || !password.equals(bindRequest.getPassword())) {
            throw new SmppProcessingException(SmppConstants.STATUS_INVPASWD);
        }
        log.info("Bind requested: sessionId={}, bindType={}", sessionId, bindRequest.getClass().getSimpleName());
    }

    @Override
    public void sessionCreated(Long sessionId, SmppServerSession session, BaseBindResp preparedBindResponse) throws SmppProcessingException {
        log.info("Session created: {}", session.getConfiguration().getSystemId());
        SmppSessionManager.addSession(session.getConfiguration().getSystemId(), session);
        session.serverReady(new DefaultSmppSessionHandler() {
            @Override
            public PduResponse firePduRequestReceived(PduRequest pduRequest) {
                if (pduRequest.getCommandId() == SmppConstants.CMD_ID_SUBMIT_SM) {
                    SubmitSm submitSm = (SubmitSm) pduRequest;
                    deliverSmService.processDeliverSm(submitSm);
                    kafkaProducerService.process(transmitterTopic, submitSm);
                    return submitSmservice.processSubmitSm(submitSm);
                } else if (pduRequest.getCommandId() == SmppConstants.CMD_ID_SUBMIT_MULTI) {
                    log.warn("SubmitMulti request received but not supported");
                    throw new IllegalStateException("SubmitMulti not supported");
                } else if (pduRequest.getCommandId() == SmppConstants.CMD_ID_UNBIND) {
                    log.info("Received Unbind request");
                    return pduRequest.createResponse();
                } else if (pduRequest.getCommandId() == SmppConstants.CMD_ID_ENQUIRE_LINK) {
                    log.info("Received EnquireLink request");
                    return pduRequest.createResponse();
                } else {
                    log.warn("Received unsupported PDU request of type: {}", pduRequest.getClass().getSimpleName());
                    throw new IllegalStateException("Unsupported PDU type: " + pduRequest.getClass().getSimpleName());
                }
            }
        });
    }

    @Override
    public void sessionDestroyed(Long sessionId, SmppServerSession session) {
        log.info("Session destroyed: {}", session.getConfiguration().getSystemId());
        SmppSessionManager.removeSession(session.getConfiguration().getSystemId());
        session.destroy();
    }
}