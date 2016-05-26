<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.text.SimpleDateFormat,java.util.Date" %>
            </div>
        </div>
        <footer>Version 0.0.2 &copy;<%=new SimpleDateFormat("yyyy").format(new Date()) %>&nbsp;cFrost&nbsp;<a id="icp"
                href="http://www.miitbeian.gov.cn" target="_Blank">粤ICP备15054669号</a>
        </footer>
    </div>
    <script src="<%=request.getContextPath()%>/resources/scripts/lib/jquery-2.1.4.js"></script>
    <script src="<%=request.getContextPath()%>/resources/scripts/util.js"></script>
    <script src="<%=request.getContextPath()%>/resources/scripts/header.js"></script>
</body>
</html>