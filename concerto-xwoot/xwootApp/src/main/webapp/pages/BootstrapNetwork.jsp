<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="header.jsp" %>
  <div id="boxes">
    <div id="box-right" class="box"  style="width:780px;">
      <div id="box_title">-- Network bootstrap --</div>
      <div class="menu_margin">
        <form id="button" method="post" action="">
          <div>
            <input type="text" name="neighbor" size="50"/>
            <input type="submit" value="Join" name="choice_join"/>
            or
            <input type="submit" value="Create" name="choice_create"/>
          </div>
        </form>
      </div>
    </div>
    <div class="clearer"></div>
  </div>
<%@ include file="footer.jsp" %>
