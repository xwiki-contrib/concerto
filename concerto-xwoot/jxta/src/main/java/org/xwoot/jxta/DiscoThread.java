package org.xwoot.jxta;

import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;

/**
 * Thread to perform the discovery process. A separate thread is needed because we don't want to hold up the main
 * thread's UI & animation. The JXTA remote discovery call can take several seconds.
 * 
 * @version $Id:$
 */
class DiscoThread extends Thread
{

    /** The discovery service of the group in which the discovery will be made. */
    private DiscoveryService disco;

    /** Advertisement type to look for. */
    private int type;

    /** The peer to get advertisements from. */
    private String targetPeerId;

    /** An attribute name by which to filter the search. */
    private String attribute;

    /** A value of the attribute to filter by. */
    private String value;

    /** Who to notify with the result. */
    private DiscoveryListener discoListener;

    /**
     * Constructor.
     * 
     * @param disco The discovery service of the group in which the discovery will be made.
     * @param targetPeerId The RDV peer to get advertisements from. If {@code null}, all RDV peers in the group will be
     *            queried.
     * @param type Advertisement type to look for.
     * @param attribute An attribute name by which to filter the search.
     * @param value A value of the attribute to filter by.
     * @param discoListener Who to notify with the result.
     */
    public DiscoThread(DiscoveryService disco, String targetPeerId, int type, String attribute, String value,
        DiscoveryListener discoListener)
    {
        this.disco = disco;
        this.type = type;
        this.discoListener = discoListener;

        if (targetPeerId != null) {
            this.targetPeerId = new String(targetPeerId);
        }
        if (attribute != null) {
            this.attribute = new String(attribute);
        }
        if (value != null) {
            this.value = new String(value);
        }
    }

    /** {@inheritDoc} */
    public void run()
    {
        disco.getRemoteAdvertisements(targetPeerId, type, attribute, value, Integer.MAX_VALUE, discoListener);
    }
}
