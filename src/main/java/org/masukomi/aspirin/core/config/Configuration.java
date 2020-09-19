/*
 * Created on Jan 5, 2004
 *
 * Copyright (c) 2004 Katherine Rhodes (masukomi at masukomi dot org)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.masukomi.aspirin.core.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.masukomi.aspirin.core.AspirinInternal;
import org.masukomi.aspirin.core.store.mail.MailStore;
import org.masukomi.aspirin.core.store.mail.SimpleMailStore;
import org.masukomi.aspirin.core.store.queue.QueueStore;
import org.masukomi.aspirin.core.store.queue.SimpleQueueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.ParseException;
import java.util.*;


/**
 * <p>This class represents the configuration of Aspirin. You can configure this
 * software two ways:</p>
 *
 * <ol>
 *   <li>Get the configuration instance and set parameters.</li>
 *   <li>Get the instance and initialize with a Properties object.</li>
 * </ol>
 *
 * <p>There is a way to change behavior of Aspirin dynamically. You can use
 * JMX to change configuration parameters. In the parameters list we marked the
 * parameters which are applied immediately. For more information view
 * {@link ConfigurationMBean}.</p>
 *
 * <table border="1" summary="Parameters">
 *   <tr>
 *     <th>Name</th>
 *     <th>Type</th>
 *     <th>Description</th>
 *   </tr>
 *   <tr>
 *     <td>aspirin.delivery.attempt.delay</td>
 *     <td>Integer</td>
 *     <td>The delay of next attempt to delivery in milliseconds. <i>Change by
 *     JMX applied immediately.</i></td>
 *   </tr>
 *   <tr>
 *     <td>aspirin.delivery.attempt.count</td>
 *     <td>Integer</td>
 *     <td>Maximal number of delivery attempts of an email. <i>Change by JMX
 *     applied immediately.</i></td>
 *   </tr>
 *   <tr>
 *     <td>aspirin.delivery.bounce-on-failure</td>
 *     <td>Boolean</td>
 *     <td>If true, a bounce email will be send to postmaster on failure.
 *     <i>Change by JMX applied immediately.</i></td>
 *   </tr>
 *   <tr>
 *     <td>aspirin.delivery.debug</td>
 *     <td>Boolean</td>
 *     <td>If true, full SMTP communication will be logged. <i>Change by JMX
 *     applied immediately.</i></td>
 *   </tr>
 *   <tr>
 *   	<td>aspirin.delivery.expiry</td>
 *   	<td>Long</td>
 *   	<td>Time of sending expiry in milliseconds. The queue send an email
 *   	until current time = queueing time + expiry. Default value is -1, it
 *   	means forever (no expiration time). <i>Change by JMX applied
 *   	immediately.</i></td>
 *   </tr>
 *   <tr>
 *     <td>aspirin.delivery.threads.active.max</td>
 *     <td>Integer</td>
 *     <td>Maximum number of active delivery threads in the pool. <i>Change by
 *     JMX applied immediately.</i></td>
 *   </tr>
 *   <tr>
 *     <td>aspirin.delivery.threads.idle.max</td>
 *     <td>Integer</td>
 *     <td>Maximum number of idle delivery threads in the pool (the delivery
 *     threads over this limit will be shutdown). <i>Change by JMX applied
 *     immediately.</i></td>
 *   </tr>
 *   <tr>
 *     <td>aspirin.delivery.timeout</td>
 *     <td>Integer</td>
 *     <td>Socket and {@link Transport} timeout in milliseconds. <i>Change by
 *     JMX applied immediately.</i></td>
 *   </tr>
 *   <tr>
 *     <td>aspirin.encoding</td>
 *     <td>String</td>
 *     <td>The MIME encoding. <i>Change by JMX applied immediately.</i></td>
 *   </tr>
 *   <tr>
 *     <td>aspirin.hostname</td>
 *     <td>String</td>
 *     <td>The hostname. <i>Change by JMX applied immediately.</i></td>
 *   </tr>
 *   <tr>
 *     <td>aspirin.logger.name</td>
 *     <td>String</td>
 *     <td>
 *       The name of the logger. <i>Change by JMX applied immediately.</i>
 *       <br>
 *       <strong>WARNING! Changing logger name cause replacing of logger.</strong>
 *     </td>
 *   </tr>
 *   <tr>
 *     <td>aspirin.logger.prefix</td>
 *     <td>String</td>
 *     <td>The prefix of the logger. This will be put in the logs at the first
 *     position. <i>Change by JMX applied immediately.</i></td>
 *   </tr>
 *   <tr>
 *     <td>aspirin.postmaster.email</td>
 *     <td>String</td>
 *     <td>The email address of the postmaster. <i>Change by JMX applied
 *     immediately.</i></td>
 *   </tr>
 *   <tr>
 *   	<td>aspirin.mailstore.class</td>
 *   	<td>String</td>
 *   	<td>The class name of mail store. Default class is SimpleMailStore in
 *   	org.masukomi.aspirin.core.store package.</td>
 *   </tr>
 *   <tr>
 *   	<td>aspirin.queuestore.class</td>
 *   	<td>String</td>
 *   	<td>The class name of queue store. Default class is SimpleQueueStore in
 *   	org.masukomi.aspirin.core.queue package.</td>
 *   </tr>
 * </table>
 *
 * @author Kate Rhodes masukomi at masukomi dot org
 * @author Laszlo Solova
 */
