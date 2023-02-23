package de.kai_morich.simple_bluetooth_le_terminal.Managers;

import de.kai_morich.simple_bluetooth_le_terminal.MainStation.Session;

public class SessionManager {

    private static Session _currentSession;

    static public void registerSession(Session session) { _currentSession = session; }

    static public Session CurrentSession() { return _currentSession; }

}
