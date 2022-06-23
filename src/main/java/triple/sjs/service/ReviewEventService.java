package triple.sjs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import triple.sjs.domain.Points;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewEventService {

    @Autowired
    DataSource dataSource;

    public String pointCheck(String userId, String reviewId, String placeId, String action, String reason){
        String sql = "";

        if(action.equals("ADD")) {
            sql = "INSERT INTO POINTS(USERID, REVIEWID, PLACEID, ACTION, REASON, POINT) VALUES('" + userId + "', '" + reviewId + "', '"
                    + placeId + "', '" + action + "', '" + reason + "', 1)";
        }else if(action.equals("MOD-P")) {
            sql = "INSERT INTO POINTS(USERID, REVIEWID, PLACEID, ACTION, REASON, POINT) VALUES('" + userId + "', '" + reviewId + "', '"
                    + placeId + "', '" + action + "', '" + reason + "', 1)";
        }else if(action.equals("MOD-M")) {
            sql = "INSERT INTO POINTS(USERID, REVIEWID, PLACEID, ACTION, REASON, POINT) VALUES('" + userId + "', '" + reviewId + "', '"
                    + placeId + "', '" + action + "', '" + reason + "', -1)";
        }

        return sql;
    }

    public String reviewAdd(HttpServletRequest httpServletRequest) {
        String reviewId = httpServletRequest.getParameter("reviewId");
        String content = httpServletRequest.getParameter("content");
        String attachedPhotoIds = httpServletRequest.getParameter("attachedPhotoIds");
        String userId = httpServletRequest.getParameter("userId");
        String placeId = httpServletRequest.getParameter("placeId");

        String duplCheckSQL = "SELECT 1 FROM REVIEW WHERE REVIEWID = '" + reviewId +"' AND USERID = '" + userId + "' AND PLACEID = '" + placeId + "'";
        String bonusCheckSQL = "SELECT COUNT(*) AS CNT FROM REVIEW WHERE PLACEID = '" + placeId + "'";
        String addSQL = "INSERT INTO REVIEW(USERID, REVIEWID, PLACEID, CONTENT, IMAGES) VALUES('" + userId + "', '" + reviewId + "', '" + placeId + "', '"
                + content + "', '" + attachedPhotoIds + "')";

        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();

            //회원리뷰가 중복등록 되는지 확인
            ResultSet rs = statement.executeQuery(duplCheckSQL);
            if(rs.last()){
                return "DUPL";
            }

            // 내용입력 포인트 증감
            if(content.length() > 0) {
                statement.execute(pointCheck(userId, reviewId, placeId, "ADD", "CONTENT"));
            }
            // 사진등록 포인트 증감
            if(attachedPhotoIds.length() > 0) {
                statement.execute(pointCheck(userId, reviewId, placeId, "ADD", "IMAGE"));
            }
            rs = statement.executeQuery(bonusCheckSQL);
            rs.next();
            // 첫 리뷰 보너스 포인트 증감
            if(rs.getString("CNT").equals("0")) {
                statement.execute(pointCheck(userId, reviewId, placeId, "ADD", "FIRST"));
            }
            // 리뷰등록 INSERT
            statement.execute(addSQL);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "ADD";
    }

    public String reviewDelete(HttpServletRequest httpServletRequest) {
        String reviewId = httpServletRequest.getParameter("reviewId");
        String userId = httpServletRequest.getParameter("userId");
        String placeId = httpServletRequest.getParameter("placeId");

        String dataCheckSQL = "SELECT 1 FROM REVIEW WHERE REVIEWID = '" + reviewId +"' AND USERID = '" + userId + "' AND PLACEID = '" + placeId + "'";
        String reviewDeleteSQL = "DELETE FROM REVIEW WHERE USERID = '" + userId + "' AND REVIEWID = '" + reviewId + "' AND PLACEID = '" + placeId + "'";
        String deletePointSQL = "INSERT INTO POINTS(REVIEWID, USERID, PLACEID, ACTION, REASON, POINT) SELECT REVIEWID, USERID, PLACEID, 'DELETE', REASON, -1 " +
                "FROM POINTS WHERE REVIEWID = '" + reviewId + "' AND USERID = '" + userId + "' AND PLACEID = '" + placeId + "' AND ACTION IN ('ADD', 'MOD-P')";

        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();

            //삭제할 데이터가 있는지 확인
            ResultSet rs = statement.executeQuery(dataCheckSQL);
            if(!rs.last()){
                return "EMPTY";
            }

            // 포인트감소 INSERT
            statement.execute(deletePointSQL);

            // 리뷰삭제 DELETE
            statement.execute(reviewDeleteSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "DELETE";
    }

    public String reviewMod(HttpServletRequest httpServletRequest) {
        String reviewId = httpServletRequest.getParameter("reviewId");
        String content = httpServletRequest.getParameter("content");
        String attachedPhotoIds = httpServletRequest.getParameter("attachedPhotoIds");
        String userId = httpServletRequest.getParameter("userId");
        String placeId = httpServletRequest.getParameter("placeId");

        String dataCheckSQL = "SELECT 1 FROM REVIEW WHERE REVIEWID = '" + reviewId +"' AND USERID = '" + userId + "' AND PLACEID = '" + placeId + "'";
        String reviewUpdateSQL = "UPDATE REVIEW SET CONTENT = '" + content + "', IMAGES = '" + attachedPhotoIds + "', EDITDATE = NOW() WHERE REVIEWID = '" +
                reviewId + "' AND USERID = '" + userId + "' AND PLACEID = '" + placeId + "'";
        String chkImgPointSQL = "SELECT IFNULL(SUM(POINT), 0) AS POINT FROM POINTS WHERE REVIEWID = '" + reviewId + "' AND USERID = '" + userId + "' AND PLACEID = '"
                + placeId + "' AND ACTION IN ('ADD', 'MOD-P', 'MOD-M') AND REASON = 'IMAGE'";

        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();

            //삭제할 데이터가 있는지 확인
            ResultSet rs = statement.executeQuery(dataCheckSQL);
            if(!rs.last()){
                return "EMPTY";
            }

            //리뷰수정 포인트 변동 INSERT
            if(content.length() > 0) {
                //사진을 등록한경우 포인트 +1
                if(attachedPhotoIds.length() > 0) {
                    rs = statement.executeQuery(chkImgPointSQL);
                    rs.next();
                    if(rs.getInt("POINT") == 0){
                        statement.execute(pointCheck(userId, reviewId, placeId, "MOD-P", "IMAGE"));
                    }
                }else { // 사진을 삭제한경우 포인트 -1
                    rs = statement.executeQuery(chkImgPointSQL);
                    rs.next();
                    if(rs.getInt("POINT") > 0){
                        statement.execute(pointCheck(userId, reviewId, placeId, "MOD-M", "IMAGE"));
                    }
                }
            }

            // 리뷰수정 UPDATE
            statement.execute(reviewUpdateSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "MOD";
    }

    public List<Points> pointList() {
        String pointList = "SELECT USERID, SUM(POINT) AS POINT FROM POINTS GROUP BY USERID";
        ResultSet rs = null;
        List<Points> list = new ArrayList<>();

        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();

            //포인트 조회
            rs = statement.executeQuery(pointList);
            while (rs.next()) {
                Points points = new Points();
                points.setUserId(rs.getString("USERID"));
                points.setPoint(rs.getLong("POINT"));

                list.add(points);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

}
