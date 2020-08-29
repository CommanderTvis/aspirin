package org.masukomi.aspirin.core.store.mail;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.mail.internet.MimeMessage;
import java.util.*;

/**
 * This store implementation has a simple hashmap to
 * store all MimeMessage objects. Please, be careful:
 * if you has a lot of objects in memory it could cause
 * OutOfMemoryError.
 *
 * @author Laszlo Solova
 */
public class SimpleMailStore implements MailStore {
    @NotNull
    private final Map<String, MimeMessage> messageMap = new HashMap<>();

    @Override
    @Nullable
    public MimeMessage get(@NotNull String mailid) {
        return messageMap.get(Objects.requireNonNull(mailid, "mailid"));
    }

    @Override
    @NotNull
    public List<String> getMailIds() {
        return new ArrayList<>(messageMap.keySet());
    }

    @Override
    public void init() {
        // Do nothing
    }

    @Override
    public void remove(@Nullable String mailid) {
        messageMap.remove(mailid);
    }

    @Override
    public void set(@NotNull String mailid, @Nullable MimeMessage msg) {
        messageMap.put(Objects.requireNonNull(mailid, "mailid"), msg);
    }
}
