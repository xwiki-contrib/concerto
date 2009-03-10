<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="header.jsp" %>
  <div id="boxes">
    <div id="box-right" class="box"  style="width:780px;">
      <div id="box_title">-- Group bootstrap --</div>
      <div class="menu_margin">
        <form id="button" method="post" action="">
          <div id="create_group" style="width=500px;">
          	Create group<br/>
            Name: <input type="text" name="groupName" size="50" /><br/>
            Description: <input type="text" name="groupDescription" size="50" /><br/>
            <input type="checkbox" name="privateGroup" /> This is a private group.<br/>
            <div id="group_password">
            	Password: <input type="password" name="createGroupPassword" /><br/>
            	Retype Password: <input type="password" name="createGroupPasswordRetyped" /><br/>
            	Local Keystore Password: <input type="password" name="createKeystorePassword" /><br/>
            </div>
            <input type="submit" value="Create" name="groupChoice"/>
          </div>
          <div id="join_group" style="width=500px;">
         	Join group<br/>
         	<select name="groupID">
            	<c:forEach items="${groups}" var="group">
            	<option value="${group.peerGroupID}"><c:out value="${group.name}"></c:out></option>
            	</c:forEach>
            </select><br/>
            Password: <input type="password" name="joinGroupPassword" /><br/>
            Local Keystore Password: <input type="password" name="joinGroupKeystorePassword" /><br/>
            <input type="checkbox" name="beRendezVous" /> Route communication for this group<br/>
			<input type="submit" value="Refresh List" name="groupChoice"/>
			<input type="submit" value="Join" name="groupChoice"/>
          </div>
        </form>
      </div>
    </div>
    <div class="clearer"></div>
  </div>
<%@ include file="footer.jsp" %>