/*
 * ***** BEGIN LICENSE BLOCK ***** Zimbra Collaboration Suite Server Copyright
 * (C) 2018 Synacor, Inc.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>. *****
 * END LICENSE BLOCK *****
 */

package com.zimbra.cs.service.mail;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang.RandomStringUtils;

import com.zimbra.common.account.ForgetPasswordEnums.CodeConstants;
import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.L10nUtil;
import com.zimbra.common.util.L10nUtil.MsgKey;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.JWEUtil;
import com.zimbra.cs.util.AccountUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.RecoverAccountRequest;
import com.zimbra.soap.mail.message.RecoverAccountResponse;
import com.zimbra.soap.mail.type.PasswordResetOperation;

public final class RecoverAccount extends MailDocumentHandler {
    public static final String LOG_OPERATION = "RecoverAccount:";

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        OperationContext octxt = getOperationContext(zsc, context);

        RecoverAccountRequest req = zsc.elementToJaxb(request);
        PasswordResetOperation op = req.getOp();
        if (op == null) {
            ZimbraLog.account.debug("%s Invalid op received", LOG_OPERATION);
            throw ServiceException.INVALID_REQUEST("Invalid op received", null);
        }
        String email = req.getEmail();
        Account user = Provisioning.getInstance().getAccountByName(email);
        if (user == null) {
            ZimbraLog.account.debug("%s Account not found for %s", LOG_OPERATION, email);
            throw ServiceException.FAILURE("Something went wrong. Please contact your administrator.", null);
        }
        if (user.getPrefPasswordRecoveryAddressStatus() == null
                || !user.getPrefPasswordRecoveryAddressStatus().isVerified()) {
            ZimbraLog.account.debug("%s Verified recovery email is not found for %s", LOG_OPERATION, email);
            throw ServiceException.FAILURE("Something went wrong. Please contact your administrator.", null);
        }
        String recoveryEmail = user.getPrefPasswordRecoveryAddress();
        if (StringUtil.isNullOrEmpty(recoveryEmail)) {
            ZimbraLog.account.debug("%s Recovery email missing or unverified for %s", LOG_OPERATION, email);
            throw ServiceException.FAILURE("Something went wrong. Please contact your administrator.", null);
        }

