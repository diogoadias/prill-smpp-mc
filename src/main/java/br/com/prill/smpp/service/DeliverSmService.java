package br.com.prill.smpp.service;

import br.com.prill.smpp.manager.SmppSessionManager;
import br.com.prill.smpp.repository.ReceiverEventRepository;
import com.cloudhopper.commons.util.windowing.WindowFuture;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.pdu.*;
import com.cloudhopper.smpp.type.RecoverablePduException;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class DeliverSmService {

    @Autowired
    ReceiverEventRepository receiverEventRepository;

    @Async
    @Retryable(
            retryFor = {SmppTimeoutException.class, RecoverablePduException.class, SmppChannelException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public void processDeliverSm(SubmitSm submitSm) {

        DeliverSm deliverSm;
        try {
            deliverSm = new DeliverSm();
            deliverSm.setServiceType(submitSm.getServiceType());
            deliverSm.setSourceAddress(submitSm.getSourceAddress());
            deliverSm.getSourceAddress().setTon(submitSm.getSourceAddress().getTon());
            deliverSm.getSourceAddress().setNpi(submitSm.getSourceAddress().getNpi());
            deliverSm.setDestAddress(submitSm.getDestAddress());
            deliverSm.getDestAddress().setTon(submitSm.getDestAddress().getTon());
            deliverSm.getDestAddress().setNpi(submitSm.getDestAddress().getNpi());
            deliverSm.setEsmClass(submitSm.getEsmClass());
            deliverSm.setProtocolId(submitSm.getProtocolId());
            deliverSm.setPriority(submitSm.getPriority());
            deliverSm.setScheduleDeliveryTime(submitSm.getScheduleDeliveryTime());
            deliverSm.setValidityPeriod(submitSm.getValidityPeriod());
            deliverSm.setRegisteredDelivery(submitSm.getRegisteredDelivery());
            deliverSm.setReplaceIfPresent(submitSm.getReplaceIfPresent());
            deliverSm.setDataCoding(submitSm.getDataCoding());
            deliverSm.setDefaultMsgId(submitSm.getDefaultMsgId());
            deliverSm.setShortMessage(submitSm.getShortMessage());
            deliverSm.setOptionalParameter(submitSm.getOptionalParameter((short) 0x001E));

            log.info("DeliverSm received from {} to {}: {}",
                    deliverSm.getSourceAddress().getAddress(),
                    deliverSm.getDestAddress().getAddress(),
                    new String(deliverSm.getShortMessage()));

            SmppServerSession receiverSession = SmppSessionManager.getSmppSessionForReceiver("receiver");
            if (receiverSession != null && receiverSession.isBound()) {
                CompletableFuture.runAsync(() -> {
                    try {
                        WindowFuture<Integer, PduRequest, PduResponse> future = receiverSession.sendRequestPdu(deliverSm, 10000, false);
                        if (future.await() && future.isSuccess()) {
                            log.info("DeliverSm successfully sent to receiver: {}", deliverSm.getDestAddress().getAddress());
                        } else {
                            log.error("Failed to send DeliverSm to receiver: {}", deliverSm.getDestAddress().getAddress(), future.getCause());
                        }

//                        processResponse(future.getResponse());
                    } catch (Exception e) {
                        log.error("Error to processing DeliverSm: {}. \nError message: {}", deliverSm.getDestAddress().getAddress(), e.getMessage());
                    }
                });
            } else {
                log.error("No active session for receiver: {}", deliverSm.getDestAddress().getAddress());
            }

        } catch (Exception e) {
            log.error("Failed to send DeliverSm to receiver: {}", submitSm.getDestAddress().getAddress(), e);
        }
    }

    public void processResponse(PduResponse pdu){
        DeliverSmResp deliverSmResp = (DeliverSmResp) pdu;
        log.info(deliverSmResp.getMessageId());
        log.info(deliverSmResp.getName());
        log.info(deliverSmResp.getResultMessage());
        log.info(deliverSmResp.toString());
    }
}