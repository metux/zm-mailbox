/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2011 Zimbra, Inc.
 *
 * The contents of this file are subject to the Zimbra Public License
 * Version 1.3 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.soap.admin.type;

import com.google.common.base.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {})
public class TZFixupRuleMatchDates {

    @XmlAttribute(name=AdminConstants.A_STDOFF /* stdoff */, required=true)
    private long stdOffset;

    @XmlAttribute(name=AdminConstants.A_DAYOFF /* dayoff */, required=true)
    private long dstOffset;

    @XmlElement(name=AdminConstants.E_STANDARD /* standard */, required=true)
    private TZFixupRuleMatchDate standard;

    @XmlElement(name=AdminConstants.E_DAYLIGHT /* daylight */, required=true)
    private TZFixupRuleMatchDate daylight;

    public TZFixupRuleMatchDates() {
    }

    public void setStdOffset(long stdOffset) { this.stdOffset = stdOffset; }
    public void setDstOffset(long dstOffset) { this.dstOffset = dstOffset; }
    public void setStandard(TZFixupRuleMatchDate standard) { this.standard = standard; }
    public void setDaylight(TZFixupRuleMatchDate daylight) { this.daylight = daylight; }
    public long getStdOffset() { return stdOffset; }
    public long getDstOffset() { return dstOffset; }
    public TZFixupRuleMatchDate getStandard() { return standard; }
    public TZFixupRuleMatchDate getDaylight() { return daylight; }

    public Objects.ToStringHelper addToStringInfo(
                Objects.ToStringHelper helper) {
        return helper
            .add("stdOffset", stdOffset)
            .add("dstOffset", dstOffset)
            .add("standard", standard)
            .add("daylight", daylight);
    }

    @Override
    public String toString() {
        return addToStringInfo(Objects.toStringHelper(this))
                .toString();
    }
}
