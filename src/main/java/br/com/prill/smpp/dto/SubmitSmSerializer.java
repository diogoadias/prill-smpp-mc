package br.com.prill.smpp.dto;

import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.tlv.Tlv;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class SubmitSmSerializer extends StdSerializer<SubmitSm> {

    public SubmitSmSerializer() {
        super(SubmitSm.class);
    }

    @Override
    public void serialize(SubmitSm submitSm, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("serviceType", submitSm.getServiceType());
        gen.writeStringField("sourceAddress", submitSm.getSourceAddress().getAddress());
        gen.writeStringField("destinationAddress", submitSm.getDestAddress().getAddress());
        gen.writeNumberField("esmClass", submitSm.getEsmClass());
        gen.writeNumberField("protocolId", submitSm.getProtocolId());
        gen.writeNumberField("priorityFlag", submitSm.getPriority());
        gen.writeStringField("scheduleDeliveryTime", submitSm.getScheduleDeliveryTime());
        gen.writeStringField("validityPeriod", submitSm.getValidityPeriod());
        gen.writeNumberField("registeredDelivery", submitSm.getRegisteredDelivery());
        gen.writeNumberField("replaceIfPresent", submitSm.getReplaceIfPresent());
        gen.writeNumberField("dataCoding", submitSm.getDataCoding());
        gen.writeNumberField("defaultMsgId", submitSm.getDefaultMsgId());
        gen.writeBinaryField("shortMessage", submitSm.getShortMessage());

        // Serializando os parâmetros opcionais
        gen.writeArrayFieldStart("optionalParameters");
        List<String> optionalParameters = submitSm.getOptionalParameters().stream()
                .map(tlv -> {
                    try {
                        return new String(tlv.getValue());
                    } catch (Exception e) {
                        return "Invalid TLV value";
                    }
                })
                .collect(Collectors.toList());
        for (String param : optionalParameters) {
            gen.writeString(param);
        }
        gen.writeEndArray();

        gen.writeEndObject();
    }
}