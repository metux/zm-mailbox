/*
 * ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 ("License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.zimbra.com/license
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 * 
 * The Original Code is: Zimbra Collaboration Suite Server.
 * 
 * The Initial Developer of the Original Code is Zimbra, Inc.
 * Portions created by Zimbra are Copyright (C) 2005, 2006 Zimbra, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): 
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.common.util;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.log4j.NDC;
import org.apache.log4j.PropertyConfigurator;


/**
 * @author schemers
 *
 */
public class ZimbraLog {
    
    /**
     * "ip" key for context. IP of request
     */
    public static final String C_IP = "ip";
    
    /**
     * "id" key for context. Id of the target account
     */
    public static final String C_ID = "id";

    /**
     * "name" key for context. Id of the target account
     */
    public static final String C_NAME = "name";

    /**
     * "aid" key for context. Id in the auth token. Only present if target id is
     * different then auth token id.
     */
    public static final String C_AID = "aid";

    /**
     * "aname" key for context. name in the auth token. Only present if target id is
     * different then auth token id.
     */
    public static final String C_ANAME = "aname";

    /**
     * "cid" is the connection id of a server that is monotonically increasing - useful
     * for tracking individual connections.
     */
    public static final String C_CONNECTIONID = "cid";
    
    /**
     * "mid" key for context. Id of requested mailbox. Only present if request is
     * dealing with a mailbox.
     */
    private static final String C_MID = "mid";    

    /**
     * "ua" key for context.  The name of the client application. 
     */
    public static final String C_USER_AGENT = "ua";
    
    /**
     * "msgid" key for context.  The Message-ID header of the message being
     * operated on.
     */
    private static final String C_MSG_ID = "msgid";
    
    /**
     * "ds" key for context.  The name of the Data Source being operated on.
     */
    private static final String C_DATA_SOURCE_NAME = "ds";
    
    
    /**
     * the "zimbra.misc" logger. For all events that don't have a specific-catagory.
     */
    public static final Log misc = LogFactory.getLog("zimbra.misc");
    
    /**
     * the "zimbra.index" logger. For indexing-related events.
     */
    public static final Log index = LogFactory.getLog("zimbra.index");
    
    /**
     * the "zimbra.journal" logger. For journal-releated events.
     */
    public static final Log journal = LogFactory.getLog("zimbra.journal");
    
    /**
     * the "zimbra.lmtp" logger. For LMTP-related events.
     */
    public static final Log lmtp = LogFactory.getLog("zimbra.lmtp");
    
    /**
     * the "zimbra.smtp" logger. For SMTP-related events.
     */
    public static final Log smtp = LogFactory.getLog("zimbra.smtp");

    /**
     * the "zimbra.nio" logger. For NIO-related events.
     */
    public static final Log nio = LogFactory.getLog("zimbra.nio");

    /**
     * the "zimbra.imap" logger. For IMAP-related events.
     */
    public static final Log imap = LogFactory.getLog("zimbra.imap");
    
    /**
     * the "zimbra.imap" logger. For POP-related events.
     */
    public static final Log pop = LogFactory.getLog("zimbra.pop");
    
    /**
     * the "zimbra.mailbox" logger. For mailbox-related events.
     */
    public static final Log mailbox = LogFactory.getLog("zimbra.mailbox");
    
    /**
     * the "zimbra.calendar" logger. For calendar-related events.
     */
    public static final Log calendar = LogFactory.getLog("zimbra.calendar");
    
    /**
     * the "zimbra.im" logger. For instant messaging-related events.
     */
    public static final Log im = LogFactory.getLog("zimbra.im");
    
    /**
     * the "zimbra.account" logger. For account-related events.
     */
    public static final Log account = LogFactory.getLog("zimbra.account");
    
    /**
     * the "zimbra.security" logger. For security-related events
     */
    public static final Log security = LogFactory.getLog("zimbra.security");    

    /**
     * the "zimbra.soap" logger. For soap-related events
     */
    public static final Log soap = LogFactory.getLog("zimbra.soap");

    /**
     * the "zimbra.test" logger. For testing-related events
     */
    public static final Log test = LogFactory.getLog("zimbra.test");

    /**
     * the "zimbra.sqltrace" logger. For tracing SQL statements sent to the database
     */
    public static final Log sqltrace = LogFactory.getLog("zimbra.sqltrace");

    /**
     * the "zimbra.perf" logger. For logging performance statistics
     */
    public static final Log perf = LogFactory.getLog("zimbra.perf");

