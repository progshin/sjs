CREATE TABLE REVIEW COMMENT '회원리뷰' (
    REVIEWID    VARCHAR(32)    NOT NULL   COMMENT '리뷰ID'
  , USERID      VARCHAR(100)    NOT NULL   COMMENT '회원ID'
  , PLACEID     VARCHAR(100)    NOT NULL   COMMENT '장소ID'
  , CONTENT     VARCHAR(100)                COMMENT '리뷰내용'
  , IMAGES      VARCHAR(1000)               COMMENT '리뷰사진'
  , ADDDATE     DATE        DEFAULT NOW()   COMMENT '리뷰등록일'
  , EDITDATE    DATE        DEFAULT NOW()   COMMENT '리뷰수정일'
  , PRIMARY KEY (REVIEWID, USERID, PLACEID)
);

CREATE INDEX REIVEW_IDX01 ON REVIEW(REVIEWID, USERID, PLACEID);
CREATE INDEX REIVEW_IDX02 ON REVIEW(PLACEID);

CREATE TABLE POINTS COMMENT '회원포인트내역' (
    REVIEWID    VARCHAR(32)    NOT NULL    COMMENT '리뷰ID'
  , USERID      VARCHAR(100)    NOT NULL    COMMENT '회원ID'
  , PLACEID     VARCHAR(100)    NOT NULL   COMMENT '장소ID'
  , ACTION      VARCHAR(100)    NOT NULL   COMMENT '작업내용'
  , REASON      VARCHAR(10)                 COMMENT '증감사유'
  , POINT       INT             NOT NULL    COMMENT '변동포인트'
  , ADDDATE     DATE        DEFAULT NOW()   COMMENT '등록일'
  , PRIMARY KEY (REVIEWID, USERID, PLACEID, ACTION, REASON)
);

CREATE INDEX POINTS_IDX01 ON POINTS(REVIEWID, USERID, PLACEID, ACTION, REASON);
CREATE INDEX POINTS_IDX02 ON POINTS(REVIEWID, USERID, PLACEID);
CREATE INDEX POINTS_IDX03 ON POINTS(USERID);