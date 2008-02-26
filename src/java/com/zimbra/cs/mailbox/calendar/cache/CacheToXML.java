/*
 * ***** BEGIN LICENSE BLOCK *****
 * 
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2008 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Yahoo! Public License
 * Version 1.0 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * 
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.cs.mailbox.calendar.cache;

import java.util.Iterator;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.IDNUtil;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.calendar.Alarm;
import com.zimbra.cs.mailbox.calendar.ZOrganizer;
import com.zimbra.cs.service.util.ItemIdFormatter;
import com.zimbra.soap.ZimbraSoapContext;

public class CacheToXML {

    // SOAP SearchResponse format for calendar items
    //
    // <appt> or <task>
    //     f - flags
    //     t - tags
    //     <or> - organizer
    //       a - address
    //       d - CN (display name)
    //       sentBy - sent-by address
    //     x-uid - UID
    //     uid - UID
    //     priority
    //     ptst - PARTSTAT
    //     fb - intended freebusy (appt only)
    //     fba - actual freebusy (appt only)
    //     transp - TRANSP (appt only)
    //     percentComplete - PERCENT-COMPLETE (task only)
    //     name - SUMMARY
    //     loc - LOCATION
    //     fr - fragment
    //     otherAtt - has attendees ("other" meaning other than organizer)
    //     isOrg - mailbox owner is organizer
    //     id - "<calItemId>-<invId>" (or is it just calItemId?  can this be fully-qualified?)
    //     invId - invite id
    //     compNum - component number
    //     l - folder id (can be fully-qualified)
    //     status - STATUS
    //     class - CLASS
    //     dueDate - DUE (task only)
    //     dur - DURATION
    //     allDay - is this an all-day appt/task?
    //     recur - is this recurring?
    //     <inst>+ (attrs are given only when different from default value at <appt> level)
    //         fba
    //         ptst
    //         s - DTSTART
    //         tzo - timezone offset in millis (for all-day only)
    //         dur
    //         ex - instance is an exception
    //         // begin exception block (following attrs are possible only when ex=1)
    //         invId - "<calItemId>-<invId>" (notice this is different from invId at appt/task)
    //         compNum
    //         fr
    //         priority
    //         fb
    //         transp
    //         percentComplete
    //         name
    //         loc
    //         otherAtt
    //         isOrg
    //         status
    //         class
    //         allDay
    //         recur
    //         // end exception block
    //         // NOTE: Only invId is a new instance-level attribute.
    //     <alarmData> (next alarm to fire)
    //         nextAlarm - time at which the next alarm fires
    //         alarmInstStart - start time of the instance for the alarm
    //         name - SUMMARY of the instance
    //         loc - LOCATION of the instance
    //         invId - integer invite id of the instance
    //         compNum - component number of the instance
    //         <alarm> (details on the alarm itself)
    //             action - alarm ACTION type
    //             <trigger>
    //                 <abs> (mutually exclusive with <rel>)
    //                     d - absolute alarm trigger time
    //                 <rel> (a duration, plus "related" attr)
    //                     neg - value is negative
    //                     w - weeks
    //                     d - days
    //                     h - hours
    //                     m - minutes
    //                     s - seconds
    //                     related - duration is from start or end of instance
    //                 <repeat> (a duration, plus "count" attr)
    //                     neg - value is negative
    //                     w - weeks
    //                     d - days
    //                     h - hours
    //                     m - minutes
    //                     s - seconds
    //                     count - how many times to repeat after initial
    //             <desc>text</desc> - DESCRIPTION
    //             <attach> (attachment info)
    //                 uri - URI of the external attachment
    //                 ct - Content-Type of attachment
    //             or, <attach>base64-encoded attachment data</attach> (Q: why not ct as well?)
    //             <summary>email summary</summary> (action=email only)
    //             <at>+ (Attendees, for email recipients)
    //                 a - address
    //                 url - address (for backward compat)
    //                 d - display name
    //                 sentBy - sent-by
    //                 dir
    //                 lang
    //                 cutype
    //                 role
    //                 ptst
    //                 member
    //                 delTo - DELEGATED-TO
    //                 delFrom - DELEGATED-FROM
    //                 // Q: Do we really need anything other than "a", and possibly "d" and "sentBy"?
    //

    private static void encodeInstanceData(Element parent, ItemIdFormatter ifmt, int calItemId,
                                           InstanceData instance,
                                           boolean isException, boolean isAppointment, boolean showAll,
                                           boolean legacyFormat)
    throws ServiceException {
        if (instance.getDtStart() != null)
            parent.addAttribute(MailConstants.A_CAL_START_TIME, instance.getDtStart().longValue());
        if (instance.getDuration() != null) {
            String attribute = !legacyFormat ? MailConstants.A_CAL_NEW_DURATION : MailConstants.A_CAL_DURATION;
            parent.addAttribute(attribute, instance.getDuration().longValue());
        }
        // TODO: DUE property of task
//        if (!isAppointment && instance.getDtEnd() != null)
//            parent.addAttribute(MailConstants.A_TASK_DUE_DATE, instance.getDtEnd().longValue());
        if (instance.getTZOffset() != null)
            parent.addAttribute(MailConstants.A_CAL_TZ_OFFSET, instance.getTZOffset().longValue());
        parent.addAttribute(MailConstants.A_CAL_PARTSTAT, instance.getPartStat());
        if (isAppointment)
            parent.addAttribute(MailConstants.A_APPT_FREEBUSY_ACTUAL, instance.getFreeBusyActual());
        else
            parent.addAttribute(MailConstants.A_TASK_PERCENT_COMPLETE, instance.getPercentComplete());

        if (!(instance instanceof FullInstanceData))
            return;

        FullInstanceData fullInstance = (FullInstanceData) instance;

        if (isException) {
            parent.addAttribute(MailConstants.A_CAL_IS_EXCEPTION, true);
            // HACK: ZWC insists on receiving recur=false on exceptions.
            parent.addAttribute(MailConstants.A_CAL_RECUR, false);
        }

        // Organizer
        ZOrganizer organizer = fullInstance.getOrganizer();
        if (organizer != null) {
            Element orgElt = parent.addUniqueElement(MailConstants.E_CAL_ORGANIZER);
            orgElt.addAttribute(MailConstants.A_ADDRESS, IDNUtil.toUnicode(organizer.getAddress()));
            orgElt.addAttribute(MailConstants.A_DISPLAY, organizer.getCn());
            orgElt.addAttribute(MailConstants.A_CAL_SENTBY, organizer.getSentBy());
        }

        if (showAll) {
            parent.addAttribute(MailConstants.A_CAL_PRIORITY, fullInstance.getPriority());
            parent.addAttribute(MailConstants.A_CAL_PARTSTAT, fullInstance.getPartStat());
            if (isAppointment) {
                parent.addAttribute(MailConstants.A_APPT_FREEBUSY, fullInstance.getFreeBusyIntended());
                parent.addAttribute(MailConstants.A_APPT_TRANSPARENCY, fullInstance.getTransparency());
            }

            parent.addAttribute(MailConstants.A_NAME, fullInstance.getSummary());
            parent.addAttribute(MailConstants.A_CAL_LOCATION, fullInstance.getLocation());

            // fragment has already been sanitized...
            String fragment = fullInstance.getFragment();
            if (fragment != null && !fragment.equals(""))
                parent.addAttribute(MailConstants.E_FRAG, fragment, Element.Disposition.CONTENT);
            Integer numAttendees = fullInstance.getNumAttendees();
            if (numAttendees != null)
                parent.addAttribute(MailConstants.A_CAL_OTHER_ATTENDEES, numAttendees.intValue() > 0);
        }

        if (fullInstance.isOrganizer() != null)
            parent.addAttribute(MailConstants.A_CAL_ISORG, fullInstance.isOrganizer().booleanValue());
        if (!isException)
            parent.addAttribute(MailConstants.A_ID, ifmt.formatItemId(calItemId));
        parent.addAttribute(MailConstants.A_CAL_INV_ID, ifmt.formatItemId(calItemId, fullInstance.getInvId()));
        parent.addAttribute(MailConstants.A_CAL_COMPONENT_NUM, fullInstance.getCompNum());
        parent.addAttribute(MailConstants.A_CAL_STATUS, fullInstance.getStatus());
        parent.addAttribute(MailConstants.A_CAL_CLASS, fullInstance.getClassProp());
        if (fullInstance.isAllDay() != null)
            parent.addAttribute(MailConstants.A_CAL_ALLDAY, fullInstance.isAllDay().booleanValue());
    }

    private static void encodeAlarmData(Element parent, AlarmData alarmData, boolean showAll) {
        Element alarmElem = parent.addElement(MailConstants.E_CAL_ALARM_DATA);

        long nextAlarm = alarmData.getNextAt();
        if (nextAlarm < Long.MAX_VALUE)
            alarmElem.addAttribute(MailConstants.A_CAL_NEXT_ALARM, nextAlarm);
        // Start time of the meeting instance we're reminding about.
        long alarmInstStart = alarmData.getNextInstanceStart();
        alarmElem.addAttribute(MailConstants.A_CAL_ALARM_INSTANCE_START, alarmInstStart);
        // Some info on the meeting instance the reminder is for.
        // These allow the UI to display tooltip and issue a Get
        // call on the correct meeting instance.
        alarmElem.addAttribute(MailConstants.A_CAL_INV_ID, alarmData.getInvId());
        alarmElem.addAttribute(MailConstants.A_CAL_COMPONENT_NUM, alarmData.getCompNum());
        if (showAll) {
            alarmElem.addAttribute(MailConstants.A_NAME, alarmData.getSummary());
            alarmElem.addAttribute(MailConstants.A_CAL_LOCATION, alarmData.getLocation());
        }
        Alarm alarmObj = alarmData.getAlarm();
        if (alarmObj != null)
            alarmObj.toXml(alarmElem);  // TODO: pay attention to showAll boolean
    }

    public static Element encodeCalendarItemData(ZimbraSoapContext zsc,
                                                 Account acct, Account authAcct, CalendarItemData calItemData,
                                                 boolean legacyFormat)
    throws ServiceException {
        ItemIdFormatter ifmt = new ItemIdFormatter(zsc);
        boolean isAppointment = calItemData.getType() == MailItem.TYPE_APPOINTMENT;

        Element calItemElem = zsc.createElement(
                isAppointment ? MailConstants.E_APPOINTMENT : MailConstants.E_TASK);

        calItemElem.addAttribute("x_uid", calItemData.getUid());
        calItemElem.addAttribute(MailConstants.A_UID, calItemData.getUid());

        FullInstanceData defaultData = calItemData.getDefaultData();
        boolean showAll = acct.allowPrivateAccess(authAcct) || calItemData.isPublic();
        if (showAll) {
            String flags = calItemData.getFlags();
            if (flags != null && !flags.equals(""))
                calItemElem.addAttribute(MailConstants.A_FLAGS, flags);
            String tags = calItemData.getTags();
            if (tags != null && !tags.equals(""))
                calItemElem.addAttribute(MailConstants.A_TAGS, tags);
        }
        calItemElem.addAttribute(MailConstants.A_FOLDER, ifmt.formatItemId(calItemData.getFolderId()));
        if (calItemData.isRecurring())
            calItemElem.addAttribute(MailConstants.A_CAL_RECUR, calItemData.isRecurring());

        int calItemId = calItemData.getCalItemId();
        encodeInstanceData(calItemElem, ifmt, calItemId, defaultData,
                           false, isAppointment, showAll, legacyFormat);

        for (Iterator<InstanceData> iter = calItemData.instanceIterator(); iter.hasNext(); ) {
            InstanceData instance = iter.next();
            Element instElem = calItemElem.addElement(MailConstants.E_INSTANCE);
            encodeInstanceData(instElem, ifmt, calItemId, instance,
                               true, isAppointment, showAll, legacyFormat);
        }

        AlarmData alarmData = calItemData.getAlarm();
        if (alarmData != null)
            encodeAlarmData(calItemElem, alarmData, showAll);

        return calItemElem;
    }

    public static void encodeCalendarData(ZimbraSoapContext zsc, Element parent,
                                          Account acct, Account authAcct, CalendarData calData,
                                          boolean legacyFormat)
    throws ServiceException {
        for (Iterator<CalendarItemData> iter = calData.calendarItemIterator(); iter.hasNext(); ) {
            CalendarItemData calItemData = iter.next();
            if (calItemData.getNumInstances() > 0) {
                Element calItemElem = encodeCalendarItemData(zsc, acct, authAcct, calItemData, legacyFormat);
                parent.addElement(calItemElem);
            }
        }
    }
}