    /**
     * the "zimbra.cache" logger. For tracing object cache activity
     */
    public static final Log cache = LogFactory.getLog("zimbra.cache");
    
    /**
     * the "zimbra.filter" logger. For filter-related logs.
     */
    public static final Log filter = LogFactory.getLog("zimbra.filter");
    
    /**
     * the "zimbra.session" logger. For session- and notification-related logs.
     */
    public static final Log session = LogFactory.getLog("zimbra.session");
    
    /**
     * the "zimbra.backup" logger. For backup/restore-related logs.
     */
    public static final Log backup = LogFactory.getLog("zimbra.backup");
    
    /**
     * the "zimbra.system" logger. For startup/shutdown and other related logs.
     */
    public static final Log system = LogFactory.getLog("zimbra.system");
    
    /**
     * the "zimbra.sync" logger. For sync client interface logs.
     */
    public static final Log sync = LogFactory.getLog("zimbra.sync");
    
    /**
     * the "zimbra.synctrace" logger. For sync client interface logs.
     */
    public static final Log synctrace = LogFactory.getLog("zimbra.synctrace");
    
    /**
     * the "zimbra.syncstate" logger. For sync client interface logs.
     */
    public static final Log syncstate = LogFactory.getLog("zimbra.syncstate");
    
    /**
     * the "zimbra.wbxml" logger. For wbxml client interface logs.
     */
    public static final Log wbxml = LogFactory.getLog("zimbra.wbxml");
    
    /**
     * the "zimbra.extensions" logger. For logging extension loading related info. 
     */
    public static final Log extensions = LogFactory.getLog("zimbra.extensions");

    /**
     * the "zimbra.zimlet" logger. For logging zimlet related info. 
     */
    public static final Log zimlet = LogFactory.getLog("zimbra.zimlet");
    
    /**
     * the "zimbra.wiki" logger. For wiki and document sharing. 
     */
    public static final Log wiki = LogFactory.getLog("zimbra.wiki");
    
    /**
     * the "zimbra.op" logger. Logs server operations
     */
    public static final Log op = LogFactory.getLog("zimbra.op");
    
    /**
     * the "zimbra.dav" logger. Logs dav operations
     */
    public static final Log dav = LogFactory.getLog("zimbra.dav");

    /**
     * the "zimbra.io" logger.  Logs file IO operations.
     */
    public static final Log io = LogFactory.getLog("zimbra.io");

    /**
     * the "zimbra.datasource" logger.  Logs file data source operations.
     */
    public static final Log datasource = LogFactory.getLog("zimbra.datasource");

    public static String getContext() {
        return NDC.peek();
    }
    
    /**
     * remote management.
     */
    public static final Log rmgmt = LogFactory.getLog("zimbra.rmgmt");

    /**
     * adds key/value to context. Doesn't check to see if already in context.
     * @param key
     * @param value
     */
    public static void addToContext(String key, String value) {
        if (key == null)
            return;
        String ndc = NDC.pop();
        if (checkContext(ndc, key))
        	NDC.push(key+"="+value+";"+ndc);
        else
            NDC.push(ndc);
    }
    
    /**
     * push key/value to end of context.
     * @param key
     * @param value
     */
    public static void pushbackContext(String key, String value) {
        if (key == null) return;
        String ndc = NDC.pop();
        String ctxt = key + "=" + value + ";";
        if (!ndc.endsWith(ctxt)) {
        	ndc += ctxt;
        }
        NDC.push(ndc);
    }
    
    public static void pushbackItemId(int itemId) {
    	pushbackContext("item", Integer.toString(itemId));
    }
    
    /**
     * pop key/value from end of context.
     * @param key
     * @param value
     */
    public static void popbackContext(String key, String value) {
        if (key == null) return;
        String ndc = NDC.pop();
        String ctxt = key + "=" + value + ";";
        if (ndc.endsWith(ctxt)) {
        	ndc = ndc.substring(0, ndc.length() - ctxt.length());
        }
        NDC.push(ndc);
    }
    
    public static void popbackItemId(int itemId) {
    	popbackContext("item", Integer.toString(itemId));
    }
    
    public static void addAccountNameToContext(String accountName) {
        ZimbraLog.addToContext(C_NAME, accountName);
    }
    
    public static void addIpToContext(String ipAddress) {
        ZimbraLog.addToContext(C_IP, ipAddress);
    }  
    
