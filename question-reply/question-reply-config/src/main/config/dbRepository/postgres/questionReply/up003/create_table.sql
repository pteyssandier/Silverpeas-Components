ALTER TABLE SC_QuestionReply_Question
ALTER COLUMN content DROP NOT NULL
;

ALTER TABLE SC_QuestionReply_Reply
ALTER COLUMN content DROP NOT NULL
;