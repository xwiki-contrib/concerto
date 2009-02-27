<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="header.jsp" %>
  <div id="boxes">
    <div id="box-right" class="box"  style="width:780px;">
      <div id="box_title">-- Network bootstrap --</div>
      <div class="menu_margin">
      <form id="button" method="post" action="">
          <div id="create_network" style="width=500px;">
          	Create network<br/>
            <input type="submit" value="Create" name="networkChoice"/>
          </div>
          <div id="join_network" style="width=500px;">
          	Join network<br/>
          	<input type="radio" name="useNetwork" value="concerto" /> Use default XWiki Concerto network. (Recommended)<br/>
          	<input type="radio" name="useNetwork" value="publicJxta" /> Use JXTA public network (Not recommended. Use only for tests.)<br/>
          	<input type="radio" name="useNetwork" value="custom" /> Use a custom network.<br/>
          	<div id="join_custom_network">
	          	RDV SeedingUri: <input type="text" name="rdvSeedingUri" size="50" /><br/>
	          	Relay SeedingUri: <input type="text" name="relaySeedingUri" size="50" /><br/>
	          	RDV Seeds: <input type="text" name="rdvSeeds" size="50" />(ex: tcp://192.168.1.200:9701, http://192.18.37.36:9700, etc...)<br/>
	          	Relay Seeds: <input type="text" name="relaySeeds" size="50" />(ex: tcp://192.168.1.200:9701, http://192.18.37.36:9700, etc...)<br/>
	            
	            <input type="checkbox" name="beRendezVous" /> Route communication for this network<br/>
            	<input type="checkbox" name="beRelay" /> Relay communication for Firewalled/NAT-ed peers in this network.<br/>
	            
				<input type="submit" value="Join" name="networkChoice"/>
			</div>
			<div id="common_settings">
				Common(Advanced) settings<br/>
				<input type="checkbox" name="useExternalIp"/> Use external IP<br/>
				<div id="extarnal_ip_settings">
					External IP: <input type="text" name="externalIp"/> <br/>
					<input type="checkbox" name="useOnlyExternalIp"/> Use only external IP for all communication<br/>
    			</div>
    			
    			<input type="checkbox" name="useTcp"/> Use TCP<br/>
				<div id="Tcp_settings">
					TCP port: <input type="text" name="tcpPort" value="9701" /> (Default is 9701)
    			</div>
    			
    			<input type="checkbox" name="useHttp"/> Use HTTP<br/>
				<div id="Http_settings">
					Http port: <input type="text" name="httpPort" value="9700" />  (Default is 9700. Recommended is 80)
    			</div>
    			
    			<input type="checkbox" name="useMulticast"/> Use Multicast to discover network members in LAN.<br/>
			</div>
          </div>
        </form>        
      </div>
    </div>
    <div class="clearer"></div>
  </div>
<%@ include file="footer.jsp" %>
