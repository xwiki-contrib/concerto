<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="header.jsp" %>
  <div id="boxes">
    <div id="box-unic" class="box"  style="width:780px;">
      <div id="box_title">-- Group bootstrap --</div>
      <div class="menu_margin">
        <form id="button" method="post" action="">
          <div id="create_group" style="border-bottom: 3px solid darkgray; padding-bottom: 10px; margin-bottom: 10px">
          	<b>Create group</b><br/><br/>
            Name: <input type="text" name="groupName" size="50" /><br/>
            Description: <input type="text" name="groupDescription" size="50" /><br/>
            <input type="checkbox" name="isPrivateGroup" value="true" /> This is a private group.<br/>
            <div id="group_password">
            	Password: <input type="password" name="createGroupPassword" /><br/>
            	Retype Password: <input type="password" name="createGroupPasswordRetyped" /><br/>
            	Local Keystore Password: <input type="password" name="createKeystorePassword" /><br/>
            </div>
			<br/>
            <input type="submit" value="Create" name="groupChoice"/>
          </div>
          <div id="join_group" style="width=500px;">
         	<b>Join group</b><br/><br/>
         	<select name="groupID">
            	<c:forEach items="${groups}" var="group">
            	<option value="${group.peerGroupID}"><c:out value="${group.name}"></c:out></option>
            	</c:forEach>
            </select><br/>
            Password: <input type="password" name="joinGroupPassword" /><br/>
            Local Keystore Password: <input type="password" name="joinGroupKeystorePassword" /><br/>
            <input type="checkbox" name="beRendezVous" value="true" /> Route communication for this group<br/>
			<br/>
			<input type="submit" value="Refresh List" name="groupChoice"/>
			<input type="submit" value="Join" name="groupChoice"/>
          </div>
        </form>
      </div>
    </div>
    <div class="clearer"></div>
  </div>
<%@ include file="footer.jsp" %>