public class Configuration implements ConfigurationMBean {
    private static final String MAIL_MIME_CHARSET = "mail.mime.charset";
    private static final String MAIL_SMTP_CONNECTIONTIMEOUT = "mail.smtp.connectiontimeout";
    private static final String MAIL_SMTP_HOST = "mail.smtp.host";
    private static final String MAIL_SMTP_LOCALHOST = "mail.smtp.localhost";
    private static final String MAIL_SMTP_TIMEOUT = "mail.smtp.timeout";
    @Nullable
    private static Configuration instance;
    @Nullable
    private static Logger log; // inherited from aspirin.logger.name
    @NotNull
    private final Map<String, Object> configParameters = new HashMap<>();
    @NotNull
    private final Object listenerLock = new Object();
    @Nullable
    protected InternetAddress postmaster; // inherited from aspirin.postmaster.email
    @Nullable
    private MailStore mailStore;
    @Nullable
    private QueueStore queueStore;
    @Nullable
    private Session mailSession;
    @Nullable
    private List<ConfigurationChangeListener> listeners;

    Configuration() {
        init(new Properties());
    }

    public static synchronized Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    public void init(@NotNull Properties props) {
        Objects.requireNonNull(props, "props");
        Collection<Parameter> parameterList = new ArrayList<>(15);
        parameterList.add(new Parameter(PARAM_DELIVERY_ATTEMPT_COUNT, 3, Parameter.TYPE_INTEGER));
        parameterList.add(new Parameter(PARAM_DELIVERY_ATTEMPT_DELAY, 300000, Parameter.TYPE_INTEGER));
        parameterList.add(new Parameter(PARAM_DELIVERY_BOUNCE_ON_FAILURE, true, Parameter.TYPE_BOOLEAN));
        parameterList.add(new Parameter(PARAM_DELIVERY_DEBUG, false, Parameter.TYPE_BOOLEAN));
        parameterList.add(new Parameter(PARAM_DELIVERY_EXPIRY, -1L, Parameter.TYPE_LONG));
        parameterList.add(new Parameter(PARAM_DELIVERY_THREADS_ACTIVE_MAX, 3, Parameter.TYPE_INTEGER));
        parameterList.add(new Parameter(PARAM_DELIVERY_THREADS_IDLE_MAX, 3, Parameter.TYPE_INTEGER));
        parameterList.add(new Parameter(PARAM_DELIVERY_TIMEOUT, 30000, Parameter.TYPE_INTEGER));
        parameterList.add(new Parameter(PARAM_ENCODING, "UTF-8", Parameter.TYPE_STRING));
        parameterList.add(new Parameter(PARAM_HOSTNAME, "localhost", Parameter.TYPE_STRING));
        parameterList.add(new Parameter(PARAM_LOGGER_NAME, "Aspirin", Parameter.TYPE_STRING));
        parameterList.add(new Parameter(PARAM_LOGGER_PREFIX, "Aspirin ", Parameter.TYPE_STRING));
        parameterList.add(new Parameter(PARAM_MAILSTORE_CLASS, SimpleMailStore.class.getCanonicalName(), Parameter.TYPE_STRING));
        parameterList.add(new Parameter(PARAM_POSTMASTER_EMAIL, null, Parameter.TYPE_STRING));
        parameterList.add(new Parameter(PARAM_QUEUESTORE_CLASS, SimpleQueueStore.class.getCanonicalName(), Parameter.TYPE_STRING));

        parameterList.forEach(param -> {
            Object o = param.extractValue(props);
            if (o != null) configParameters.put(param.getName(), o);
        });

        log = LoggerFactory.getLogger((String) configParameters.get(PARAM_LOGGER_NAME));
        setPostmasterEmail((String) configParameters.get(PARAM_POSTMASTER_EMAIL));
        updateMailSession();
    }

