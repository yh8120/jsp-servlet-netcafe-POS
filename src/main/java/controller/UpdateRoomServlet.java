package controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.DaoFactory;
import dao.RoomDao;
import dao.RoomTypeDao;
import domain.Room;
import domain.RoomType;

@WebServlet("/updateRoom")
public class UpdateRoomServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		Integer roomId = Integer.parseInt(request.getParameter("roomId"));

		try {
			RoomTypeDao roomTypeDao = DaoFactory.createRoomTypeDao();
			List<RoomType> roomTypeList = roomTypeDao.findAll();
			request.setAttribute("roomTypeList", roomTypeList);

			RoomDao roomDao = DaoFactory.createRoomDao();
			Room room = roomDao.findById(roomId);

			request.setAttribute("roomName", room.getRoomName());
			request.setAttribute("roomTypeId", room.getRoomTypeId());
			request.getRequestDispatcher("/WEB-INF/view/updateRoom.jsp")
					.forward(request, response);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			RoomTypeDao roomTypeDao = DaoFactory.createRoomTypeDao();
			List<RoomType> roomTypeList = roomTypeDao.findAll();
			request.setAttribute("roomTypeList", roomTypeList);
			
			Integer roomId = Integer.parseInt(request.getParameter("roomId"));

			String roomName = request.getParameter("roomName");
			Integer roomTypeId = Integer.parseInt(request.getParameter("roomTypeId"));

			request.setAttribute("roomName", roomName);
			request.setAttribute("roomTypeId", roomTypeId);

			boolean isError = false;

			if (roomName.isEmpty()) {
				request.setAttribute("roomNameError", "名前が未入力です。");
				isError = true;
			}
			if (isError == true) {
				request.getRequestDispatcher("/WEB-INF/view/updateRoom.jsp")
						.forward(request, response);
				return;
			}

			Room room = new Room();
			room.setRoomId(roomId);
			room.setRoomName(roomName);
			room.setRoomTypeId(roomTypeId);

			RoomDao roomDao = DaoFactory.createRoomDao();
			roomDao.update(room);

			request.getRequestDispatcher("/WEB-INF/view/updateRoomDone.jsp")
					.forward(request, response);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}