    public static void addConnectionIdToContext(String connectionId) {
        ZimbraLog.addToContext(C_CONNECTIONID, connectionId);
    }
    
    public static void addMboxToContext(int mboxId) {
        addToContext(C_MID, Integer.toString(mboxId));
    }
    
    public static void addMsgIdToContext(String messageId) {
        addToContext(C_MSG_ID, messageId);
    }
    
    public static void addDataSourceNameToContext(String dataSourceName) {
        ZimbraLog.addToContext(C_DATA_SOURCE_NAME, dataSourceName);
    }
    
    private static boolean checkContext(String context, String key) {
        if (context == null || key == null)
            return false;
        return (!context.startsWith(key + "=") && context.indexOf(";" + key + "=") == -1);
    }

    public static void clearContext() {
        NDC.clear();
    }

    /**
     * Setup log4j for our command line tools.  
     * 
     * If System.getProperty(zimbra.log4j.level) is set then log at that level.
     * Else log at the specified defaultLevel.
     */
    public static void toolSetupLog4j(String defaultLevel, String logFile, boolean showThreads) {
        String level = System.getProperty("zimbra.log4j.level");
        if (level == null) {
            level = defaultLevel;
        }
        Properties p = new Properties();
        p.put("log4j.rootLogger", level + ",A1");
        if (logFile != null) {
            p.put("log4j.appender.A1", "org.apache.log4j.FileAppender");
            p.put("log4j.appender.A1.File", logFile);
            p.put("log4j.appender.A1.Append", "false");
        } else {
            p.put("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
        }
        p.put("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
        if (showThreads) {
        	p.put("log4j.appender.A1.layout.ConversionPattern", "[%t] [%x] %p: %m%n");
        } else {
        	p.put("log4j.appender.A1.layout.ConversionPattern", "[%x] %p: %m%n");
        }
        PropertyConfigurator.configure(p);
    }

    /**
     * Setup log4j for command line tool using specified log4j.properties file.
     * If file doesn't exist System.getProperty(zimbra.home)/conf/log4j.properties
     * file will be used.
     * @param defaultLevel
     * @param propsFile full path to log4j.properties file
     */
    public static void toolSetupLog4j(String defaultLevel, String propsFile) {   
        if (propsFile != null && new File(propsFile).exists()) {
        	PropertyConfigurator.configure(propsFile);
        } else {
            toolSetupLog4j(defaultLevel, null, false);
        }
    }

    
    private static void encodeArg(StringBuffer sb, String name, String value) {
        if (value == null) value = "";
        if (value.indexOf(';') != -1) value = value.replaceAll(";", ";;");
        // replace returns ref to original string if char to replace doesn't exist
        value = value.replace('\r', ' ');        
        value = value.replace('\n', ' ');
        sb.append(name);
        sb.append("=");
        sb.append(value);
        sb.append(';');
    }

    /**
     * Take an array of Strings [ "name1", "value1", "name2", "value2", ...] and format them for logging purposes.
     * @param strings
     * @return
     */
    public static String encodeAttrs(String[] args) {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i < args.length; i += 2) {
            if (i > 0) sb.append(' ');            
            encodeArg(sb, args[i], args[i+1]);
        }
        return sb.toString();
    }
    

    /**
     * Take an array of Strings [ "name1", "value1", "name2", "value2", ...] and format them for logging purposes
     * into: name1=value1; name2=value; semicolons are escaped with two semicolons (value a;b is encoded as a;;b)
     * @param strings
     * @return
     */
    public static String encodeAttrs(String[] args, Map extraArgs) {
        StringBuffer sb = new StringBuffer();
        boolean needSpace = false;
        for (int i=0; i < args.length; i += 2) {
            if (needSpace) sb.append(' '); else needSpace = true;
            encodeArg(sb, args[i], args[i+1]);
        }
        if (extraArgs != null) {
            for (Iterator it=extraArgs.entrySet().iterator(); it.hasNext();) {
                if (needSpace) sb.append(' '); else needSpace = true;                
                Map.Entry entry = (Entry) it.next();
                String name = (String) entry.getKey();
                Object v = entry.getValue();
                if (v == null) {
                    encodeArg(sb, name, "");
                } else if (v instanceof String) {
                    encodeArg(sb, name, (String)v);
                } else if (v instanceof String[]) {
                    String values[] = (String[]) v;
                    for (int i=0; i < values.length; i++) {
                        encodeArg(sb, name, values[i]);
                    }
                }
            }
        }
        return sb.toString();
    }

}
