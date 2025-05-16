package br.com.prill.smpp.entity;

import com.cloudhopper.smpp.pdu.DeliverSmResp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiverEvent {

    @Id
    private String eventId;
    private DeliverSmResp deliverSmResp;
//    private LocalDateTime timestamp;
//    private String sourceAddress;
//    private String destinationAddress;
//    private String message;
//    private String status;
}
