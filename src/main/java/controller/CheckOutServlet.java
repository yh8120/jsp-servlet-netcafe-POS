package controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dao.CustomerDao;
import dao.DaoFactory;
import dao.PricePlanDao;
import dao.ReceiptDataDao;
import dao.RoomDao;
import domain.Customer;
import domain.PricePlan;
import domain.ReceiptData;
import domain.Room;
import domain.User;

@WebServlet("/checkOut")
public class CheckOutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			Integer roomId = Integer.parseInt(request.getParameter("roomId"));
			RoomDao roomdao = DaoFactory.createRoomDao();
			Room room = roomdao.findById(roomId);

			Integer customerId = room.getCustomerId();
			CustomerDao customerDao = DaoFactory.createCustomerDao();
			Customer customer = customerDao.findById(customerId);

			Date startTime = room.getStarted();
			Date now = new Date();
			Time time = new Time(startTime.getTime());

			//時計表示用
			BigDecimal stayingTime = new BigDecimal(now.getTime() - startTime.getTime());
			String timeDisplay = timeDisplay(stayingTime);

			//料金計算用
			PricePlanDao pricePlanDao = DaoFactory.createPricePlanDao();
			List<PricePlan> pricePlanList = pricePlanDao.findByNow(time);
			Room calcRoom=null;
			
			for(PricePlan plisePlan:pricePlanList) {
				Room calcRoomNew = planToRoom(stayingTime, plisePlan);
				if (calcRoom==null) {
					calcRoom=calcRoomNew;
				}
				if(calcRoom.getSumPrice()>calcRoomNew.getSumPrice()) {
					calcRoom = calcRoomNew;
				}
			}
			
			
			
