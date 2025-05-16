package br.com.prill.smpp.manager;

import br.com.prill.smpp.entity.TransmitterEvent;
import br.com.prill.smpp.kafka.service.KafkaProducerService;
import br.com.prill.smpp.repository.TransmitterEventRepository;
import br.com.prill.smpp.service.DeliverSmService;
import br.com.prill.smpp.service.SubmitSmService;
import br.com.prill.smpp.service.TransmitterEventService;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppServerHandler;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.*;
import com.cloudhopper.smpp.tlv.Tlv;
import lombok.extern.slf4j.Slf4j;
import org.jsmpp.SMPPConstant;
import org.jsmpp.extra.ProcessRequestException;

import java.nio.charset.StandardCharsets;

@Slf4j
public class SMSCServerManager implements SmppServerHandler {

    private final DeliverSmService deliverSmService;
    private final SubmitSmService submitSmservice;
    private final KafkaProducerService kafkaProducerService;
    private final String password;
    private final String transmitterTopic;
    private final TransmitterEventService transmitterEventService;

    public SMSCServerManager(DeliverSmService deliverSmService,
                             SubmitSmService submitSmservice,
                             KafkaProducerService kafkaProducerService,
                             String password,
                             String transmitterTopic,
                             TransmitterEventService transmitterEventService
                            ) {
        this.deliverSmService = deliverSmService;
        this.submitSmservice = submitSmservice;
        this.kafkaProducerService = kafkaProducerService;
        this.password = password;
        this.transmitterTopic = transmitterTopic;
        this.transmitterEventService = transmitterEventService;
    }


    @Override
    public void sessionBindRequested(Long sessionId, SmppSessionConfiguration smppSessionConfiguration, BaseBind baseBind) {
        if (baseBind.getPassword() == null || !password.equals(baseBind.getPassword())) {
            log.warn("Invalid password: systemId={}", baseBind.getSystemId());
            try {
                throw new ProcessRequestException("Invalid password", SMPPConstant.STAT_ESME_RBINDFAIL);
            } catch (ProcessRequestException e) {
                throw new RuntimeException(e);
            }
        }

        log.info("Bind success: systemId={}", baseBind.getSystemId());

    }

    @Override
    public void sessionCreated(Long sessionId, SmppServerSession session, BaseBindResp baseBindResp)  {

        SmppSessionManager.addSession(session.getConfiguration().getSystemId(), session);
        session.serverReady(new DefaultSmppSessionHandler() {
            @Override
            public PduResponse firePduRequestReceived(PduRequest pduRequest) {
                if (pduRequest.getCommandId() == SmppConstants.CMD_ID_SUBMIT_SM) {
                    SubmitSm submitSm = (SubmitSm) pduRequest;
                    SubmitSmResp submitSmResp = submitSmservice.processSubmitSm(submitSm);
                    submitSm.addOptionalParameter(new Tlv((short) 0x001E, submitSmResp.getMessageId().getBytes(StandardCharsets.UTF_8)));
                    deliverSmService.processDeliverSm(submitSm);
                    kafkaProducerService.process(transmitterTopic,submitSmResp, submitSm);
                    transmitterEventService.saveMessage(submitSmResp, submitSm);

                    return submitSmResp;
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