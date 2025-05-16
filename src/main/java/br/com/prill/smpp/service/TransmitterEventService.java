package br.com.prill.smpp.service;

import br.com.prill.smpp.entity.TransmitterEvent;
import br.com.prill.smpp.repository.TransmitterEventRepository;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransmitterEventService {

    @Autowired
    private TransmitterEventRepository transmitterEventRepository;

    public void saveMessage(SubmitSmResp submitSmResp, SubmitSm submitSm){
        TransmitterEvent transmitterEvent = new TransmitterEvent(submitSmResp.getMessageId(), submitSm);
        transmitterEventRepository.insert(transmitterEvent);
    }
}