//			BigDecimal basicPrice = new BigDecimal(500);
//			BigDecimal basicMS = new BigDecimal(3600000);
//			BigDecimal addPrice = new BigDecimal(100);
//			BigDecimal addMS = new BigDecimal(600000);
//			BigDecimal tax = new BigDecimal(0.1);
//
//			// 超過時間（＝利用時間－基本時間）
//			// (利用時間が基本時間以下の場合超過時間は0)
//			BigDecimal excMS = stayingTime.subtract(basicMS);
//			if (excMS.compareTo(BigDecimal.ZERO) == -1) {
//				excMS = BigDecimal.ZERO;
//			}
//			// 追加料金発生回数（＝超過時間÷追加料金時間(切り上げ））
//			BigDecimal timesOfAdded = excMS.divide(addMS, RoundingMode.UP);
//			// 追加料金（＝追加料金発生時間×追加料金）
//			BigDecimal excPrice = (timesOfAdded).multiply(addPrice);
//
//			// 小計（＝基本料金＋(追加料金)）
//			BigDecimal subtotal = basicPrice.add(excPrice);
//			// 内消費税（＝小計×税率(四捨五入)）
//			BigDecimal innerTax = subtotal.multiply(tax);
//			innerTax = innerTax.setScale(0, RoundingMode.HALF_UP);
//			// 合計（＝小計＋内消費税）
//			BigDecimal sumPrice = subtotal.add(innerTax);
//
			room.setStayingTime(stayingTime.longValue());
			room.setSubtotal(calcRoom.getSubtotal());
			room.setInnerTax(calcRoom.getInnerTax());
			room.setSumPrice(calcRoom.getSumPrice());

			roomdao.preCheckOut(room);

			request.setAttribute("room", room);
			request.setAttribute("customer", customer);
			request.setAttribute("timeDisplay", timeDisplay);

			request.getRequestDispatcher("/WEB-INF/view/checkOut.jsp").forward(request, response);

		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
			//			HttpServletResponse res = (HttpServletResponse) response;
			//			res.sendRedirect("manager");
		}

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {

			String strPayment = request.getParameter("payment");
			Integer roomId = Integer.parseInt(request.getParameter("roomId"));

			RoomDao roomdao = DaoFactory.createRoomDao();
			Room room = roomdao.findById(roomId);

			if (room.getInUse()) {

				Integer customerId = room.getCustomerId();
				CustomerDao customerDao = DaoFactory.createCustomerDao();
				Customer customer = customerDao.findById(customerId);

				BigDecimal stayingTime = new BigDecimal(room.getStayingTime());
				String timeDisplay = timeDisplay(stayingTime);

				request.setAttribute("payment", strPayment);
				request.setAttribute("room", room);
				request.setAttribute("customer", customer);
				request.setAttribute("timeDisplay", timeDisplay);

				// バリデーション
				Integer sumPrice = room.getSumPrice();
				Integer payment = 0;
				boolean isError = false;

				if (strPayment.isEmpty()) {
					request.setAttribute("paymentError", "預り金が未入力です");
					isError = true;
				} else {
					try {
						payment = Integer.parseInt(strPayment);
						if (payment <= 0) {
							request.setAttribute("paymentError", "預り金が不正です。");
							isError = true;
						}
					} catch (NumberFormatException e) {
						e.printStackTrace();
						request.setAttribute("paymentError", "預り金が不正です。");
						isError = true;
					}
				}
				if (sumPrice > payment) {
					request.setAttribute("paymentError", "預り金が足りません。");
					isError = true;
				}

				if (isError) {
					request.getRequestDispatcher("/WEB-INF/view/checkOut.jsp")
							.forward(request, response);
					return;
				}

				//　レシートデータ作成
				HttpServletRequest req = (HttpServletRequest) request;
				HttpSession session = req.getSession();
				User user = (User) session.getAttribute("user");
				ReceiptData receiptData = new ReceiptData();

				receiptData.setShopId(user.getShopId());
				receiptData.setUserId(user.getUserId());
				receiptData.setCustomerId(room.getCustomerId());
				receiptData.setCheckOutTime(room.getCheckOutTime());
				receiptData.setSumPrice(sumPrice);
				receiptData.setInnerTax(room.getInnerTax());
				receiptData.setPayment(payment);
				Integer changeMoney = payment - sumPrice;
				request.setAttribute("changeMoney", changeMoney);
				receiptData.setChangeMoney(changeMoney);
				receiptData.setTimeSpent(room.getStayingTime());

				ReceiptDataDao receiptDataDao = DaoFactory.createRecieptDataDao();


				//DB処理１　レシートデータ作成
				Integer receiptId = receiptDataDao.insert(receiptData);
				//DB処理２　退室状態に変更
				roomdao.checkOut(room);

				request.getRequestDispatcher("/WEB-INF/view/checkOutDone.jsp").forward(request, response);
			} else {
				response.sendRedirect("manager");
			}

		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
	
	private String timeDisplay(BigDecimal stayingTime) {
		BigDecimal currentHour = stayingTime.divide(BigDecimal.valueOf(3600000), RoundingMode.DOWN);
		BigDecimal currentHourMS = currentHour.multiply(BigDecimal.valueOf(3600000));
		BigDecimal currentMin = (stayingTime.subtract(currentHourMS)).divide(BigDecimal.valueOf(60000),
				RoundingMode.DOWN);
		BigDecimal currentHourMinMS = currentHourMS.add(currentMin.multiply(BigDecimal.valueOf(60000)));
		BigDecimal currentSec = (stayingTime.subtract(currentHourMinMS)).divide(BigDecimal.valueOf(1000),
				RoundingMode.DOWN);
		
		String timeDisplay = currentHour+"時間"+currentMin+"分"+currentSec+"秒";
		return timeDisplay;
	}
	
	private  Room planToRoom(BigDecimal stayingTime,PricePlan pricePlan) {
		//料金計算用
		BigDecimal basicPrice = new BigDecimal(pricePlan.getBasicPrice());
		BigDecimal basicMS = new BigDecimal(pricePlan.getBasicTime());
		BigDecimal addPrice = new BigDecimal(pricePlan.getAddPrice());
		BigDecimal addMS = new BigDecimal(pricePlan.getAddTime());
		BigDecimal tax = new BigDecimal(pricePlan.getTaxRate());

		// 超過時間（＝利用時間－基本時間）
		// (利用時間が基本時間以下の場合超過時間は0)
		BigDecimal excMS = stayingTime.subtract(basicMS);
		if (excMS.compareTo(BigDecimal.ZERO) == -1) {
			excMS = BigDecimal.ZERO;
		}
		// 追加料金発生回数（＝超過時間÷追加料金時間(切り上げ））
		BigDecimal timesOfAdded = excMS.divide(addMS, RoundingMode.UP);
		// 追加料金（＝追加料金発生時間×追加料金）
		BigDecimal excPrice = (timesOfAdded).multiply(addPrice);

		// 小計（＝基本料金＋(追加料金)）
		BigDecimal subtotal = basicPrice.add(excPrice);
		// 内消費税（＝小計×税率(四捨五入)）
		BigDecimal innerTax = subtotal.multiply(tax);
		innerTax = innerTax.setScale(0, RoundingMode.HALF_UP);
		// 合計（＝小計＋内消費税）
		BigDecimal sumPrice = subtotal.add(innerTax);
		
		Room room = new Room();
		room.setSubtotal(subtotal.intValue());
		room.setInnerTax(innerTax.intValue());
		room.setSumPrice(sumPrice.intValue());
		
		return room;
	}

}
