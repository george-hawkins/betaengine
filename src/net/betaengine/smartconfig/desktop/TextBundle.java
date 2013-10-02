/**
 * Copyright 2013 George C. Hawkins
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.betaengine.smartconfig.desktop;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class TextBundle
{
    private final static ResourceBundle BUNDLE = ResourceBundle.getBundle(TextBundle.class.getName());
    
    public final static String INTERFACE_WARNING = BUNDLE.getString("interface.warning");
    public final static String BAND_WARNING = BUNDLE.getString("band.warning");
    public final static String MTU_WARNING = BUNDLE.getString("mtu.warning");
    public final static String NO_NETWORK_NAME_ERROR = BUNDLE.getString("no.network.name.error");
    public final static String AES_KEY_LENGTH_ERROR = BUNDLE.getString("aes.key.length.error");
    public final static String BROADCASTING_MONITORING = BUNDLE.getString("broadcasting.monitoring");
    public final static String BROADCASTING = BUNDLE.getString("broadcasting");
    public final static String BROADCAST_SUCCESS = BUNDLE.getString("broadcast.success");
    public final static String BROADCAST_TIMEOUT = BUNDLE.getString("broadcast.timeout");
    public final static String BROADCAST_ERROR = BUNDLE.getString("broadcast.error");
    public final static String BROADCAST_UNDEFINED_ERROR = BUNDLE.getString("broadcast.undefined.error");
    public final static String NO_DEVICE_NAME_WARNING = BUNDLE.getString("no.device.name.warning");
    public final static String NO_DEVICE_NAME_WARNING_DISABLE = BUNDLE.getString("no.device.name.warning.disable");
    public final static String SEND = BUNDLE.getString("send");
    public final static String CANCEL = BUNDLE.getString("cancel");
    public final static String STOP = BUNDLE.getString("stop");
    public final static String NETWORK_NAME = BUNDLE.getString("network.name");
    public final static String PASSWORD = BUNDLE.getString("password");
    public final static String HIDE_PASSWORD = BUNDLE.getString("hide.password");
    public final static String GATEWAY = BUNDLE.getString("gateway");
    public final static String AES_KEY = BUNDLE.getString("aes.key");
    public final static String HIDE_AES_KEY = BUNDLE.getString("hide.aes.key");
    public final static String DEVICE_NAME = BUNDLE.getString("device.name");
    public final static String MAIN_TITLE = BUNDLE.getString("main.title");
    public final static String INFORMATION_TITLE = BUNDLE.getString("information.title");
    public final static String WARNING_TITLE = BUNDLE.getString("warning.title");
    public final static String ERROR_TITLE = BUNDLE.getString("error.title");
    public final static String BROADCASTING_TITLE = BUNDLE.getString("broadcasting.title");
    
    public static String format(String pattern, Object... arguments)
    {
        return MessageFormat.format(pattern, arguments);
    }
}