    /**
     * @return The email address of the postmaster in a MailAddress object.
     */
    @Nullable
    public InternetAddress getPostmaster() {
        return postmaster;
    }

    @Nullable
    public String getHostname() {
        return (String) configParameters.get(PARAM_HOSTNAME);
    }

    public void setHostname(@Nullable String hostname) {
        configParameters.put(PARAM_HOSTNAME, hostname);
        updateMailSession();
        notifyListeners(PARAM_HOSTNAME);
    }

    @Nullable
    public String getEncoding() {
        return (String) configParameters.get(PARAM_ENCODING);
    }

    public void setEncoding(@Nullable String encoding) {
        configParameters.put(PARAM_ENCODING, encoding);
//		this.encoding = encoding;
        updateMailSession();
        notifyListeners(PARAM_ENCODING);
    }

    @Override
    public int getDeliveryAttemptCount() {
        return (Integer) configParameters.get(PARAM_DELIVERY_ATTEMPT_COUNT);
//		return maxAttempts;
    }

    @Override
    public void setDeliveryAttemptCount(int attemptCount) {
        configParameters.put(PARAM_DELIVERY_ATTEMPT_COUNT, attemptCount);
//		this.maxAttempts = attemptCount;
        notifyListeners(PARAM_DELIVERY_ATTEMPT_COUNT);
    }

    @Override
    public int getDeliveryAttemptDelay() {
        return (Integer) configParameters.get(PARAM_DELIVERY_ATTEMPT_DELAY);
//		return (int)retryInterval;
    }

    @Override
    public void setDeliveryAttemptDelay(int delay) {
        configParameters.put(PARAM_DELIVERY_ATTEMPT_DELAY, delay);
//		this.retryInterval = delay;
        notifyListeners(PARAM_DELIVERY_ATTEMPT_DELAY);
    }

    @Override
    public int getDeliveryThreadsActiveMax() {
        return (Integer) configParameters.get(PARAM_DELIVERY_THREADS_ACTIVE_MAX);
    }

    @Override
    public void setDeliveryThreadsActiveMax(int activeThreadsMax) {
        configParameters.put(PARAM_DELIVERY_THREADS_ACTIVE_MAX, activeThreadsMax);
        notifyListeners(PARAM_DELIVERY_THREADS_ACTIVE_MAX);
    }

    @Override
    public int getDeliveryThreadsIdleMax() {
        return (Integer) configParameters.get(PARAM_DELIVERY_THREADS_IDLE_MAX);
    }

    @Override
    public void setDeliveryThreadsIdleMax(int idleThreadsMax) {
        configParameters.put(PARAM_DELIVERY_THREADS_IDLE_MAX, idleThreadsMax);
        notifyListeners(PARAM_DELIVERY_THREADS_IDLE_MAX);
    }

    @Override
    public int getDeliveryTimeout() {
        return (Integer) configParameters.get(PARAM_DELIVERY_TIMEOUT);
    }

    @Override
    public void setDeliveryTimeout(int timeout) {
        configParameters.put(PARAM_DELIVERY_TIMEOUT, timeout);
//		this.connectionTimeout = timeout;
        updateMailSession();
        notifyListeners(PARAM_DELIVERY_TIMEOUT);
    }

    @Override
    public long getExpiry() {
        return (Long) configParameters.get(PARAM_DELIVERY_EXPIRY);
    }

    @Override
    public void setExpiry(long expiry) {
        configParameters.put(PARAM_DELIVERY_EXPIRY, expiry);
        notifyListeners(PARAM_DELIVERY_EXPIRY);
    }

    @Override
    @Nullable
    public String getLoggerName() {
        return (String) configParameters.get(PARAM_LOGGER_NAME);
    }

    @Override
    public void setLoggerName(@Nullable String loggerName) {
        configParameters.put(PARAM_LOGGER_NAME, loggerName);
//		Configuration.loggerName = loggerName;
        log = LoggerFactory.getLogger(loggerName);
        notifyListeners(PARAM_LOGGER_NAME);
    }

