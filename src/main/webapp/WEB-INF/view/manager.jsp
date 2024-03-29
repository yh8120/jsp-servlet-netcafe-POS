<%@ page pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/bootstrap.min.css" rel="stylesheet">
<link href="css/style.css" rel="stylesheet">
<title>入室管理</title>
</head>
<body class="pb-5">
    <nav class="navbar navbar-expand-sm navbar-dark bg-secondary mb-2">
        <div class="container">
            <a class="navbar-brand me-5" href="manager">
                <img src="images/posh.jpg" alt="" width="72" />
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNavDropdown"
                aria-controls="navbarNavDropdown" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNavDropdown">
                <ul class="navbar-nav">
                    <li class="nav-item text-end"><a class="nav-link active" aria-current="page" href="manager">店舗管理</a></li>
                    <li class="nav-item text-end"><a class="nav-link" href="addCustomer">会員登録</a></li>
                    <li class="nav-item text-end"><a class="nav-link" href="#">売上集計</a></li>
                    <li class="nav-item dropdown text-end"><a class="nav-link dropdown-toggle" href="#"
                            id="navbarDropdownMenuLink" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                            店舗設定</a>
                        <ul class="dropdown-menu dropdown-menu-dark" aria-labelledby="navbarDropdownMenuLink">
                            <li><a class="dropdown-item text-center" href="listRoom">ルーム管理</a></li>
                            <li><a class="dropdown-item text-center" href="listUser">ユーザー管理</a></li>
                            <li><a class="dropdown-item text-center" href="listPricePlan">料金管理</a></li>
                            <li><a class="dropdown-item text-center" href="listProduct">商品管理</a></li>
                            <li><a class="dropdown-item text-center" href="listReceipt">レシート表示</a></li>
                            <li><a id="logout-button" class="dropdown-item text-center" href="logout">ログアウト</a></li>
                        </ul></li>
                </ul>
            </div>
        </div>
    </nav>
    <div class="container">
        <div class="row">
            <div class="col text-center bg-light pt-1 fw-bold">
            <ul class="navbar-nav">
                <li><span class="me-3"><c:out value="${user.shopName}" /></span><span>担当：<c:out value="${user.userName}" /></span></li>
                <li><span id="degitalClockSample1"></span></li>
            </ul>
            </div>
        </div>


        <div class="row">
            <div class="accordion accordion-flush" id="accordionFlushExample">

                <c:forEach items="${roomList}" var="room">

                    <c:if test="${not empty room.roomOrder}">

                        <div class="accordion-item">
                            <h2 class="accordion-header" id="flush-heading${room.roomId }">
                                <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
                                    data-bs-target="#flush-collapse${room.roomId }" aria-expanded="false"
                                    aria-controls="flush-collapse${room.roomId }">
                                    <c:if test="${room.cleaningStatus != 1}">
                                        <c:out value="※${room.cleaningName }※" />
                                    </c:if>
                                    <c:out value="◆　${room.roomName }" />
                                    <c:if test="${room.inUse}">
                                        <c:out value="　◆会員：${room.customerName }　◆入室：" />
                                        <fmt:formatDate value="${room.startTime }" pattern="d日HH時mm分" />
                                    </c:if>
                                </button>
                            </h2>
                            <div id="flush-collapse${room.roomId }" class="accordion-collapse collapse"
                                aria-labelledby="flush-heading${room.roomId }" data-bs-parent="#accordionFlushExample">
                                <div class="accordion-body">
                                    <c:choose>
                                        <c:when test="${room.inUse }">
                                            <div class="vacancy">
                                                <a href="checkOut?roomId=<c:out value="${room.roomId }"/>"
                                                    class="btn btn-success col-auto mr-2">退室</a>
                                                <a href="shopping?roomId=<c:out value="${room.roomId }"/>"
                                                    class="btn btn-success col-auto mr-2">販売</a>
                                                <a href="" class="btn btn-success col-auto mr-2">ﾚﾝﾀﾙ</a>
                                                <a href="" class="btn btn-success col-auto">ﾛｽﾀｲﾑ</a>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <c:if test="${room.cleaningStatus == 1}">
                                                <a href="checkIn?roomId=<c:out value="${room.roomId }"/>"
                                                    class="btn btn-success col-auto mr-2">入室</a>
                                                <a href="" class="btn btn-success col-auto mr-2">予約</a>
                                            </c:if>

                                            <c:if test="${room.cleaningStatus == 2}">
                                                <a href="cleaning?roomId=<c:out value="${room.roomId }"/>&cleaningId=1"
                                                    class="btn btn-success col-auto mr-2">清掃完了</a>
                                            </c:if>

                                            <c:if test="${room.cleaningStatus == 1}">
                                                <a href="cleaning?roomId=<c:out value="${room.roomId }"/>&cleaningId=2"
                                                    class="btn btn-success col-auto mr-2">未清掃</a>
                                            </c:if>

                                            <c:if test="${room.cleaningStatus == 3}">
                                                <a href="cleaning?roomId=<c:out value="${room.roomId }"/>&cleaningId=2"
                                                    class="btn btn-success col-auto mr-2">点検解除</a>
                                            </c:if>

                                            <c:if test="${room.cleaningStatus == 1}">
                                                <a href="cleaning?roomId=<c:out value="${room.roomId }"/>&cleaningId=3"
                                                    class="btn btn-success col-auto mr-2">点検</a>
                                            </c:if>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>
                    </c:if>
                </c:forEach>
            </div>
        </div>
    </div>
    <script src="js/jquery-3.6.1.min.js"></script>
    <script src="js/bootstrap.bundle.min.js"></script>
    <script>
					jQuery.extend({
						clock : function clock(target) {
							var dayOfTheWeek = new Array("日", "月", "火", "水",
									"木", "金", "土");
							var now = new Date();
							var year = now.getFullYear();
							var month = now.getMonth() + 1;
							var date = now.getDate();
							var day = now.getDay();
							var hour = now.getHours();
							var min = now.getMinutes();
							var sec = now.getSeconds();
							if (month < 10) {
								month = "0" + month;
							}
							if (date < 10) {
								date = "0" + date;
							}
							if (hour < 10) {
								hour = "0" + hour;
							}
							if (min < 10) {
								min = "0" + min;
							}
							if (sec < 10) {
								sec = "0" + sec;
							}
							var time_str = year + "年" + month + "月" + date
									+ "日" + "（" + dayOfTheWeek[day] + "）"
									+ hour + "時" + min + "分" + sec + "秒";

							// htmlの内容を更新
							target.text(time_str);
							//target.html(time_str);

							// 1000ミリ秒（1秒）毎に更新
							setTimeout(function() {
								clock(target)
							}, 1000);
						}
					});

					// 現在日時を表示します。
					jQuery.clock(jQuery("#degitalClockSample1"));

					$(document).ready(function() {
						$("#logout").click(function() {
							return confirm("ログアウトしますか？");
						});

					});
					
				    $(document).ready(function() {
				          
				          $("#logout-button").on("click",function() {
				                if(window.confirm("本当にログアウトしますか？")){
				                  return true;
				                  }else{
				                    return false}});
				        });
				    
				</script>
</body>
</html>
