<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="header.jsp" %>
  <div id="boxes">
    <div id="box-unic" class="box"  style="width:780px;">
      <div id="box_title">-- Network bootstrap --</div>
      <div class="menu_margin">
      <form id="button" method="post" action="">
          <div id="create_network" style="border-bottom: 3px solid darkgray; padding-bottom: 10px; margin-bottom: 10px">
          	<b>Create network</b><br/><br/>
            <input type="submit" value="Create" name="networkChoice"/>
          </div>
          <div id="join_network">
          	<b>Join network</b><br/><br/>
          	<input type="radio" name="useNetwork" value="concerto" ${useConcertoNetwork} /> Use default XWiki Concerto network. (Recommended)<br/>
          	<input type="radio" name="useNetwork" value="publicJxta" ${usePublicJxtaNetwork} /> Use JXTA public network (Not recommended. Use only for tests.)<br/>
          	<input type="radio" name="useNetwork" value="custom" ${useCustomNetwork} /> Use a custom network.<br/>
          	<div id="join_custom_network" style="padding-left:40px">
	          	RDV SeedingUri: <input type="text" name="rdvSeedingUri" size="50" value="${rdvSeedingUri}" /><br/>
	          	Relay SeedingUri: <input type="text" name="relaySeedingUri" size="50" value="${relaySeedingUri}" /><br/>
	          	RDV Seeds: <input type="text" name="rdvSeeds" size="50" value="${rdvSeeds}" />(ex: tcp://192.168.1.200:9701, http://192.18.37.36:9700, etc...)<br/>
	          	Relay Seeds: <input type="text" name="relaySeeds" size="50" value="${relaySeeds}" />(ex: tcp://192.168.1.200:9701, http://192.18.37.36:9700, etc...)<br/>
	            
	            <input type="checkbox" name="beRendezVous" value="true" ${beRendezVous} /> Route communication for this network (WARNING: can lead to network island if no other seeds are specified or not accessible for a long time!)<br/>
            	<input type="checkbox" name="beRelay" value="true" ${beRelay} /> Relay communication for Firewalled/NAT-ed peers in this network.<br/>
			</div>
			
			<br/>
			<input type="submit" value="Join" name="networkChoice"/>
			
			<div id="common_settings" style="border-top: 1px solid darkgray; padding-top: 10px; margin-top: 10px">
				<b>Common(Advanced) settings</b><br/><br/>
				<input type="checkbox" name="useExternalIp" value="true" ${useExternalIp} /> Use external IP<br/>
				<div id="extarnal_ip_settings">
					External IP: <input type="text" name="externalIp" value="${externalIp}" /> <br/>
					<input type="checkbox" name="useOnlyExternalIp" value="true" ${useOnlyExternalIp} /> Use only external IP for all communication<br/>
    			</div>
    			
    			<input type="checkbox" name="useTcp" value="true" ${useTcp} /> Use TCP<br/>
				<div id="Tcp_settings">
					TCP port: <input type="text" name="tcpPort" value="${tcpPort}" /> (Default is 9701) <br/>
					<input type="checkbox" name="useMulticast" value="true" ${useMulticast} /> Use TCP Multicast to discover and communicate with network members in LAN.<br/>
    			</div>
    			
    			<input type="checkbox" name="useHttp" value="true" ${useHttp} /> Use HTTP<br/>
				<div id="Http_settings">
					Http port: <input type="text" name="httpPort" value="${httpPort}" />  (Default is 9700. Recommended are 80 or 8080)
    			</div>
    			
			</div>
          </div>
        </form>        
      </div>
    </div>
    <div class="clearer"></div>
  </div>
<%@ include file="footer.jsp" %>
