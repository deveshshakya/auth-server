package com.server.auth.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class IPAddressUtility {
  public static boolean isValidInet4Address(String ip) {
    try {
      return Inet4Address.getByName(ip).getHostAddress().equals(ip);
    } catch (UnknownHostException ex) {
      return false;
    }
  }

  public static String getSourceIP(HttpServletRequest request) {
    String sourceIp = request.getHeader("X-FORWARDED-FOR");
    if (!StringUtils.isEmpty(sourceIp)) {
      String[] ipSplits = sourceIp.split(",");
      sourceIp = ipSplits[0].trim(); // Fetching source IP
      return sourceIp;
    }
    return "";
  }
}
