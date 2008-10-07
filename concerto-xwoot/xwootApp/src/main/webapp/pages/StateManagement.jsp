<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="header.jsp" %>
  <div id="boxes">
    <div id="box-right" class="box"  style="width:780px;">
      <div id="box_title">-- State Management --</div>
      <div class="menu_margin">
        <form action="" enctype="multipart/form-data" method="post">
          <div>
            Import State file:
            <input type="file" name="statefile" size="20"/>
            <input type="submit" value="Import" name="action_upload"/>
            or
            <c:if test="${neighbor eq null}">
              <input type="submit" value="Compute new state" name="action_create"/>
            </c:if>
            <c:if test="${neighbor ne null}">
              <inout type="hidden" name="neighbor" value="${neighbor}"/>
              <input type="submit" value="Ask state to neighbor" name="action_retrieve"/>
            </c:if>
            <c:if test="${hasState eq true}">
              <c:if test="${currentState ne null}">
                <br/> == Current state : ${currentState} ==
              </c:if>
            </c:if>
          </div>
        </form>
      </div>
    </div>
    <div class="clearer"></div>
  </div>
<%@ include file="footer.jsp" %>