    @Override
    @Nullable
    public String getLoggerPrefix() {
        return (String) configParameters.get(PARAM_LOGGER_PREFIX);
    }

    @Override
    public void setLoggerPrefix(@Nullable String loggerPrefix) {
        configParameters.put(PARAM_LOGGER_PREFIX, loggerPrefix);
//		this.loggerPrefix = loggerPrefix;
        notifyListeners(PARAM_LOGGER_PREFIX);
    }

    @NotNull
    public MailStore getMailStore() {
        if (mailStore == null) {
            String mailStoreClassName = (String) configParameters.get(PARAM_MAILSTORE_CLASS);
            try {
                Class<?> storeClass = Class.forName(mailStoreClassName);
                if (storeClass.getInterfaces()[0].equals(MailStore.class))
                    mailStore = (MailStore) storeClass.getConstructor().newInstance();
            } catch (Exception e) {
                log.error(getClass().getSimpleName() + " Mail store class could not be instantiated. Class=" + mailStoreClassName, e);
                mailStore = new SimpleMailStore();
            }
        }
        return mailStore;
    }

    public void setMailStore(@Nullable MailStore mailStore) {
        this.mailStore = mailStore;
        notifyListeners(PARAM_MAILSTORE_CLASS);
    }

    @Override
    @NotNull
    public String getPostmasterEmail() {
        return String.valueOf(postmaster);
    }

    @Override
    public void setPostmasterEmail(@Nullable String emailAddress) {
        if (emailAddress == null) {
            postmaster = null;
            return;
        }
        try {
            postmaster = new InternetAddress(emailAddress);
            notifyListeners(PARAM_POSTMASTER_EMAIL);
        } catch (ParseException e) {
            log.error(getClass().getSimpleName() + ".setPostmasterEmail(): The email address is unparseable.", e);
        }
    }

    @NotNull
    public QueueStore getQueueStore() {
        if (queueStore == null) {
            String queueStoreClassName = (String) configParameters.get(PARAM_QUEUESTORE_CLASS);
            try {
                Class<?> storeClass = Class.forName(queueStoreClassName);
                if (storeClass.getInterfaces()[0].equals(QueueStore.class))
                    queueStore = (QueueStore) storeClass.getConstructor().newInstance();
            } catch (Exception e) {
                log.error(getClass().getSimpleName() + " Queue store class could not be instantiated. Class=" + queueStoreClassName, e);
                queueStore = new SimpleQueueStore();
            }
        }
        return queueStore;
    }

    public void setQueueStore(@Nullable QueueStore queueStore) {
        this.queueStore = queueStore;
        notifyListeners(PARAM_QUEUESTORE_CLASS);
    }

    @Override
    public boolean isDeliveryBounceOnFailure() {
        return (Boolean) configParameters.get(PARAM_DELIVERY_BOUNCE_ON_FAILURE);
    }

    @Override
    public void setDeliveryBounceOnFailure(boolean bounce) {
        configParameters.put(PARAM_DELIVERY_BOUNCE_ON_FAILURE, bounce);
        notifyListeners(PARAM_DELIVERY_BOUNCE_ON_FAILURE);
    }

    @Override
    public boolean isDeliveryDebug() {
        return (Boolean) configParameters.get(PARAM_DELIVERY_DEBUG);
    }

    @Override
    public void setDeliveryDebug(boolean debug) {
        configParameters.put(PARAM_DELIVERY_DEBUG, debug);
        updateMailSession();
        notifyListeners(PARAM_DELIVERY_DEBUG);
    }

    public void addListener(@Nullable ConfigurationChangeListener listener) {
        if (listeners == null)
            listeners = new ArrayList<>();
        synchronized (listenerLock) {
            listeners.add(listener);
        }
    }

    public void removeListener(@Nullable ConfigurationChangeListener listener) {
        if (listeners != null) {
            synchronized (listenerLock) {
                listeners.remove(listener);
            }
        }
    }

    private void notifyListeners(@NotNull String changedParameterName) {
        Objects.requireNonNull(changedParameterName, "changedParameterName");

        if (listeners != null && !listeners.isEmpty()) {
            if (log.isInfoEnabled())
                log.info(getClass().getSimpleName() + ".notifyListeners(): Configuration parameter '" + changedParameterName + "' changed.");

            synchronized (listenerLock) {
                listeners.forEach(listener -> listener.configChanged(changedParameterName));
            }
        }
    }

