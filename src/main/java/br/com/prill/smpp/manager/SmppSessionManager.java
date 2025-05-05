package br.com.prill.smpp.manager;

import com.cloudhopper.smpp.SmppServerSession;
import java.util.concurrent.ConcurrentHashMap;

public class SmppSessionManager {
    private static final ConcurrentHashMap<String, SmppServerSession> sessions = new ConcurrentHashMap<>();

    private SmppSessionManager(){}

    public static void addSession(String address, SmppServerSession session) {
        sessions.put(address, session);
    }

    public static SmppServerSession getSmppSessionForReceiver(String address) {
        return sessions.get(address);
    }

    public static void removeSession(String address) {
        sessions.remove(address);
    }
}