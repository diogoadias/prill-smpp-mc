package br.com.prill.smpp.service;

import br.com.prill.smpp.manager.SmppSessionManager;
import com.cloudhopper.commons.util.windowing.WindowFuture;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.type.RecoverablePduException;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class DeliverSmService {

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
            deliverSm.setSourceAddress(submitSm.getSourceAddress());
            deliverSm.setDestAddress(submitSm.getDestAddress());
            deliverSm.setShortMessage(submitSm.getShortMessage());

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
}