        RecoverAccountResponse resp = new RecoverAccountResponse();
        switch (op) {
            case GET_RECOVERY_EMAIL:
                recoveryEmail = StringUtil.maskEmail(recoveryEmail);
                ZimbraLog.account.debug("%s Recovery email: %s", LOG_OPERATION, recoveryEmail);
                resp.setRecoveryEmail(recoveryEmail);
                break;
            case SEND_RECOVERY_CODE:
                String storedCodeString = user.getResetPasswordRecoveryCode();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss z")
                        .withZone(ZoneId.of("GMT"));
                ZonedDateTime current = ZonedDateTime.now(ZoneId.of("GMT"));
                Map<String, String> recoveryCodeMap = new HashMap<String, String>();
                if (!StringUtil.isNullOrEmpty(storedCodeString)) {
                    ZimbraLog.account.debug("%s Recovery code found for %s", LOG_OPERATION, email);
                    recoveryCodeMap = JWEUtil.getDecodedJWE(storedCodeString);
                    ZonedDateTime storedDate = ZonedDateTime
                            .parse(recoveryCodeMap.get(CodeConstants.EXPIRY_TIME.toString()), formatter);
                    if (ChronoUnit.MILLIS.between(current, storedDate) >= 0) {
                        ZimbraLog.account.debug(
                                "%s Recovery code expired, so generating new one and reseting resend count.",
                                LOG_OPERATION);
                        recoveryCodeMap.put(CodeConstants.CODE.toString(), RandomStringUtils.random(8, true, true));
                        recoveryCodeMap.put(CodeConstants.EXPIRY_TIME.toString(), current.format(formatter));
                        recoveryCodeMap.put(CodeConstants.RESEND_COUNT.toString(), String.valueOf(0));
                    } else {
                        int resendCount = Integer.valueOf(recoveryCodeMap.get(CodeConstants.RESEND_COUNT.toString()));
                        if (user.getPasswordRecoveryMaxAttempts() < resendCount) {
                            throw ServiceException.INVALID_REQUEST("Max resend attempts reached", null);
                        } else {
                            recoveryCodeMap.put(CodeConstants.RESEND_COUNT.toString(), String.valueOf(resendCount + 1));
                        }
                    }
                } else {
                    ZimbraLog.account.debug("%s Recovery code not found for %s, creating new one", LOG_OPERATION,
                            email);
                    recoveryCodeMap.put(CodeConstants.EMAIL.toString(), recoveryEmail);
                    recoveryCodeMap.put(CodeConstants.CODE.toString(), RandomStringUtils.random(8, true, true));
                    recoveryCodeMap.put(CodeConstants.EXPIRY_TIME.toString(), current.format(formatter));
                    recoveryCodeMap.put(CodeConstants.RESEND_COUNT.toString(), String.valueOf(0));
                }
                sendAndStoreForgetPasswordCode(zsc, octxt, user, recoveryCodeMap);
                break;
            default:
                throw ServiceException.INVALID_REQUEST("Invalid op received", null);
        }
        return zsc.jaxbToElement(resp);
    }

    private void sendAndStoreForgetPasswordCode(ZimbraSoapContext zsc, OperationContext octxt, Account user,
            Map<String, String> recoveryCodeMap) throws ServiceException {
        Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(user);
        Locale locale = user.getLocale();
        String displayName = user.getDisplayName();
        if (displayName == null) {
            displayName = user.getName();
        }
        String subject = L10nUtil.getMessage(MsgKey.sendPasswordRecoveryEmailSubject, locale, user.getDomainName());
        String charset = user.getAttr(Provisioning.A_zimbraPrefMailDefaultCharset, MimeConstants.P_CHARSET_UTF8);
        String mimePartText = L10nUtil.getMessage(MsgKey.sendPasswordRecoveryEmailBodyText, locale, displayName,
                recoveryCodeMap.get(CodeConstants.CODE.toString()),
                recoveryCodeMap.get(CodeConstants.EXPIRY_TIME.toString()));
        String mimePartHtml = L10nUtil.getMessage(MsgKey.sendPasswordRecoveryEmailBodyHtml, locale, displayName,
                recoveryCodeMap.get(CodeConstants.CODE.toString()),
                recoveryCodeMap.get(CodeConstants.EXPIRY_TIME.toString()));
        try {
            MimeMultipart mmp = AccountUtil.generateMimeMultipart(mimePartText, mimePartHtml, null);
            MimeMessage mm = AccountUtil.generateMimeMessage(user, user, subject, charset, null, null,
                    recoveryCodeMap.get(CodeConstants.EMAIL.toString()), mmp);
            mbox.getMailSender().sendMimeMessage(octxt, mbox, false, mm, null, null, null, null, false);
        } catch (MessagingException me) {
            ZimbraLog.account.debug("%s Error occured while sending recovery code in email to ", LOG_OPERATION,
                    recoveryCodeMap.get(CodeConstants.EMAIL.toString()));
            throw ServiceException.FAILURE("Error occured while sending recovery code in email to "
                    + recoveryCodeMap.get(CodeConstants.EMAIL.toString()), me);
        }
        ZimbraLog.account.debug("%s Recovery code sent in email to %s", LOG_OPERATION,
                StringUtil.maskEmail(recoveryCodeMap.get(CodeConstants.EMAIL.toString())));
        // store the same in ldap attribute for user
        HashMap<String, Object> prefs = new HashMap<String, Object>();
        prefs.put(Provisioning.A_zimbraResetPasswordRecoveryCode, JWEUtil.getJWE(recoveryCodeMap));
        Provisioning.getInstance().modifyAttrs(mbox.getAccount(), prefs, true, zsc.getAuthToken());
    }
}