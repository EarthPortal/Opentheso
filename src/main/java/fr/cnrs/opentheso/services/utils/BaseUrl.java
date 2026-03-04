package fr.cnrs.opentheso.services.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BaseUrl {

    @Autowired
    private HttpServletRequest request;

    public String getBaseUrl() {
        String scheme = request.getScheme(); // http ou https
        String serverName = request.getServerName(); // ex: localhost
        int serverPort = request.getServerPort(); // ex: 8099
        String contextPath = request.getContextPath(); // ex: /

        // si port standard, on peut l’omettre
        String portPart = (serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort;

        return scheme + "://" + serverName + portPart + contextPath;
    }
}
