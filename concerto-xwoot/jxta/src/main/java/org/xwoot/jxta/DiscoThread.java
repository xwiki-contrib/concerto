package org.xwoot.jxta;

import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;

/** 
 * Thread to perform the discovery process.
 * A separate thread is needed because we don't want to hold up
 * the main thread's UI & animation.  The JXTA remote discovery call
 * can take several seconds.
 */
public class DiscoThread extends Thread {

    DiscoveryService disco;
    int type;
    String targetPeerId;
    String attribute;
    String value;
    DiscoveryListener discoListener;

    public DiscoThread(DiscoveryService disco,
                       String targetPeerId,
                       int type,
                       String attribute,
                       String value,
                       DiscoveryListener discoListener) {
        this.disco = disco;
        this.type = type;
        this.discoListener = discoListener;

        if (targetPeerId != null)
            this.targetPeerId = new String(targetPeerId);
        if (attribute != null)
            this.attribute = new String(attribute);
        if (value != null)
            this.value = new String(value);
    }

    public void run() {
        disco.getRemoteAdvertisements(targetPeerId, type, attribute,
                                      value, Integer.MAX_VALUE, discoListener);
    }
}