    @Nullable
    @Override
    public String getMailStoreClassName() {
        return (String) configParameters.get(PARAM_MAILSTORE_CLASS);
    }

    @Override
    public void setMailStoreClassName(@Nullable String className) {
        configParameters.put(PARAM_MAILSTORE_CLASS, className);
        mailStore = null;
        notifyListeners(PARAM_MAILSTORE_CLASS);
//		this.mailStoreClassName = className;
    }

    @Nullable
    @Override
    public String getQueueStoreClassName() {
        return (String) configParameters.get(PARAM_QUEUESTORE_CLASS);
    }

    @Override
    public void setQueueStoreClassName(@Nullable String className) {
        configParameters.put(PARAM_QUEUESTORE_CLASS, className);
        queueStore = null;
        notifyListeners(PARAM_QUEUESTORE_CLASS);
//		this.queueStoreClassName = className;
    }

    @NotNull
    public Logger getLogger() {
        return LoggerFactory.getLogger((String) configParameters.get(PARAM_LOGGER_PREFIX));
    }

    @NotNull
    public Session getMailSession() {
        return Session.getInstance(mailSession.getProperties());
    }

    @Nullable
    public Object getProperty(@NotNull String name) {
        Objects.requireNonNull(name, "name");
        return configParameters.get(name);
    }

    public void setProperty(String name, Object value) {
        configParameters.put(name, value);
    }

    private void updateMailSession() {
        // Set up default session
        Properties mailSessionProps = System.getProperties();
        mailSessionProps.setProperty(MAIL_SMTP_HOST, getHostname()); //The SMTP server to connect to.
        mailSessionProps.setProperty(MAIL_SMTP_LOCALHOST, getHostname()); //Local host name. Defaults to InetAddress.getLocalHost().getHostName(). Should not normally need to be set if your JDK and your name service are configured properly.
        mailSessionProps.setProperty(MAIL_MIME_CHARSET, getEncoding()); //The mail.mime.charset System property can be used to specify the default MIME charset to use for encoded words and text parts that don't otherwise specify a charset. Normally, the default MIME charset is derived from the default Java charset, as specified in the file.encoding System property. Most applications will have no need to explicitly set the default MIME charset. In cases where the default MIME charset to be used for mail messages is different than the charset used for files stored on the system, this property should be set.
        mailSessionProps.setProperty(MAIL_SMTP_CONNECTIONTIMEOUT, String.valueOf(getDeliveryTimeout())); //Socket connection timeout value in milliseconds. Default is infinite timeout.
        mailSessionProps.setProperty(MAIL_SMTP_TIMEOUT, String.valueOf(getDeliveryTimeout())); //Socket I/O timeout value in milliseconds. Default is infinite timeout.
        Session newSession = Session.getInstance(mailSessionProps);

        // Set communication debug
        if ((AspirinInternal.getLogger().isDebugEnabled()) && isDeliveryDebug())
            newSession.setDebug(true);

        mailSession = newSession;
    }

    private static final class Parameter {
        public static final int TYPE_STRING = 0;
        public static final int TYPE_INTEGER = 1;
        public static final int TYPE_LONG = 2;
        public static final int TYPE_BOOLEAN = 3;
        @NotNull
        private final String name;
        private final int type;
        @Nullable
        private final Object defaultValue;

        Parameter(@NotNull String name, @Nullable Object defaultValue, int type) {
            this.name = Objects.requireNonNull(name, "name");
            this.defaultValue = defaultValue;
            this.type = type;
        }

        @NotNull
        public String getName() {
            return name;
        }

        @Nullable
        Object extractValue(@NotNull Properties props) {
            Objects.requireNonNull(props, "props");
            String tempString = props.getProperty(name);

            if (tempString == null)
                tempString = System.getProperty(name);

            if (tempString != null) {
                switch (type) {
                    case TYPE_INTEGER:
                        return Integer.valueOf(tempString);
                    case TYPE_LONG:
                        return Long.valueOf(tempString);
                    case TYPE_BOOLEAN:
                        return ("true".equalsIgnoreCase(tempString)) ? Boolean.TRUE : Boolean.FALSE;
                    default:
                        return tempString;
                }
            }

            return defaultValue;
        }
    }
}
